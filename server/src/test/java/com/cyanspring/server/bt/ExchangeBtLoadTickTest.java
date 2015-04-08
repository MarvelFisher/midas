package com.cyanspring.server.bt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.cyanspring.common.marketdata.*;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.data.DataObject;

import static org.junit.Assert.assertTrue;

public class ExchangeBtLoadTickTest implements IMarketDataListener {
	private Quote currentQuote;
	private static final Logger log = LoggerFactory
			.getLogger(ExchangeBtLoadTickTest.class);
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/common/log4j.xml");
	}

	@Test
	public void test() throws TickDataException, IOException, MarketDataException {
		String[] files = new String[]{
			"src/test/resources/ticks/ANZ.AX.txt",
			"src/test/resources/ticks/BHP.AX.txt",
			"src/test/resources/ticks/RIO.AX.txt",
			"src/test/resources/ticks/WBC.AX.txt",
		};
		ExchangeBT exchange = new ExchangeBT();
		exchange.init();
		exchange.subscribeMarketData(null, this);
		exchange.loadTickDataFiles(files);
		exchange.replay();
	}

	@Override
	public void onQuote(InnerQuote innerQuote) {
		log.info("Quote: " + innerQuote.getSymbol() + ", " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(innerQuote.getQuote().getTimeStamp()));
		if(null != currentQuote) {
			assertTrue(innerQuote.getQuote().getTimeStamp().equals(currentQuote.getTimeStamp()) ||
					innerQuote.getQuote().getTimeStamp().after(currentQuote.getTimeStamp()));
		}
		currentQuote = innerQuote.getQuote();
	}

	@Override
	public void onTrade(Trade trade) {
		log.info("Trade: " + trade.getSymbol() + ", " + trade.getPrice() + ", " + trade.getQuantity());
	}

	@Override
	public void onQuoteExt(DataObject quoteExt, int sourceId) {		
	}
}
