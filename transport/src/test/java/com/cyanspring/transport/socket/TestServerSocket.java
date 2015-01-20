package com.cyanspring.transport.socket;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.transport.IServerSocketListener;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.transport.socket.ServerSocketService;

@Ignore
public class TestServerSocket {
	private static final Logger log = LoggerFactory
			.getLogger(TestServerSocket.class);
	
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		ServerSocketService service = new ServerSocketService();
		IServerSocketListener listener = new IServerSocketListener() {

			@Override
			public void onConnected(boolean connected, IUserSocketContext ctx) {
				log.info("connection is: " + connected);
			}

			@Override
			public void onMessage(Object obj, IUserSocketContext ctx) {
				log.info("Server received: " + obj);
				ctx.send(obj);
			}
			
		};
		service.addListener(listener);
		service.init();
	}

}
