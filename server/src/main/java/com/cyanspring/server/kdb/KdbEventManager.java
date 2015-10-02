package com.cyanspring.server.kdb;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.kdb.VolatilityListReplyEvent;
import com.cyanspring.common.event.kdb.VolatilityListRequestEvent;
import com.cyanspring.common.event.kdb.VolatilityUpdateEvent;
import com.cyanspring.common.kdb.Volatility;
import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QCallbackConnection;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QErrorMessage;
import com.exxeleron.qjava.QMessage;
import com.exxeleron.qjava.QMessagesListener;
import com.exxeleron.qjava.QMinute;
import com.exxeleron.qjava.QTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KdbEventManager implements IPlugin {

    private static final Logger log = LoggerFactory.getLogger(KdbEventManager.class);

    public static String ID = KdbEventManager.class.toString();

    final QCallbackConnection volatilityKdbForSub = new QCallbackConnection("localhost", 5012, "", "");
    final QConnection volatilityKdbForQuery = new QBasicConnection("localhost", 5012, "", "");

    @Autowired
    private IRemoteEventManager eventManager;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(VolatilityListRequestEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    @Override
    public void init() throws Exception {
        log.info("Initialising...");

        eventProcessor.setHandler(this);
        eventProcessor.init();

        if (eventProcessor.getThread() != null) {
            eventProcessor.getThread().setName("KdbEventManager");
        }

        startVolatilityListener();
    }

    @Override
    public void uninit() {
        eventProcessor.uninit();
        stopVolatilityListener();
    }

    private void startVolatilityListener() {

        final QMessagesListener listener = new QMessagesListener() {

            public void messageReceived(final QMessage message) {

                final Object data = message.getData();

                if (data instanceof Object[]) {
                    // unpack upd message
                    final Object[] params = ((Object[]) data);

                    if (params.length == 3 && params[0].equals("upd") && params[2] instanceof QTable) {

                        final QTable table = (QTable) params[2];
                        int timeIndex = table.getColumnIndex("time");
                        int symbolIndex = table.getColumnIndex("symbol");
                        int scaleIndex = table.getColumnIndex("scale");

                        List<Volatility> result = new ArrayList<>();

                        for (final QTable.Row row : table) {

                            QMinute time = (QMinute) row.get(timeIndex);
                            String symbol = (String) row.get(symbolIndex);
                            Double scale = (Double) row.get(scaleIndex);

                            result.add(new Volatility(symbol, new java.sql.Time(time.toDateTime().getTime()), scale));
                        }

                        try {
                            eventManager.sendRemoteEvent(new VolatilityUpdateEvent(ID, null, result));

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }

            public void errorReceived(final QErrorMessage message) {
                log.warn("KDB listener error", message.getCause());
            }
        };

        volatilityKdbForSub.addMessagesListener(listener);
        try {
            volatilityKdbForSub.open();
            volatilityKdbForSub.sync(".u.sub", "volaty", "");
            volatilityKdbForSub.startListener();

        } catch (final Exception e) {
            log.error("KDB subscription error", e);
        }
    }

    private void stopVolatilityListener() {

        volatilityKdbForSub.stopListener();

        try {
            volatilityKdbForSub.close();
        } catch (IOException e) {
            log.warn("KDB connection close error", e);
        }
    }

    public void processVolatilityListRequestEvent(VolatilityListRequestEvent event) {

        log.debug("Received VolatilityListRequestEvent: " + event);

        try {
            volatilityKdbForQuery.open();

            List<Volatility> result = new ArrayList<>();

            QTable qTable = (QTable) volatilityKdbForQuery.sync("getdaylist[]");
            int timeIndex = qTable.getColumnIndex("time");
            int symbolIndex = qTable.getColumnIndex("symbol");
            int scaleIndex = qTable.getColumnIndex("scale");

            for (QTable.Row row : qTable) {

                QMinute time = (QMinute) row.get(timeIndex);
                String symbol = (String) row.get(symbolIndex);
                Double scale = (Double) row.get(scaleIndex);

                result.add(new Volatility(symbol, new java.sql.Time(time.toDateTime().getTime()), scale));
            }

            eventManager.sendRemoteEvent(new VolatilityListReplyEvent(event.getKey(), event.getSender(), result));

        } catch (Exception e) {

            log.error(e.getMessage(), e);

        } finally {
            try {
                volatilityKdbForQuery.close();
            } catch (IOException e) {
                log.warn("KDB connection close error", e);
            }
        }
    }
}
