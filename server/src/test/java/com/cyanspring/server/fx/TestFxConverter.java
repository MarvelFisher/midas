package com.cyanspring.server.fx;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cyanspring.common.fx.FxException;
import com.cyanspring.common.marketdata.Quote;

public class TestFxConverter {

	@Test
	public void test() throws FxException {
		FxConverter fxConverter = new FxConverter();
		
		Quote quote = new Quote("AUDUSD", null, null);
		quote.setBid(0.4);
		quote.setAsk(0.8);
		fxConverter.updateRate(quote);
		assertEquals(400, fxConverter.getFxQty("AUD", "USD", 1000), 0.000001);
		assertEquals(1250, fxConverter.getFxQty("USD", "AUD", 1000), 0.000001);
		
		quote = new Quote("USDJPY", null, null);
		quote.setBid(125);
		quote.setAsk(250);
		fxConverter.updateRate(quote);
		assertEquals(1250000, fxConverter.getFxQty("USD", "JPY", 10000), 0.000001);
		assertEquals(5000, fxConverter.getFxQty("JPY", "USD", 1250000), 0.000001);
		assertEquals(-10000, fxConverter.getFxQty("JPY", "USD", -1250000), 0.000001);
		
	}
}
