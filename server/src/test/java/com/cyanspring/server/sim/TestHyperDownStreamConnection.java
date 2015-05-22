package com.cyanspring.server.sim;

import com.cyanspring.common.marketdata.PriceQuoteChecker;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;

public class TestHyperDownStreamConnection {
	private static final String symbol = "0005.HK";
	private HyperDownStreamConnection connection;
	private ChildOrder buyOrder, sellOrder;
	private ChildOrder buyMarketOrder, sellMarketOrder;
	private Quote quote;
	private int updateCount;
	
	private double getHitPrice(Quote quote, ChildOrder order) {
		if(order.getSide().isBuy())
			return quote.getAsk();
		else
			return quote.getBid();
	}
	
	IDownStreamListener fillOrderListener = new IDownStreamListener() {
		@Override
		public void onState(boolean on) {
		}

		@Override
		public void onOrder(ExecType execType, ChildOrder order,
				Execution execution, String message) {
			assertEquals(ExecType.FILLED, execType);
			assertTrue(PriceUtils.Equal(order.getCumQty(), order.getQuantity()));
			assertTrue(PriceUtils.Equal(order.getAvgPx(), getHitPrice(quote, order)));
			assertEquals(OrdStatus.FILLED, order.getOrdStatus());
			assertTrue(PriceUtils.Equal(execution.getQuantity(), order.getQuantity()));
			assertTrue(PriceUtils.Equal(execution.getPrice(), getHitPrice(quote, order)));
			assertEquals(execution.getOrderId(), order.getId());
			updateCount++;
		}

		@Override
		public void onError(String orderId, String message) {
		}
	};

	IDownStreamListener newOrderListener = new IDownStreamListener() {
		@Override
		public void onState(boolean on) {
		}

		@Override
		public void onOrder(ExecType execType, ChildOrder order,
				Execution execution, String message) {
			assertEquals(ExecType.NEW, execType);
			assertTrue(PriceUtils.Equal(order.getCumQty(), 0));
			assertTrue(PriceUtils.Equal(order.getAvgPx(), 0));
			assertEquals(OrdStatus.NEW, order.getOrdStatus());
			assertEquals(execution, null);
			updateCount++;
		}

		@Override
		public void onError(String orderId, String message) {
		}
	};
	
	IDownStreamListener rejectOrderListener = new IDownStreamListener() {
		@Override
		public void onState(boolean on) {
		}

		@Override
		public void onOrder(ExecType execType, ChildOrder order,
				Execution execution, String message) {
			assertEquals(ExecType.REJECTED, execType);
			assertEquals(OrdStatus.REJECTED, order.getOrdStatus());
			assertEquals(execution, null);
			updateCount++;
		}

		@Override
		public void onError(String orderId, String message) {
		}
	};

	@Before
	public void before() throws Exception {
		connection = new HyperDownStreamConnection();
        connection.setQuoteChecker(new PriceQuoteChecker());
		connection.setSync(true);
		//connection.init();
		buyOrder = new ChildOrder(symbol, OrderSide.Buy, 2000,
				68, ExchangeOrderType.LIMIT, "parentOrderId", "strategyId", Default.getUser(), Default.getAccount(), null);
		
		sellOrder = new ChildOrder(symbol, OrderSide.Sell, 2000,
				67, ExchangeOrderType.LIMIT, "parentOrderId", "strategyId", Default.getUser(), Default.getAccount(), null);
		
		buyMarketOrder = new ChildOrder(symbol, OrderSide.Buy, 2000,
				0.0, ExchangeOrderType.MARKET, "parentOrderId", "strategyId", Default.getUser(), Default.getAccount(), null);
		
		sellMarketOrder = new ChildOrder(symbol, OrderSide.Sell, 2000,
				0.0, ExchangeOrderType.MARKET, "parentOrderId", "strategyId", Default.getUser(), Default.getAccount(), null);
		
		
		quote = new Quote(symbol, null, null);
		quote.setAsk(68);
		quote.setBid(67);
		updateCount = 0;
	}
	
	@Test
	public void testFillBuyOrder() throws DownStreamException {
		IDownStreamSender sender = connection.setListener(fillOrderListener);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		sender.newOrder(buyOrder);
		assertEquals(1, updateCount);
	}
	
	@Test
	public void testFillSellOrder() throws DownStreamException {
		IDownStreamSender sender = connection.setListener(fillOrderListener);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		sender.newOrder(sellOrder);
		assertEquals(1, updateCount);
	}

	@Test
	public void testNewBuyOrder() throws DownStreamException {
		IDownStreamSender sender = connection.setListener(newOrderListener);
		quote.setAsk(69);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		sender.newOrder(buyOrder);
		connection.setListener(null);
		sender = connection.setListener(fillOrderListener);
		quote.setAsk(67.5);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		assertEquals(2, updateCount);
	}
	
	@Test
	public void testNewSellOrder() throws DownStreamException {
		IDownStreamSender sender = connection.setListener(newOrderListener);
		quote.setBid(66);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		sender.newOrder(sellOrder);
		connection.setListener(null);
		sender = connection.setListener(fillOrderListener);
		quote.setBid(67.5);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		assertEquals(2, updateCount);
	}
	
	@Test
	public void testFillBuyMarketOrder() throws DownStreamException {
		IDownStreamSender sender = connection.setListener(fillOrderListener);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		sender.newOrder(buyMarketOrder);
		assertEquals(1, updateCount);
	}
	
	@Test
	public void testFillSellMarketOrder() throws DownStreamException {
		IDownStreamSender sender = connection.setListener(fillOrderListener);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		sender.newOrder(sellMarketOrder);
		assertEquals(1, updateCount);
	}

	@Test
	public void testMarketOrderWithoutMarket() throws DownStreamException {
		IDownStreamSender sender = connection.setListener(newOrderListener);
		quote.setBid(0);
		quote.setAsk(0);
		connection.processQuoteEvent(new QuoteEvent(symbol, null, quote));
		sender.newOrder(buyMarketOrder);
		assertEquals(1, updateCount);
		sender.newOrder(sellMarketOrder);
		assertEquals(2, updateCount);
	}

	
}
