package com.cyanspring.transport.socket;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.transport.IClientSocketListener;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.transport.socket.ClientSocketService;

@Ignore
public class TestClientSocket {
	private static final Logger log = LoggerFactory
			.getLogger(TestClientSocket.class);
	
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		final ClientSocketService service = new ClientSocketService();
		IClientSocketListener listener = new IClientSocketListener() {

			@Override
			public void onMessage(Object obj) {
				log.info("Client received: " + obj);
			}

			@Override
			public void onConnected(boolean connected) {
				log.info("connection is: " + connected);
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
					service.sendMessage(list);
				}
			}
			
		};
		service.addListener(listener);
		service.init();
		
	}


}
