package com.cyanspring.adaptor.future.wind.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


public class WindDataClient implements Runnable {
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.WindDataClient.class);
	
	private boolean bAlive = true;

	@Override
	public void run() {
		runNetty();
	}
	
	private void runNetty() {
		log.debug("Run Netty Data Client");
		EventLoopGroup group = new NioEventLoopGroup(2);
		ChannelFuture f;
		Bootstrap bootstrap = new Bootstrap()
		.group(group)
		.channel(NioSocketChannel.class)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
		.handler(new windDataClientInitializer());
		
		try {			
			while(bAlive) {
				
				try {
					// Start the client.
					//f = bootstrap.connect(host,port).sync();
					f = bootstrap.connect(WindGateway.upstreamIp,WindGateway.upstreamPort);
					f.awaitUninterruptibly();
					if(f.isSuccess()) {
						// Wait until the connection is closed.				
						f.channel().closeFuture().sync();
					}
				} catch (Exception e) {
					log.warn(e.getMessage(),e);
				}				
				log.info("Wind Data client disconnect with - " + WindGateway.upstreamIp + " : " + WindGateway.upstreamPort + " , will try again after 3 seconds.");
				Thread.sleep(3000);				
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
	}

}
