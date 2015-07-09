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
    private List<String> sentOrder = new ArrayList<String>();
    private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private String dailyCloseTime;
    private String user = "fdt-lumpsum";
    private String account;
    private String suffix = "-FX";

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
        @Override
        public void subscribeToEvents() {
            subscribeToEvent(QuoteEvent.class, null);
            subscribeToEvent(UpdateParentOrderEvent.class, null);
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
        if (order.getOrdStatus().equals(OrdStatus.FILLED)) {
            String symbol = order.getSymbol();
            List<OpenPosition> positions = getAccountsPositionBySymbol(symbol);
            for (OpenPosition position : positions) {
                Execution exec = createClosePositionExec(symbol, position.getPrice(), position.getQty(),
                        position.getUser(), position.getAccount(), "");
                try {
                    if (exec == null)
                        continue;
                    positionKeeper.processExecution(exec, accountKeeper.getAccount(position.getAccount()));
                } catch (PositionException e) {
                    log.error("Cannot process execution account: {}, symbol: {}", position.getAccount(),
                            position.getSymbol());
                }
            }
        }
        sentOrder.remove(order.getId());
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        Map<String, Double> totalPositions = calculateTotalSymbolPosition();
        for (Map.Entry<String, Double> entry : totalPositions.entrySet()) {
            EnterParentOrderEvent epoEvent = createLumpSumOrderEvent(entry.getKey(), entry.getValue());
//            Map<String, Object> field = epoEvent.getFields();
            sentOrder.add(epoEvent.getTxId());
            eventManager.sendEvent(epoEvent);
        }

        setScheduleLumpSumEvent();
    }

    @Override
    public void init() throws Exception {
        log.info("initializing");
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("LumpSumManager");

        if (dailyCloseTime != null) {
            setScheduleLumpSumEvent();
        } else {
            log.warn("dailyCloseTime is not set, No schedule event");
        }
        
        this.account = this.user + this.suffix;
        createLumpSumAccount();
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

    private EnterParentOrderEvent createLumpSumOrderEvent(String symbol, double qty) {
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(OrderField.SYMBOL.value(), symbol);
        fields.put(OrderField.SIDE.value(), PriceUtils.GreaterThan(qty, 0) ? OrderSide.Sell : OrderSide.Buy);
        fields.put(OrderField.TYPE.value(), OrderType.Market);
        fields.put(OrderField.QUANTITY.value(), qty);
        fields.put(OrderField.STRATEGY.value(), "SDMA");
        fields.put(OrderField.USER.value(), user);
        fields.put(OrderField.ACCOUNT.value(), account);

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
    
    
}
