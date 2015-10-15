package com.cyanspring.adaptor.future.wind.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.network.transport.FDTFrameDecoder;
import com.cyanspring.network.transport.FDTFrameEncoder;

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

public class MsgPackLiteDataClient implements Runnable {
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.client.MsgPackLiteDataClient.class);	
	
	private String serverIp;
	private int serverPort;
	private boolean bAlive = true;
	private Channel channel = null;	
	
	public MsgPackLiteDataClient(String ip,int port) {
		this.serverIp = ip;
		this.serverPort = port;
	}
	
	public void run() {
		runNetty();
	}
	
	public void stop() {
		bAlive = false;
		if(channel != null) {
			channel.close();
		}
	}
	
	private void runNetty() {		
		
		EventLoopGroup group = new NioEventLoopGroup(2);
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
					f = bootstrap.connect(serverIp,serverPort);
					f.awaitUninterruptibly();
					if(f.isSuccess()) {
						// Wait until the connection is closed.
						channel = f.channel();
						f.channel().closeFuture().sync();
						channel = null;
					}
				} catch (Exception e) {
					log.warn(e.getMessage(),e);
				}				
				log.info("Wind MsgPackLite Data client can not connect with - " + serverIp + " : " + serverPort + " , will try again after 3 seconds.");
				if(bAlive) {
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

}

class MsgPackLiteDataClientInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline pipeline = arg0.pipeline();
		
		int clientIdleInterval = 0;
		int writeIdleInterval = 0; 
	 	pipeline.addLast("decoder",new FDTFrameDecoder());
	 	pipeline.addLast("encoder",new FDTFrameEncoder());		
		pipeline.addLast("idleStateHandler", new IdleStateHandler(clientIdleInterval, writeIdleInterval , 0));
		pipeline.addLast("idleHandler", new MsgPackLiteDataClientIdleHandler());
		pipeline.addLast("hadler", new MsgPackLiteDataClientHandler());
	}
	
}

class MsgPackLiteDataClientIdleHandler extends ChannelDuplexHandler {
	private static final Logger log = LoggerFactory
			.getLogger(MsgPackLiteDataClientIdleHandler.class);
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
            	log.info("Read Idle");
            	ctx.close();
            } 
            if (e.state() == IdleState.WRITER_IDLE) {
            	//ctx.channel().writeAndFlush(Unpooled.copiedBuffer(idDataClientHandler.heartBeat()));
            }
        }
    } 	
}
