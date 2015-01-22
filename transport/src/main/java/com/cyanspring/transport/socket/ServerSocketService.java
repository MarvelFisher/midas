package com.cyanspring.transport.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.transport.IMessageListener;
import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.transport.ISender;
import com.cyanspring.common.transport.IServerUserSocketService;
import com.cyanspring.common.transport.ITransportService;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.transport.IServerSocketListener;
import com.cyanspring.common.util.DualMap;
import com.cyanspring.common.util.IdGenerator;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class ServerSocketService implements IServerUserSocketService, IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(ServerSocketService.class);

	private XStream xstream = new XStream(new DomDriver("UTF_8"));
	private int port = 52368;
	private String host = "0.0.0.0";
	private int buffSize = 200*8192;
	//private ChannelGroup channels;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;
	// channel structures
	private Map<Channel, String> channels = new ConcurrentHashMap<Channel, String>();
	private Map<String, IUserSocketContext> idChannels = 
			new ConcurrentHashMap<String, IUserSocketContext>();
	private Map<String, List<IUserSocketContext>> userChannels = 
				new ConcurrentHashMap<String, List<IUserSocketContext>>();
	
	private List<IServerSocketListener> listeners = 
			Collections.synchronizedList(new ArrayList<IServerSocketListener>());
	
	synchronized private IUserSocketContext addConnectionContext(Channel channel) {
		UserSocketContext ctx = new UserSocketContext(IdGenerator.getInstance().getNextID(), null, channel, xstream);
		channels.put(channel, ctx.getId());
		idChannels.put(ctx.getId(), ctx);
    	log.info("Channel count: " + channels.size());
		return ctx;
	}
	
	synchronized private IUserSocketContext removeConnectionContext(Channel channel) {
		IUserSocketContext ctx = null;
		String id = channels.remove(channel);
		if(null != id) {
			ctx = idChannels.remove(id);
			if(null != ctx && null != ctx.getUser()) {
				List<IUserSocketContext> list = userChannels.remove(ctx.getUser());
				if(null != list)
					list.remove(ctx);
			}
		}
		return ctx;
	}
	
	@Override
	synchronized public void setUserContext(String user, IUserSocketContext ctx) {
		ctx.setUser(user);
		List<IUserSocketContext> list = userChannels.get(user);
		if(null == list) {
			list = Collections.synchronizedList(new LinkedList<IUserSocketContext>());
			userChannels.put(user, list);
		}
		list.add(ctx);
	}
	
	private class ServerMessageHandler extends SimpleChannelInboundHandler<String> {

	    @Override
	    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
	    	log.info("Channel connected: " + ctx.channel());
	    	IUserSocketContext userCtx = addConnectionContext(ctx.channel());
			for(IServerSocketListener listener: listeners) {
				try {
					listener.onConnected(true, userCtx);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}    	
	    	log.info("Channel count: " + channels.size());
	    	super.channelActive(ctx);
	    }

	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	    	log.info("Channel disconnected: " + ctx.channel());
	    	IUserSocketContext userCtx = removeConnectionContext(ctx.channel());
	    	if(null != userCtx) {
				for(IServerSocketListener listener: listeners) {
					try {
						listener.onConnected(false, userCtx);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
	    	}
	    	log.info("Channel count: " + channels.size());
	    	super.channelInactive(ctx);
	    }

	    @Override
	    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
	    	log.debug(msg);
	    	Object obj = xstream.fromXML(msg);
	    	Channel channel = ctx.channel();
	    	String id = channels.get(channel);
	    	if(null == id) {
	    		log.error("Received message in a channel not registered: " + channel);
	    		return;
	    	}

	    	IUserSocketContext usc = idChannels.get(id);
	    	if(null == usc) {
	    		log.error("Received message in a channel that has no IUserSocketContext: " + channel);
	    		return;
	    	}
	    	
			for(IServerSocketListener listener: listeners) {
				try {
					listener.onMessage(obj, usc);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}    	
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        log.warn(cause.getMessage(), cause);
	        ctx.close();
	    	log.info("Channel error: " + ctx.channel());
	    	IUserSocketContext userCtx = removeConnectionContext(ctx.channel());
	    	if(null != userCtx) {
				for(IServerSocketListener listener: listeners) {
					try {
						listener.onConnected(false, userCtx);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
	    	}
	    	log.info("Channel count: " + channels.size());
	    }
	    
	}
	
	@Override
	public void init() throws Exception {
		// Configure the server.
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		//channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_BACKLOG, 150)
					.option(ChannelOption.SO_TIMEOUT, 7000)
					.handler(new LoggingHandler(LogLevel.DEBUG))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new DelimiterBasedFrameDecoder(buffSize, Delimiters.nulDelimiter()));// inbound
							p.addLast(new StringDecoder(CharsetUtil.UTF_8));// inbound
							p.addLast(new StringEncoder(CharsetUtil.UTF_8)); // outbound
							p.addLast(new ServerMessageHandler()); // Custom inbound
						}
					});

			// Start the server.
			ChannelFuture serverChannel = serverBootstrap.bind(host, port);
			serverChannel.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture arg0) throws Exception {
					log.info("SocketService is listening : " + port);
				}
			});

		} catch (Exception e) {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void uninit() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	@Override
	synchronized public IUserSocketContext getContext(String key) {
		return idChannels.get(key);
	}

	@Override
	synchronized public List<IUserSocketContext> getContextByUser(String key) {
		List<IUserSocketContext> list = userChannels.get(key);
		LinkedList<IUserSocketContext> result = new LinkedList<IUserSocketContext>();
		if(null != list)
			result.addAll(list);
		return result;
	}

	@Override
	synchronized public boolean addListener(IServerSocketListener listener) {
		if(listeners.contains(listener))
			return false;
		
		listeners.add(listener);
		return true;
	}
	
	@Override
	synchronized public boolean removeListener(IServerSocketListener listener) {
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

	
}
