package com.cyanspring.server.account;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.*;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderException;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.server.BusinessManager;
import com.cyanspring.server.livetrading.TradingUtil;
import com.cyanspring.server.order.RiskOrderController;

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
    private ScheduleManager scheduleManager;

    @Autowired
    private BusinessManager businessManager;

    @Autowired
    private RiskOrderController riskOrderController;

    private Map<String, Quote> marketData = new HashMap<>();
    private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private String dailyCloseTime;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
        @Override
        public void subscribeToEvents() {
            subscribeToEvent(QuoteEvent.class, null);
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

    public void processServerReadyEvent(ServerReadyEvent event) {
        if (dailyCloseTime != null) {
            setScheduleLumpSumEvent();
        } else {
            log.warn("dailyCloseTime is not set, No schedule event");
        }
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        processLumpSum();
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

    private void processClosePositionExecution(String symbol, OpenPosition oPosition, double qty) throws PositionException, OrderException {
    	Execution exec = createClosePositionExec(symbol,
        		qty, oPosition.getUser(), oPosition.getAccount(), "");
        if (exec == null) {
            log.error("Account:{}, Price:{} is not available, return without action.", oPosition.getAccount());
            return;
        } else
            log.info("processClosePositionExecution: " + exec.toString());
        positionKeeper.processExecution(exec, accountKeeper.getAccount(oPosition.getAccount()));
    }

    private void processLumpSum() {
        log.info("Start lumpSum process.");
        List<Account> allAccount = accountKeeper.getAllAccounts();
        for (Account account : allAccount)
            TradingUtil.cancelAllOrders(account, positionKeeper, eventManager, OrderReason.DayTradingMode, riskOrderController);

        List<String> routers = accountKeeper.getAllRouters();
        for (String router : routers) {
            try {
                Map<String, List<OpenPosition>> totalPositions = new HashMap<>(); // symbol/positions
                Map<String, OpenPosition> originPositions = new HashMap<>();
                List<Account> accounts = accountKeeper.getAccountsByRouter(router);
                for (Account account : accounts) {
                    AccountSetting setting = accountKeeper.getAccountSetting(account.getId());
                    if (!setting.isLiveTrading())
                        continue;
                    List<OpenPosition> accountPositions = positionKeeper.getOverallPosition(account);
                    for (OpenPosition accountPosition : accountPositions) {
                    	originPositions.put(accountPosition.getAccount() + accountPosition.getSymbol(), accountPosition.clone());
                        List<OpenPosition> pList = totalPositions.get(accountPosition.getSymbol());
                        if (pList == null) {
                            pList = new ArrayList<>();
                            totalPositions.put(accountPosition.getSymbol(), pList);
                            pList.add(accountPosition);
                            continue;
                        }

                        Double positionQty = accountPosition.getQty();
                        OpenPosition p = pList.get(0);

                        if (p == null || PriceUtils.Equal(Math.signum(positionQty), Math.signum(p.getQty()))) {
                            pList.add(accountPosition);
                        } else {
                            List<OpenPosition> removes = new ArrayList<>();
                            double remain;
                            boolean closed = false;
                            for (OpenPosition o : pList) {
                                double qty = o.getQty();
                                remain = Math.abs(qty) - Math.abs(positionQty);
                                if (remain > 0) {
                                    o.setQty(o.getQty() + positionQty);
                                    processClosePositionExecution(accountPosition.getSymbol(), accountPosition, accountPosition.getQty());
                                    closed = true;
                                    break;
                                } else if (remain < 0) {
                                    positionQty += qty;
                                    removes.add(o);
                                } else {
                                    processClosePositionExecution(accountPosition.getSymbol(), accountPosition, accountPosition.getQty());
                                    removes.add(o);
                                    closed = true;
                                    break;
                                }
                            }
                            
                            if(!closed){
//                            	double minus = position.getQty() - positionQty;
//                            	processClosePositionExecution(position.getSymbol(), position, minus);
                            	accountPosition.setQty(positionQty);
                            	pList.add(accountPosition);
                            }

                            for (OpenPosition remove : removes) {
                            	OpenPosition oPosition = originPositions.get(remove.getAccount() + remove.getSymbol());
                                processClosePositionExecution(oPosition.getSymbol(), oPosition, oPosition.getQty());
                                pList.remove(remove);
                            }
                        }
                    }
                }

                for (List<OpenPosition> positions : totalPositions.values()) {
                    for (OpenPosition position : positions) {
                    	OpenPosition oPosition = originPositions.get(position.getAccount() + position.getSymbol());
                    	if(!PriceUtils.Equal(position.getQty(), oPosition.getQty()))
                    		processClosePositionExecution(oPosition.getSymbol(), oPosition, oPosition.getQty() - position.getQty());
                    	
                    	log.info("process close position, account: " 
                    			+ position.getAccount() + ", symbol: " + position.getSymbol() + ", Qty: " + position.getQty());
                    	
                        businessManager.processClosePosition(null, null, null, OrderReason.DayTradingMode,
                                accountKeeper.getAccount(position.getAccount()), position.getSymbol(),
                                position.getQty() > 0 ? OrderSide.Sell : OrderSide.Buy, Math.abs(position.getQty()));
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        log.info("End lumpSum process");
    }

    private Execution createClosePositionExec(String symbol, double qty,
                                              String user, String account, String route) throws OrderException {
        Quote quote = marketData.get(symbol);
        double price = QuoteUtils.getMarketablePrice(quote, qty);
        if (PriceUtils.isZero(price))
            price = quote.getLast();
        if (!PriceUtils.validPrice(price))
            return null;

        Execution exec = new Execution(symbol, qty > 0 ? OrderSide.Sell : OrderSide.Buy,
                Math.abs(qty),
                price,
                "", "",
                "", IdGenerator.getInstance().getNextID() + "LumpSum",
                user, account, route);
        exec.put(OrderField.ID.value(), IdGenerator.getInstance().getNextID() + "LumpSum");
        return exec;
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
        if (TimeUtil.getTimePass(Clock.getInstance().now(), date) > 0)
            date = TimeUtil.getNextDay(date);

        scheduleManager.scheduleTimerEvent(date, eventProcessor, timerEvent);
    }

    public void setDailyCloseTime(String dailyCloseTime) {
        this.dailyCloseTime = dailyCloseTime;
    }

}
