package com.fdt.lts.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.apievent.obj.*;
import com.cyanspring.apievent.reply.*;
import com.cyanspring.apievent.request.*;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.RemoteAsyncEvent;
//import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.event.ClientSocketEventManager;
import com.cyanspring.transport.socket.ClientSocketService;
import com.fdt.lts.client.error.Error;
import com.fdt.lts.client.error.OrderChecker;

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

	public LtsApi(String host, int port) {
		if (host == null || host.trim().equals("") || port == 0) {
			log.error("Error, null host or port!");
			return;
		}

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
//				subscribeToEvent(ClosedPositionUpdateEvent.class, null);
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
		if (user == null || user.trim().equals("") || password == null
				|| password.trim().equals("") || subscribeSymbolList == null
				|| tAct == null) {
			log.error("Error, null user or password or subscribeSymbolList or tAct");
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
			sendEvent(new UserLoginEvent(getId(), null, user, password,
					IdGenerator.getInstance().getNextID()));
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
		Execution newExe = new Execution();
		newExe.setAccount(exe.getAccount());
		newExe.setCreated(exe.getCreated());
		newExe.setExecID(exe.getExecID());
		newExe.setId(exe.getId());
		newExe.setModified(exe.getModified());
		newExe.setOrderID(exe.getOrderID());
		newExe.setParentOrderID(exe.getParentOrderID());
		newExe.setPrice(exe.getPrice());
		newExe.setQuantity(exe.getQuantity());
		newExe.setServerID(exe.getServerID());
		newExe.setSide(exe.getSide());
		newExe.setStrategyID(exe.getStrategyID());
		newExe.setSymbol(exe.getSymbol());
		newExe.setUser(exe.getUser());
		accountInfo.addExecution(exe.getSymbol(), exe.getOrderID(), newExe);
	}

	private void setOpenPositionData(OpenPosition oPosition) {
		OpenPosition newPosition = new OpenPosition();
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
		accountInfo.addOpenPosition(oPosition.getSymbol(), oPosition.getId(),
				newPosition);
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
		tAdaptor.onAccountUpdate();
	}

	public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
		setOpenPositionData(event.getPosition());
	}

//	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
//		// No need implement in this version
//		// log.debug("Closed Position: " + event.getPosition());
//	}

	public void processQuoteEvent(QuoteEvent event) {
		tAdaptor.onQuote(event.getQuote());
	}

	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		if (!event.isOk()){
			tAdaptor.onError(Error.LOGIN_ERROR.getCode(), Error.LOGIN_ERROR.getMsg());
			return;
		}
		log.info("Login Success.");
		sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
	}

	public void processStrategySnapshotEvent(StrategySnapshotEvent event) {
		for (Order order : event.getOrders()) {
			orderMap.put(order.getId(), setOrderData(order));
		}
		tAdaptor.onStart();
		for (String symbol : subQuoteLst) {
			sendEvent(new QuoteSubEvent(getId(), null, symbol));
		}
	}

	private Order setOrderData(Order order) {
		Order newOrder = new Order();
		newOrder.setId(order.getId());
		newOrder.setPrice(order.getPrice());
		newOrder.setQuantity(order.getQuantity());
		newOrder.setSide(OrderSide.valueOf(order.getSide().toString()));
		newOrder.setStopLossPrice(order.getStopLossPrice());
		newOrder.setSymbol(order.getSymbol());
		newOrder.setType(order.getType());
		newOrder.setState(order.getState());
		newOrder.setStatus(order.getStatus());
		return newOrder;
	}

	public void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		if (!event.isOk()) {
			tAdaptor.onError(Error.NEW_ORDER_ERROR.getCode(),
					Error.NEW_ORDER_ERROR.getMsg());
		} else {
			Order newOrder = setOrderData(event.getOrder());
			if (orderMap.containsKey(newOrder.getId()))
				orderMap.remove(newOrder.getId());
			orderMap.put(newOrder.getId(), newOrder);
			tAdaptor.onNewOrderReply(newOrder);
		}
	}

	public void processAmendParentOrderReplyEvent(
			AmendParentOrderReplyEvent event) {
		if (!event.isOk()) {
			tAdaptor.onError(Error.AMEND_ORDER_ERROR.getCode(),
					Error.AMEND_ORDER_ERROR.getMsg());
		} else {
			Order amendOrder = setOrderData(event.getOrder());
			if (orderMap.containsKey(amendOrder.getId()))
				orderMap.remove(amendOrder.getId());
			orderMap.put(amendOrder.getId(), amendOrder);
			tAdaptor.onAmendOrderReply(amendOrder);
		}
	}

	public void processCancelParentOrderReplyEvent(
			CancelParentOrderReplyEvent event) {
		if (!event.isOk()) {
			tAdaptor.onError(Error.CANCEL_ORDER_ERROR.getCode(),
					Error.CANCEL_ORDER_ERROR.getMsg());
		} else {
			Order cancelOrder = setOrderData(event.getOrder());
			if (orderMap.containsKey(cancelOrder.getId()))
				orderMap.remove(cancelOrder.getId());
			tAdaptor.onCancelOrderReply(cancelOrder);
		}
	}

	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		Order updateOrder = setOrderData(event.getOrder());
		if (orderMap.containsKey(updateOrder.getId()))
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
		if(!OrderChecker.checkNewOrder(order)){
			tAdaptor.onError(Error.NEW_ORDER_VAR_ERROR.getCode(), Error.NEW_ORDER_VAR_ERROR.getMsg());
			return;
		}
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();

		fields.put(OrderField.SYMBOL.value(), order.getSymbol());
		fields.put(OrderField.SIDE.value(), order.getSide());
		fields.put(OrderField.TYPE.value(), order.getType());
		fields.put(OrderField.PRICE.value(), order.getPrice());
		fields.put(OrderField.QUANTITY.value(), order.getQuantity());

		// fields.put(OrderField.SYMBOL.value(), "AUDUSD");
		// fields.put(OrderField.SIDE.value(), OrderSide.Buy);
		// fields.put(OrderField.TYPE.value(), OrderType.Limit);
		// fields.put(OrderField.PRICE.value(), 0.700);
		// fields.put(OrderField.QUANTITY.value(), 20000.0); 

		fields.put(OrderField.STRATEGY.value(), "SDMA");
		fields.put(OrderField.USER.value(), user);
		fields.put(OrderField.ACCOUNT.value(), account);
		enterOrderEvent = new EnterParentOrderEvent(getId(), null, fields,
				IdGenerator.getInstance().getNextID(), false);
		sendEvent(enterOrderEvent);
	}

	@Override
	public void putStopOrder(Order order) {
		if(!OrderChecker.checkStopOrder(order)){
			tAdaptor.onError(Error.New_STOP_ORDER_VAR_ERROR.getCode(), Error.New_STOP_ORDER_VAR_ERROR.getMsg());
			return;
		}
		// SDMA
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();

		fields.put(OrderField.SYMBOL.value(), order.getSymbol());
		fields.put(OrderField.SIDE.value(), order.getSide());
		fields.put(OrderField.TYPE.value(), order.getType());
		fields.put(OrderField.QUANTITY.value(), order.getQuantity());
		fields.put(OrderField.STOP_LOSS_PRICE.value(), order.getStopLossPrice());

		// fields.put(OrderField.SYMBOL.value(), "AUDUSD");
		// fields.put(OrderField.SIDE.value(), OrderSide.Buy);
		// fields.put(OrderField.TYPE.value(), OrderType.Market);
		// fields.put(OrderField.QUANTITY.value(), 20000.0); 
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
		if(!OrderChecker.checkAmendOrder(order)){
			tAdaptor.onError(Error.AMEND_ORDER_VAR_ERROR.getCode(), Error.AMEND_ORDER_VAR_ERROR.getMsg());
			return;
		}
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.PRICE.value(), order.getPrice());
		fields.put(OrderField.QUANTITY.value(), order.getQuantity());
		AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(),
				null, order.getId(), fields, IdGenerator.getInstance()
						.getNextID());
		sendEvent(amendEvent);
	}

	@Override
	public void putCancelOrder(Order order) {
		if(!OrderChecker.checkCancelOrder(order)){
			tAdaptor.onError(Error.CANCEL_ORDER_VAR_ERROR.getCode(), Error.CANCEL_ORDER_VAR_ERROR.getMsg());
			return;
		}
		CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(
				getId(), null, order.getId(), false, IdGenerator.getInstance()
						.getNextID());
		sendEvent(cancelEvent);
	}

	@Override
	public void terminate() {
		eventProcessor.uninit();
		eventManager.uninit();
	}

	private String getId() {
		return user;
	}

}
