package com.cyanspring.adaptor.future.wind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Util;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientIdleHandler extends ChannelDuplexHandler {

	private static final Logger log = LoggerFactory
			.getLogger(WindFutureDataAdaptor.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				LogUtil.logInfo(log, "READER_IDLE");
				// reconnect to wind getway
				ctx.close();
				WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
				WindFutureDataAdaptor.isConnected = false;
				adaptor.updateState(false);
				WindFutureDataAdaptor.instance.reconClient();
			} else if (e.state() == IdleState.WRITER_IDLE) {
//				LogUtil.logInfo(log, "WRITER_IDLE");
				ctx.writeAndFlush(makeHeartBeatMsg());
			}
		}
	}

	/**
	 * Make Client HeartBeat Message
	 */
	public static String makeHeartBeatMsg() {
		FixStringBuilder fsb = new FixStringBuilder('=', '|');

		fsb.append("API");
		fsb.append("ClientHeartBeat");
		int fsbhashCode = fsb.toString().hashCode();
		fsb.append("Hash");
		fsb.append(String.valueOf(fsbhashCode));

//		LogUtil.logInfo(log, "[ClientHeartBeat]%s", fsb.toString());
//		Util.addLog("[ClientHeartBeat]%s", fsb.toString());
		return fsb.toString() + "\r\n";

	}
}
