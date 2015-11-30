package com.cyanspring.transport.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

import com.cyanspring.common.transport.ISerialization;
import com.cyanspring.common.transport.IUserSocketContext;

public class UserSocketContext implements IUserSocketContext {
	private static final Logger log = LoggerFactory
			.getLogger(UserSocketContext.class);

	String id;
	String user;
	Channel channel;
	ISerialization serialization;

	UserSocketContext(String id, String user, Channel channel,
			ISerialization serialization) {
		super();
		this.id = id;
		this.user = user;
		this.channel = channel;
		this.serialization = serialization;
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
		String msg = null;
		try {
			msg = (String) serialization.serialize(obj);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (msg == null) {
			log.error("Sending");
		}
		log.debug("Sending message is : " + msg);
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
