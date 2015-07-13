package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MsgPackRefDataClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory
            .getLogger(MsgPackRefDataClientHandler.class);

    static ChannelHandlerContext context;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HashMap) processMsgPackRead((HashMap) msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Wind RefData channel Active");
        context = ctx;
        sendReqHeartbeat(); // send request heartbeat message
        WindRefDataAdapter.isConnected = true;
        WindRefDataAdapter.codeTableIsProcessEnd = false;
        WindRefDataAdapter.serverHeartBeatCountAfterCodeTableCome = -1;
        //Request CodeTable
        log.debug("request codetable");
        List<String> marketsList = WindRefDataAdapter.instance.getMarketsList();
        if (marketsList != null && marketsList.size() > 0) {
            for (int i = 0; i < marketsList.size(); i++) {
                sendRequestCodeTable(marketsList.get(i));
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Wind RefData channel InActive");
        WindRefDataAdapter.isConnected = false;
        WindRefDataAdapter.codeTableIsProcessEnd = false;
        WindRefDataAdapter.serverHeartBeatCountAfterCodeTableCome = -1;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    public void processMsgPackRead(HashMap hashMap) {
        StringBuffer sb = new StringBuffer();
        for (Object key : hashMap.keySet()) {
            sb.append(key + "=" + hashMap.get(key) + ",");
        }
        if (WindRefDataAdapter.instance.isMarketDataLog()) log.debug(sb.toString());
        int packType = (int) hashMap.get(FDTFields.PacketType);
        if (packType == FDTFields.PacketArray) {
            ArrayList<HashMap> arrayList = (ArrayList<HashMap>) hashMap.get(FDTFields.ArrayOfPacket);
            for (HashMap innerHashMap : arrayList) {
                WindRefDataAdapter.instance.process(parsePackTypeToDataType((int) innerHashMap.get(FDTFields.PacketType), innerHashMap), null, innerHashMap);
            }
        } else {
            WindRefDataAdapter.instance.process(parsePackTypeToDataType(packType, hashMap), null, hashMap);
        }
    }

    public int parsePackTypeToDataType(int packType, HashMap hashMap) {
        int dataType = -1;
        if (packType == FDTFields.WindCodeTable) dataType = WindDef.MSG_SYS_CODETABLE_RESULT;
        if (hashMap.get(FDTFields.WindSymbolCode) == null) dataType = -1;
        if (packType == FDTFields.Heartbeat) dataType = WindDef.MSG_WINDGW_SERVERHEARTBEAT;
        return dataType;
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
        log.info("[RequestCodeTable]" + fsb.toString());
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
        log.info("[ReqHeartBeat]" + fsb.toString());
        sendData(fsb.toString());
    }

    /**
     * sendData to server
     *
     * @param data
     */
    public static void sendData(String data) {
        context.channel().writeAndFlush(data);
    }
}
