package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountDaily;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.CreateAccountEvent;
import com.cyanspring.common.event.account.CreateAccountReplyEvent;
import com.cyanspring.common.event.account.CreateUserEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.event.account.ExecutionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.PmChangeAccountSettingEvent;
import com.cyanspring.common.event.account.PmCreateAccountEvent;
import com.cyanspring.common.event.account.PmCreateUserEvent;
import com.cyanspring.common.event.account.PmEndOfDayRollEvent;
import com.cyanspring.common.event.account.PmRemoveDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.PmUpdateDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUpdateUserEvent;
import com.cyanspring.common.event.account.PmUserLoginEvent;
import com.cyanspring.common.event.account.UserLoginEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.fx.IFxConverter;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.server.persistence.PersistenceManager;

public class AccountPositionManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(AccountPositionManager.class);

	private final static String ID = AccountPositionManager.class.toString();
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private AsyncTimerEvent dayEndEvent = new AsyncTimerEvent();
	private long dayEndOffset;
	private long jobInterval = 1000;
	private List<String> fxSymbols = new ArrayList<String>();
	private boolean allFxRatesReceived = false;
	private Map<String, Quote> marketData = new HashMap<String, Quote>();
	private int dayOfYear;
	private IQuoteChecker quoteChecker;
	
	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	UserKeeper userKeeper;
	
	@Autowired
	AccountKeeper accountKeeper;
	
	@Autowired
	PositionKeeper positionKeeper;
	
	@Autowired
	ScheduleManager scheduleManager;
	
	@Autowired
	IFxConverter fxConverter;
	
	@Autowired
	RefDataManager refDataManager;
	
	private IQuoteFeeder quoteFeeder = new IQuoteFeeder() {

		@Override
		public Quote getQuote(String symbol) {
			Quote quote = marketData.get(symbol);
			if(null == quote)
				eventManager.sendEvent(new QuoteSubEvent(AccountPositionManager.ID, null, symbol));
			return quote;
		}
	};
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(UserLoginEvent.class, null);
			subscribeToEvent(CreateUserEvent.class, null);
			subscribeToEvent(CreateAccountEvent.class, null);
			subscribeToEvent(UpdateChildOrderEvent.class, null);
			subscribeToEvent(AccountSnapshotRequestEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(MarketDataReadyEvent.class, null);
			subscribeToEvent(UpdateParentOrderEvent.class, null);
			subscribeToEvent(AccountSettingSnapshotRequestEvent.class, null);
			subscribeToEvent(ChangeAccountSettingRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};
	
	private AsyncEventProcessor timerProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}

	};
	
	private int getDayOfYear() {
		Calendar cal = Default.getCalendar();
		cal.setTime(Clock.getInstance().now());
		return cal.get(Calendar.DAY_OF_YEAR);
	}
	
	private void scheduleDayEndEvent() {
		Calendar cal = Default.getCalendar();
		Date now = Clock.getInstance().now();
		Date date = TimeUtil.getOnlyDate(cal, now);
		
		long timePass = TimeUtil.getTimePass(now, date);
		
		long offset = dayEndOffset;
		if(timePass > dayEndOffset) // already pass day end
			offset += 24 * 60 * 60 * 1000; 
		
		date.setTime(date.getTime() + offset);
		log.info("Scheduling day end processing at: " + date);
		scheduleManager.scheduleTimerEvent(date, timerProcessor, dayEndEvent);
	}
	
	@Override
	public void init() throws Exception {
		positionKeeper.setListener(positionListener);
		positionKeeper.setQuoteFeeder(quoteFeeder);
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("UserAccountManager");
		
		timerProcessor.setHandler(this);
		timerProcessor.init();
		if(timerProcessor.getThread() != null)
			timerProcessor.getThread().setName("UserAccountManager-Timer");

		dayOfYear = getDayOfYear();
		scheduleManager.scheduleRepeatTimerEvent(jobInterval, timerProcessor, timerEvent);
	}

	IPositionListener positionListener = new IPositionListener() {
		@Override
		public void onRemoveDetailOpenPosition(OpenPosition position) {
			eventManager.sendEvent(new PmRemoveDetailOpenPositionEvent(PersistenceManager.ID, position));
			
		}

		@Override
		public void onUpdateDetailOpenPosition(OpenPosition position) {
			eventManager.sendEvent(new PmUpdateDetailOpenPositionEvent(PersistenceManager.ID, position));
		}
		
		@Override
		public void onOpenPositionUpdate(OpenPosition position) {
			try {
				eventManager.sendRemoteEvent(new OpenPositionUpdateEvent(position.getAccount(), null, position));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		@Override
		public void onOpenPositionDynamiceUpdate(OpenPosition position) {
			try {
				eventManager.sendRemoteEvent(new OpenPositionDynamicUpdateEvent(position.getAccount(), null, position));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		@Override
		public void onClosedPositionUpdate(ClosedPosition position) {
			try {
				eventManager.sendGlobalEvent(new ClosedPositionUpdateEvent(position.getAccount(), null, position));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		@Override
		public void onAccountUpdate(Account account) {
			try {
				eventManager.sendRemoteEvent(new AccountUpdateEvent(account.getId(), null, account));
				eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		@Override
		public void onAccountDynamicUpdate(Account account) {
			try {
				eventManager.sendRemoteEvent(new AccountDynamicUpdateEvent(account.getId(), null, account));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

	};
	
	@Override
	public void uninit() {
		positionKeeper.setListener(null);
		eventProcessor.uninit();
		scheduleManager.cancelTimerEvent(timerEvent);
		timerProcessor.uninit();
	}
	
	public void processUserLoginEvent(UserLoginEvent event) {
		log.debug("Received UserLoginEvent: " + event.getUserId());
		eventManager.sendEvent(new PmUserLoginEvent(PersistenceManager.ID, event.getReceiver(), userKeeper, accountKeeper, event));
	}
	
	public void processCreateUserEvent(CreateUserEvent event) {
		boolean ok = true;
		User user = event.getUser();
		String message = "";
		if(null != userKeeper && null != accountKeeper) {
			try {
				userKeeper.createUser(user);
				//Account account = new Account(generateAccountId(), event.getUser().getId(), defaultCurrency);
				String defaultAccountId = user.getDefaultAccount();
				if(null == user.getDefaultAccount() || user.getDefaultAccount().equals("")) {
					if(!accountKeeper.accountExists(user.getId())) {
						defaultAccountId = user.getId();
					} else {
						defaultAccountId = generateAccountId();
						if(accountKeeper.accountExists(defaultAccountId)) {
							throw new UserException("Cannot create default account for user: " +
									user.getId() + ", last try: " + defaultAccountId);
						}
					}
				}
				user.setDefaultAccount(defaultAccountId);
				if(null == user.getUserType())
					user.setUserType(UserType.NORMAL);

				Account account = new Account(defaultAccountId, event.getUser().getId());
				accountKeeper.createAccount(account);
				
				eventManager.sendEvent(new PmCreateUserEvent(PersistenceManager.ID, null, user, event, Arrays.asList(account)));
			} catch (UserException ue) {
				message = ue.getMessage();
				ok = false;
			} catch (AccountException ae) {
				message = ae.getMessage();
				ok = false;
			}
		} else {
			ok = false;
			message = "System doesn't support user creation";
		}
		
		if(!ok)
		{
			try {
				eventManager.sendRemoteEvent(new CreateUserReplyEvent(event.getKey(), 
						event.getSender(), user, ok, message, event.getTxId()));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void processCreateAccountEvent(CreateAccountEvent event) {
		boolean ok = true;
		String message = "";
		if(null != userKeeper && null != accountKeeper) {
			try {
				Account account = event.getAccount();
				if(!userKeeper.userExists(account.getUserId()))
					throw new AccountException("User doesn't exists: " + account.getUserId());
				accountKeeper.createAccount(account);
			} catch (AccountException ae) {
				message = ae.getMessage();
				ok = false;
			}
		} else {
			ok = false;
			message = "System doesn't support account creation";
		}
		
		try {
			eventManager.sendRemoteEvent(new CreateAccountReplyEvent(event.getKey(), 
					event.getSender(), ok, message, event.getTxId()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		
	}
	
	public void processUpdateParentOrderEvent(UpdateParentOrderEvent event) {
		Account account = accountKeeper.getAccount(event.getParent().getAccount());
		positionKeeper.processParentOrder(event.getParent(), account);
	}
	
	public void processUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		Quote quote = marketData.get(event.getOrder().getSymbol());
		if(null == quote) {
			eventManager.sendEvent(new QuoteSubEvent(AccountPositionManager.ID, null, 
					event.getOrder().getSymbol()));
		
		}
		
		Execution execution = event.getExecution();
		if(null != execution) {
			log.debug("Process execution: " + execution);			
			try {
				Account account = accountKeeper.getAccount(execution.getAccount());
				positionKeeper.processExecution(execution, account);
				eventManager.sendRemoteEvent(new ExecutionUpdateEvent(execution.getAccount(), 
						null, execution));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		Quote quote = event.getQuote();
		if(null != quoteChecker && !quoteChecker.check(quote)) {
			return;
		}
		
		marketData.put(event.getQuote().getSymbol(), event.getQuote());
		RefData refData = refDataManager.getRefData(event.getQuote().getSymbol());
		if(refData != null && 
		   refData.getExchange() != null && 
		   refData.getExchange().equals("FX")) {
			fxConverter.updateRate(event.getQuote());
		}
	}
	
	public void processAccountSnapshotRequestEvent(AccountSnapshotRequestEvent event) {
		Account account = accountKeeper.getAccount(event.getAccountId());
		AccountSnapshotReplyEvent reply;
		if(null == account) {
			reply = new AccountSnapshotReplyEvent(event.getKey(), event.getSender(), 
					null, null, null, null, null);
			log.warn("processAccountSnapshotRequestEvent, account doesn't exist: " + event.getAccountId());
		} else {
			List<OpenPosition> openPositions = positionKeeper.getOverallPosition(account);
			List<ClosedPosition> closedPosition = positionKeeper.getClosedPositions(event.getAccountId());
			List<Execution> executions = positionKeeper.getExecutions(event.getAccountId());
			AccountSetting accountSetting = null;
			try {
				accountSetting = accountKeeper.getAccountSetting(account.getId());
			} catch (AccountException e) {
				log.error(e.getMessage(), e);
			}
			reply = new AccountSnapshotReplyEvent(event.getKey(), event.getSender(), 
					account, accountSetting, openPositions, closedPosition, executions);
		}
		try {
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void processMarketDataReadyEvent(MarketDataReadyEvent event) {
		if(fxSymbols == null)
			return;
		
		for(String symbol: fxSymbols) {
			eventManager.sendEvent(new QuoteSubEvent(AccountPositionManager.ID, null, symbol));
		}
	}
	
	public void processAccountSettingSnapshotRequestEvent(AccountSettingSnapshotRequestEvent event) {
		boolean ok = true;
		String message = null;
		AccountSetting accountSetting = null;
		try {
			accountSetting = accountKeeper.getAccountSetting(event.getAccountId());
		} catch (AccountException e) {
			ok = false;
			message =  e.getMessage();
		}
		
		ChangeAccountSettingReplyEvent reply = new ChangeAccountSettingReplyEvent(event.getKey(), 
				event.getSender(), accountSetting, ok, message);
		try {
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	public void processChangeAccountSettingRequestEvent(ChangeAccountSettingRequestEvent event) {
		boolean ok = true;
		String message = null;
		AccountSetting accountSetting = null;
		try {
			accountSetting = accountKeeper.setAccountSetting(event.getAccountSetting());
		} catch (AccountException e) {
			ok = false;
			message =  e.getMessage();
		}
		
		ChangeAccountSettingReplyEvent reply = new ChangeAccountSettingReplyEvent(event.getKey(), 
				event.getSender(), accountSetting, ok, message);
		try {
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		PmChangeAccountSettingEvent pmEvent = new PmChangeAccountSettingEvent(PersistenceManager.ID, 
				null, accountSetting);
		eventManager.sendEvent(pmEvent);
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if(event == timerEvent) {
			updateDynamicData();
			int today = getDayOfYear();
			if(dayOfYear != today) {
				dayOfYear = today;
				processDayEndTasks();
			}
		} else if (event == dayEndEvent) {
			
		}
	}
	
	private void updateDynamicData() {
		if(!allFxRatesReceived) {
			for(String symbol: fxSymbols) {
				Double rate = fxConverter.getFxRate(symbol);
				if(null == rate || PriceUtils.isZero(rate))
					return;
			}
			allFxRatesReceived = true;
			log.info("FX rates: " + fxConverter.toString());
		}
		List<Account> accounts = accountKeeper.getJobs();
		positionKeeper.updateDynamicData(accounts);
		
	}
	
	private void processDayEndTasks() {
		log.info("Account day end processing start");
		List<Account> list = accountKeeper.getAllAccounts();
		for(Account account: list) {
			account.updateEndOfDay();
		}
		
		eventManager.sendEvent(new PmEndOfDayRollEvent(PersistenceManager.ID, null));
	}
	
	private String generateAccountId() {
		return Default.getAccountPrefix() + IdGenerator.getInstance().getNextSimpleId();
	}

	public void injectUsers(List<User> users) {
		userKeeper.injectUsers(users);
		User defaultUser = userKeeper.tryCreateDefaultUser();
		if(null != defaultUser)
			eventManager.sendEvent(new PmCreateUserEvent(PersistenceManager.ID, null, defaultUser, null));
	}
	
	public void injectAccounts(List<Account> accounts) {
		accountKeeper.injectAccounts(accounts);
		Account defaultAccount = accountKeeper.tryCreateDefaultAccount();
		if(null != defaultAccount)
			eventManager.sendEvent(new PmCreateAccountEvent(PersistenceManager.ID, null, defaultAccount));	
	}
	
	public void injectAccountSettings(List<AccountSetting> accountSettings) {
		accountKeeper.injectAccountSettings(accountSettings);
	}
	
	public void injectExecutions(List<Execution> executions) {
		positionKeeper.injectExecutions(executions);
	}
	
	public void injectPositions(List<OpenPosition> opens, List<ClosedPosition> closed) {
		positionKeeper.injectOpenPositions(opens);
		positionKeeper.injectClosedPositions(closed);
	}

	// getters and setters
	public long getJobInterval() {
		return jobInterval;
	}

	public void setJobInterval(long jobInterval) {
		this.jobInterval = jobInterval;
	}

	public List<String> getFxSymbols() {
		return fxSymbols;
	}

	public void setFxSymbols(List<String> fxSymbols) {
		this.fxSymbols = fxSymbols;
	}

	public long getDayEndOffset() {
		return dayEndOffset;
	}

	public void setDayEndOffset(long dayEndOffset) throws Exception {
		if(dayEndOffset < 0)
			throw new Exception("dayEndOffset can not be less than 0");
		this.dayEndOffset = dayEndOffset;
	}

	public IQuoteChecker getQuoteChecker() {
		return quoteChecker;
	}

	public void setQuoteChecker(IQuoteChecker quoteChecker) {
		this.quoteChecker = quoteChecker;
	}
	
}
