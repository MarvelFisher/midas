package com.cyanspring.server.account;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.*;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.account.CreateUserEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.strategy.IStrategyFactory;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;

/**
 * The Manager is used to summarize all account's positions in one
 * lump sum account and close it at specific time in live trading
 * mode and send update position to every account. The Manager can
 * only use in live trading mode.
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class LumpSumManager implements IPlugin {
    private static final Logger log = LoggerFactory.getLogger(LumpSumManager.class);

    @Autowired
    private IRemoteEventManager eventManager;

    @Autowired
    private AccountKeeper accountKeeper;

    @Autowired
    private PositionKeeper positionKeeper;

    @Autowired
    private IStrategyFactory strategyFactory;

    @Autowired
    private ScheduleManager scheduleManager;

    private Map<String, Quote> marketData = new HashMap<String, Quote>();
    private Map<String, List<OpenPosition>> symbolPositionMap = new HashMap<String, List<OpenPosition>>();  // symbol/OpenPosition
    private Map<String, Double> symbolQtyMap = new HashMap<>(); // symbol/Qty
    private List<String> sentOrder = new ArrayList<String>();
    private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private String dailyCloseTime;
    private String user = "fdt-lumpsum";
    private String account;
    private String suffix = "-FX";
    private String router;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
        @Override
        public void subscribeToEvents() {
            subscribeToEvent(QuoteEvent.class, null);
            subscribeToEvent(UpdateParentOrderEvent.class, null);
            subscribeToEvent(ServerReadyEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    public void processQuoteEvent(QuoteEvent event) {
        Quote quote = event.getQuote();
        marketData.put(quote.getSymbol(), quote);
    }

    public void processUpdateParentOrderEvent(UpdateParentOrderEvent event) {
        ParentOrder order = event.getParent();
        if (!sentOrder.contains(event.getTxId()))
            return;
        if (order.getOrdStatus().equals(OrdStatus.FILLED) || order.getOrdStatus().equals(OrdStatus.PARTIALLY_FILLED)) {
            String symbol = order.getSymbol();
            double newQty = order.getQuantity();
            Double remainQty = symbolQtyMap.get(symbol);
            if(remainQty == null)
            	remainQty = 0.0;
            double totalQty = newQty + remainQty;
            
            List<OpenPosition> positions = getAccountsPositionBySymbol(symbol);
            if(PriceUtils.LessThan(totalQty, 0))
            	Collections.reverse(positions);
            for (OpenPosition position : positions) {
            	if(PriceUtils.Equal(Math.signum(totalQty), Math.signum(position.getQty()))){
            		if(Math.abs(totalQty) - Math.abs(position.getQty()) < 0){
                		symbolQtyMap.put(symbol, totalQty);
                		break;
                	}
            		totalQty -= position.getQty();
            	}
            	
                Execution exec = createClosePositionExec(symbol, order.getPrice(), position.getQty(),
                        position.getUser(), position.getAccount(), "");
                try {
                    if (exec == null)
                        continue;
                    positionKeeper.processExecution(exec, accountKeeper.getAccount(position.getAccount()));
                    removeAccountPositionBySymbol(symbol, position.getAccount());
                
                } catch (PositionException e) {
                    log.error("Cannot process execution account: {}, symbol: {}", position.getAccount(),
                            position.getSymbol());
                }
            }
            if(getAccountsPositionBySymbol(symbol) == null)
                sentOrder.remove(order.getId());
        }
    }
    
    public void processServerReadyEvent(ServerReadyEvent event){
    	if (dailyCloseTime != null) {
            setScheduleLumpSumEvent();
        } else {
            log.warn("dailyCloseTime is not set, No schedule event");
        }
    	
    	this.account = this.user + this.suffix;
    	Account account = accountKeeper.getAccount(this.account);
    	if(account == null){
    		createLumpSumAccount();
    		return;
    	}
    	
		try {
			AccountSetting setting = accountKeeper.getAccountSetting(this.account);
			if(setting == null)
	    		setting = AccountSetting.createEmptySettings(this.account);
			this.router = setting.getRoute();
		} catch (AccountException e) {
			log.error(e.getMessage(), e);
		}
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        Map<String, Double> totalPositions = calculateTotalSymbolPosition();
        for (Map.Entry<String, Double> entry : totalPositions.entrySet()) {
        	String symbol = entry.getKey();
        	double qty = entry.getValue();
        	if(!PriceUtils.isZero(qty)){
        		EnterParentOrderEvent epoEvent = createLumpSumOrderEvent(symbol, qty);
        		sentOrder.add(epoEvent.getTxId());
        		eventManager.sendEvent(epoEvent);
        	} else {
        		List<OpenPosition> oPositions = getAccountsPositionBySymbol(symbol);
        		Quote quote = marketData.get(symbol);
        		for(OpenPosition oPosition : oPositions){
        			double price = QuoteUtils.getMarketablePrice(quote, oPosition.getQty());
        			Execution exec = createClosePositionExec(symbol, price, 
        					oPosition.getQty(), oPosition.getUser(), oPosition.getAccount(), "");
        			if(exec == null)
        				continue;
        			try{
        				positionKeeper.processExecution(exec, accountKeeper.getAccount(oPosition.getAccount()));
        				removeAccountPositionBySymbol(symbol, oPosition.getAccount());
        			} catch(PositionException e){
        				log.error("Cannot process execution account: {}, symbol: {}", oPosition.getAccount(),
                                oPosition.getSymbol());
        			}
        		}
        	}
        }
        sortPostionsWithQty();
        setScheduleLumpSumEvent();
    }

    @Override
    public void init() throws Exception {
        log.info("initializing");
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("LumpSumManager");
    }

    @Override
    public void uninit() {
        if (!eventProcessor.isSync())
            scheduleManager.uninit();
        eventProcessor.uninit();
    }
    
    private List<OpenPosition> getAccountsPositionBySymbol(String symbol) {
        return symbolPositionMap.get(symbol);
    }

    private void removeAccountPositionBySymbol(String symbol, String account){
    	List<OpenPosition> oPositions = symbolPositionMap.get(symbol);
    	for(int i=0; i < oPositions.size(); i++){
    		OpenPosition oPosition = oPositions.get(i);
    		if(oPosition.getAccount().equals(account))
    			oPositions.remove(i);
    	}
    	if(oPositions.size() == 0)
    		symbolPositionMap.remove(symbol);
    }
    
    private Map<String, Double> calculateTotalSymbolPosition() {
        Map<String, Double> totalPositions = new HashMap<String, Double>();
        List<Account> list = accountKeeper.getAllAccounts();
        for (Account account : list) {
        	if(account.getId().equals(this.account))
        		continue;
            List<OpenPosition> oPositions = positionKeeper.getOverallPosition(account);
            for (OpenPosition position : oPositions) {
                Double num = totalPositions.get(position.getSymbol());
                if (num == null)
                	num = 0.0;
                num += position.getQty();
                totalPositions.put(position.getSymbol(), num);

                List<OpenPosition> positions = symbolPositionMap.get(position.getSymbol());
                if (positions == null) {
                    positions = new ArrayList<OpenPosition>();
                    positions.add(position);
                    symbolPositionMap.put(position.getSymbol(), positions);
                    continue;
                }
                if (!positions.contains(position))
                    positions.add(position);
            }
        }

        return totalPositions;
    }
    
    private void sortPostionsWithQty(){
    	for(Entry<String, List<OpenPosition>> entry : symbolPositionMap.entrySet()){
    		Collections.sort(entry.getValue(), new Comparator<OpenPosition>() {
    			@Override
    			public int compare(OpenPosition o1, OpenPosition o2) {
    				if(PriceUtils.GreaterThan(o1.getQty(), o2.getQty()))
    					return 1;
    				return 0;
    			}
    		});
    	}
    }

    private EnterParentOrderEvent createLumpSumOrderEvent(String symbol, double qty) {
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(OrderField.SYMBOL.value(), symbol);
        fields.put(OrderField.SIDE.value(), PriceUtils.GreaterThan(qty, 0) ? OrderSide.Sell : OrderSide.Buy);
        fields.put(OrderField.TYPE.value(), OrderType.Market);
        fields.put(OrderField.QUANTITY.value(), qty);
        fields.put(OrderField.STRATEGY.value(), "SDMA");
        fields.put(OrderField.USER.value(), user);
        fields.put(OrderField.ACCOUNT.value(), account);
        fields.put(OrderField.ROUTE.value(), router);

        return new EnterParentOrderEvent(null, null, fields, IdGenerator.getInstance().getNextID() + "LumpSum", false);
    }

    private Execution createClosePositionExec(String symbol, double price, double qty,
                                              String user, String account, String route) {
//        Quote quote = marketData.get(symbol);
//        double price = QuoteUtils.getMarketablePrice(quote, qty);
        if (!PriceUtils.validPrice(price)) {
            log.warn("Account:{}, Price:{} is not available, return without action.", account, price);
            return null;
        }
        Execution exec = new Execution(symbol, qty > 0 ? OrderSide.Sell : OrderSide.Buy,
                Math.abs(qty),
                price,
                "", "",
                "", "LumpSum",
                user, account, route);
        exec.put(OrderField.ID.value(), IdGenerator.getInstance().getNextID() + "LumpSum");
        return exec;
    }

    private void createLumpSumAccount() {
        User user = new User("FDT-lumpSum", "xxx");
        user.setName(this.user);
        user.setEmail("fdt@fdt.com.tw");
        user.setPhone("0987654321");
        user.setUserType(UserType.ADMIN);
        CreateUserEvent event = new CreateUserEvent(null, null, user, "", "",
                IdGenerator.getInstance().getNextID() + "LumpSum");
        eventManager.sendEvent(event);
    }

    private void setScheduleLumpSumEvent() {
        String[] times = dailyCloseTime.split(":");
        if (times.length != 3) {
            log.error("Wrong setting of dailyCloseTime");
            return;
        }

        int hour = Integer.parseInt(times[0]);
        int min = Integer.parseInt(times[1]);
        int sec = Integer.parseInt(times[2]);

        Date date = TimeUtil.getScheduledDate(Calendar.getInstance(), Clock.getInstance().now(), hour, min, sec);
        if(TimeUtil.getTimePass(Clock.getInstance().now(), date) > 0)
        	date = TimeUtil.getNextDay(date);
        
        scheduleManager.scheduleTimerEvent(date, eventProcessor, timerEvent);
    }

    public void setDailyCloseTime(String dailyCloseTime) {
        this.dailyCloseTime = dailyCloseTime;
    }

	public void setUser(String user) {
		this.user = user;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
    
	public void setRouter(String router) {
		this.router = router;
	}
    
}
