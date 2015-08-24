/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/


package com.cyanspring.server.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.marketdata.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.TimeInForce;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.event.AsyncEventProcessor;

public class HyperDownStreamConnection extends AsyncEventProcessor implements IDownStreamConnection, IDownStreamSender{
	private static final Logger log = LoggerFactory
			.getLogger(HyperDownStreamConnection.class);

	@Autowired
	protected IAsyncEventManager eventManager;

	@Autowired
	protected MarketDataManager marketDataManager;
	
	private IQuoteChecker quoteChecker;

    private boolean checkSingleSide;

	private String id = "HyperMarket" + "-" + IdGenerator.getInstance().getNextID();
	private Map<String, Map<String, ChildOrder>> orders = new ConcurrentHashMap<String, Map<String, ChildOrder>>();
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	private IDownStreamListener listener;
	private boolean cancel;
	private boolean reject;
	
	private class HyperEnterOrderEvent extends AsyncEvent {
		private ChildOrder order;

		public HyperEnterOrderEvent(ChildOrder order) {
			super();
			this.order = order;
		}

		public ChildOrder getOrder() {
			return order;
		}
	}
	
	private class HyperAmendOrderEvent extends AsyncEvent {
		private ChildOrder order;
		private Map<String, Object> fields;
		
		public HyperAmendOrderEvent(ChildOrder order, Map<String, Object> fields) {
			super();
			this.order = order;
			this.fields = fields;
		}

		public ChildOrder getOrder() {
			return order;
		}
		public Map<String, Object> getFields() {
			return fields;
		}
	}
	
	private class HyperCancelOrderEvent extends AsyncEvent {
		private ChildOrder order;

		public HyperCancelOrderEvent(ChildOrder order) {
			super();
			this.order = order;
		}

		public ChildOrder getOrder() {
			return order;
		}
	}
	
	private void acceptOrder(ChildOrder order) {
		Map<String, ChildOrder> list = orders.get(order.getSymbol());
		if(null == list) {
			list = new ConcurrentHashMap<String, ChildOrder>();
			orders.put(order.getSymbol(), list);
		}
		list.put(order.getId(), order);
		order.setOrdStatus(OrdStatus.NEW);
		listener.onOrder(ExecType.NEW, order, null, null);		
	}
	
	private void fillOrder(Quote quote, ChildOrder order) {
		double tradePrice = order.getSide().isBuy()?quote.getAsk():quote.getBid();
		if(PriceUtils.isZero(tradePrice) && order.getType() == ExchangeOrderType.MARKET ) {
			order.setOrdStatus(OrdStatus.REJECTED);
			listener.onOrder(ExecType.REJECTED, order, null, "No market for market order");
		}
			
		order.setAvgPx(tradePrice);
		order.setCumQty(order.getQuantity());
		order.setModified(Clock.getInstance().now());
		order.setOrdStatus(OrdStatus.FILLED);
		
		Execution execution = new Execution(order.getSymbol(), 
				order.getSide(), order.getQuantity(), 
				tradePrice, order.getId(), order.getParentOrderId(), 
				order.getStrategyId(),
				IdGenerator.getInstance().getNextID() + "T",
				order.getUser(), order.getAccount(), order.getRoute());

		listener.onOrder(ExecType.FILLED, order, execution, null);
	}
	
	private void deleteOrder(ChildOrder order) {
		order.setOrdStatus(OrdStatus.CANCELED);
		listener.onOrder(ExecType.CANCELED, order, null, null);
	}
	
	private void rejectOrder(ChildOrder order) {
		order.setOrdStatus(OrdStatus.REJECTED);
		listener.onOrder(ExecType.REJECTED, order, null, null);
	}
	
	private boolean quoteIsValid(Quote quote) {
		if(null != quoteChecker && !quoteChecker.check(quote))
			return false;
		
		return !quote.isStale();
	}

    private boolean checkOrder(Quote quote, ChildOrder order){
        if(order.getSide().isBuy()) {
            return PriceUtils.validPrice(quote.getAsk()) &&
                    PriceUtils.EqualGreaterThan(order.getPrice(), quote.getAsk());
        } else {
            return PriceUtils.validPrice(quote.getBid()) &&
                    PriceUtils.EqualLessThan(order.getPrice(), quote.getBid());
        }
    }
    
    private boolean checkVolume(Quote quote, ChildOrder order){
    	if(order.getSide().isBuy()){
    		return quote.getAskVol() >= 0 && PriceUtils.validPrice(quote.getAsk());
    	} else {
    		return quote.getBid() >= 0 && PriceUtils.validPrice(quote.getBid());
    	}
    }

	private boolean isMarketable(Quote quote, ChildOrder order) {
		if(!quoteIsValid(quote))
			return false;

		if (order.getType() == ExchangeOrderType.MARKET) {
			if (checkSingleSide)
	            return checkVolume(quote, order);
			else 
				return true;
		}
		
		return checkOrder(quote, order);
	}

	@Override
	public void init() throws Exception {
		super.init();
		if(null != this.getThread())
			this.getThread().setName("HyperMarket-Thread");
		log.info("Initialising...");
	}

	@Override
	public void uninit() {
		super.uninit();
		log.info("uninitialising");
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean getState() {
		return true;
	}

	@Override
	public IDownStreamSender setListener(IDownStreamListener listener) throws DownStreamException {
		if(listener != null && this.listener != null)
			throw new DownStreamException("Only support one listener per downstream connection",ErrorMessage.DOWN_STREAM_TOO_MANY_LISTENER);
		
		this.listener = listener;
		if(null != this.listener)
			this.listener.onState(true);
		return this;
	}

	// IDownStreamSender
	@Override
	public void subscribeToEvents() {
		log.info("subscribe to events...");
		subscribeToEvent(QuoteEvent.class, null);
	}

	@Override
	public IAsyncEventManager getEventManager() {
		return eventManager;
	}
	
	private void processQuote(Quote quote) {
		quotes.put(quote.getSymbol(), quote);
		Map<String, ChildOrder> list = orders.get(quote.getSymbol());
		ArrayList<ChildOrder> matched = new ArrayList<ChildOrder>();
		if(null != list) {
			for(Entry<String, ChildOrder> entry: list.entrySet()) {
				if(isMarketable(quote, entry.getValue())) {
					matched.add(entry.getValue());
				}
			}
		}
		
		for(ChildOrder order: matched) {
			list.remove(order.getId());
			fillOrder(quote, order);
		}
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		processQuote(event.getQuote());
	}
	
	public void processHyperEnterOrderEvent(HyperEnterOrderEvent event) {
		ChildOrder order = event.getOrder();
		if(this.isReject()) {
			rejectOrder(order);
			return;
		}
		
		if(this.isCancel()) {
			deleteOrder(order);
			return;
		}
			
		Quote quote = quotes.get(order.getSymbol());
		if(null == quote) {
			acceptOrder(order);
			QuoteSubEvent sub = new QuoteSubEvent(this.id, null, order.getSymbol());
			eventManager.sendEvent(sub);
		} else {
			// for simplistic sake we don't check volume here
			if (isMarketable(quote, order)) {
				fillOrder(quote, order);
			} else {
				TimeInForce tif = order.get(TimeInForce.class, OrderField.TIF.value());
				if(null != tif && 
				 (tif.equals(TimeInForce.FILL_OR_KILL) || tif.equals(TimeInForce.IMMEDIATE_OR_CANCEL))) {
					deleteOrder(order);
				} else {
					acceptOrder(order);
				}				
			}
		}
	}

	@Override
	public void newOrder(ChildOrder order) throws DownStreamException {
		this.onEvent(new HyperEnterOrderEvent(order));
	}
	
	public void processHyperAmendOrderEvent(HyperAmendOrderEvent event)  {
		ChildOrder order = event.getOrder();
		Map<String, Object> fields = event.getFields();
		Map<String, ChildOrder> list = orders.get(order.getSymbol());
		if(null == list) {
			listener.onOrder(ExecType.REJECTED, order, null, "Unable to find this child order: " + order.getId());
			return;
		}
		
		ChildOrder existing = list.get(order.getId());
		if(null == existing) {
			listener.onOrder(ExecType.REJECTED, order, null, "Unable to find this child order: " + order.getId());
			return;
		}
		
		order = existing;
		Object oQty = fields.get(OrderField.QUANTITY.value());
		if(oQty != null) {
			double qty = (Double)oQty;
			order.setQuantity(qty);
		}
		
		Object oPrice = fields.get(OrderField.PRICE.value());
		if(oPrice != null) {
			double price = (Double)oPrice;
			order.setPrice(price);
		}
		
		Quote quote = quotes.get(order.getSymbol());
		if(null == quote) {
			return;
		}
		
		if(isMarketable(quote, order)) {
			list.remove(order.getId());
			fillOrder(quote, order);
		} else {
			order.setOrdStatus(OrdStatus.REPLACED);
			listener.onOrder(ExecType.REPLACE, order, null, null);
		}
	}

	@Override
	public void amendOrder(ChildOrder order, Map<String, Object> fields)
			throws DownStreamException {
		this.onEvent(new HyperAmendOrderEvent(order, fields));
	}

	public void processHyperCancelOrderEvent(HyperCancelOrderEvent event) {
		ChildOrder order = event.getOrder();

		Map<String, ChildOrder> list = orders.get(order.getSymbol());
		if(null == list) {
			listener.onOrder(ExecType.REJECTED, order, null, "Unable to find this child order: " + order.getId());
			return;
		}
		
		ChildOrder existing = list.get(order.getId());
		if(null == existing) {
			listener.onOrder(ExecType.REJECTED, order, null, "Unable to find this child order: " + order.getId());
			return;
		}
		
		list.remove(order.getId());
		order.setOrdStatus(OrdStatus.CANCELED);
		listener.onOrder(ExecType.CANCELED, order, null, null);
	}
	
	@Override
	public void cancelOrder(ChildOrder order) throws DownStreamException {
		this.onEvent(new HyperCancelOrderEvent(order));
	}

	// getters and setters
	public IQuoteChecker getQuoteChecker() {
		return quoteChecker;
	}

	public void setQuoteChecker(IQuoteChecker quoteChecker) {
		this.quoteChecker = quoteChecker;
	}

    public void setCheckSingleSide(boolean checkSingleSide) {
        this.checkSingleSide = checkSingleSide;
    }

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	public boolean isReject() {
		return reject;
	}

	public void setReject(boolean reject) {
		this.reject = reject;
	}
    
}
