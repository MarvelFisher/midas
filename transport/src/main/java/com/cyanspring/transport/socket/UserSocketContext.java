package com.cyanspring.transport.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

import com.cyanspring.common.transport.IUserSocketContext;
import com.thoughtworks.xstream.XStream;

public class UserSocketContext implements IUserSocketContext {
	private static final Logger log = LoggerFactory
			.getLogger(UserSocketContext.class);
	
	String id;
	String user;
	Channel channel;
	XStream xstream;
	
	UserSocketContext(String id, String user, Channel channel, XStream xstream) {
		super();
		this.id = id;
		this.user = user;
		this.channel = channel;
		this.xstream = xstream;
	}

	@Override
	public String getId() {
		return this.id;
	}
	@Override
	public String getUser() {
		// TODO Auto-generated method stub
		return this.user;
	}
	@Override
	public void send(Object obj) {
		String msg = xstream.toXML(obj);
		log.debug("Sending: \n" + msg);
		log.debug("Writing message size: " + msg.length());
		channel.write(msg);
		channel.writeAndFlush("\0");
	}

	@Override
	public void setUser(String user) {
		this.user = user;
	}
	
	@Override
	public void close() {
		channel.close();
	}
	
	@Override
	public boolean isOpen() {
		return channel.isActive();
	}

}
