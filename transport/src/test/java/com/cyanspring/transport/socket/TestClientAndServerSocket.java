package com.cyanspring.transport.socket;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.transport.IClientSocketListener;
import com.cyanspring.common.transport.IServerSocketListener;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.type.QtyPrice;

public class TestClientAndServerSocket {
	private static final Logger log = LoggerFactory
			.getLogger(TestClientAndServerSocket.class);

	ServerSocketService serverService;
	Object serverReceive;
	ClientSocketService clientService;
	Object clientReceive;
	
	@Test
	public void test() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		// server
		serverService = new ServerSocketService();
		serverService.setPort(53211);
		IServerSocketListener serverListener = new IServerSocketListener() {

			@Override
			public void onConnected(boolean connected, IUserSocketContext ctx) {
			}

			@Override
			public void onMessage(Object obj, IUserSocketContext ctx) {
				log.info("Test server receive ok");
				serverReceive = obj;
				ctx.send(obj);
			}
			
		};
		serverService.addListener(serverListener);
		serverService.init();
		
		// client
		clientService = new ClientSocketService();
		clientService.setPort(53211);
		IClientSocketListener clientListener = new IClientSocketListener() {

			@Override
			public void onMessage(Object obj) {
				log.info("Test client receive ok");
				clientReceive = obj;
				synchronized(TestClientAndServerSocket.this) {
					TestClientAndServerSocket.this.notify();
				}
			}

			@Override
			public void onConnected(boolean connected) {
				if(connected) {
					List<Quote> list = new ArrayList<Quote>();
					for(int i=0; i<1; i++) {
						List<QtyPrice> bids = new ArrayList<QtyPrice>();
						List<QtyPrice> asks = new ArrayList<QtyPrice>();	
						Quote quote = new Quote("AUDUSD", bids, asks);
						quote.setAsk(0.8);
						quote.setAskVol(200000.0);
						quote.setBid(.78);
						quote.setBidVol(3000000.0);
						list.add(quote);
					}
					clientService.sendMessage(list);
				}
			}
			
		};
		clientService.addListener(clientListener);
		clientService.init();

		synchronized(this) {
			this.wait();
		}
		ArrayList<Quote> list;
		assertTrue(serverReceive instanceof ArrayList);
		list = (ArrayList<Quote>)serverReceive;
		assertTrue(list.size() == 1);

		assertTrue(clientReceive instanceof ArrayList);
		list = (ArrayList<Quote>)clientReceive;
		assertTrue(list.size() == 1);
	}
	
	@After
	public void after() {
		clientService.uninit();
		serverService.uninit();
	}
	

}
