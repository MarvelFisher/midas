package com.cyanspring.kdb;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.kdb.MarketIntelligenceUpdateEvent;
import com.cyanspring.common.kdb.MarketIntelligence;
import com.cyanspring.common.kdb.MarketIntelligenceIndex;
import com.exxeleron.qjava.QCallbackConnection;
import com.exxeleron.qjava.QErrorMessage;
import com.exxeleron.qjava.QMessage;
import com.exxeleron.qjava.QMessagesListener;
import com.exxeleron.qjava.QTable;
import com.exxeleron.qjava.QTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MarketIntelligenceReceiver implements IPlugin {

    private static final Logger log = LoggerFactory.getLogger(MarketIntelligenceReceiver.class);

    public static String ID = MarketIntelligenceReceiver.class.toString();

    private String kdbHost;

    private int kdbPort;

    private QCallbackConnection kdbConnection;

    @Autowired
    @Qualifier("eventManager")
    private IRemoteEventManager eventManager;

    @Override
    public void init() throws Exception {
        startKdbListener();
    }

    @Override
    public void uninit() {
        stopKdbListener();
    }

    private void startKdbListener() {

        kdbConnection = new QCallbackConnection(getKdbHost(), getKdbPort(), "", "");

        final QMessagesListener listener = new QMessagesListener() {

            public void messageReceived(final QMessage message) {

                final Object data = message.getData();

                if (data instanceof Object[]) {
                    // unpack upd message
                    final Object[] params = ((Object[]) data);

                    if (params.length == 3 && params[0].equals("upd") && params[2] instanceof QTable) {

                        // e.x. idx15, idx60
                        String tableName = (String) params[1];
                        int ti = Integer.parseInt(tableName.substring(3));

                        final QTable table = (QTable) params[2];
                        int timeIndex = table.getColumnIndex("time");
                        int symbolIndex = table.getColumnIndex("sym");
                        int rviIndex = table.getColumnIndex("rvi");
                        int minrviIndex = table.getColumnIndex("minrvi");
                        int maxrviIndex = table.getColumnIndex("maxrvi");
                        int cviIndex = table.getColumnIndex("cvi");
                        int mincviIndex = table.getColumnIndex("mincvi");
                        int maxcviIndex = table.getColumnIndex("maxcvi");
                        int miIndex = table.getColumnIndex("mi");
                        int minmiIndex = table.getColumnIndex("minmi");
                        int maxmiIndex = table.getColumnIndex("maxmi");
                        int umibIndex = table.getColumnIndex("umib");
                        int maxumibIndex = table.getColumnIndex("maxumib");
                        int umisIndex = table.getColumnIndex("umis");
                        int maxumisIndex = table.getColumnIndex("maxumis");

                        List<MarketIntelligence> result = new ArrayList<>();

                        for (final QTable.Row row : table) {

                            QTimestamp time = (QTimestamp) row.get(timeIndex);
                            String symbol = (String) row.get(symbolIndex);
                            Double rvi = (Double) row.get(rviIndex);
                            Double minrvi = (Double) row.get(minrviIndex);
                            Double maxrvi = (Double) row.get(maxrviIndex);
                            Double cvi = (Double) row.get(cviIndex);
                            Double mincvi = (Double) row.get(mincviIndex);
                            Double maxcvi = (Double) row.get(maxcviIndex);
                            Double mi = (Double) row.get(miIndex);
                            Double minmi = (Double) row.get(minmiIndex);
                            Double maxmi = (Double) row.get(maxmiIndex);
                            Double umib = (Double) row.get(umibIndex);
                            Double maxumib = (Double) row.get(maxumibIndex);
                            Double umis = (Double) row.get(umisIndex);
                            Double maxumis = (Double) row.get(maxumisIndex);

                            MarketIntelligenceIndex relativeVolatility =
                                    new MarketIntelligenceIndex(rvi, maxrvi.isNaN() ? 0 : maxrvi, minrvi.isNaN() ? 0 : minrvi);
                            MarketIntelligenceIndex currentVolatility =
                                    new MarketIntelligenceIndex(cvi, maxcvi.isNaN() ? 0 : maxcvi, mincvi.isNaN() ? 0 : mincvi);
                            MarketIntelligenceIndex momentum =
                                    new MarketIntelligenceIndex(mi, maxmi.isNaN() ? 0 : maxmi, minmi.isNaN() ? 0 : minmi);
                            MarketIntelligenceIndex ultimateMomentumBuy =
                                    new MarketIntelligenceIndex(umib, maxumib.isNaN() ? 0 : maxumib, 0);
                            MarketIntelligenceIndex ultimateMomentumSell =
                                    new MarketIntelligenceIndex(umis, maxumis.isNaN() ? 0 : maxumis, 0);

                            MarketIntelligence index = new MarketIntelligence(ti, symbol, time.toDateTime(), relativeVolatility, currentVolatility, momentum, ultimateMomentumBuy, ultimateMomentumSell);

                            result.add(index);
                        }

                        try {
                            eventManager.sendRemoteEvent(new MarketIntelligenceUpdateEvent(ID, null, result));

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

        kdbConnection.addMessagesListener(listener);
        try {
            kdbConnection.open();
            kdbConnection.sync(".u.sub", "", "");
            kdbConnection.startListener();

        } catch (final Exception e) {
            log.error("KDB subscription error", e);
        }
    }

    private void stopKdbListener() {

        kdbConnection.stopListener();

        try {
            kdbConnection.close();
        } catch (IOException e) {
            log.warn("KDB connection close error", e);
        }
    }

    public String getKdbHost() {
        return kdbHost;
    }

    public void setKdbHost(String kdbHost) {
        this.kdbHost = kdbHost;
    }

    public int getKdbPort() {
        return kdbPort;
    }

    public void setKdbPort(int kdbPort) {
        this.kdbPort = kdbPort;
    }

    public static void main(String[] args) throws Exception {
        MarketIntelligenceReceiver r = new MarketIntelligenceReceiver();
        r.setKdbHost("localhost");
        r.setKdbPort(5013);

        r.init();
    }
}
