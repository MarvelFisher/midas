package com.cyanspring.server.account;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.*;
import com.cyanspring.common.business.Execution;
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
import org.springframework.util.StringUtils;

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
        log.info("Start lumpSum process.");
        Map<String, Double> totalPositions = calculateTotalSymbolPosition();
        for (Map.Entry<String, Double> entry : totalPositions.entrySet()) {
            String symbol = entry.getKey();
            double qty = entry.getValue();
            if (!PriceUtils.isZero(qty)) {
                try {
                    log.info("Send close position event, symbol: {}, qty: {}", symbol, qty);
                    businessManager.processClosePosition(null, null, null, OrderReason.DayTradingMode,
                            accountKeeper.getAccount(Default.getAccount()), symbol,
                            qty > 0 ? OrderSide.Sell : OrderSide.Buy, Math.abs(qty));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("End lumpSum process");
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

    private void processClosePositionExecution(String symbol, OpenPosition oPosition) throws PositionException {
        Execution exec = createClosePositionExec(symbol,
                oPosition.getQty(), oPosition.getUser(), oPosition.getAccount(), "");
        if (exec == null) {
            log.warn("Account:{}, Price:{} is not available, return without action.", oPosition.getAccount());
            return;
        } else
            log.info(exec.toString());
        positionKeeper.processExecution(exec, accountKeeper.getAccount(oPosition.getAccount()));
    }

    private Map<String, Double> calculateTotalSymbolPosition() {
        Map<String, Double> totalPositions = new HashMap<>();
        List<Account> list = accountKeeper.getAllAccounts();

        for (Account account : list) {
            try {
                if (account.getId().equals(Default.getAccount())) {
                    List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
                    for (OpenPosition position : positions) {
                        processClosePositionExecution(position.getSymbol(), position);
                    }
                    continue;
                }
                AccountSetting setting = accountKeeper.getAccountSetting(account.getId());
                if (setting == null)
                    continue;
                if (!StringUtils.hasText(setting.getRoute()))
                    continue;
                if (!setting.isLiveTrading())
                    continue;
                TradingUtil.cancelAllOrders(account, positionKeeper, eventManager, OrderReason.DayTradingMode, riskOrderController);
                List<OpenPosition> oPositions = positionKeeper.getOverallPosition(account);
                for (OpenPosition position : oPositions) {
                    Double num = totalPositions.get(position.getSymbol());
                    if (num == null)
                        num = 0.0;
                    num += position.getQty();
                    totalPositions.put(position.getSymbol(), num);
                    processClosePositionExecution(position.getSymbol(), position);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return totalPositions;
    }

    private Execution createClosePositionExec(String symbol, double qty,
                                              String user, String account, String route) {
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
