package com.cyanspring.id;

import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.*;
import com.cyanspring.id.Library.Util.Network.SocketUtil;
import com.cyanspring.id.Library.Util.Network.SpecialCharDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Parser implements IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(IdMarketDataAdaptor.class);

    static final int MAX_COUNT = 1024 * 1024;
    ;
    static Parser instance = new Parser();

    public static Parser instance() {
        return instance;
    }

    RingBuffer buffer = new RingBuffer();
    RingBuffer source = new RingBuffer();
    protected RequestThread reqThread = new RequestThread(this, "Parser");

    static Date tLast = new Date(0);

    /**
     * constructor
     */
    public Parser() {
        buffer.init(MAX_COUNT, true);
        source.init(MAX_COUNT, true);
        reqThread.start();
    }

    /**
     * clear Ringbuffer to empty status
     */
    public void clearRingbuffer() {
        source.purge(-1);
    }

    /**
     * processData - add data to queue if data packed, unpack first
     *
     * @param time receive time
     * @param data received data
     */
    public void processData(Date time, byte[] data) {
        if (IdMarketDataAdaptor.instance.gateway) {
            source.write(data, data.length);
            ArrayList<byte[]> list = SocketUtil.unPackData(source);
            for (byte[] ba : list) {
                addData(time, ba);
            }
        } else {
            addData(time, data);
        }
        data = null;
    }

    /**
     * parse :
     *
     * @param srcData
     */
    public void parse(Date time, byte[] srcData) {

        buffer.write(srcData, srcData.length);
        srcData = null;
        try {
            while (true) {

                byte[] data = new byte[6];

                if (buffer.getQueuedSize() <= 0
                        || buffer.read(data, 6, false) != 6) {
                    break;
                }

                if (data[0] != SpecialCharDef.EOT) {
                    LogUtil.logTrace(log,
                            "Parser.Parse szTempBuf[0] != EOT [0x%02x]",
                            data[0]);
                    buffer.purge(1); // Skip One Byte
                    continue;
                }
                if (data[1] != SpecialCharDef.SPC) {
                    LogUtil.logTrace(log, "Parser.Parse szTempBuf[1] != SPC");
                    LogUtil.logTrace(log, "Parser.Parse pop [0x%02x]",
                            data[0]);
                    buffer.purge(1); // Skip One Byte
                    continue;
                }

                int iDataLength = 0;
                try {
                    iDataLength = (int) BitConverter.toLong(data, 2,
                            data.length);
                    if (iDataLength < 0 || iDataLength >= MAX_COUNT) {
                        LogUtil.logError(log, "DataLength : " + iDataLength);
                        buffer.purge(1); // Skip One Byte
                        continue;
                    }
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    buffer.purge(1); // Skip One Byte
                    continue;
                }

                int iPacketDataLength = iDataLength + 7;
                int dwQueueLength = buffer.getQueuedSize();
                if (iPacketDataLength >= buffer.getBufSize()) {
                    LogUtil.logError(log,
                            "Parser.Parse iPacketDataLength[%d] >= sizeof(szTempBuf)[%d]",
                            iPacketDataLength, data.length);
                    LogUtil.logError(log, "Parser.Parse pop [0x%02x]",
                            data[0]);
                    buffer.purge(1); // Skip One Byte
                    continue;
                }

                if (dwQueueLength >= iPacketDataLength) {
                    data = new byte[iPacketDataLength];
                    int nSize = buffer.read(data, iPacketDataLength, false);
                    if (nSize != iPacketDataLength) {
                        LogUtil.logError(log,
                                "Parser.Parse m_RecvQueue.PeekData Fail! iPacketDataLength[%d]",
                                iPacketDataLength);
                        break;
                    }

                    if (data[iPacketDataLength - 1] != SpecialCharDef.ETX) {
                        LogUtil.logTrace(log,
                                "Parser.Parse szTempBuf[iPacketDataLength - 1][0x%02x] != ETX iPacketDataLength = %d",
                                data[iPacketDataLength - 1], iPacketDataLength);
                        LogUtil.logTrace(log, "Parser.Parse pop [0x%02x]",
                                data[0]);
                        buffer.purge(1); // Skip One Byte
                        continue;
                    }

                    byte[] data2 = new byte[iDataLength];
                    System.arraycopy(data, 6, data2, 0, iDataLength);
                    buffer.purge(iPacketDataLength);

                    String str = new String(data2, Charset.defaultCharset());

                    boolean bOk = false;
                    try {
                        bOk = parseLine(time, str);
                    }catch (Exception ex){
                        log.warn("parseLine String:" + str);
                        throw ex;
                    }
                    if (!bOk)
                        continue;

                    Date tNow = new Date();
                    TimeSpan ts = TimeSpan.getTimeSpan(tNow, tLast);
                    if (ts.getTotalSeconds() >= 30) {
                        tLast = tNow;
                        Util.addMsg("[%d] %s", str.length(), str);
                        log.debug(str);
                    }
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            LogUtil.logException(log, ex);
        }
    }

    /**
     * @param strLine
     * @return
     */
    boolean parseLine(Date time, String strLine) {

        ConcurrentHashMap<Integer, String> dataByFieldIdMap = new ConcurrentHashMap<Integer, String>();
        String strID = "";
        int nDP = 0;
        String nContributeCode = "";

        Date tTime = new Date(0);
        String[] vec = StringUtil.split(strLine, '|');
        int nSource = 0;
        for (int i = 0; i < vec.length; i++) {

            String[] vec2 = StringUtil.split(vec[i], '=');
            if (vec2.length != 2)
                continue;

            int nField;
            nField = Integer.parseInt(vec2[0]);

            switch (nField) {
                case FieldID.Contributecode: {
                    nContributeCode = new String(vec2[1]);
                    dataByFieldIdMap.put(nField, vec2[1]);
                }
                break;

                case FieldID.SourceID: {
                    nSource = Integer.parseInt(vec2[1]);
                }
                break;
                case FieldID.Symbol: {

                    String sID = new String(vec2[1]);
                    sID = IdSymbolUtil.toSymbol(sID, nSource);

                    if (sID.isEmpty()
                            || QuoteMgr.instance().checkSymbol(sID) == false)
                        return false;

                    strID = sID;
                }
                break;
                case FieldID.DisplayPrecision: {
                    nDP = Integer.parseInt(vec2[1]);
                }
                break;
                case FieldID.LastTradeTime: {

                    tTime = new Date(); //(long) (Double.parseDouble(vec2[1]) * 1000));
                }
                break;
                case FieldID.LastActivityTime:
                case FieldID.QuoteTime: {
                    if (0 == tTime.getTime()) {
                        tTime = new Date(
                                (long) (Double.parseDouble(vec2[1]) * 1000));
                    }
                }
                break;
                default: {
                    dataByFieldIdMap.put(nField, vec2[1]);
                }
                break;
            }
        }

        if (strID.isEmpty()) {
            return false;
        }

        if (0 == tTime.getTime()) {
            return false;
        }

        IdMarketDataAdaptor adaptor = IdMarketDataAdaptor.instance;
        //check Contribute
        List<String> contributeList = adaptor.getContributeList();
        List<String> unContributeList = adaptor.getUnContributeList();
        Map<String, String> pluginContributeBySymbolMap = adaptor.getPluginContributeBySymbolMap();

        if (contributeList != null && contributeList.size() > 0) {
            if (Collections.binarySearch(contributeList, nContributeCode) < 0) {
                if (pluginContributeBySymbolMap != null) {
                    if (!(pluginContributeBySymbolMap.containsKey(strID) && nContributeCode.equals(pluginContributeBySymbolMap.get(strID))))
                        return false;
                } else {
                    return false;
                }
            }
        } else {
            if (unContributeList != null && unContributeList.size() > 0) {
                if (Collections.binarySearch(unContributeList, nContributeCode) >= 0) {
                    return false;
                }
            }
        }

        // Get ForexData and process ForexData
        SymbolItem item = QuoteMgr.instance().getItem(strID);
        if (item != null) {
            item.parseTick(time, tTime, nDP, dataByFieldIdMap);
            return true;
        }

        return false;

    }

    /**
     * @return
     */
    public int getQueueSize() {
        return reqThread.getQueueSize();
    }

    /**
     * @param data
     */
    public void addData(Date time, byte[] data) {
        reqThread.addRequest(new Object[]{time, data});
        data = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cyanspring.id.Library.Threading.IReqThreadCallback#onStartEvent(com
     * .cyanspring.id.Library.Threading.RequestThread)
     */
    @Override
    public void onStartEvent(RequestThread sender) {
        try {
            throw new Exception("NotImplementedException");
        } catch (Exception e) {
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cyanspring.id.Library.Threading.IReqThreadCallback#onRequestEvent
     * (com.cyanspring.id.Library.Threading.RequestThread, java.lang.Object)
     */
    @Override
    public void onRequestEvent(RequestThread sender, Object reqObj) {
        Object[] arrObjects = (Object[]) reqObj;
        if (arrObjects.length != 2) {
            reqObj = null;
            arrObjects = null;
            return;
        }

        Date time = (Date) arrObjects[0];
        byte[] data = (byte[]) arrObjects[1];
        try {
            instance.parse(time, data);
        } catch (Exception ex) {
            LogUtil.logException(log, ex);
        }
        data = null;
        reqObj = null;
        arrObjects = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cyanspring.id.Library.Threading.IReqThreadCallback#onStopEvent(com
     * .cyanspring.id.Library.Threading.RequestThread)
     */
    @Override
    public void onStopEvent(RequestThread sender) {

        try {
            throw new Exception("NotImplementedException");
        } catch (Exception e) {
        }
    }

    public void close() {
        if (reqThread != null) {
            reqThread.close();
            reqThread = null;
        }
    }
}
