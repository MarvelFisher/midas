package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.account.PositionException;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.CreateAccountEvent;
import com.cyanspring.common.event.account.CreateAccountReplyEvent;
import com.cyanspring.common.event.account.CreateUserEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.event.account.ExecutionUpdateEvent;
import com.cyanspring.common.event.account.InternalResetAccountRequestEvent;
import com.cyanspring.common.event.account.OnUserCreatedEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.PmChangeAccountSettingEvent;
import com.cyanspring.common.event.account.PmCreateAccountEvent;
import com.cyanspring.common.event.account.PmCreateUserEvent;
import com.cyanspring.common.event.account.PmEndOfDayRollEvent;
import com.cyanspring.common.event.account.PmRemoveDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.PmUpdateDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUserCreateAndLoginEvent;
import com.cyanspring.common.event.account.PmUserLoginEvent;
import com.cyanspring.common.event.account.ResetAccountReplyEvent;
import com.cyanspring.common.event.account.ResetAccountReplyType;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.account.UserCreateAndLoginEvent;
import com.cyanspring.common.event.account.UserCreateAndLoginReplyEvent;
import com.cyanspring.common.event.account.UserLoginEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.marketsession.SettlementEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.marketsession.TradeDateRequestEvent;
import com.cyanspring.common.event.order.CancelStrategyOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.fx.IFxConverter;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.staticdata.FuRefDataManager;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PerfDurationCounter;
import com.cyanspring.common.util.PerfFrequencyCounter;
import com.cyanspring.common.util.TimeThrottler;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventMultiProcessor;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.server.persistence.PersistenceManager;

public class AccountPositionManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(AccountPositionManager.class);

	private final static String ID = AccountPositionManager.class.toString();
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private AsyncTimerEvent dayEndEvent = new AsyncTimerEvent();
	private long jobInterval = 1000;
	private List<String> fxSymbols = new ArrayList<String>();
	private boolean allFxRatesReceived = false;
	private Map<String, Quote> marketData = new HashMap<String, Quote>();
	private String dailyExecTime;
	private IQuoteChecker quoteChecker = new PriceQuoteChecker();
	private long dynamicUpdateInterval = 2000;
	private long rmUpdateInterval = 900;
	private TimeThrottler dynamicUpdateThrottler;
	private TimeThrottler rmUpdateThrottler;
	private Map<String, Account> accountUpdates = new ConcurrentHashMap<String, Account>();
	private Map<String, OpenPosition> positionUpdates = new ConcurrentHashMap<String, OpenPosition>();
	private long perfUpdateInterval = 20000;
	private long perfRmInterval = 20000;
	private PerfDurationCounter perfDataRm;
	private PerfDurationCounter perfDataUpdate;
	private PerfFrequencyCounter perfFqyAccountUpdate;
	private PerfFrequencyCounter perfFqyPositionUpdate;
	private String tradeDate;
	private int asyncSendBatch = 3000;
	private long asyncSendInterval = 3000;
	private boolean sendDynamicPositionUpdate = false;
	private boolean recoveryDone = false;
	private boolean resetMarginHeld = false;
	
	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	UserKeeper userKeeper;
	
	@Autowired
	AccountKeeper accountKeeper;
	
	@Autowired
	PositionKeeper positionKeeper;
	
	ScheduleManager scheduleManager = new ScheduleManager();
	
	@Autowired
	IFxConverter fxConverter;
	
	@Autowired
	IRefDataManager refDataManager;
	
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
			subscribeToEvent(UserCreateAndLoginEvent.class, null);	//for Facebook, QQ, WeChat
			subscribeToEvent(UserLoginEvent.class, null);
			subscribeToEvent(CreateUserEvent.class, null);
			subscribeToEvent(CreateAccountEvent.class, null);
			subscribeToEvent(AccountSnapshotRequestEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(MarketDataReadyEvent.class, null);
			subscribeToEvent(AccountSettingSnapshotRequestEvent.class, null);
			subscribeToEvent(ChangeAccountSettingRequestEvent.class, null);
			subscribeToEvent(AllAccountSnapshotRequestEvent.class, null);
			subscribeToEvent(OnUserCreatedEvent.class, null);
			subscribeToEvent(TradeDateEvent.class, null);		
			subscribeToEvent(InternalResetAccountRequestEvent.class, null);
			subscribeToEvent(SettlementEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};
	
	private AsyncEventMultiProcessor eventMultiProcessor = new AsyncEventMultiProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(UpdateParentOrderEvent.class, null);
			subscribeToEvent(UpdateChildOrderEvent.class, null);
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
	
	private Date getScheuledDate() throws AccountException
	{
		if(dailyExecTime == null || dailyExecTime.length() == 0)
			throw new AccountException("didn't set scheduled daily execution time");
		
		String[] times = dailyExecTime.split(":");
		if(times.length != 3)
			throw new AccountException("daily execution time is not valid");
		
		int nHour = Integer.parseInt(times[0]);
		int nMin = Integer.parseInt(times[1]);
		int nSecond = Integer.parseInt(times[2]);
		
		Calendar cal = Default.getCalendar();
		Date now = Clock.getInstance().now();
		
		Date scheduledToday = TimeUtil.getScheduledDate(cal, now, nHour, nMin, nSecond);
		
		if(TimeUtil.getTimePass(now, scheduledToday) > 0)
			scheduledToday = TimeUtil.getNextDay(scheduledToday);
		
		return scheduledToday;
	}
	
	private void scheduleDayEndEvent() {
		try{
			Date date = getScheuledDate();
			log.info("Scheduling day end processing at: " + date);
			scheduleManager.scheduleTimerEvent(date, eventProcessor, dayEndEvent);
		}catch(AccountException e){
			log.error("can't schedule daily timer", e);
		}
	}
	
	@Override
	public void init() throws Exception {
		if(null != accountKeeper)
			accountKeeper.init();
		
		perfDataUpdate = new PerfDurationCounter("Dynamic data update", perfUpdateInterval);
		perfDataRm = new PerfDurationCounter("Risk management", perfRmInterval);
		perfFqyAccountUpdate = new PerfFrequencyCounter("Dynamic account update", 30000);
		perfFqyPositionUpdate = new PerfFrequencyCounter("Dynamic position update", 30000);
		
		dynamicUpdateThrottler = new TimeThrottler(dynamicUpdateInterval);
		rmUpdateThrottler = new TimeThrottler(rmUpdateInterval);
		
		positionKeeper.setListener(positionListener);
		positionKeeper.setQuoteFeeder(quoteFeeder);
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("AccountPositionManager");
		
		eventMultiProcessor.setHandler(this);
		eventMultiProcessor.setHash(true);
		eventMultiProcessor.init();
		eventMultiProcessor.setName("AccountPositionTP");

		timerProcessor.setHandler(this);
		timerProcessor.init();
		if(timerProcessor.getThread() != null)
			timerProcessor.getThread().setName("AccountPositionManager-Timer");

		if(tradeDate == null){
			try{
				TradeDateRequestEvent tdrEvent = new TradeDateRequestEvent(null, null);
				eventManager.sendEvent(tdrEvent);
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
		}
		
		scheduleManager.scheduleRepeatTimerEvent(jobInterval, timerProcessor, timerEvent);
		
		scheduleDayEndEvent();
	}

	IPositionListener positionListener = new IPositionListener() {

		boolean dynamicDataHasChanged(Account account) {
			Account last = accountUpdates.get(account.getId());
			if(last == null || account == null || last.getMargin() != account.getMargin() || 
					last.getUrPnL() != account.getUrPnL() || last.getCashAvailable() != account.getCashAvailable()) {
				try {
					accountUpdates.put(account.getId(), account.clone());
				} catch (CloneNotSupportedException e) {
					log.error(e.getMessage(), e);
				}
				return true;
			}
			return false;
		}
		
		boolean dynamicDataHasChanged(OpenPosition position) {
			OpenPosition last = positionUpdates.get(position.getId());
			if(last == null || position == null || last.getPnL() != position.getPnL()) {
				positionUpdates.put(position.getId(), position);
				return true;
			}
			return false;
		}
		
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
				positionUpdates.put(position.getId(), position);
				eventManager.sendGlobalEvent(new OpenPositionUpdateEvent(position.getAccount(), null, position));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		@Override
		public void onOpenPositionDynamiceUpdate(OpenPosition position) {
			try {
				if(sendDynamicPositionUpdate && dynamicDataHasChanged(position)) {
					perfFqyPositionUpdate.count();
					eventManager.sendRemoteEvent(new OpenPositionDynamicUpdateEvent(position.getAccount(), null, position));
				}
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
				accountUpdates.put(account.getId(), account.clone());
				eventManager.sendRemoteEvent(new AccountUpdateEvent(account.getId(), null, account));
				eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		@Override
		public void onAccountDynamicUpdate(Account account) {
			try {
				if(dynamicDataHasChanged(account)) {
					perfFqyAccountUpdate.count();
					eventManager.sendRemoteEvent(new AccountDynamicUpdateEvent(account.getId(), null, account));
				}
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
		log.debug("Received UserLoginEvent: " + event.getUserId() + ", txid: " + event.getTxId() + ", k: " + event.getKey());
		eventManager.sendEvent(new PmUserLoginEvent(PersistenceManager.ID, event.getReceiver(), userKeeper, accountKeeper, event));
	}
	
	public void processCreateUserEvent(CreateUserEvent event) {
		boolean ok = true;
		User user = event.getUser();
		String message = "";
		if(null != userKeeper && null != accountKeeper) {
			try {
				user.setId(user.getId().toLowerCase());
				if(userKeeper.userExists(user.getId()))
					throw new UserException("User already exists: " + user.getId(),ErrorMessage.USER_ALREADY_EXIST);

				String defaultAccountId = getDefaultAccountId(user);
				user.setDefaultAccount(defaultAccountId);

				if(null == user.getUserType())
					user.setUserType(UserType.NORMAL);

				Account account = new Account(defaultAccountId, event.getUser().getId());
				accountKeeper.setupAccount(account);
				
				eventManager.sendEvent(new PmCreateUserEvent(PersistenceManager.ID, null, user, event, Arrays.asList(account)));
			} catch (UserException ue) {
				message = MessageLookup.buildEventMessage(ue.getClientMessage(),ue.getMessage());
				ok = false;
			} catch (AccountException ae) {
				message = MessageLookup.buildEventMessage(ae.getClientMessage(),ae.getMessage());
				ok = false;
			} 
		} else {
			ok = false;
			message = MessageLookup.buildEventMessage(ErrorMessage.CREATE_USER_FAILED, "System doesn't support user creation");
		}
		
		log.info("processCreateUserEvent: " + event.getUser() + ", " + ok + ", " + message);
		
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

	public void processUserCreateAndLoginEvent(UserCreateAndLoginEvent event) {
		log.debug("Received UserCreateAndLoginEvent: " + event.getUser().getId());
		boolean ok = true;
		User user = event.getUser();
		String message = "";

		if(null != userKeeper && null != accountKeeper) {
			try {
				checkUserType(user);

				user.setId(user.getId().toLowerCase());
				if(!userKeeper.userExists(user.getId()))
				{
					String defaultAccountId = getDefaultAccountId(user);

					user.setDefaultAccount(defaultAccountId);
					Account account = new Account(defaultAccountId, event.getUser().getId());
					accountKeeper.setupAccount(account);
					
					//user not exist
					eventManager.sendEvent(new PmUserCreateAndLoginEvent(PersistenceManager.ID, event.getReceiver(), user, event, userKeeper, accountKeeper, Arrays.asList(account)));
				}
				else
				{
					//user exist
					eventManager.sendEvent(new PmUserCreateAndLoginEvent(PersistenceManager.ID, event.getReceiver(), null, event, userKeeper, accountKeeper, null));
				}

			} catch (UserException ue) {
				message = MessageLookup.buildEventMessage(ue.getClientMessage(), ue.getMessage());
				ok = false;
			} catch (AccountException ae) {
				message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, ae.getMessage());
				ok = false;
			} 
		} else {
			ok = false;			
			//message = "System not yet Ready for Authentication";
			message = MessageLookup.buildEventMessage(ErrorMessage.SYSTEM_NOT_READY, "System not yet Ready for Authentication");

		}
		
		log.info("processUserCreateAndLoginEvent: " + event.getUser() + ", " + ok + ", " + message);
		
		if(!ok)
		{
			try {
				eventManager.sendRemoteEvent(new UserCreateAndLoginReplyEvent(event.getKey(), 
						event.getSender(), user, null, null, false, event.getOriginalID(), message, event.getTxId(), false));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private String getDefaultAccountId(User user) throws UserException {
		String defaultAccountId = user.getDefaultAccount();

		if (null == user.getDefaultAccount() || user.getDefaultAccount().equals("")) {
			if (!accountKeeper.accountExists(user.getId() + "-" + Default.getMarket())) {
				defaultAccountId = user.getId() + "-" + Default.getMarket();
			} else {
				defaultAccountId = generateAccountId();
				if (accountKeeper.accountExists(defaultAccountId)) {
					throw new UserException("Cannot create default account for user: " +
							user.getId() + ", last try: " + defaultAccountId, ErrorMessage.CREATE_DEFAULT_ACCOUNT_ERROR);
				}
			}
		}

		return defaultAccountId;
	}

	private void checkUserType(User user) throws UserException {
		if (null == user.getUserType() || (!user.getUserType().equals(UserType.FACEBOOK)
				&& !user.getUserType().equals(UserType.QQ) && !user.getUserType().equals(UserType.WECHAT)
				&& !user.getUserType().equals(UserType.TWITTER))) {
			throw new UserException("Cannot create user by wrong UserType", ErrorMessage.WRONG_USER_TYPE);
		}
	}

	public void processOnUserCreatedEvent(OnUserCreatedEvent event) {
		try {
			userKeeper.createUser(event.getUser());
			for(Account account: event.getAccounts())
				accountKeeper.addAccount(account);
		} catch (Exception e) {
			log.error(e.getMessage() + ", possible data inconsistency", e);
		}
		log.info("User created in cache: " + event.getUser().getId());
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
				//message = ae.getMessage();
				message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, ae.getMessage());
				
				ok = false;
			}
		} else {
			ok = false;
			//message = "System doesn't support account creation";
			message = MessageLookup.buildEventMessage(ErrorMessage.CREATE_USER_FAILED, "System doesn't support account creation");

		}
		
		try {
			eventManager.sendRemoteEvent(new CreateAccountReplyEvent(event.getKey(), 
					event.getSender(), ok, message, event.getTxId()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		
	}
	
	public void processUpdateParentOrderEvent(UpdateParentOrderEvent event) {
		log.debug("processUpdateParentOrderEvent: " + event.getParent());
		Account account = accountKeeper.getAccount(event.getParent().getAccount());
		positionKeeper.processParentOrder(event.getParent().clone(), account);
	}
	
	public void processUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		Quote quote = marketData.get(event.getOrder().getSymbol());
		if(null == quote) {
			eventManager.sendEvent(new QuoteSubEvent(AccountPositionManager.ID, null, 
					event.getOrder().getSymbol()));
		
		}
		
		Execution execution = event.getExecution();
		if(null != execution) {
			log.debug("Process execution: " + execution + ", " + event.getOrder().getId() + ", " + event.getOrder().getStrategyId());			
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
		
		marketData.put(event.getQuote().getSymbol(), event.getQuote());
		updateFxRates(quote);
	}
	
	private void updateFxRates(Quote quote) {
		RefData refData = refDataManager.getRefData(quote.getSymbol());
		if(refData != null && 
		   refData.getExchange() != null && 
		   refData.getExchange().equals("FX")) {
			fxConverter.updateRate(quote);
		}
	}
	
	public void processAccountSnapshotRequestEvent(AccountSnapshotRequestEvent event) {
		Account account = accountKeeper.getAccount(event.getAccountId());
		AccountSnapshotReplyEvent reply;
		if(null == account) {
			reply = new AccountSnapshotReplyEvent(event.getKey(), event.getSender(), 
					null, null, null, null, null, event.getTxId());
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
					account, accountSetting, openPositions, closedPosition, executions, event.getTxId());
		}
		
		try {
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void processAllAccountSnapshotRequestEvent(AllAccountSnapshotRequestEvent event) {
		if(null == accountKeeper)
			return;
		
		List<Account> allAccounts = accountKeeper.getAllAccounts();
		asyncSendAccountSnapshot(event, allAccounts);
	}
	
	private void asyncSendAccountSnapshot(final AllAccountSnapshotRequestEvent event, final List<Account> allAccounts) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				int i = 0;
				List<Account> batch = new ArrayList<Account>();
				for(i=1; i<=allAccounts.size(); i++) {
					batch.add(allAccounts.get(i-1));
					if(i%asyncSendBatch == 0 || i == allAccounts.size()) {
						AllAccountSnapshotReplyEvent reply = new AllAccountSnapshotReplyEvent(
								event.getKey(), event.getSender(), batch);
						try {
							eventManager.sendRemoteEvent(reply);
							log.info("AllAccountSnapshotReplyEvent sent: " + batch.size());
							Thread.sleep(asyncSendInterval);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						batch.clear();
					}
				}
				
			}
			
		});
		thread.start();
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
			//message =  e.getMessage();
			message = MessageLookup.buildEventMessage(e.getClientMessage(), e.getMessage());

		}
		
		AccountSettingSnapshotReplyEvent reply = new AccountSettingSnapshotReplyEvent(event.getKey(), 
				event.getSender(), accountSetting, ok, message, event.getTxId());
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
			log.info("Updating account settings: " + event.getAccountSetting());
			accountSetting = accountKeeper.setAccountSetting(event.getAccountSetting());
		} catch (AccountException e) {
			ok = false;
			//message =  e.getMessage();
			message = MessageLookup.buildEventMessage(e.getClientMessage(),e.getMessage());

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
	
	public void processInternalResetAccountRequestEvent(InternalResetAccountRequestEvent event) {
		ResetAccountRequestEvent evt = event.getEvent();
		log.info("Received InternalResetAccountRequestEvent: " + evt.getAccount());
		positionKeeper.resetAccount(evt.getAccount());
		try {
			eventManager.sendRemoteEvent(new ResetAccountReplyEvent(evt.getKey(), 
					evt.getSender(), evt.getAccount(), evt.getTxId(), evt.getUserId(), evt.getMarket(), evt.getCoinId(), ResetAccountReplyType.LTSCORE, true, ""));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if(event == timerEvent) {
			updateDynamicData();
		} else if (event == dayEndEvent) {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			int nDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			
			if(nDayOfWeek != Calendar.SUNDAY && nDayOfWeek != Calendar.SATURDAY)
				processDayEndTasks();
			scheduleDayEndEvent();
			if(refDataManager instanceof FuRefDataManager){				
				try {
					refDataManager.init();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	private void updateDynamicData() {
		if(!recoveryDone)
			return;
		
		if(!allFxRatesReceived) {
			for(String symbol: fxSymbols) {
				Double rate = fxConverter.getFxRate(symbol);
				if(null == rate || PriceUtils.isZero(rate)) {
					log.debug("Waiting on FX rate: " + symbol);
					return;
				}
			}
			allFxRatesReceived = true;
			log.info("FX rates ready: " + fxConverter.toString());
		}
		
		if(resetMarginHeld) {
			resetMarginHeld = false;
			positionKeeper.resetMarginHeld();
		}
		
		if(rmUpdateThrottler.check()) {
			perfDataRm.start();
			List<Account> accounts = accountKeeper.getRmJobs().getJobs();
			for(Account account: accounts) {
				positionKeeper.updateAccountDynamicData(account);
				if(!checkStopLoss(account))
					checkMarginCall(account);
			}
			perfDataRm.end();
		}
		
		if(dynamicUpdateThrottler.check()) {
			perfDataUpdate.start();
			List<Account> accounts = accountKeeper.getDynamicJobs().getJobs();
			for(Account account: accounts) {
				List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
				for(OpenPosition position: positions) {
					positionListener.onOpenPositionDynamiceUpdate(position);
				}
				positionListener.onAccountDynamicUpdate(account);
			}
			perfDataUpdate.end();
		}
	}
	
	private boolean quoteIsValid(Quote quote) {
		if(null != quoteChecker && !quoteChecker.check(quote))
			return false;
		
		return !quote.isStale();
	}
	
	private boolean checkStopLoss(Account account) {
		AccountSetting accountSetting = null;
		try {
			accountSetting = accountKeeper.getAccountSetting(account.getId());
		} catch (AccountException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		
		Double positionStopLoss = Default.getPositionStopLoss();
		
		if(null != accountSetting) {
			positionStopLoss = accountSetting.getStopLossValue();
			Double companyStopLoss = accountSetting.getCompanySLValue();
			if(null == positionStopLoss)
				positionStopLoss = Default.getPositionStopLoss();
			
			if(null != companyStopLoss && !PriceUtils.isZero(companyStopLoss)) {
				if(PriceUtils.isZero(positionStopLoss))
					positionStopLoss = companyStopLoss;
				else
					positionStopLoss = Math.min(positionStopLoss, companyStopLoss);
			}
		}
		
		if(PriceUtils.isZero(positionStopLoss))
			return false;
		
		boolean result = false;
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);	
		for(OpenPosition position: positions) {
			Quote quote = marketData.get(position.getSymbol());
			if(PriceUtils.EqualLessThan(position.getAcPnL(), -positionStopLoss) && 
					null != quote &&
					quoteIsValid(quote)) {
						if(positionKeeper.checkAccountPositionLock(account.getId(), position.getSymbol())) {
							log.debug("Position stop loss over threshold but account is locked: " + 
								account.getId() + ", " + position.getSymbol());
							continue;
						}
				log.info("Position loss over threshold, cutting loss: " + position.getAccount() + ", " +
						position.getSymbol() + ", " + position.getAcPnL() + ", " + 
						positionStopLoss + ", " + quote);
				ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(), 
						null, position.getAccount(), position.getSymbol(), 0.0, OrderReason.StopLoss,
						IdGenerator.getInstance().getNextID());
				
				eventManager.sendEvent(event);
				result = true;
			}
		}
		return result;
	}
	
	private boolean checkMarginCall(Account account) {
		boolean result = false;
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
		if(PriceUtils.EqualLessThan(account.getCashAvailable(), 0.0) && positions.size() > 0) {
			log.info("Margin call: " + account.getId() + ", " + account.getCash() + ", " + account.getUrPnL() + ", " + account.getCashAvailable());
			
			List<ParentOrder> orders = positionKeeper.getParentOrders(account.getId());
			
			if(orders.size()> 0) {
				for(ParentOrder order: orders) {
					Quote quote = marketData.get(order.getSymbol());
					if(!quoteIsValid(quote))
						continue;
					
					if(order.getOrdStatus().isCompleted())
						continue;
					
					log.info("Margin cut cancel order: " + account.getId() + ", " +
							order.getSymbol() + ", " + 
							account.getMargin() + ", " + 
							account.getCashAvailable() + ", " + quote);

					try {
						positionKeeper.lockAccountPosition(order);
						String source = order.get(String.class, OrderField.SOURCE.value());
						String txId = order.get(String.class, OrderField.CLORDERID.value());
						CancelStrategyOrderEvent cancel = 
								new CancelStrategyOrderEvent(order.getId(), order.getSender(), txId, source, OrderReason.MarginCall, false);
						eventManager.sendEvent(cancel);
						result = true;
						break;
					} catch (AccountException e) {
						log.warn("Try to cancel order but account is locked: " + e.getMessage());
					}
				}
			} else {

				Collections.sort(positions, new Comparator<OpenPosition>() {
	
					@Override
					public int compare(OpenPosition p1, OpenPosition p2) {
						if(PriceUtils.GreaterThan(p1.getAcPnL(), p2.getAcPnL()))
							return 1;
						else if(PriceUtils.LessThan(p1.getAcPnL(), p2.getAcPnL()))
							return -1;
						
						return 0;
					}
					
				});
				
				String sortedList = "";
				for(OpenPosition position: positions) {
					sortedList += position.getAcPnL() + ",";
				}
				log.debug("Sorted list: " + sortedList);
				
				for(int i=0; i<positions.size(); i++) {
					OpenPosition position = positions.get(i);
					Quote quote = marketData.get(position.getSymbol());
					if(!quoteIsValid(quote))
						continue;
	
					if(positionKeeper.checkAccountPositionLock(account.getId(), position.getSymbol())) {
						log.debug("Margin call but account is locked: " + 
							account.getId() + ", " + position.getSymbol());
						return true;
					}
	
					log.info("Margin cut close position: " + position.getAccount() + ", " +
							position.getSymbol() + ", " + position.getAcPnL() + ", " + 
							account.getMargin() + ", " + 
							account.getCashAvailable() + ", " + quote);
					
					double qty = Math.min(Math.abs(position.getQty()), Default.getMarginCut());
					ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(), 
							null, position.getAccount(), position.getSymbol(), qty, OrderReason.MarginCall,
							IdGenerator.getInstance().getNextID());
					
					eventManager.sendEvent(event);
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	private void processDayEndTasks() {
		log.info("Account day end processing start");
		List<Account> list = accountKeeper.getAllAccounts();
		for(Account account: list) {
			Account copy = positionKeeper.rollAccount(account);
			eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, copy));
			account.resetDailyPnL();
		}
		eventManager.sendEvent(new PmEndOfDayRollEvent(PersistenceManager.ID, null, tradeDate));
	}
	
	public void processTradeDateEvent(TradeDateEvent event){
		tradeDate = event.getTradeDate();
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
	
	public void endAcountPositionRecovery() {
		recoveryDone = true;
		log.info("Account position recovery done");
	}
	
	public void processSettlementEvent(SettlementEvent event) {
		String symbol  = event.getSymbol();
		log.info("Received SettlementEvent: " + symbol);
		Quote quote = marketData.get(symbol);
		if(null == quote && QuoteUtils.validQuote(quote)) {
			log.error("processSettlementDayEvent quote is null or invalid: " + symbol + ", " + quote);
			return;
		}
		List<Account> accounts = accountKeeper.getAllAccounts();
		for(Account account: accounts) {
			OpenPosition position = positionKeeper.getOverallPosition(account, symbol);
			if(!PriceUtils.isZero(position.getQty())) {
				Execution exec = new Execution(symbol, position.getQty()>0?OrderSide.Sell:OrderSide.Buy,
						Math.abs(position.getQty()),
						QuoteUtils.getMarketablePrice(quote, position.getQty()),
						"", "", 
						"", "Settlement",
						position.getUser(), position.getAccount(), "Settlement");
				exec.put(OrderField.ID.value(), IdGenerator.getInstance().getNextID() + "STLM");
				try {
					log.debug("Settling position: " + position + "  with " + quote);
					positionKeeper.processExecution(exec, account);
				} catch (PositionException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
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

	public void setDailyExecTime(String dailyExecTime){
		this.dailyExecTime = dailyExecTime;
	}
	
	public String getDailyExecTime(){
		return this.dailyExecTime;
	}

	public IQuoteChecker getQuoteChecker() {
		return quoteChecker;
	}

	public void setQuoteChecker(IQuoteChecker quoteChecker) {
		this.quoteChecker = quoteChecker;
	}

	public long getDynamicUpdateInterval() {
		return dynamicUpdateInterval;
	}

	public void setDynamicUpdateInterval(long dynamicUpdateInterval) {
		this.dynamicUpdateInterval = dynamicUpdateInterval;
	}

	public long getRmUpdateInterval() {
		return rmUpdateInterval;
	}

	public void setRmUpdateInterval(long rmUpdateInterval) {
		this.rmUpdateInterval = rmUpdateInterval;
	}

	public long getPerfUpdateInterval() {
		return perfUpdateInterval;
	}

	public void setPerfUpdateInterval(long perfUpdateInterval) {
		this.perfUpdateInterval = perfUpdateInterval;
	}

	public long getPerfRmInterval() {
		return perfRmInterval;
	}

	public void setPerfRmInterval(long perfRmInterval) {
		this.perfRmInterval = perfRmInterval;
	}

	public int getAsyncSendBatch() {
		return asyncSendBatch;
	}

	public void setAsyncSendBatch(int asyncSendBatch) {
		this.asyncSendBatch = asyncSendBatch;
	}

	public long getAsyncSendInterval() {
		return asyncSendInterval;
	}

	public void setAsyncSendInterval(long asyncSendInterval) {
		this.asyncSendInterval = asyncSendInterval;
	}

	public boolean isSendDynamicPositionUpdate() {
		return sendDynamicPositionUpdate;
	}

	public void setSendDynamicPositionUpdate(boolean sendDynamicPositionUpdate) {
		this.sendDynamicPositionUpdate = sendDynamicPositionUpdate;
	}

	public boolean isResetMarginHeld() {
		return resetMarginHeld;
	}

	public void setResetMarginHeld(boolean resetMarginHeld) {
		this.resetMarginHeld = resetMarginHeld;
	}

}
