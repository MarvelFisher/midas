package com.fdt.lts.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

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

public class LtsApiAdaptor {
	private static Logger log = LoggerFactory.getLogger(LtsApiAdaptor.class);
	private String user = "test1";
	private String account = "test1-FX";
	private String password = "xxx";
	@Autowired
	private IRemoteEventManager eventManager = new ClientSocketEventManager();
	
	protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(ServerReadyEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(EnterParentOrderReplyEvent.class, getId());
			subscribeToEvent(AmendParentOrderReplyEvent.class, getId());
			subscribeToEvent(CancelParentOrderReplyEvent.class, getId());
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
	
	private String getId() {
		return user;
	}
	
	public void init() throws Exception {
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("LtsApiAdaptor");
		
		eventManager.init(null, null);
	}

	
	private void sendEvent(RemoteAsyncEvent event) {
		try {
			eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processServerReadyEvent(ServerReadyEvent event) {
		log.debug("Received ServerReadyEvent: " + event.getSender() + ", " + event.isReady());
		if(event.isReady()) {
			sendEvent(new UserLoginEvent(getId(), null, user, password, IdGenerator.getInstance().getNextID()));
		}
	}

	public void processAccountSnapshotReplyEvent(AccountSnapshotReplyEvent event) {
		log.debug("### Account Snapshot Start ###");
		log.debug("Account: " + event.getKey());
		log.debug("Account settings: " + event.getAccountSetting());
		log.debug("Open positions: " + event.getOpenPositions());
		log.debug("Closed positions: " + event.getClosedPositions());
		log.debug("Trades :" + event.getExecutions());
		log.debug("### Account Snapshot End ###");
	}
	
	public void processAccountUpdateEvent(AccountUpdateEvent event) {
		log.debug("Account: " + event.getAccount());
	}
	
	public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
		log.debug("Position: " + event.getPosition());
	}
	
	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
		log.debug("Closed Position: " + event.getPosition());
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		log.debug("Received QuoteEvent: " + event.getKey() + ", " + event.getQuote());
	}
	
	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		log.debug("User login is " + event.isOk() + ", " + event.getMessage() + ", lastLogin: " + event.getUser().getLastLogin() );
		
		if(!event.isOk())
			return;
		
		sendEvent(new QuoteSubEvent(getId(), null, "AUDUSD"));
		sendEvent(new QuoteSubEvent(getId(), null, "USDJPY"));
		sendEvent(new StrategySnapshotRequestEvent(account, null, null));
		sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
		sendEvent(getEnterOrderEvent());
	}

	public void processStrategySnapshotEvent(StrategySnapshotEvent event) {
		List<ParentOrder> orders = event.getOrders();
		log.debug("### Start parent order list ###");
		for(ParentOrder order: orders) {
			log.debug("ParentOrder: " + order);
		}
		log.debug("### End parent order list ###");
	}

	public void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		if(!event.isOk()) {
			log.debug("Received EnterParentOrderReplyEvent(NACK): " + event.getMessage());
		} else {
			log.debug("Received EnterParentOrderReplyEvent(ACK)");
			Map<String, Object> fields = new HashMap<String, Object>();
			fields.put(OrderField.PRICE.value(), 0.81);
			fields.put(OrderField.QUANTITY.value(), 3000.0); // note: you must put xxx.0 to tell java this is a double type here!!
			AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(), null, 
					event.getOrder().getId(), fields, IdGenerator.getInstance().getNextID());
			sendEvent(amendEvent);
		} 
	}
	
	public void processAmendParentOrderReplyEvent(
			AmendParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Received AmendParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
			CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(getId(), null, 
					event.getOrder().getId(), IdGenerator.getInstance().getNextID());
			sendEvent(cancelEvent);
		} else {
			log.debug("Received AmendParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public void processCancelParentOrderReplyEvent(
			CancelParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Received CancelParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
		} else {
			log.debug("Received CancelParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		log.debug("Received ParentOrderUpdateEvent: " + event.getExecType() + ", order: " + event.getOrder());
	}

	EnterParentOrderEvent getEnterOrderEvent() {
		// SDMA 
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
		fields.put(OrderField.SYMBOL.value(), "AUDUSD");
		fields.put(OrderField.SIDE.value(), OrderSide.Buy);
		fields.put(OrderField.TYPE.value(), OrderType.Limit);
		fields.put(OrderField.PRICE.value(), 0.700);
		//fields.put(OrderField.PRICE.value(), 0.87980);
		fields.put(OrderField.QUANTITY.value(), 20000.0); // note: you must put xxx.0 to tell java this is a double type here!!
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		fields.put(OrderField.USER.value(), user);
		fields.put(OrderField.ACCOUNT.value(), account);
		enterOrderEvent = new EnterParentOrderEvent(getId(), null, fields, IdGenerator.getInstance().getNextID(), false);
		return enterOrderEvent;
	}
	
	EnterParentOrderEvent getEnterStopOrderEvent() {
		// SDMA 
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
		fields.put(OrderField.SYMBOL.value(), "AUDUSD");
		fields.put(OrderField.SIDE.value(), OrderSide.Buy);
		fields.put(OrderField.TYPE.value(), OrderType.Market);
		//fields.put(OrderField.PRICE.value(), 0.87980);
		fields.put(OrderField.QUANTITY.value(), 20000.0); // note: you must put xxx.0 to tell java this is a double type here!!
		fields.put(OrderField.STRATEGY.value(), "STOP");
		fields.put(OrderField.STOP_LOSS_PRICE.value(), 0.92);
		fields.put(OrderField.USER.value(), user);
		fields.put(OrderField.ACCOUNT.value(), account);
		enterOrderEvent = new EnterParentOrderEvent(getId(), null, fields, IdGenerator.getInstance().getNextID(), false);
		return enterOrderEvent;
	}
	
	public void processSystemErrorEvent(SystemErrorEvent event) {
		log.error("Error code: " + event.getErrorCode() + " - " + event.getMessage());
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

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/apilog4j.xml");
		String configFile = "conf/api.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		LtsApiAdaptor adaptor = (LtsApiAdaptor)context.getBean("apiAdaptor");
		adaptor.init();
	}
}
