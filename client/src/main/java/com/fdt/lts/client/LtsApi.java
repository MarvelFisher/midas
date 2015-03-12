package com.fdt.lts.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.UserLoginEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.order.StrategySnapshotEvent;
import com.cyanspring.common.event.order.StrategySnapshotRequestEvent;
import com.cyanspring.common.event.system.SystemErrorEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.event.ClientSocketEventManager;
import com.fdt.lts.client.obj.AccountInfo;
import com.fdt.lts.client.obj.Order;
import com.fdt.lts.client.obj.Quote;

public abstract class LtsApi implements ITrade{	
	private static Logger log = LoggerFactory.getLogger(LtsApi.class);
	
	protected abstract void onStrategySnapshot();
	protected abstract void onParentOrderUpdate();
	protected abstract void onSystemError();
	
	private String user;
	private String account;
	private String password;
	private List<String> subQuoteLst;
	private TradeAdaptor tAct;
		
	@Autowired
	private IRemoteEventManager eventManager = new ClientSocketEventManager();
	protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
	
			@Override
			public void subscribeToEvents() {
				subscribeToEvent(ServerReadyEvent.class, null);
				subscribeToEvent(QuoteEvent.class, null);
				subscribeToEvent(EnterParentOrderReplyEvent.class, user);
				subscribeToEvent(AmendParentOrderReplyEvent.class, user);
				subscribeToEvent(CancelParentOrderReplyEvent.class, user);
				subscribeToEvent(ParentOrderUpdateEvent.class, null);
				subscribeToEvent(StrategySnapshotEvent.class, null);
				subscribeToEvent(UserLoginReplyEvent.class, null);
				subscribeToEvent(AccountSnapshotReplyEvent.class, null);
				subscribeToEvent(AccountUpdateEvent.class, null);
				subscribeToEvent(OpenPositionUpdateEvent.class, null);
				subscribeToEvent(ClosedPositionUpdateEvent.class, null);
				subscribeToEvent(SystemErrorEvent.class, null);
			}
	
			@Override
			public IAsyncEventManager getEventManager() {
				return eventManager;
			}
			
		};

	private void init() throws Exception {
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("LtsApiAdaptor");
		
		eventManager.init(null, null);
	}
	
	public void init(List<String> subscribeSymbolList, TradeAdaptor tAct) throws Exception{
		subQuoteLst = subscribeSymbolList;
		this.tAct = tAct;
		init();
	}

	protected void sendEvent(RemoteAsyncEvent event) {
		try {
			eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processServerReadyEvent(ServerReadyEvent event) {
		if(event.isReady()) {
			log.info("> Server is connected. Starting Login...");
			if(user != null && password != null)
				sendEvent(new UserLoginEvent(getId(), null, user, password, IdGenerator.getInstance().getNextID()));
			else
				log.error("> User ID or Password is empty.");
		}
	}

	public void processAccountSnapshotReplyEvent(AccountSnapshotReplyEvent event) {
		if(tAct.getAccountInfo() == null){			
			AccountInfo aInfo = new AccountInfo();
			tAct.setAccountInfo(aInfo);
		}
		
		// Add AccountInfo data
		
//		log.debug("### Account Snapshot Start ###");
//		log.debug("Account: " + event.getKey());
//		log.debug("Account settings: " + event.getAccountSetting());
//		log.debug("Open positions: " + event.getOpenPositions());
//		log.debug("Closed positions: " + event.getClosedPositions());
//		log.debug("Trades :" + event.getExecutions());
//		log.debug("### Account Snapshot End ###");
	}

	public void processAccountUpdateEvent(AccountUpdateEvent event) {
		// Add AccountInfo data update		
		log.debug("Account: " + event.getAccount());
	}

	public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
		// Add AccountInfo data update
		log.debug("Position: " + event.getPosition());
	}

	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
		// Add AccountInfo data update
		log.debug("Closed Position: " + event.getPosition());
	}

	public void processQuoteEvent(QuoteEvent event) {
		Quote quote = new Quote();
		quote.symbol = event.getQuote().getSymbol();
		quote.bid = event.getQuote().getBid();
		quote.ask = event.getQuote().getAsk();
		quote.last = event.getQuote().getLast();
		quote.high = event.getQuote().getHigh();
		quote.low = event.getQuote().getLow();
		quote.open = event.getQuote().getOpen();
		quote.close = event.getQuote().getClose();
		quote.timeStamp = event.getQuote().getTimeStamp();
		quote.timeSent = event.getQuote().getTimeSent();
		quote.stale = event.getQuote().isStale();
		if(tAct != null){
			tAct.onQuote(quote);			
		}else{
			log.info("Get Quote data: " + quote);			
		}
	}

	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		
		if(!event.isOk())
			return;
		log.info("Login is" + event.isOk() + ", " + event.getMessage() + ", Last login:" + event.getUser().getLastLogin());
		for(String symbol: subQuoteLst){
			sendEvent(new QuoteSubEvent(getId(), null, symbol));
		}
		
		sendEvent(new StrategySnapshotRequestEvent(account, null, null));
		sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
//		sendEvent(getEnterOrderEvent());
	}

	public void processStrategySnapshotEvent(StrategySnapshotEvent event) {
		List<ParentOrder> orders = event.getOrders();
		log.debug("### Start parent order list ###");
		for(ParentOrder order: orders) {
			log.debug("ParentOrder: " + order);
		}
		log.debug("### End parent order list ###");
	}

	public void processEnterParentOrderReplyEvent(EnterParentOrderReplyEvent event) {
		if(!event.isOk()) {
			log.debug("Received EnterParentOrderReplyEvent(NACK): " + event.getMessage());
		} else {
			log.debug("Received EnterParentOrderReplyEvent(ACK)");
			tAct.onTradeOrderReply();
		} 
	}

	public void processAmendParentOrderReplyEvent(AmendParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Received AmendParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
			tAct.onAmendOrderReply();
		} else {
			log.debug("Received AmendParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public void processCancelParentOrderReplyEvent(CancelParentOrderReplyEvent event) {
		if(event.isOk()) {
			tAct.onCancelOrderReply();
		} else {
			log.debug("Received CancelParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		log.debug("Received ParentOrderUpdateEvent: " + event.getExecType() + ", order: " + event.getOrder());
	}	

	public void processSystemErrorEvent(SystemErrorEvent event) {
		log.error("Error code: " + event.getErrorCode() + " - " + event.getMessage());
		onSystemError();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public void putOrder(Order order) {
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
	
		fields.put(OrderField.SYMBOL.value(), order.symbol);
		fields.put(OrderField.SIDE.value(), order.side);
		fields.put(OrderField.TYPE.value(), order.type);
		fields.put(OrderField.PRICE.value(), order.value);
		fields.put(OrderField.QUANTITY.value(), new Double(order.quantity));
	
		// fields.put(OrderField.SYMBOL.value(), "AUDUSD");
		// fields.put(OrderField.SIDE.value(), OrderSide.Buy);
		// fields.put(OrderField.TYPE.value(), OrderType.Limit);
		// fields.put(OrderField.PRICE.value(), 0.700);
		// fields.put(OrderField.QUANTITY.value(), 20000.0); // note: you must
		// put xxx.0 to tell java this is a double type here!!
	
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		fields.put(OrderField.USER.value(), user);
		fields.put(OrderField.ACCOUNT.value(), account);
		enterOrderEvent = new EnterParentOrderEvent(getId(), null, fields,
				IdGenerator.getInstance().getNextID(), false);
		sendEvent(enterOrderEvent);
	}
	@Override
	public void putStopOrder(Order order) {
		// SDMA
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
	
		fields.put(OrderField.SYMBOL.value(), order.symbol);
		fields.put(OrderField.SIDE.value(), order.side);
		fields.put(OrderField.TYPE.value(), order.type);
		fields.put(OrderField.QUANTITY.value(), new Double(order.quantity));
		fields.put(OrderField.STOP_LOSS_PRICE.value(), order.stopLossPrice);
	
		// fields.put(OrderField.SYMBOL.value(), "AUDUSD");
		// fields.put(OrderField.SIDE.value(), OrderSide.Buy);
		// fields.put(OrderField.TYPE.value(), OrderType.Market);
		// fields.put(OrderField.QUANTITY.value(), 20000.0); // note: you must
		// put xxx.0 to tell java this is a double type here!!
		// fields.put(OrderField.STOP_LOSS_PRICE.value(), 0.92);
	
		fields.put(OrderField.STRATEGY.value(), "STOP");
		fields.put(OrderField.USER.value(), user);
		fields.put(OrderField.ACCOUNT.value(), account);
		enterOrderEvent = new EnterParentOrderEvent(getId(), null, fields,
				IdGenerator.getInstance().getNextID(), false);
		sendEvent(enterOrderEvent);
	}
	@Override
	public void putAmendOrder(Order order){
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.PRICE.value(), 0.81);
		fields.put(OrderField.QUANTITY.value(), 3000.0); // note: you must put xxx.0 to tell java this is a double type here!!

		AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(), null, 
				order.id, fields, IdGenerator.getInstance().getNextID());
		sendEvent(amendEvent);
	}
	@Override
	public void putCancelOrder(Order order){
		CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(getId(), null, 
				order.id, false, IdGenerator.getInstance().getNextID());
		sendEvent(cancelEvent);
	}
	private String getId(){
		return user;
	}
}