package com.fdt.lts.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.business.Execution;
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
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.event.ClientSocketEventManager;
import com.cyanspring.transport.socket.ClientSocketService;
import com.fdt.lts.client.error.Error;
import com.fdt.lts.client.obj.AccountInfo;
import com.fdt.lts.client.obj.Order;
import com.fdt.lts.client.obj.OrderSide;
import com.fdt.lts.client.obj.OrderType;
import com.fdt.lts.client.obj.QuoteData;

public final class LtsApi implements ITrade {
	private static Logger log = LoggerFactory.getLogger(LtsApi.class);
	private String user;
	private String account;
	private String suffix = "-FX";
	private String password;
	private AccountInfo accountInfo;
	private Map<String, Order> orderMap;
	private List<String> subQuoteLst;
	private TradeAdaptor tAdaptor;

	private IRemoteEventManager eventManager;
	private AsyncEventProcessor eventProcessor;
	
	public LtsApi(String host, int port){
		ClientSocketService socketService = new ClientSocketService();
		socketService.setHost(host);
		socketService.setPort(port);
		eventManager = new ClientSocketEventManager(socketService);
		
		eventProcessor = new AsyncEventProcessor() {

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
		
		
	}

	private void init() throws Exception {
		accountInfo = new AccountInfo();
		accountInfo.newAccount();
		tAdaptor.setAccountInfo(accountInfo);
		orderMap = new ConcurrentHashMap<String, Order>();
		tAdaptor.setOrderMap(orderMap);
		tAdaptor.setAdaptor(this);
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("LtsApi");

		eventManager.init(null, null);
	}

	public void start(String user, String password,
			List<String> subscribeSymbolList, TradeAdaptor tAct) {
		if(user == null || password == null || subscribeSymbolList == null || tAct == null){
			tAdaptor.onError(Error.INIT_ERROR.getCode(),
					Error.INIT_ERROR.getMsg());
			return;
		}
		this.user = user;
		this.password = password;
		this.account = user + suffix;
		subQuoteLst = subscribeSymbolList;
		this.tAdaptor = tAct;
		try {
			init();
		} catch (Exception e) {
			tAdaptor.onError(Error.INIT_ERROR.getCode(),
					Error.INIT_ERROR.getMsg());
		}
	}

	private void sendEvent(RemoteAsyncEvent event) {
		try {
			eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			tAdaptor.onError(Error.SEND_ERROR.getCode(),
					Error.SEND_ERROR.getMsg());
		}
	}

	public void processServerReadyEvent(ServerReadyEvent event) {
		if (event.isReady()) {
			log.info("> Server is connected. Starting Login...");
			if (user != null && password != null)
				sendEvent(new UserLoginEvent(getId(), null, user, password,
						IdGenerator.getInstance().getNextID()));
			else {
				tAdaptor.onError(Error.LOGIN_ERROR.getCode(),
						Error.LOGIN_ERROR.getMsg());
			}
		} else {
			tAdaptor.onError(Error.SERVER_ERROR.getCode(),
					Error.SERVER_ERROR.getMsg());
		}
	}

	public void processAccountSnapshotReplyEvent(AccountSnapshotReplyEvent event) {
		setAccountData(event.getAccount());

		for (OpenPosition oPosition : event.getOpenPositions()) {
			setOpenPositionData(oPosition);
		}
		for (Execution exe : event.getExecutions()) {
			setExecutionData(exe);
		}
		sendEvent(new StrategySnapshotRequestEvent(account, null, null));
	}

	private void setExecutionData(Execution exe) {
		AccountInfo.Execution newExe = accountInfo.new Execution();
		newExe.setAccount(exe.getAccount());
		newExe.setCreated(exe.getCreated());
		newExe.setExecID(exe.getExecId());
		newExe.setId(exe.getId());
		newExe.setModified(exe.getModified());
		newExe.setOrderID(exe.getOrderId());
		newExe.setParentOrderID(exe.getParentOrderId());
		newExe.setPrice(exe.getPrice());
		newExe.setQuantity(new Double(exe.getQuantity()).longValue());
		newExe.setServerID(exe.getServerId());
		newExe.setSide(exe.getSide().toString());
		newExe.setStrategyID(exe.getStrategyId());
		newExe.setSymbol(exe.getSymbol());
		newExe.setUser(exe.getUser());
		accountInfo.addExecution(exe.getSymbol(), exe.getOrderId(), newExe);
	}

	private void setOpenPositionData(OpenPosition oPosition) {
		AccountInfo.OpenPosition newPosition = accountInfo.new OpenPosition();
		newPosition.setAccount(oPosition.getAccount());
		newPosition.setAcPnL(oPosition.getAcPnL());
		newPosition.setCreated(oPosition.getCreated());
		newPosition.setId(oPosition.getId());
		newPosition.setMargin(oPosition.getMargin());
		newPosition.setPnL(oPosition.getPnL());
		newPosition.setPrice(oPosition.getPrice());
		newPosition.setQty(oPosition.getQty());
		newPosition.setSymbol(oPosition.getSymbol());
		newPosition.setUser(oPosition.getUser());	
		accountInfo.addOpenPosition(oPosition.getSymbol(), oPosition.getId(), newPosition);
	}

	private void setAccountData(Account account) {
		accountInfo.getAccount().setAllTimePnL(account.getAllTimePnL());
		accountInfo.getAccount().setCash(account.getCash());
		accountInfo.getAccount().setCurrency(account.getCurrency());
		accountInfo.getAccount().setDailyPnL(account.getDailyPnL());
		accountInfo.getAccount().setMargin(account.getMargin());
		accountInfo.getAccount().setPnL(account.getPnL());
		accountInfo.getAccount().setUrPnL(account.getUrPnL());
		accountInfo.getAccount().setValue(account.getValue());		
	}

	public void processAccountUpdateEvent(AccountUpdateEvent event) {
		setAccountData(event.getAccount());
	}

	public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
		setOpenPositionData(event.getPosition());
	}

	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
		// No need implement in this version
//		log.debug("Closed Position: " + event.getPosition());
	}

	public void processQuoteEvent(QuoteEvent event) {
		QuoteData quote = setQuoteData(event.getQuote());
		tAdaptor.onQuote(quote);		
	}

	private QuoteData setQuoteData(Quote iquote) {
		QuoteData quote = new QuoteData();
		quote.setSymbol(iquote.getSymbol());
		quote.setBid(iquote.getBid());
		quote.setAsk(iquote.getAsk());
		quote.setLast(iquote.getLast());
		quote.setHigh(iquote.getHigh());
		quote.setLow(iquote.getLow());
		quote.setOpen(iquote.getOpen());
		quote.setClose(iquote.getClose());
		quote.setTimeStamp(iquote.getTimeStamp());
		quote.setTimeSent(iquote.getTimeSent());
		quote.setStale(iquote.isStale());
		return quote;
	}

	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		log.info("Login is" + event.isOk() + ", " + event.getMessage()
				+ ", Last login:" + event.getUser().getLastLogin());
		if (!event.isOk())
			return;
			
		sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
	}

	public void processStrategySnapshotEvent(StrategySnapshotEvent event) {
		for (ParentOrder order : event.getOrders()) {
			orderMap.put(order.getId(), setOrderData(order));
		}
		tAdaptor.onStart();
		for (String symbol : subQuoteLst) {
			sendEvent(new QuoteSubEvent(getId(), null, symbol));
		}
	}

	private Order setOrderData(ParentOrder order) {
		Order newOrder = new Order();
		newOrder.setId(order.getId());
		newOrder.setPrice(order.getPrice());
		newOrder.setQuantity(new Double(order.getQuantity()).longValue());
		newOrder.setSide(OrderSide.valueOf(order.getSide().toString()));
		newOrder.setStopLossPrice(order.get(double.class, OrderField.STOP_LOSS_PRICE.value()));
		newOrder.setSymbol(order.getSymbol());
		newOrder.setType(OrderType.valueOf(order.getOrderType().toString()));
		newOrder.setState(order.getState().toString());
		newOrder.setStatus(order.getOrdStatus().toString());
		return newOrder;
	}

	public void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		if (!event.isOk()) {
			tAdaptor.onError(Error.NEW_ORDER_ERROR.getCode(), Error.NEW_ORDER_ERROR.getMsg());			
		} else {
			Order newOrder = setOrderData(event.getOrder());
			if(orderMap.containsKey(newOrder.getId()))
				orderMap.remove(newOrder.getId());
			orderMap.put(newOrder.getId(), newOrder);
			tAdaptor.onNewOrderReply(newOrder);
		}
	}

	public void processAmendParentOrderReplyEvent(
			AmendParentOrderReplyEvent event) {
		if (!event.isOk()) {
			tAdaptor.onError(Error.AMEND_ORDER_ERROR.getCode(), Error.AMEND_ORDER_ERROR.getMsg());
		} else {
			Order amendOrder = setOrderData(event.getOrder());
			if(orderMap.containsKey(amendOrder.getId()))
				orderMap.remove(amendOrder.getId());
			orderMap.put(amendOrder.getId(), amendOrder);
			tAdaptor.onAmendOrderReply(amendOrder);
		}
	}

	public void processCancelParentOrderReplyEvent(
			CancelParentOrderReplyEvent event) {
		if (!event.isOk()) {
			tAdaptor.onError(Error.CANCEL_ORDER_ERROR.getCode(), Error.CANCEL_ORDER_ERROR.getMsg());
		} else {
			Order cancelOrder = setOrderData(event.getOrder());
			if(orderMap.containsKey(cancelOrder.getId()))
				orderMap.remove(cancelOrder.getId());
			tAdaptor.onCancelOrderReply(cancelOrder);
		}
	}

	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		Order updateOrder = setOrderData(event.getOrder());
		if(orderMap.containsKey(updateOrder.getId()))
			orderMap.remove(updateOrder.getId());
		orderMap.put(updateOrder.getId(), updateOrder);
		tAdaptor.onOrderUpdate(updateOrder);
	}

	public void processSystemErrorEvent(SystemErrorEvent event) {
		tAdaptor.onError(event.getErrorCode(), event.getMessage());
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
	public void putNewOrder(Order order) {
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();

		fields.put(OrderField.SYMBOL.value(), order.getSymbol());
		fields.put(OrderField.SIDE.value(), order.getSide());
		fields.put(OrderField.TYPE.value(), order.getType());
		fields.put(OrderField.PRICE.value(), order.getPrice());
		fields.put(OrderField.QUANTITY.value(), new Double(order.getQuantity()));

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

		fields.put(OrderField.SYMBOL.value(), order.getSymbol());
		fields.put(OrderField.SIDE.value(), order.getSide());
		fields.put(OrderField.TYPE.value(), order.getType());
		fields.put(OrderField.QUANTITY.value(), new Double(order.getQuantity()));
		fields.put(OrderField.STOP_LOSS_PRICE.value(), order.getStopLossPrice());

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
	public void putAmendOrder(Order order) {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.PRICE.value(), order.getPrice());
		fields.put(OrderField.QUANTITY.value(), new Double(order.getQuantity())); // note: you must put
																			 // xxx.0 to tell
																			 // java this is a
																			 // double type
																			 // here!!
		AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(),
				null, order.getId(), fields, IdGenerator.getInstance().getNextID());
		sendEvent(amendEvent);
	}

	@Override
	public void putCancelOrder(Order order) {
		CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(
				getId(), null, order.getId(), false, IdGenerator.getInstance()
						.getNextID());
		sendEvent(cancelEvent);
	}

	private String getId() {
		return user;
	}
}