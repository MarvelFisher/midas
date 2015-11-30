package com.cyanspring.transport.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.transport.IClientSocketListener;
import com.cyanspring.common.transport.IClientSocketService;
import com.cyanspring.common.transport.ISerialization;
import com.cyanspring.transport.tools.GsonSerialization;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ClientSocketService implements IClientSocketService, IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(ClientSocketService.class);

	// private XStream xstream = new XStream(new DomDriver("UTF_8"));
	private ISerialization serialization = new GsonSerialization();

	private int port = 52368;
	private String host = "";
	private int buffSize = 2000 * 8192;
	private boolean autoReconnect = false;
	private EventLoopGroup group;
	private Channel channel;
	private List<IClientSocketListener> listeners = Collections
			.synchronizedList(new ArrayList<IClientSocketListener>());
	private int connectionTimeout = 3000;
	private long retryInterval = 10000;
	private int maxRetry = 0;
	private int alreadyRetry = 0;

	private class ClientMessageHandler extends
			SimpleChannelInboundHandler<String> {
		@Override
		public void channelActive(final ChannelHandlerContext ctx)
				throws Exception {
			log.info("Channel connected: " + ctx.channel());
			ClientSocketService.this.channel = ctx.channel();
			for (IClientSocketListener listener : listeners) {
				try {
					listener.onConnected(true);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			super.channelActive(ctx);
		}

		@Override
		public void channelRead0(ChannelHandlerContext ctx, String msg)
				throws Exception {
			log.debug(msg);
			// Object obj = xstream.fromXML(msg);
			Object obj = serialization.deSerialize(msg);
			for (IClientSocketListener listener : listeners) {
				try {
					listener.onMessage(obj);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			log.info("Channel disconnected: " + ctx.channel());
			if (null != ClientSocketService.this.channel) {
				ClientSocketService.this.channel = null;
				for (IClientSocketListener listener : listeners) {
					try {
						listener.onConnected(false);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
			if (autoReconnect) {
				tryReconnect();
			}
			super.channelInactive(ctx);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			log.error(cause.getMessage(), cause);
			ctx.close();
			if (null != ClientSocketService.this.channel) {
				ClientSocketService.this.channel = null;
				for (IClientSocketListener listener : listeners) {
					try {
						listener.onConnected(false);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	private void tryReconnect() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				uninit();
				try {
					init();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}

		});
		thread.start();
	}

	@Override
	public void init() throws Exception {
		group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap
					.group(group)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
							connectionTimeout).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new DelimiterBasedFrameDecoder(buffSize,
									Delimiters.nulDelimiter())); // inbound
							p.addLast(new StringDecoder(CharsetUtil.UTF_8));// inbound
							p.addLast(new StringEncoder(CharsetUtil.UTF_8)); // outbound
							p.addLast(new ClientMessageHandler()); // inbound
						}
					});

			// Start the connection attempt.
			boolean connected = false;
			while (!connected) {
				log.info("Trying to connect to: " + host + ", " + port);
				try {
					bootstrap.connect(host, port).sync().channel();
					connected = true;
				} catch (Exception e) {
					log.debug(e.getMessage());
				}
				if (!connected) {
					if (maxRetry != 0) {
						alreadyRetry++;
						if (alreadyRetry >= maxRetry)
							throw new Exception("Connection time out: " + host
									+ ", " + port);
					}
					Thread.sleep(retryInterval);
				}
			}
			log.info("Connected : " + host + ", " + port);
		} catch (Exception e) {
			alreadyRetry = 0;
			group.shutdownGracefully();
			throw e;
		}
	}

	@Override
	public void uninit() {
		if (group != null) {
			group.shutdownGracefully();
		}
	}

	@Override
	public boolean sendMessage(Object obj) {
		if (null == channel) {
			return false;
		}
		String msg = null;
		try {
			msg = (String) serialization.serialize(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// String msg = xstream.toXML(obj);
		log.debug("Sending: \n" + msg);
		log.debug("Writing message size: " + msg.length());
		channel.write(msg);
		channel.writeAndFlush("\0");
		return true;
	}

	@Override
	public boolean addListener(IClientSocketListener listener) {
		if (listeners.contains(listener))
			return false;

		listeners.add(listener);
		return true;
	}

	@Override
	public boolean removeListener(IClientSocketListener listener) {
		return listeners.remove(listener);
	}

	// getters and setters

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getBuffSize() {
		return buffSize;
	}

	public void setBuffSize(int buffSize) {
		this.buffSize = buffSize;
	}

	public long getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

	public boolean isAutoReconnect() {
		return autoReconnect;
	}

	public void setAutoReconnect(boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public ISerialization getSerialization() {
		return serialization;
	}

	public void setSerialization(ISerialization serialization) {
		this.serialization = serialization;
	}
}
