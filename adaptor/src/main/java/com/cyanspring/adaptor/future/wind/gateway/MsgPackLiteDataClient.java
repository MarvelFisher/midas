package com.cyanspring.adaptor.future.wind.gateway;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.network.transport.FDTFrameDecoder;
import com.cyanspring.network.transport.FDTFrameEncoder;

public class MsgPackLiteDataClient implements Runnable {
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.MsgPackLiteDataClient.class);
	
	private boolean bAlive = true;
	private Channel channel = null;

	@Override
	public void run() {
		runNetty();
	}
	
	private void runNetty() {
		log.debug("Run Netty Message Pack Lite Data Client");
		EventLoopGroup group = new NioEventLoopGroup(4);
		ChannelFuture f;
		Bootstrap bootstrap = new Bootstrap()
		.group(group)
		.channel(NioSocketChannel.class)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
		.handler(new MsgPackLiteDataClientInitializer());
		
		try {			
			while(bAlive) {
				
				try {
					// Start the client.
					//f = bootstrap.connect(host,port).sync();
					f = bootstrap.connect(WindGateway.mpUpstreamIp,WindGateway.mpUpstreamPort);
					f.awaitUninterruptibly();
					if(f.isSuccess()) {
						// Wait until the connection is closed.
						channel = f.channel();
						f.channel().closeFuture().sync();
					}
				} catch (Exception e) {
					log.warn(e.getMessage(),e);
				}				
				log.info("Message Pack Lite Data client disconnect with - " + WindGateway.mpUpstreamIp + " : " + WindGateway.mpUpstreamPort);
				if(bAlive) {
					log.info("will try again after 3 seconds.");
					Thread.sleep(3000);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		finally {
			group.shutdownGracefully();
		}
	}
	
	public void stop() {
		bAlive = false;
		if(channel != null) {
			channel.close();
		}
	}
}

class MsgPackLiteDataClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
	 	ch.pipeline().addLast("decoder",new FDTFrameDecoder());
	 	ch.pipeline().addLast("encoder",new FDTFrameEncoder());
		pipeline.addLast("idleStateHandler", new IdleStateHandler(MsgPackLiteServer.readIdleInterval, MsgPackLiteServer.writeIdleInterval, 0));
		pipeline.addLast("idleHandler", new MsgPackLiteDataClientIdleHandler());
		pipeline.addLast("handler", new MsgPackLiteDataClientHandler());		
		
	}
}

class MsgPackLiteDataClientIdleHandler extends ChannelDuplexHandler {
	private static final Logger log = LoggerFactory.getLogger(MsgPackLiteDataClientIdleHandler.class);
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
            		log.warn("Msg Pack Lite Upstream reader idle for " + MsgPackLiteServer.readIdleInterval + " seconds , channel will close : " + ctx.channel().remoteAddress().toString());
            		ctx.close();
            } 
            if (e.state() == IdleState.WRITER_IDLE) {
            	ctx.channel().writeAndFlush(MsgPackLiteDataServerHandler.addHashTail("API=ClientHeartBeat",true));
            }
        }
    } 	
}