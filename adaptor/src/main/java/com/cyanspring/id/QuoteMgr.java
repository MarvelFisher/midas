package com.cyanspring.id;

import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.FileMgr;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class QuoteMgr implements AutoCloseable, TimerEventHandler {

    private static final Logger log = LoggerFactory
            .getLogger(IdMarketDataAdaptor.class);

    static QuoteMgr instance = new QuoteMgr();
    Object m_lock = new Object();

    public static QuoteMgr instance() {
        return instance;
    }

    public ConcurrentHashMap<String, SymbolItem> symbolMap = new ConcurrentHashMap<>();

    TimerThread timerThread = new TimerThread();

    public QuoteMgr() {

    }

    public void init() {
        initTimer();
    }

    @Override
    public void onTimer(TimerThread objSender) {
        int nSize = Parser.instance().getQueueSize();
        if (nSize > 0) {
            Util.addLog("Queue size : %d", nSize);
        }
    }

    public void initTimer() {
        timerThread.setName("QuoteMgr.Timer");
        timerThread.TimerEvent = this;
        timerThread.setInterval(1000);
        timerThread.start();
    }

    void uninit() {

        if (timerThread != null) {
            try {
                timerThread.stop(3000);
                timerThread.close();
            } catch (Exception e) {
                LogUtil.logException(log, e);
            }
            timerThread = null;
        }

        ArrayList<SymbolItem> list = new ArrayList<SymbolItem>(
                symbolMap.values());
        synchronized (m_lock) {

            for (SymbolItem item : list) {
                item.close();
            }
            list.clear();
            symbolMap.clear();
        }
    }

    /**
     * @return
     */
    public ArrayList<String> SymbolList() {
        return new ArrayList<String>(symbolMap.keySet());
    }

    @Override
    public void close() {
        uninit();
        FinalizeHelper.suppressFinalize(this);
    }

    public void addSymbol(String symbol) {
        if (checkSymbol(symbol) == false) {
            SymbolItem item = new SymbolItem(symbol);
            synchronized (m_lock) {
                symbolMap.put(symbol, item);
            }
        }
    }

    /**
     * @param key
     * @return
     */
    public SymbolItem getItem(String key) {
        synchronized (m_lock) {
            return symbolMap.get(key);
        }

    }

    /**
     * @param strSymbol
     * @return
     */
    public boolean checkSymbol(String strSymbol) {

        return symbolMap.containsKey(strSymbol);
    }

    /**
     * @param list
     */
    public void initSymbols(List<String> list) {
        for (String symbol : list) {
            SymbolItem item = new SymbolItem(symbol);
            symbolMap.put(symbol, item);
        }
    }

    public void writeFile(boolean closeTime, boolean isAsync) {
        String strPath = IdMarketDataAdaptor.instance.getDataPath("id-forex");
        LogUtil.logInfo(log, "Write File %s", strPath);

        try {
            ArrayList<String> listData = new ArrayList<String>();
            synchronized (m_lock) {

                Set<String> set = symbolMap.keySet();
                Iterator<String> itr = set.iterator();
                while (itr.hasNext()) {
                    String strID = itr.next();

                    if (strID.isEmpty())
                        continue;

                    SymbolItem item = symbolMap.get(strID);
                    if (closeTime) {
                        item.setClose();
                    }
                    String strData = item.toString();
                    listData.add(strData);
                }
            }

            if (isAsync) { // async method
                FileMgr.instance().writeDataToFile(strPath, listData, true);
            } else { // sync method
                FileMgr.writeFile(strPath, listData, true);
            }

        } catch (Exception ex) {
            LogUtil.logException(log, ex);
            LogUtil.logError(log, "Fail to write file %s", strPath);
        }
    }
}
