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

import com.cyanspring.common.staticdata.AccountSaver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.ILeverageManager;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.account.PositionException;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.business.AuditType;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEventMultiProcessor;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountStateReplyEvent;
import com.cyanspring.common.event.account.AccountStateRequestEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.AddCashEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotRequestEvent;
import com.cyanspring.common.event.account.AllUserSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllUserSnapshotRequestEvent;
import com.cyanspring.common.event.account.CSTWUserLoginEvent;
import com.cyanspring.common.event.account.CSTWUserLoginReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.event.account.ChangeAccountStateReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountStateRequestEvent;
import com.cyanspring.common.event.account.ChangeUserRoleEvent;
import com.cyanspring.common.event.account.ChangeUserRoleReplyEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.CreateAccountEvent;
import com.cyanspring.common.event.account.CreateAccountReplyEvent;
import com.cyanspring.common.event.account.CreateGroupManagementEvent;
import com.cyanspring.common.event.account.CreateGroupManagementReplyEvent;
import com.cyanspring.common.event.account.CreateUserEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.event.account.DeleteGroupManagementEvent;
import com.cyanspring.common.event.account.DeleteGroupManagementReplyEvent;
import com.cyanspring.common.event.account.ExecutionUpdateEvent;
import com.cyanspring.common.event.account.GroupManageeReplyEvent;
import com.cyanspring.common.event.account.GroupManageeRequestEvent;
import com.cyanspring.common.event.account.InternalResetAccountRequestEvent;
import com.cyanspring.common.event.account.OnUserCreatedEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.OverAllPositionReplyEvent;
import com.cyanspring.common.event.account.OverAllPositionRequestEvent;
import com.cyanspring.common.event.account.PmAddCashEvent;
import com.cyanspring.common.event.account.PmChangeAccountSettingEvent;
import com.cyanspring.common.event.account.PmCreateAccountEvent;
import com.cyanspring.common.event.account.PmCreateGroupManagementEvent;
import com.cyanspring.common.event.account.PmCreateUserEvent;
import com.cyanspring.common.event.account.PmDeleteGroupManagementEvent;
import com.cyanspring.common.event.account.PmEndOfDayRollEvent;
import com.cyanspring.common.event.account.PmRemoveDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.PmUpdateDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUpdateUserEvent;
import com.cyanspring.common.event.account.PmUserCreateAndLoginEvent;
import com.cyanspring.common.event.account.PmUserLoginEvent;
import com.cyanspring.common.event.account.ResetAccountReplyEvent;
import com.cyanspring.common.event.account.ResetAccountReplyType;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.account.UserCreateAndLoginEvent;
import com.cyanspring.common.event.account.UserCreateAndLoginReplyEvent;
import com.cyanspring.common.event.account.UserLoginEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.SettlementEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.marketsession.TradeDateRequestEvent;
import com.cyanspring.common.event.order.CancelStrategyOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.ManualClosePositionRequestEvent;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.event.order.UpdateOpenPositionPriceEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.fx.FxUtils;
import com.cyanspring.common.fx.IFxConverter;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.ExtraEventMessage;
import com.cyanspring.common.message.ExtraEventMessageBuilder;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PerfDurationCounter;
import com.cyanspring.common.util.PerfFrequencyCounter;
import com.cyanspring.common.util.TimeThrottler;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.server.livetrading.LiveTradingSetting;
import com.cyanspring.server.livetrading.TradingUtil;
import com.cyanspring.server.livetrading.checker.FrozenStopLossCheck;
import com.cyanspring.server.livetrading.checker.LiveTradingCheckHandler;
import com.cyanspring.server.livetrading.checker.TerminateStopLossCheck;
import com.cyanspring.server.order.RiskOrderController;
import com.cyanspring.server.persistence.PersistenceManager;
import com.cyanspring.server.validation.transaction.SystemSuspendValidator;
import com.google.common.base.Strings;

public class AccountPositionManager implements IPlugin {
    private static final Logger log = LoggerFactory
            .getLogger(AccountPositionManager.class);

    private final static String ID = AccountPositionManager.class.toString();
    private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private AsyncTimerEvent dayEndEvent = new AsyncTimerEvent();
    private long jobInterval = 1000;
    private List<String> fxSymbols = new ArrayList<String>();
    private boolean allFxRatesReceived = false;
    private Map<String, Quote> marketData = new HashMap<>();
    private Map<String, Double> settlePrices = new HashMap<>();
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
    private int limitUser = 2000;
    private boolean sendDynamicPositionUpdate = false;
    private boolean recoveryDone = false;
    private boolean resetMarginHeld = false;
    private boolean checkStoploss = true;
    private boolean checkMargincut = true;
    private TotalPnLCalculator totalPnLCalculator = new TotalPnLCalculator();
    private TimeThrottler totalPnLCalculatorThrottler = new TimeThrottler(20000);

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

    @Autowired(required = false)
    LiveTradingCheckHandler liveTradingCheckHandler;

    @Autowired(required = false)
    LiveTradingSetting liveTradingSetting;

	@Autowired
	ILeverageManager leverageManager;

	@Autowired(required=false)
	RiskOrderController riskOrderController;

	@Autowired(required = false)
	CoinManager coinManager;

	@Autowired(required = false)
	SystemSuspendValidator systemSuspendValidator;

    @Autowired(required = false)
    AccountSaver accountSaver;

	private IQuoteFeeder quoteFeeder = new IQuoteFeeder() {

        @Override
        public Quote getQuote(String symbol) {
            Quote quote = marketData.get(symbol);
            if (null == quote) {
				eventManager.sendEvent(new QuoteSubEvent(AccountPositionManager.ID, null, symbol));
			}
            return quote;
        }
    };
    
    private AsyncEventProcessor quoteProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
            subscribeToEvent(QuoteEvent.class, null);
            subscribeToEvent(QuoteExtEvent.class, null);			
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
    	
    };

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(UserCreateAndLoginEvent.class, null);    //for Facebook, QQ, WeChat
            subscribeToEvent(UserLoginEvent.class, null);
            subscribeToEvent(CreateUserEvent.class, null);
            subscribeToEvent(CreateAccountEvent.class, null);
            subscribeToEvent(AccountSnapshotRequestEvent.class, null);
            subscribeToEvent(MarketDataReadyEvent.class, null);
            subscribeToEvent(AccountSettingSnapshotRequestEvent.class, null);
            subscribeToEvent(ChangeAccountSettingRequestEvent.class, null);
            subscribeToEvent(AllAccountSnapshotRequestEvent.class, null);
            subscribeToEvent(AllPositionSnapshotRequestEvent.class, null);
            subscribeToEvent(OnUserCreatedEvent.class, null);
            subscribeToEvent(TradeDateEvent.class, null);
            subscribeToEvent(IndexSessionEvent.class, null);
            subscribeToEvent(InternalResetAccountRequestEvent.class, null);
            subscribeToEvent(SettlementEvent.class, null);
            subscribeToEvent(AccountStateRequestEvent.class,null);
            subscribeToEvent(CreateGroupManagementEvent.class,null);
            subscribeToEvent(DeleteGroupManagementEvent.class,null);
            subscribeToEvent(GroupManageeRequestEvent.class,null);
            subscribeToEvent(CSTWUserLoginEvent.class,null);
            subscribeToEvent(AllUserSnapshotRequestEvent.class,null);
            subscribeToEvent(ChangeUserRoleEvent.class,null);
            subscribeToEvent(AddCashEvent.class, null);
            subscribeToEvent(ChangeAccountStateRequestEvent.class,null);
            subscribeToEvent(ManualClosePositionRequestEvent.class, null);
            subscribeToEvent(UpdateOpenPositionPriceEvent.class, null);
            subscribeToEvent(OverAllPositionRequestEvent.class, null);
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

    private Date getScheuledDate() throws AccountException {
        if (dailyExecTime == null || dailyExecTime.length() == 0) {
			throw new AccountException("didn't set scheduled daily execution time");
		}

        String[] times = dailyExecTime.split(":");
        if (times.length != 3) {
			throw new AccountException("daily execution time is not valid");
		}

        int nHour = Integer.parseInt(times[0]);
        int nMin = Integer.parseInt(times[1]);
        int nSecond = Integer.parseInt(times[2]);

        Calendar cal = Default.getCalendar();
        Date now = Clock.getInstance().now();

        Date scheduledToday = TimeUtil.getScheduledDate(cal, now, nHour, nMin, nSecond);

        if (TimeUtil.getTimePass(now, scheduledToday) > 0) {
			scheduledToday = TimeUtil.getNextDay(scheduledToday);
		}

        return scheduledToday;
    }

    private void scheduleDayEndEvent() {
        try {
            Date date = getScheuledDate();
            log.info("Scheduling day end processing at: " + date);
            scheduleManager.scheduleTimerEvent(date, eventProcessor, dayEndEvent);
        } catch (AccountException e) {
            log.error("can't schedule daily timer", e);
        }
    }

    @Override
    public void init() throws Exception {
        if (null != accountKeeper) {
			accountKeeper.init();
		}

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
        if (eventProcessor.getThread() != null) {
			eventProcessor.getThread().setName("AccountPositionManager");
		}
 
        quoteProcessor.setHandler(this);
        quoteProcessor.init();
        if (quoteProcessor.getThread() != null) {
        	quoteProcessor.getThread().setName("AccountPositionManager(quote)");
		}
        
        eventMultiProcessor.setHandler(this);
        eventMultiProcessor.setHash(true);
        eventMultiProcessor.init();
        eventMultiProcessor.setName("AccountPositionTP");

        timerProcessor.setHandler(this);
        timerProcessor.init();
        if (timerProcessor.getThread() != null) {
			timerProcessor.getThread().setName("AccountPositionManager-Timer");
		}

        if (tradeDate == null) {
            try {
                TradeDateRequestEvent tdrEvent = new TradeDateRequestEvent(null, null);
                eventManager.sendEvent(tdrEvent);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        scheduleManager.scheduleRepeatTimerEvent(jobInterval, timerProcessor, timerEvent);

        scheduleDayEndEvent();
    }

    IPositionListener positionListener = new IPositionListener() {

        boolean dynamicDataHasChanged(Account account) {
            Account last = accountUpdates.get(account.getId());
            if (last == null || account == null || last.getMargin() != account.getMargin() ||
                    last.getUrPnL() != account.getUrPnL() || last.getCashAvailable() != account.getCashAvailable()
                    || last.getPnL() != account.getPnL() || last.getUrLastPnL() != account.getUrLastPnL()) {
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
            if (last == null || position == null || last.getPnL() != position.getPnL() || last.getAcPnL() != position.getAcPnL()
            		|| last.getAcLastPnL() != position.getAcLastPnL()) {
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
                if (sendDynamicPositionUpdate && dynamicDataHasChanged(position)) {
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
                if (dynamicDataHasChanged(account)) {
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
        scheduleManager.uninit();
        quoteProcessor.uninit();
        eventProcessor.uninit();
        timerProcessor.uninit();
        eventMultiProcessor.uninit();
    }

    public void processOverAllPositionRequestEvent(OverAllPositionRequestEvent event){
    	
    	log.info("start processOverAllPositionRequestEvent");
    	List <Account> accountList = new ArrayList<Account>();
    	
    	if(null == event.getAccountIdList()){
    		
    		accountList = accountKeeper.getAllAccounts();
    		if(accountList.size() > limitUser){
                try {
                	OverAllPositionReplyEvent reply = new OverAllPositionReplyEvent(event.getKey()
                			, event.getSender(),false,"over limit user counts:"+limitUser,null,null);
                    eventManager.sendRemoteEvent(reply);
                    log.info("OverAllPositionReplyEvent sent fail: over limit user counts:"+limitUser);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
    		}
    	}else{
    		
    		for(String id : event.getAccountIdList()){
    			Account account = accountKeeper.getAccount(id);
    			if( null != account)
    				accountList.add(account);
    			else
            		log.info("(AllPositionRequestEvent) can't find this account:{}",id);

    		}
    	}
    	
    	log.info("accountList size:{}",accountList.size());
    	asyncSendOverallPosition(event,accountList);
    }
    
    private void asyncSendOverallPosition(final OverAllPositionRequestEvent event, final List<Account> accounts) {
    	
        Thread thread = new Thread(new Runnable() {
        	
        	private void sendOverAllPositionReplyEvent(List<OpenPosition> openPositionList, List<ClosedPosition> closedPositionList){
                try {
                	OverAllPositionReplyEvent reply = new OverAllPositionReplyEvent(event.getKey()
                			, event.getSender(),true,""
                			,openPositionList
                			,closedPositionList);

                    eventManager.sendRemoteEvent(reply);
                    log.info("OverAllPositionReplyEvent sent: op:{} cp:{}",openPositionList.size(),closedPositionList.size());
                    Thread.sleep(asyncSendInterval);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
        	}
        	
            @Override
            public void run() {
            	
            	int positionCount = 0;
                List<OpenPosition> openPositionList = new ArrayList<>();
                List<ClosedPosition> closedPositionList = new ArrayList<>();
                for (int i = 0; i < accounts.size(); i++) {
                	
                	Account account = accounts.get(i);               	           	
                    List<OpenPosition> tempOpList = positionKeeper.getOverallPosition(account);
                    List<ClosedPosition> tempCpList = positionKeeper.getClosedPositions(account.getId());
                	if ( (null == tempOpList || tempOpList.isEmpty())
                			&& (null == tempCpList || tempCpList.isEmpty()) ) {
						continue;
					}
     
                	for (OpenPosition op: tempOpList) {
                		openPositionList.add(op);
                		positionCount++;
                		if (positionCount % asyncSendBatch == 0 ) {
                			sendOverAllPositionReplyEvent(openPositionList,closedPositionList);
                			positionCount = 0 ;
                			openPositionList.clear();
                			closedPositionList.clear();
                		}
                	}
                	
                	for (ClosedPosition cp: tempCpList) {
                		closedPositionList.add(cp);
                		positionCount++;
                		if (positionCount % asyncSendBatch == 0 ) {
                			sendOverAllPositionReplyEvent(openPositionList,closedPositionList);
                			positionCount = 0 ;
                			openPositionList.clear();
                			closedPositionList.clear();
                		}
                	}
                }//for account
                
            	if (positionCount != 0) {
        			sendOverAllPositionReplyEvent(openPositionList,closedPositionList);
           			positionCount = 0 ;
           			openPositionList.clear();
        			closedPositionList.clear();
            	}
            }
            
        });
        thread.start();
    }
    
    public void processUpdateOpenPositionPriceEvent(UpdateOpenPositionPriceEvent event) {
    	String symbol = event.getSymbol();
    	String account = event.getAccount();
    	double price = event.getPrice();
    	log.info("processUpdateOpenPositionPriceEvent, account: " + account +
    			", symbol: " + symbol + ", price: " + price);
    	if (!StringUtils.hasText(symbol) ||
    			!StringUtils.hasText(account) || PriceUtils.EqualLessThan(price, 0)) {
    		log.error("processUpdateOpenPositionPriceEvent fail, account: " + account +
    				", symbol: " + symbol + ", price: " + price);
    		return;
    	}

    	try {
			positionKeeper.updateAccountOpenPosition(account, symbol, price);
		} catch (AccountException e) {
			log.error(e.getMessage(), e);
		}

    }

    public void processManualClosePositionRequestEvent(ManualClosePositionRequestEvent event) {
    	OpenPosition position = event.getOpenPosition();
    	double price = event.getPrice();
    	log.info("processManualClosePositionRequestEvent, "
    			+ "account: "+ position.getAccount() +", symbol: " + position.getSymbol() + ", price: " + price);

    	if (PriceUtils.LessThan(price, 0)) {
			log.warn("The close price is equal or less than zero.");
		} else if (PriceUtils.Equal(price, 0)) {
    		Quote quote = marketData.get(position.getSymbol());
    		price = QuoteUtils.getMarketablePrice(quote, position.getQty());
    	}
    	Execution exec = new Execution(position.getSymbol(), position.getQty() > 0 ? OrderSide.Sell : OrderSide.Buy,
    			Math.abs(position.getQty()), price, "", "", "", IdGenerator.getInstance().getNextID(),
    			position.getUser(), position.getAccount(), "");

    	try {
			positionKeeper.processExecution(exec, accountKeeper.getAccount(position.getAccount()));
		} catch (PositionException e) {
			log.error(e.getMessage(), e);
		}
    }

    public void processAddCashEvent(AddCashEvent event) {
        log.info("process AddCashEvent, account: {}, cash: {}", event.getAccount(), event.getCash());
        Account account = accountKeeper.getAccount(event.getAccount());

        if (account == null) {
        	log.error("Cannot find account: {}", event.getAccount());
        	return;
        }
        account.addCash(event.getCash());
        PmAddCashEvent pmAddCashEvent = new PmAddCashEvent(null, null,
                account, event.getCash(), AuditType.DEPOSIT);
        eventManager.sendEvent(pmAddCashEvent);
        positionListener.onAccountUpdate(account);
    }

    public void processUserLoginEvent(UserLoginEvent event) {
        log.debug("Received UserLoginEvent: " + event.getUserId() + ", txid: " + event.getTxId() + ", k: " + event.getKey());
        eventManager.sendEvent(new PmUserLoginEvent(PersistenceManager.ID, event.getReceiver(), userKeeper, accountKeeper, event));
    }

    public void processCreateUserEvent(CreateUserEvent event) {
        boolean ok = true;
        User user = event.getUser();
        String message = "";
        if (null != userKeeper && null != accountKeeper) {
            try {
                user.setId(user.getId().toLowerCase());
                if (userKeeper.userExists(user.getId())) {
					throw new UserException("User already exists: " + user.getId(), ErrorMessage.USER_ALREADY_EXIST);
				}

                String defaultAccountId = getDefaultAccountId(user);
                user.setDefaultAccount(defaultAccountId);

                if (null == user.getUserType()) {
					user.setUserType(UserType.NORMAL);
				}

                Account account = new Account(defaultAccountId, event.getUser().getId());
                accountKeeper.setupAccount(account);

                eventManager.sendEvent(new PmCreateUserEvent(PersistenceManager.ID, null, user, event, Arrays.asList(account)));
            } catch (UserException ue) {
                message = MessageLookup.buildEventMessage(ue.getClientMessage(), ue.getMessage());
                ok = false;
            } catch (AccountException ae) {
                message = MessageLookup.buildEventMessage(ae.getClientMessage(), ae.getMessage());
                ok = false;
            }
        } else {
            ok = false;
            message = MessageLookup.buildEventMessage(ErrorMessage.CREATE_USER_FAILED, "System doesn't support user creation");
        }

        log.info("processCreateUserEvent: " + event.getUser() + ", " + ok + ", " + message);

        if (!ok) {
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

        if (null != userKeeper && null != accountKeeper) {
            try {
                checkUserType(user);

                if (null == user.getId()) {
                    user.setId("");
                }

                user.setId(user.getId().toLowerCase());

                if (Strings.isNullOrEmpty(user.getId()) || !userKeeper.userExists(user.getId())) {
                    String defaultAccountId = getDefaultAccountId(user);

                    user.setDefaultAccount(defaultAccountId);
                    Account account = new Account(defaultAccountId, event.getUser().getId());
                    accountKeeper.setupAccount(account);

                    //user not exist, but it should
                    if (event.isExistUser()) {
                        throw new UserException("userid or password invalid", ErrorMessage.INVALID_USER_ACCOUNT_PWD);
                    }

                    //user not exist
                    eventManager.sendEvent(new PmUserCreateAndLoginEvent(PersistenceManager.ID, event.getReceiver(), user, event, userKeeper, accountKeeper, Arrays.asList(account)));
                } else {
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

        if (!ok) {
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
            for (Account account : event.getAccounts()) {
				accountKeeper.addAccount(account);
			}
        } catch (Exception e) {
            log.error(e.getMessage() + ", possible data inconsistency", e);
        }
        log.info("User created in cache: " + event.getUser().getId());
    }

    public void processChangeAccountStateRequestEvent(ChangeAccountStateRequestEvent event) {
    	String id = event.getId();
    	String message = "";
    	boolean isOk =false;
    	AccountState newState = event.getState();
    	Account account = null;
    	check :{

    		if (!StringUtils.hasText(id)) {
    			message = MessageLookup.buildEventMessage(ErrorMessage.CHANGE_ACCOUNT_STATE_FAILED, "id is empty!");
    			break check;
    		}

    		if ( null == newState ) {
    			message = MessageLookup.buildEventMessage(ErrorMessage.CHANGE_ACCOUNT_STATE_FAILED, "State is empty!");
    			break check;
    		}

    		account = accountKeeper.getAccount(id);
    		if ( null == account) {
    			message = MessageLookup.buildEventMessage(ErrorMessage.CHANGE_ACCOUNT_STATE_FAILED, "can't find this account!");
    			break check;
    		}

    		if (newState.equals(account.getState())) {
    			message = MessageLookup.buildEventMessage(ErrorMessage.CHANGE_ACCOUNT_STATE_FAILED, "State aready is:"+newState.name());
    			break check;
    		}

    	}

    	if (!StringUtils.hasText(message)) {
        	if (AccountState.ACTIVE.equals(newState)) {
        		activeAccount(account,event);
        	} else if (AccountState.FROZEN.equals(newState) || AccountState.TERMINATED.equals(newState)) {
            	TradingUtil.closeAllPositoinAndOrder(account, positionKeeper, eventManager, true, OrderReason.CompanyManualClose, riskOrderController);
            	setAccountState(account,newState,event);
        	}
    	} else {
    		ChangeAccountStateReplyEvent reply = new ChangeAccountStateReplyEvent(event.getKey(),event.getSender(),isOk,message,account);
            try {
				eventManager.sendRemoteEvent(reply);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }

    private void setAccountState(Account account, AccountState newState,ChangeAccountStateRequestEvent event) {
		log.info("Account:{}, old state:{} --> new state:{}",new Object[]{account.getId(),account.getState(),newState});
    	account.setState(newState);
    	ChangeAccountStateReplyEvent reply = new ChangeAccountStateReplyEvent(event.getKey(),event.getSender(),true,"",account);
		AccountUpdateEvent accountStateUpdate = new AccountUpdateEvent(account.getId(), null, account);

    	try {
			eventManager.sendRemoteEvent(reply);
			eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
	    	eventManager.sendRemoteEvent(accountStateUpdate);
    	} catch (Exception e) {
			log.warn(e.getMessage(),e);
		}
	}

    private void activeAccount(Account account,ChangeAccountStateRequestEvent event) {
    	AccountSetting accountSetting;
    	TerminateStopLossCheck terminateCheck = new TerminateStopLossCheck();
    	FrozenStopLossCheck frozenCheck  = new FrozenStopLossCheck();
    	boolean isOk = true;
    	String message = "";
		try {
			accountSetting = accountKeeper.getAccountSetting(account.getId());

			ChangeAccountStateReplyEvent reply = null;

	    	if (AccountState.ACTIVE.equals(account.getState())) {
	        	isOk = false;
	            message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_ALREADY_ACTIVE,"Account already in ACTIVE state");
	            reply = new ChangeAccountStateReplyEvent(event.getKey()
	            		,event.getSender(),isOk,message,account);
	    	} else if (terminateCheck.isOverTerminateLoss(account, accountSetting)) {
	        	isOk = false;
	            message = MessageLookup.buildEventMessage(ErrorMessage.OVER_TERMINATE_LOSS,"Still over terminate loss, you must set terminate loss percent/value under current loss");
	            reply = new ChangeAccountStateReplyEvent(event.getKey()
	            		,event.getSender(),isOk,message,account);
	    	} else if (frozenCheck.isOverFrozenLoss(account, accountSetting)) {
	        	isOk = false;
	            message = MessageLookup.buildEventMessage(ErrorMessage.OVER_FROZEN_LOSS,"Still over frozen loss, you must set frozen loss percent/value under current loss");
	            reply = new ChangeAccountStateReplyEvent(event.getKey()
	            		,event.getSender(),isOk,message,account);
	    	}

	    	if (isOk) {
	    		log.info("Active Account:{}, old state:{}",account.getId(),account.getState());
		    	account.setState(AccountState.ACTIVE);
		        reply = new ChangeAccountStateReplyEvent(event.getKey()
	            		,event.getSender(),isOk,message,account);
				AccountUpdateEvent accountStateUpdate = new AccountUpdateEvent(account.getId(), null, account);
		    	eventManager.sendRemoteEvent(accountStateUpdate);
				eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
	    	}

            eventManager.sendRemoteEvent(reply);

		} catch (AccountException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processCSTWUserLoginEvent(CSTWUserLoginEvent event) {
    	String id = event.getId();
    	String pwd = event.getPassword();
    	boolean isOk = false;
    	String message = "";
    	UserGroup userGroup = null;
    	List <Account> accountList = null;
    	Map<String, Account> user2AccountMap = null;

    	boolean isAdminRole = false;
    	boolean isServerShutdownEvent = event.getShutdownServer();

    	if (userKeeper.isAdmin(id, pwd)) {
    		isAdminRole = true;
    	}

    	if (isAdminRole) {
    		userGroup = new UserGroup(id,UserRole.Admin);
    		CSTWUserLoginReplyEvent reply = new CSTWUserLoginReplyEvent(event.getKey(),event.getSender(),true,"",userGroup,accountList,user2AccountMap );
    		try {
    			eventManager.sendRemoteEvent(reply);
    		} catch (Exception e) {
    			log.warn(e.getMessage(),e);
    		}
    		return;
		}


    	try{
	    	if (!StringUtils.hasText(id) || !StringUtils.hasText(pwd)) {
        		throw new UserException("id or password is empty!",ErrorMessage.CSTW_LOGIN_FAILED);
	    	} else {
	    		id = id.toLowerCase().trim();
	    	}

	    	if ( null == userKeeper.getUser(id) ) {
        		throw new UserException("id not exist",ErrorMessage.CSTW_LOGIN_FAILED);
	    	}

	    	User user = userKeeper.getUser(id);
	    	if (!pwd.equals(user.getPassword())) {
        		throw new UserException("wrong password!",ErrorMessage.CSTW_LOGIN_FAILED);
	    	}

	    	if (isServerShutdownEvent && !isAdminRole) {
				throw new UserException("only admin has permission",
						ErrorMessage.CSTW_LOGIN_FAILED);
			}

	    	if (!StringUtils.hasText(message)) {
				isOk =true;
			}

	    	userGroup = userKeeper.getUserGroup(id);
	    	accountList = accountKeeper.getAccounts(id);
	    	if ( null == userGroup ) {
	    		userGroup = new UserGroup(id,user.getRole());
	    	}
	    	user2AccountMap = new HashMap<>();
	    	user2AccountMap.put(id, accountList.get(0));
			for (UserGroup ug : userGroup.getManageeSet()) {
				user2AccountMap.put(ug.getUser(),
						accountKeeper.getAccounts(ug.getUser()).get(0));
			}
	    	log.info("CSTW Login success:{} - {}",id,userGroup.getRole());
			user.setLastLogin(Clock.getInstance().now());
			eventManager.sendEvent(new PmUpdateUserEvent(PersistenceManager.ID, null, user));

    	}catch(UserException e) {
    		message = MessageLookup.buildEventMessage(e.getClientMessage(), e.getMessage());
    		log.info("CSTW Login fail:{} - {}",id,message);
    	}

		CSTWUserLoginReplyEvent reply = new CSTWUserLoginReplyEvent(event.getKey(),event.getSender(),isOk,message,userGroup,accountList,user2AccountMap);
		try {
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
		}
    }
    public void processGroupManageeRequestEvent(GroupManageeRequestEvent event) {
    	String manager = event.getManager();
    	boolean isOk = true;
    	String message = "";
    	try{
        	if (!StringUtils.hasText(manager)) {
        		throw new UserException("Manager is empty",ErrorMessage.GET_GROUP_MANAGEMENT_INFO_FAILED);
        	}
    		if ( null == userKeeper.getUser(manager) ) {
    			throw new UserException("Manager:"+manager+" doen't exist in User",ErrorMessage.GET_GROUP_MANAGEMENT_INFO_FAILED);
    		}
//    		if ( null == userKeeper.getUserGroup(manager) ) {
//    			throw new UserException("this user doesn't have any group",ErrorMessage.GET_GROUP_MANAGEMENT_INFO_FAILED);
//    		}
    	}catch(UserException e) {
    		isOk = false;
			message = MessageLookup.buildEventMessage(ErrorMessage.GET_GROUP_MANAGEMENT_INFO_FAILED, e.getLocalizedMessage());
			log.info(message);
    	}

		UserGroup userGroup = null;
		if (isOk) {
			userGroup = userKeeper.getUserGroup(manager);
		}

		if ( null == userGroup ) {
			User user = userKeeper.getUser(manager);
			userGroup = new UserGroup(manager,user.getRole());
		}

		GroupManageeReplyEvent reply = new GroupManageeReplyEvent(event.getKey(),event.getSender(),isOk,message,userGroup);
		try {
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
		}
    }

    public void processDeleteGroupManagementEvent(DeleteGroupManagementEvent event) {
    	List<GroupManagement> groupList = event.getGroupManagementList();
    	List<GroupManagement> successList = new ArrayList<GroupManagement>();
    	Map <GroupManagement,String> resultMap = new HashMap<GroupManagement,String>();
    	boolean isOk = true;
    	String message = "";
    	if ( null != userKeeper) {
    		for (GroupManagement group : groupList) {
    			try {
					userKeeper.deleteGroup(group);
					successList.add(group);
					resultMap.put(group, "OK");
					log.info("delete group : {},{}",group.getManager(),group.getManaged());
				} catch (UserException e) {
					isOk = false;
					resultMap.put(group, e.getLocalizedMessage());
        			continue;
				}
    		}
    		if (!successList.isEmpty()) {
    			PmDeleteGroupManagementEvent pmEvent = new PmDeleteGroupManagementEvent(null, null, successList);
        		try {
    				eventManager.sendEvent(pmEvent);
    			} catch (Exception e) {
    				log.error(e.getMessage(),e);
    				isOk = false;
        			message = MessageLookup.buildEventMessage(ErrorMessage.DELETE_GROUP_MANAGEMENT_FAILED, "Exception error:"+e.getMessage());
    			}
    		}
    	} else {
    		isOk = false;
			message = MessageLookup.buildEventMessage(ErrorMessage.DELETE_GROUP_MANAGEMENT_FAILED, "userkeeper not initialized!");
    	}

		DeleteGroupManagementReplyEvent replyEvent = new DeleteGroupManagementReplyEvent(event.getKey(), event.getSender(), isOk, message,resultMap);
		try {
			eventManager.sendRemoteEvent(replyEvent);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
    }

    public void processCreateGroupManagementEvent(CreateGroupManagementEvent event) {
    	List<GroupManagement> groupList = event.getGroupManagementList();
    	List<GroupManagement> successList = new ArrayList<GroupManagement>();
    	Map <GroupManagement,String> resultMap = new HashMap<GroupManagement,String>();
    	boolean isOk = true;
    	String message = "";
    	if ( null != userKeeper) {
    		for (GroupManagement group : groupList) {
    			try {
					userKeeper.createGroup(group);
					successList.add(group);
					resultMap.put(group, "OK");
				} catch (UserException e) {
					isOk = false;
					resultMap.put(group, e.getLocalizedMessage());
        			continue;
				}
    		}
    		if (!successList.isEmpty()) {
    			PmCreateGroupManagementEvent pmEvent = new PmCreateGroupManagementEvent(null, null, successList);
        		try {
    				eventManager.sendEvent(pmEvent);
    			} catch (Exception e) {
    				log.error(e.getMessage(),e);
    				isOk = false;
        			message = MessageLookup.buildEventMessage(ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED, "Exception error:"+e.getMessage());
    			}
    		}
    	} else {
    		isOk = false;
			message = MessageLookup.buildEventMessage(ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED, "userkeeper not initialized!");
    	}

		CreateGroupManagementReplyEvent replyEvent = new CreateGroupManagementReplyEvent(event.getKey(), event.getSender(), isOk, message,resultMap);
		try {
			eventManager.sendRemoteEvent(replyEvent);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
    }
    public void processCreateAccountEvent(CreateAccountEvent event) {
        boolean ok = true;
        String message = "";
        if (null != userKeeper && null != accountKeeper) {
            try {
                Account account = event.getAccount();
                if (!userKeeper.userExists(account.getUserId())) {
					throw new AccountException("User doesn't exists: " + account.getUserId());
				}
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
        if (null == quote) {
            eventManager.sendEvent(new QuoteSubEvent(AccountPositionManager.ID, null,
                    event.getOrder().getSymbol()));

        }

        Execution execution = event.getExecution();
        if (null != execution) {
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
        if (refData != null &&
                refData.getExchange() != null &&
                refData.getExchange().equals("FX")) {
            fxConverter.updateRate(quote);
        }
    }

    public void processQuoteExtEvent(QuoteExtEvent event) {
        if (event.getQuoteExt().fieldExists(QuoteExtDataField.SETTLEPRICE.value())) {
            settlePrices.put(event.getSymbol(), event.getQuoteExt().get(Double.class, QuoteExtDataField.SETTLEPRICE.value()));
        }
    }

    public void processAccountSnapshotRequestEvent(AccountSnapshotRequestEvent event) {
        Account account = accountKeeper.getAccount(event.getAccountId());
        AccountSnapshotReplyEvent reply;
        if (null == account) {
            reply = new AccountSnapshotReplyEvent(event.getKey(), event.getSender(),
                    null, null, null, null, null, event.getTxId(),null);
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
                    account, accountSetting, openPositions, closedPosition, executions, event.getTxId(),getUserLiveTradingTime(accountSetting));
        }

        try {
            eventManager.sendRemoteEvent(reply);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processAllPositionSnapshotRequestEvent(AllPositionSnapshotRequestEvent event) {
        if (null == accountKeeper || null == positionKeeper) {
			return;
		}

        List<Account> allAccounts = accountKeeper.getAllAccounts();
        asyncSendPositionSnapshot(event, allAccounts);
    }

    public void processChangeUserRoleEvent(ChangeUserRoleEvent event) {
    	String id = event.getId();
    	UserRole role= event.getRole();
    	User user = userKeeper.getUser(id);
    	UserGroup userGroup = userKeeper.getUserGroup(id);
    	boolean isOk = true;
    	boolean needClearAllManagee = false;
    	String message = "";

    	try{
        	if ( null == user) {
        		throw new UserException("user not exist!");
        	}

       		UserRole originRole = user.getRole();

    		if (null != originRole && originRole.equals(role)) {
    			throw new UserException("this user role already is "+role);
    		}

        	if ( null != userGroup) {
        		if (UserRole.Trader.equals(role) || UserRole.Admin.equals(role)) {
        			needClearAllManagee = true;
        		}
        	}

    	}catch(UserException e) {
    		isOk = false;
            message = MessageLookup.buildEventMessage(ErrorMessage.CHANGE_USER_ROLE_FAILED, e.getMessage());
            log.info("message:{}",message);
    	}

    	 try {

	    	if (isOk) {
	    		log.info("{} change User role:{} to {} needClearAllManagee:{}",new Object[]{user.getId(),user.getRole(),role,needClearAllManagee});
	    		user.setRole(role);

	    		if ( null != userGroup) {
					userGroup.setRole(role);
				}

	    		PmUpdateUserEvent updateUserEvent = new PmUpdateUserEvent(PersistenceManager.ID,null,user);
	            eventManager.sendEvent(updateUserEvent);
	    		if (needClearAllManagee && !userGroup.getNoneRecursiveManageeList().isEmpty()) {
	    			PmDeleteGroupManagementEvent deleteGroupManagementEvent = new PmDeleteGroupManagementEvent(PersistenceManager.ID, null, userGroup.exportGroupManagementList());
		            eventManager.sendEvent(deleteGroupManagementEvent);
		            userGroup.clearManageeList();
	    		}
	    	}

	    	ChangeUserRoleReplyEvent reply = new ChangeUserRoleReplyEvent(event.getKey(), event.getSender(), isOk, message, user);
            eventManager.sendRemoteEvent(reply);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processAllUserSnapshotRequestEvent(AllUserSnapshotRequestEvent event) {
        if (null == userKeeper) {
			return;
		}


        List<User> allUsers = userKeeper.getAllUsers();
        asyncSendUserSnapshot(event, allUsers);
    }

    private void asyncSendUserSnapshot(final AllUserSnapshotRequestEvent event,
			final List<User> allUsers) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = 0;
                List<User> batch = new ArrayList<User>();
                for (i = 1; i <= allUsers.size(); i++) {
                    batch.add(allUsers.get(i - 1));
                    if (i % asyncSendBatch == 0 || i == allUsers.size()) {
                        AllUserSnapshotReplyEvent reply = new AllUserSnapshotReplyEvent(
                                event.getKey(), event.getSender(), batch);
                        try {
                            eventManager.sendRemoteEvent(reply);
                            log.info("AllUserSnapshotReplyEvent sent: " + batch.size());
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

	public void processAllAccountSnapshotRequestEvent(AllAccountSnapshotRequestEvent event) {
        if (null == accountKeeper) {
			return;
		}

        List<Account> allAccounts = accountKeeper.getAllAccounts();
        asyncSendAccountSnapshot(event, allAccounts);
    }

    private void asyncSendPositionSnapshot(final AllPositionSnapshotRequestEvent event, final List<Account> allAccounts) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
            	int positionCount = 0;
                List<OpenPosition> openPositionList = new ArrayList<>();

                for (int i = 0; i < allAccounts.size(); i++) {

                	Account account = allAccounts.get(i);
                    List<OpenPosition> tempOpList = positionKeeper.getOverallPosition(account);
                	if ( null == tempOpList || tempOpList.isEmpty()) {
						continue;
					}

                	for (OpenPosition op: tempOpList) {
                		openPositionList.add(op);
                		positionCount++;
                		if (positionCount % asyncSendBatch == 0 ) {
                            try {
                            	AllPositionSnapshotReplyEvent reply = new AllPositionSnapshotReplyEvent(
                                        event.getKey(), event.getSender(), openPositionList);
                                eventManager.sendRemoteEvent(reply);
                                log.info("AllPositionSnapshotReplyEvent sent: " + openPositionList.size());
                                Thread.sleep(asyncSendInterval);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                			positionCount = 0 ;
                			openPositionList.clear();
                		}
                	}
                }//for account
            	if (positionCount != 0) {

                    try {
                    	AllPositionSnapshotReplyEvent reply = new AllPositionSnapshotReplyEvent(
                                event.getKey(), event.getSender(), openPositionList);
                        eventManager.sendRemoteEvent(reply);
                        log.info("AllPositionSnapshotReplyEvent sent: " + openPositionList.size());
                        Thread.sleep(asyncSendInterval);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

           			positionCount = 0 ;
           			openPositionList.clear();
            	}
            }

        });
        thread.start();
    }
    private void asyncSendAccountSnapshot(final AllAccountSnapshotRequestEvent event, final List<Account> allAccounts) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = 0;
                List<Account> batch = new ArrayList<Account>();
                for (i = 1; i <= allAccounts.size(); i++) {
                    batch.add(allAccounts.get(i - 1));
                    if (i % asyncSendBatch == 0 || i == allAccounts.size()) {
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
        if (fxSymbols == null) {
			return;
		}

        for (String symbol : fxSymbols) {
            eventManager.sendEvent(new QuoteSubEvent(AccountPositionManager.ID, null, symbol));
        }
    }

    public void processAccountStateRequestEvent(AccountStateRequestEvent event) {
    	boolean isOk = true;
    	String message = "";
    	String id = event.getId();
    	Account account = accountKeeper.getAccount(id);
    	AccountStateReplyEvent reply = null;

    	if (null == account) {
    		isOk = false;
            message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST,"Account not exist");
            reply = new AccountStateReplyEvent(event.getKey()
        			,event.getSender(),isOk,message,id,null,null);
    	} else {
    		reply = new AccountStateReplyEvent(event.getKey()
        			,event.getSender(),isOk,message,id,account.getUserId(),account.getState());
    	}

        try {
            eventManager.sendRemoteEvent(reply);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
                event.getSender(), accountSetting, ok, message, event.getTxId(),getUserLiveTradingTime(accountSetting));

        try {
            eventManager.sendRemoteEvent(reply);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
    private ExtraEventMessageBuilder getUserLiveTradingTime(AccountSetting accountSetting) {

    	if ( null != liveTradingSetting
    			 && null != accountSetting && accountSetting.isLiveTrading() ) {

    			ExtraEventMessageBuilder builder = new ExtraEventMessageBuilder();

    			builder.putMessage(ExtraEventMessage.USER_STOP_LIVE_TRADING_START_TIME, liveTradingSetting.getUserStopLiveTradingStartTime())
    			.putMessage(ExtraEventMessage.USER_STOP_LIVE_TRADING_END_TIME, liveTradingSetting.getUserStopLiveTradingEndTime());

    	    	return builder;
    	 }

    	return null;
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
            message = MessageLookup.buildEventMessage(e.getClientMessage(), e.getMessage());
            accountSetting = event.getAccountSetting();
        }

        ChangeAccountSettingReplyEvent reply = new ChangeAccountSettingReplyEvent(event.getKey(),
                event.getSender(), accountSetting, ok, message,getUserLiveTradingTime(accountSetting));
        try {
            eventManager.sendRemoteEvent(reply);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (ok) {
            PmChangeAccountSettingEvent pmEvent = new PmChangeAccountSettingEvent(PersistenceManager.ID,
                    null, accountSetting);
            eventManager.sendEvent(pmEvent);
        }
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
        if (event == timerEvent) {
            updateDynamicData();
        } else if (event == dayEndEvent) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            int nDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            if (nDayOfWeek != Calendar.SUNDAY && nDayOfWeek != Calendar.SATURDAY) {
				processDayEndTasks();
			}
            scheduleDayEndEvent();
        }
    }

    private void updateDynamicData() {
    	try {
	        if (!recoveryDone) {
				return;
			}

	        if (!allFxRatesReceived) {
	            for (String symbol : fxSymbols) {
	                Double rate = fxConverter.getFxRate(symbol);
	                if (null == rate || PriceUtils.isZero(rate)) {
	                    log.debug("Waiting on FX rate: " + symbol);
	                    return;
	                }
	            }
	            allFxRatesReceived = true;
	            log.info("FX rates ready: " + fxConverter.toString());
	        }

	        if (resetMarginHeld) {
	            resetMarginHeld = false;
	            positionKeeper.resetMarginHeld();
	        }

	        if (rmUpdateThrottler.check()) {
	            perfDataRm.start();
	            List<Account> accounts = accountKeeper.getRmJobs().getJobs();
	            for (Account account : accounts) {
	                positionKeeper.updateAccountDynamicData(account);

	                AccountSetting accountSetting = null;
	                try {
	                    accountSetting = accountKeeper.getAccountSetting(account.getId());
	                } catch (AccountException e) {
	                    log.error(e.getMessage(), e);
	                    continue;
	                }

	                totalPnLCalculator.calculate(account, accountSetting);
	                if (totalPnLCalculatorThrottler.check()) {
	                	log.info("Total PnL: " + totalPnLCalculator.getTotalPnL() + ", " +
	                			totalPnLCalculator.getTotalAccountValue() + ", " +
	                			totalPnLCalculator.getLiveTradingPnL() + ", " +
	                			totalPnLCalculator.getLiveTradingAccountValue());
	                }

	                if (systemSuspendValidator != null && systemSuspendValidator.isSuspended()) {
						continue;
					}
	                if (null != liveTradingCheckHandler && accountSetting.isUserLiveTrading()) {
	                	if (liveTradingCheckHandler.startCheckChain(account, accountSetting)) {
	                		continue;
	                	}
	                } else {
		                if (checkStopLoss(account, accountSetting)) {
	                		continue;
		                }
	                }

	                checkMarginCall(account, accountSetting);
	            }
	            perfDataRm.end();
	        }

	        if (dynamicUpdateThrottler.check()) {
	            perfDataUpdate.start();
	            List<Account> accounts = accountKeeper.getDynamicJobs().getJobs();
	            for (Account account : accounts) {
	                List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
	                for (OpenPosition position : positions) {
	                    positionListener.onOpenPositionDynamiceUpdate(position);
	                }
	                positionListener.onAccountDynamicUpdate(account);
	            }
	            perfDataUpdate.end();
	        }
    	} catch (Exception e) {
    		log.error(e.getMessage(), e);
    	}
    }

    private boolean quoteIsValid(Quote quote) {
        if (null != quoteChecker && !quoteChecker.check(quote)) {
			return false;
		}

        return !quote.isStale();
    }

	private boolean checkDailyStopLoss(Account account, AccountSetting accountSetting) {

    	double dailyStopLoss = accountSetting.getDailyStopLoss();
		if (PriceUtils.isZero(dailyStopLoss)) {
			return false;
		}

		if (null != coinManager && !coinManager.canCheckDailyStopLoss(account.getId())) {
			return false;
		}

		if (PriceUtils.EqualLessThan(account.getDailyPnL(), -dailyStopLoss)) {

			if (account.getState().equals(AccountState.ACTIVE)) {
				log.info("Account:"+account.getId()+" Daily loss: " + account.getDailyPnL() + " over " + -dailyStopLoss);
				account.setState(AccountState.FROZEN);
				try {
					eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
				} catch (Exception e) {
					log.error(e.getMessage(),e);
				}
			}
			TradingUtil.closeAllPositoinAndOrder(account, positionKeeper, eventManager, true,
					OrderReason.CompanyDailyStopLoss, riskOrderController);
			return true;
		}

		return false;
    }

    private boolean checkStopLoss(Account account, AccountSetting accountSetting) {
        if (!checkStoploss) {
			return false;
		}

        Double positionStopLoss = Default.getPositionStopLoss();

        if (checkDailyStopLoss(account,accountSetting)) {
        	return true;
        }

        if (null != coinManager && !coinManager.canCheckPositionStopLoss(account.getId())) {
			return false;
		}

        try{
            if (null != accountSetting) {
                positionStopLoss = accountSetting.getStopLossValue();
                Double companyStopLoss = accountSetting.getCompanySLValue();
                if (null == positionStopLoss || PriceUtils.isZero(positionStopLoss)) {
					positionStopLoss = Default.getPositionStopLoss();
				}

                if (null != companyStopLoss && !PriceUtils.isZero(companyStopLoss)) {
                    if (PriceUtils.isZero(positionStopLoss)) {
						positionStopLoss = companyStopLoss;
					} else {
						positionStopLoss = Math.min(positionStopLoss, companyStopLoss);
					}
                }
            }
        } catch (Exception e) {
            log.error("Check StopLoss fail at {}, {}", account.getUserId(), account.getId());
            return false;
        }

        if (PriceUtils.isZero(positionStopLoss)) {
			return false;
		}

        boolean result = false;
        List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
        for (OpenPosition position : positions) {
        	if (PriceUtils.Equal(position.getAvailableQty(), 0))
        		continue;
            Quote quote = marketData.get(position.getSymbol());
            if (PriceUtils.EqualLessThan(position.getAcPnL(), -positionStopLoss) &&
                    null != quote &&
                    quoteIsValid(quote)) {
                if (positionKeeper.checkAccountPositionLock(account.getId(), position.getSymbol())) {
                    log.debug("Position stop loss over threshold but account is locked: " +
                            account.getId() + ", " + position.getSymbol());
                    continue;
                }
                log.info("Position loss over threshold, cutting loss: " + position.getAccount() + ", " +
                        position.getSymbol() + ", " + position.getQty() + ", " + position.getAcPnL() + ", " +
                        positionStopLoss + ", " + quote);

        		if (!TradingUtil.checkRiskOrderCount(riskOrderController, account.getId())) {
					return true;
				}

                ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(),
                        null, position.getAccount(), position.getSymbol(), 0.0, OrderReason.PositionStopLoss,
                        IdGenerator.getInstance().getNextID(),true);

                eventManager.sendEvent(event);
                result = true;
            }
        }
        return result;
    }

    private boolean checkMarginCall(Account account, AccountSetting accountSetting) {
        if (!checkMargincut) {
			return false;
		}
        boolean result = false;
        List<OpenPosition> positions = positionKeeper.getOverallPosition(account);

        if (PriceUtils.EqualLessThan(account.getCashAvailable(), 0.0) && positions.size() > 0) {
            log.info("Margin call: " + account.getId() + ", " + account.getCash() + ", " + account.getUrPnL() + ", " + account.getCashAvailable());

            List<ParentOrder> orders = positionKeeper.getParentOrders(account.getId());

            if (orders.size() > 0) {
                for (ParentOrder order : orders) {
                    Quote quote = marketData.get(order.getSymbol());
                    if (!quoteIsValid(quote)) {
						continue;
					}

                    if (order.getOrdStatus().isCompleted()) {
						continue;
					}

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
                        if (PriceUtils.GreaterThan(p1.getAcPnL(), p2.getAcPnL())) {
							return 1;
						} else if (PriceUtils.LessThan(p1.getAcPnL(), p2.getAcPnL())) {
							return -1;
						}

                        return 0;
                    }

                });

                String sortedList = "";
                for (OpenPosition position : positions) {
                    sortedList += position.getAcPnL() + ",";
                }
                log.debug("Sorted list: " + sortedList);

                for (int i = 0; i < positions.size(); i++) {
                    OpenPosition position = positions.get(i);
                    if (PriceUtils.Equal(position.getAvailableQty(), 0))
                    	continue;
                    Quote quote = marketData.get(position.getSymbol());
                    if (!quoteIsValid(quote)) {
						continue;
					}

                    if (positionKeeper.checkAccountPositionLock(account.getId(), position.getSymbol())) {
                        log.debug("Margin call but account is locked: " +
                                account.getId() + ", " + position.getSymbol());
                        return true;
                    }

                    double marketablePrice = QuoteUtils.getMarketablePrice(quote, position.getQty());
                    double lossQty = FxUtils.calculateQtyFromValue(refDataManager, fxConverter, account.getCurrency(),
							quote.getSymbol(), Math.abs(account.getCashAvailable()), marketablePrice);

                    log.info("Margin cut close position: " + position.getAccount() + ", " +
                            position.getSymbol() + ", " + position.getAcPnL() + ", " +
                            position.getQty() + ", " +
                            lossQty + ", " +
                            account.getCashAvailable() + ", " + quote);

                	RefData refData = refDataManager.getRefData(quote.getSymbol());
    				double lev = leverageManager.getLeverage(refData, accountSetting);
    				lossQty *= lev;

    				double qty = Default.getMarginCut();
                    if (lossQty > Default.getMarginCut()) {
                    	long lot = Math.max(Math.max((long)Default.getMarginCut(), refData.getLotSize()), 1);
        				long n = ((long)lossQty)/lot;
                    	qty = Default.getMarginCut() * (n+1);
                    }
                    qty = Math.min(Math.abs(position.getAvailableQty()), qty);

            		if (!TradingUtil.checkRiskOrderCount(riskOrderController, account.getId())) {
						return true;
					}

                    ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(),
                            null, position.getAccount(), position.getSymbol(), qty, OrderReason.MarginCall,
                            IdGenerator.getInstance().getNextID(),true);

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
        if (accountSaver != null) {
            accountSaver.setAccounts(list);
            accountSaver.saveToFile();
        }

        for (Account account : list) {

        	boolean sendUpdate = false;
        	if (AccountState.FROZEN.equals(account.getState())) {
        		sendUpdate = true;
        	}

            Account copy = positionKeeper.rollAccount(account);
            eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, copy));

            if (sendUpdate) { // notify frozen accounts are reactivated
    			AccountUpdateEvent event = new AccountUpdateEvent(account.getId(), null, account);
    			try {
					eventManager.sendRemoteEvent(event);
				} catch (Exception e) {
					log.info(e.getMessage(),e);
				}
        	}
            account.resetDailyPnL();
        }

        eventManager.sendEvent(new PmEndOfDayRollEvent(null, null, tradeDate));
    }

    public void processTradeDateEvent(TradeDateEvent event) {
        tradeDate = event.getTradeDate();
    }

    public void processIndexSessionEvent(IndexSessionEvent event) {
    	Map<String, MarketSessionData> map = event.getDataMap();
    	List<MarketSessionData> list = new ArrayList<>(map.values());
    	if (list.size() > 0) {
    		MarketSessionData first = list.get(0);
    		tradeDate = first.getTradeDateByString();   // Take first marketsession's trade date as system's trade date, but not a great choice
    	} else {
    		log.warn("No data in IndexSessionEvent.");
    	}
    }

    private String generateAccountId() {
        return Default.getAccountPrefix() + IdGenerator.getInstance().getNextSimpleId();
    }

    public void injectUsers(List<User> users) {
        userKeeper.injectUsers(users);
        User defaultUser = userKeeper.tryCreateDefaultUser();
        if (null != defaultUser) {
			eventManager.sendEvent(new PmCreateUserEvent(PersistenceManager.ID, null, defaultUser,
                    new CreateUserEvent(PersistenceManager.ID, null, defaultUser, "TW", "TW", "")));
		}
    }

    public void injectGroups(List<GroupManagement> groups) {
    	userKeeper.injectGroup(groups);
    }

    public void injectAccounts(List<Account> accounts) {
        accountKeeper.injectAccounts(accounts);
        Account defaultAccount = accountKeeper.tryCreateDefaultAccount();
        if (null != defaultAccount) {
			eventManager.sendEvent(new PmCreateAccountEvent(PersistenceManager.ID, null, defaultAccount));
		}
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

        String symbol = event.getSymbol();
        log.info("Received SettlementEvent: " + symbol);

        Double settlePrice = settlePrices.get(symbol);
        Quote quote = null;

        if (null == settlePrice || PriceUtils.EqualLessThan(settlePrice, 0.0)) {

            quote = marketData.get(symbol);
            if (null == quote || !QuoteUtils.validQuote(quote)) {
                log.error("processSettlementDayEvent quote is null or invalid: " + symbol + ", " + quote);
                return;
            }
        }

        List<Account> accounts = accountKeeper.getAllAccounts();
        for (Account account : accounts) {
            OpenPosition position = positionKeeper.getOverallPosition(account, symbol);

            if (!PriceUtils.isZero(position.getQty())) {

                double price = quote != null ? QuoteUtils.getMarketablePrice(quote, position.getQty()) : settlePrice;

                Execution exec = new Execution(symbol, position.getQty() > 0 ? OrderSide.Sell : OrderSide.Buy,
                        Math.abs(position.getQty()),
                        price,
                        "", "",
                        "", "Settlement",
                        position.getUser(), position.getAccount(), "Settlement");
                exec.put(OrderField.ID.value(), IdGenerator.getInstance().getNextID() + "STLM");
                try {
					log.debug("Settling position: " + position + "  with " + price);
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

    public void setDailyExecTime(String dailyExecTime) {
        this.dailyExecTime = dailyExecTime;
    }

    public String getDailyExecTime() {
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

    public void setCheckStoploss(boolean checkStoploss) {
        this.checkStoploss = checkStoploss;
    }

    public void setCheckMargincut(boolean checkMargincut) {
        this.checkMargincut = checkMargincut;
    }
}
