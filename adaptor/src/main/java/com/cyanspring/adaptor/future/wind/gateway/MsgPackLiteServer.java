package com.cyanspring.adaptor.future.wind.gateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.network.transport.FDTFrameDecoder;
import com.cyanspring.network.transport.FDTFrameEncoder;


public class MsgPackLiteServer implements Runnable {
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.MsgPackLiteServer.class);
	
	private int serverPort = 10048;
	public static  int readIdleInterval = 7;
	public static  int writeIdleInterval = 10;
	
	private Channel channel = null;
	private boolean bAlive = true;
	

	@Override
	public void run() {
		runNetty();
	}	
	
	public void stop() {
		bAlive = false;
		if(this.channel != null) {
			this.channel.close();
		}
	}
	
	private void runNetty() {
		int threads = 16;
		EventLoopGroup bossGroup = new NioEventLoopGroup(2);
		EventLoopGroup workerGroup = new NioEventLoopGroup(threads);

        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                	 	ch.pipeline().addLast("decoder",new FDTFrameDecoder());
                	 	ch.pipeline().addLast("encoder",new FDTFrameEncoder());
                		ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(readIdleInterval, writeIdleInterval, 0));
                		ch.pipeline().addLast("idleHandler", new windDataServerIdleHandler());                	
                		ch.pipeline().addLast(new MsgPackLiteDataServerHandler());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true)  // (6)
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
             .option(ChannelOption.SO_REUSEADDR,false)
             .childOption(ChannelOption.TCP_NODELAY, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f;

            while(bAlive) {
            	try {
            		f = b.bind(serverPort).sync();
            		if(f.isSuccess()) {
            			log.info("Wind Message Pack Lite Data Server listen at port : " + serverPort + " , max worker threads : " + threads);

            			// Wait until the server socket is closed.
            			// In this example, this does not happen, but you can do that to gracefully
            			// shut down your server.
            			this.channel = f.channel();
            			f.channel().closeFuture().sync();
            		
            		}
            	} catch(Exception e) {
            		log.warn(e.getMessage(),e);
            	}   
            	log.info("Wind Message Pack Lite Server Closed");
            	if(bAlive) {
            		log.warn("Wind Message Pack Lite Data server can not bind at port : " + serverPort + " , will try again after 3 seconds.");
            		Thread.sleep(3000);
            	}
            }
			
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
		}
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }		
	}
	
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}	

	public int getReadIdleInterval() {
		return readIdleInterval;
	}
	public void setReadIdleInterval(int interval) {
		readIdleInterval = interval;
	}
	
	public int getWriteIdleInterval() {
		return writeIdleInterval;
	}
	public void setWriteIdleInterval(int interval) {
		writeIdleInterval = interval;
	}	
}

class windDataServerIdleHandler extends ChannelDuplexHandler {
	private static final Logger log = LoggerFactory
			.getLogger(windDataServerIdleHandler.class);
	private int heartbeatCounter = 0;
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {            	
            		log.info("Read Idle for " + MsgPackLiteServer.readIdleInterval + " seconds , close client : " + ctx.channel().remoteAddress());
            		ctx.close();            	
            } else if(e.state() == IdleState.WRITER_IDLE) {
            	MsgPackLiteDataServerHandler.sendHeartbeat(ctx.channel(),++heartbeatCounter);
            }
        }
    } 	
}