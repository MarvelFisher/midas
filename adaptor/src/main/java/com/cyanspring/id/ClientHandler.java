package com.cyanspring.id;

import com.cyanspring.id.Library.Util.*;
import com.cyanspring.id.Library.Util.Network.SpecialCharDef;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * Handler implementation for Forex data message, by using command
 * subscribe/unsubscribe to decide which market data to receive.
 *
 * @author Hudson Chen
 */
public class ClientHandler extends ChannelInboundHandlerAdapter implements
        AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(IdMarketDataAdaptor.class);

    static ChannelHandlerContext context; // context deal with server

    /**
     * sendData to server
     *
     * @param data
     */
    public static void sendData(byte[] data) {
        final ByteBuf buffer = Unpooled.copiedBuffer(data);
        data = null;
        ChannelFuture future = context.writeAndFlush(buffer);

        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture arg0) throws Exception {
                if (buffer.refCnt() > 0)
                    buffer.release();
            }
        });
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                log.warn("Id Client Read idle");
                ctx.close();
                IdMarketDataAdaptor.instance.isConnected = false;
                IdMarketDataAdaptor.instance.updateState();
            }
            if (e.state() == IdleState.WRITER_IDLE) {
                sendHeartBeat();
            }
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Id Adapter Channel Active!");
        context = ctx;
        logOn(IdMarketDataAdaptor.instance.getAccount(), IdMarketDataAdaptor.instance.getPassword());
        String[] arrSymbol = IdMarketDataAdaptor.instance.getRefSymbol();
        if (arrSymbol.length > 0) {
            subscribe(arrSymbol);
        }
        IdMarketDataAdaptor.isConnected = true;
        IdMarketDataAdaptor.instance.updateState();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.logInfo(log, "Id Adapter Channel InActive");
        IdMarketDataAdaptor.isConnected = false;
        IdMarketDataAdaptor.instance.updateState();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            final ByteBuf buffer = (ByteBuf) msg;
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            Parser.instance().processData(new Date(), data);
            // buffer.release();
            data = null;
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        LogUtil.logException(log, (Exception) cause);
        ctx.close();
    }

    /**
     * covert String to Comstock frame 0x040x20 + 4 bytes Length + String + 0x03
     *
     * @param strData
     * @return
     */
    public static byte[] makeFrame(String strData) {
        byte[] data = null;
        int nSizeLen = 4;
        byte[] arrData = strData.getBytes();

        data = BitConverter.toBytes(arrData.length, nSizeLen);
        int nLen = arrData.length + nSizeLen + 3;

        byte[] frame = new byte[nLen];
        int i = 0;
        frame[i] = SpecialCharDef.EOT;
        frame[i + 1] = SpecialCharDef.SPC;
        System.arraycopy(data, 0, frame, i + 2, nSizeLen);
        System.arraycopy(arrData, 0, frame, i + 2 + nSizeLen, arrData.length);
        frame[nLen - 1] = SpecialCharDef.ETX;
        return frame;
    }

    /**
     * Send login frame
     *
     * @param strUsername
     * @param strPassword
     */
    public static void logOn(String strUsername, String strPassword) {
        FixStringBuilder login = new FixStringBuilder('=', '|');

        login.append(5022);
        login.append("LoginUser");
        login.append(5026);
        login.append(7);
        login.append(5028);
        login.append(strUsername);
        login.append(5029);
        login.append(strPassword);

        String strLogon = login.toString();
        LogUtil.logInfo(log, "[logOn] %s", strLogon);
        Util.addLog("[logOn] %s", strLogon);

        byte[] arrData = makeFrame(strLogon);

        sendData(arrData);
    }

    /**
     * Send Subscription frame
     *
     * @param nSourceID e.g. 687 is Forex Exancge
     */
    public static void subscribe(int nSourceID) {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append(5022);
        sbSymbol.append("Subscribe");
        sbSymbol.append(4);
        sbSymbol.append(nSourceID);
        sbSymbol.append(5026);
        sbSymbol.append(1);

        String strSetCTFOn = sbSymbol.toString();
        LogUtil.logInfo(log, "[Subscribe]%s", strSetCTFOn);
        Util.addLog("[Subscribe]%s", strSetCTFOn);

        byte[] arrData = makeFrame(strSetCTFOn);

        sendData(arrData);
    }

    /**
     * Send Subscription frame
     */
    public static void subscribeSelect() {
        ArrayList<String> list = QuoteMgr.instance().SymbolList();
        for (String s : list) {
            subscribe(IdMarketDataAdaptor.instance.getExch(), s);
        }
    }

    /**
     * Send Subscription frame
     *
     * @param arrSymbol symbol list to subscribe
     */
    public static void subscribe(String[] arrSymbol) {

        for (String symbol : arrSymbol) {
            subscribe(IdMarketDataAdaptor.instance.getExch(symbol), symbol);
        }
    }

    /**
     * Send Subscription frame
     *
     * @param nSourceID : exchange
     * @param strSymbol Symbol "X:S"+ symbol is comstock symbol
     */
    public static void subscribe(int nSourceID, String strSymbol) {

        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append(5022);
        sbSymbol.append("Subscribe");
        sbSymbol.append(4);
        sbSymbol.append(nSourceID);
        sbSymbol.append(5);
        String idSymbol = IdSymbolUtil.toIdSymbol(strSymbol, nSourceID);
        sbSymbol.append(idSymbol);
        sbSymbol.append(5026);
        sbSymbol.append(1);

        String strSetCTFOn = sbSymbol.toString();
        LogUtil.logInfo(log, "[Subscribe]%s", strSetCTFOn);

        byte[] arrData = makeFrame(strSetCTFOn);

        sendData(arrData);
    }

    /**
     * Send unSubscription frame
     *
     * @param nSourceID : exchange
     * @param strSymbol Symbol "X:S"+ symbol is comstock symbol
     */
    public static void unSubscribe(int nSourceID, String strSymbol) {

        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append(5022);
        sbSymbol.append("Unsubscribe");
        sbSymbol.append(4);
        sbSymbol.append(nSourceID);
        sbSymbol.append(5);
        String idSymbol = IdSymbolUtil.toIdSymbol(strSymbol, nSourceID);
        sbSymbol.append(idSymbol);

        sbSymbol.append(5026);
        sbSymbol.append(1);

        String strSetCTFOn = sbSymbol.toString();
        LogUtil.logInfo(log, "[unSubscribe]%s", strSetCTFOn);

        byte[] arrData = makeFrame(strSetCTFOn);

        sendData(arrData);
    }

    public static void sendHeartBeat() {
        log.trace("Send HeartBeat");
        sendData(makeFrame("5022=HeartBeat"));
    }

    @Override
    protected void finalize() throws Throwable {
        uninit();
    }


    void uninit() throws Exception {
    }

    @Override
    public void close() throws Exception {
        uninit();
        FinalizeHelper.suppressFinalize(this);
    }
}
