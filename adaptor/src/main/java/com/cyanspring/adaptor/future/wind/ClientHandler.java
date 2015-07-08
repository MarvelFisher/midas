package com.cyanspring.adaptor.future.wind;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.Network.Transport.FDTFrameDecoder;
import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ClientHandler extends ChannelInboundHandlerAdapter implements
        TimerEventHandler, AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(WindGateWayAdapter.class);

    public static Date lastRecv = DateUtil.now();
    public static Date lastCheck = DateUtil.now();
    static TimerThread timer = null;
    static ChannelHandlerContext context; // context deal with server
    private int bufLenMin = 0, bufLenMax = 0, dataReceived = 0, blockCount = 0;
    private long msDiff = 0, msLastTime = 0, throughput = 0;

    public ClientHandler() {
        if (timer == null) {
            timer = new TimerThread();
            timer.setName("Wind ClientHandler.Timer");
            timer.TimerEvent = this;
            timer.start();
        }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        lastRecv = DateUtil.now();
        try {
            if (WindGateWayAdapter.instance.isMsgPack()) {
                if (msg instanceof HashMap) {
                    processMsgPackRead((HashMap) msg);
                    if (calculateMessageFlow(FDTFrameDecoder.getPacketLen(), FDTFrameDecoder.getReceivedBytes()))
                        FDTFrameDecoder.ResetCounter();
                }
            } else {
                if (msg instanceof String) {
                    String msgStr = (String) msg;
                    processNoMsgPackRead(msgStr);
                    if (calculateMessageFlow(msgStr.length(), dataReceived)) dataReceived = 0;
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void processMsgPackRead(HashMap hashMap) {
        StringBuffer sb = new StringBuffer();
        for (Object key : hashMap.keySet()) {
            sb.append(key + "=" + hashMap.get(key) + ",");
        }
        if (WindGateWayAdapter.instance.isMarketDataLog()) log.debug(sb.toString());
//        Check packType
        int packType = (int) hashMap.get(FDTFields.PacketType);
        if (packType == FDTFields.PacketArray) {
            ArrayList<HashMap> arrayList = (ArrayList<HashMap>) hashMap.get(FDTFields.ArrayOfPacket);
            for (HashMap innerHashMap : arrayList) {
                WindGateWayAdapter.instance.processGateWayMessage(parsePackTypeToDataType((int) innerHashMap.get(FDTFields.PacketType), innerHashMap), null, innerHashMap);
            }
        } else {
            WindGateWayAdapter.instance.processGateWayMessage(parsePackTypeToDataType(packType, hashMap), null, hashMap);
        }
    }

    public int parsePackTypeToDataType(int packType, HashMap hashMap) {
        int dataType = -1;
        if (packType == FDTFields.WindFutureData) dataType = WindDef.MSG_DATA_FUTURE;
        if (packType == FDTFields.WindMarketData) dataType = WindDef.MSG_DATA_MARKET;
        if (packType == FDTFields.WindIndexData) dataType = WindDef.MSG_DATA_INDEX;
        if (packType == FDTFields.WindTransaction) dataType = WindDef.MSG_DATA_TRANSACTION;
        if (hashMap.get(FDTFields.WindSymbolCode) == null) dataType = -1;
        return dataType;
    }

    public void processNoMsgPackRead(String in) {
        String strHash = null;
        String strDataType = null;
        int dataType = -1;
        if (in != null) {
            String[] in_arr = in.split("\\|");
            for (String str : in_arr) {
                if (str.contains("API=")) {
                    strDataType = str.substring(4);
                }
                if (str.contains("Hash=")) {
                    strHash = str.substring(5);
                }
            }
            int endindex = in.indexOf("|Hash=");
            if (endindex > 0) {
                String tempStr = in.substring(0, endindex);
                int hascode = tempStr.hashCode();

                // Compare hash code
                if (hascode == Integer.parseInt(strHash)) {
                    if (WindGateWayAdapter.instance.isMarketDataLog()) {
                        LogUtil.logDebug(log, in);
                    }
                    if (strDataType.equals("DATA_FUTURE")) {
                        dataType = WindDef.MSG_DATA_FUTURE;
                    }
                    if (strDataType.equals("DATA_MARKET")) {
                        dataType = WindDef.MSG_DATA_MARKET;
                    }
                    if (strDataType.equals("DATA_INDEX")) {
                        dataType = WindDef.MSG_DATA_INDEX;
                    }
                    if (strDataType.equals("Heart Beat")) {
                        dataType = WindDef.MSG_SYS_HEART_BEAT;
                    }
                    if (strDataType.equals("QDateChange")) {
                        dataType = WindDef.MSG_SYS_QUOTATIONDATE_CHANGE;
                        LogUtil.logDebug(log, in);
                    }
                    if (strDataType.equals("MarketClose")) {
                        dataType = WindDef.MSG_SYS_MARKET_CLOSE;
                        LogUtil.logDebug(log, in);
                    }
                    WindGateWayAdapter.instance.processGateWayMessage(
                            dataType, in_arr, null);
                }
            }
        }
    }

    private boolean calculateMessageFlow(int rBytes, int dataReceived) {
        if (bufLenMin > rBytes) {
            bufLenMin = rBytes;
            log.info("WindC-minimal recv len from wind gateway : " + bufLenMin);
        } else {
            if (bufLenMin == 0) {
                bufLenMin = rBytes;
                log.info("WindC-first time recv len from wind gateway : " + bufLenMin);
            }
        }
        if (bufLenMax < rBytes) {
            bufLenMax = rBytes;
            log.info("WindC-maximal recv len from gateway : " + bufLenMax);
        }

        blockCount += 1;
        msDiff = System.currentTimeMillis() - msLastTime;
        if (msDiff > 1000) {
            msLastTime = System.currentTimeMillis();
            if (throughput < dataReceived * 1000 / msDiff) {
                throughput = dataReceived * 1000 / msDiff;
                if (throughput > 1024) {
                    log.info("WindC-maximal throughput : " + throughput / 1024 + " KB/Sec , " + blockCount + " blocks/Sec");
                } else {
                    log.info("WindC-maximal throughput : " + throughput + " Bytes/Sec , " + blockCount + " blocks/Sec");
                }
            }
            blockCount = 0;
            return true;
        }
        return false;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        LogUtil.logException(log, (Exception) cause);
        ctx.close();
        WindGateWayAdapter adaptor = WindGateWayAdapter.instance;
        WindGateWayAdapter.isConnected = false;
        adaptor.updateState(WindGateWayAdapter.isConnected);
        WindGateWayAdapter.instance.reconClient();

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.logInfo(log, "Wind channel Active");
        context = ctx;

        String[] arrSymbol = WindGateWayAdapter.instance.getRefSymbol();
        if (arrSymbol.length > 0) {
            for (String symbol : arrSymbol) {
                subscribe(symbol);
            }
        }

        WindGateWayAdapter adaptor = WindGateWayAdapter.instance;
        WindGateWayAdapter.isConnected = true;
        WindGateWayAdapter.isConnecting = false;
        adaptor.updateState(WindGateWayAdapter.isConnected);

        msLastTime = System.currentTimeMillis();

        sendReqHeartbeat(); // send request heartbeat message

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.logInfo(log, "Wind channel InActive");
        WindGateWayAdapter adaptor = WindGateWayAdapter.instance;
        WindGateWayAdapter.isConnected = false;
        adaptor.updateState(WindGateWayAdapter.isConnected);
    }

    @Override
    public void onTimer(TimerThread objSender) {
        if (lastCheck.getTime() < lastRecv.getTime()) {
            lastCheck = lastRecv;
        }

        Date now = DateUtil.now();
        TimeSpan ts = TimeSpan.getTimeSpan(now, lastCheck);
        if (!WindGateWayAdapter.isConnecting
                && !WindGateWayAdapter.isConnected
                && lastCheck.getTime() != 0 && ts.getTotalSeconds() > 20) {
            lastCheck = now;
            WindGateWayAdapter.instance.reconClient();
        }
    }

    /**
     * sendData to server
     *
     * @param data
     */
    public static void sendData(String data) {
        if (!WindGateWayAdapter.instance.isMsgPack()) data = data + "\r\n";
        ChannelFuture future = context.channel().writeAndFlush(data);
    }

    /**
     * get markets
     */
    public static void sendRequestMarket() {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');

        fsb.append("API");
        fsb.append("GetMarkets");
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));

        LogUtil.logInfo(log, "[RequestMarket]%s", fsb.toString());
        sendData(fsb.toString());
    }

    /**
     * get exchange symbol list
     *
     * @param market
     */
    public static void sendRequestCodeTable(String market) {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');

        fsb.append("API");
        fsb.append("GetCodeTable");
        fsb.append("Market");
        fsb.append(market);
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));

        LogUtil.logInfo(log, "[RequestCodeTable]%s", fsb.toString());
        sendData(fsb.toString());
    }

    /**
     * Send Request HeartBeat Message
     */
    public static void sendReqHeartbeat() {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');

        fsb.append("API");
        fsb.append("ReqHeartBeat");
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));

        LogUtil.logInfo(log, "[ReqHeartBeat]%s", fsb.toString());
        sendData(fsb.toString());

    }

    /**
     * Send Subscription frame
     *
     * @param symbol e.g. IF1502
     */
    public static void subscribe(String symbol) {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append("API");
        sbSymbol.append("SUBSCRIBE");
        sbSymbol.append("Symbol");
        sbSymbol.append(symbol);
        String subscribeStr = sbSymbol.toString();
        subscribeStr = subscribeStr + "|Hash="
                + String.valueOf(subscribeStr.hashCode());
        LogUtil.logInfo(log, "[Subscribe]%s", subscribeStr);
        sendData(subscribeStr);

        if (WindGateWayAdapter.instance.isSubTrans()) {
            sbSymbol = new FixStringBuilder('=', '|');
            sbSymbol.append("API");
            sbSymbol.append("SubsTrans");
            sbSymbol.append("Symbol");
            sbSymbol.append(symbol);
            subscribeStr = sbSymbol.toString();
            subscribeStr = subscribeStr + "|Hash="
                    + String.valueOf(subscribeStr.hashCode());
            LogUtil.logInfo(log, "[Subscribe]%s", subscribeStr);
            sendData(subscribeStr);
        }

    }

    /**
     * Send unSubscription frame
     *
     * @param symbol e.g. IF1502
     */
    public static void unSubscribe(String symbol) {

        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append("API");
        sbSymbol.append("UNSUBSCRIBE");
        sbSymbol.append("Symbol");
        sbSymbol.append(symbol);

        String unsubscribeStr = sbSymbol.toString();

        unsubscribeStr = unsubscribeStr + "|Hash="
                + String.valueOf(unsubscribeStr.hashCode());
        LogUtil.logInfo(log, "[UnSubscribe]%s", unsubscribeStr);

        sendData(unsubscribeStr);
    }

    /**
     * send Clear Subscription frame
     */
    public static void sendClearSubscribe() {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append("API");
        sbSymbol.append("ClearSubscribe");

        String subscribeStr = sbSymbol.toString();

        subscribeStr = subscribeStr + "|Hash="
                + String.valueOf(subscribeStr.hashCode());
        LogUtil.logInfo(log, "[sendClearSubscribe]%s", subscribeStr);

        sendData(subscribeStr);
    }

    @Override
    public void close() throws Exception {
        uninit();
        FinalizeHelper.suppressFinalize(this);
    }

    void uninit() throws Exception {
    }

}
