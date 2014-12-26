package com.cyanspring.soak;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.client.ClientAdaptor;
import com.cyanspring.common.Clock;
import com.cyanspring.common.account.User;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.account.CreateUserEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.event.account.UserLoginEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;

public class SoakTest extends ClientAdaptor {
	private static Logger log = LoggerFactory.getLogger(SoakTest.class);
	private String password = "xxx";
	private AsyncTimerEvent jobEvent = new AsyncTimerEvent();
	private Random random = new Random();
	private List<User> users = new ArrayList<User>();
	private final static int preCreateUserCount = 10;
	private int userCount = 1;
	private String[] symbolList = {
		"AUDUSD",
		"USDJPY",
		"USDCHF",
		"USDCAD",
		"EURUSD",
		"EURJPY",
		"GBPUSD",
		"NZDUSD",
		"GBPJPY",
	};
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	private Map<String, ParentOrder> orders = new HashMap<String, ParentOrder>();
	private PerfRecord tranPerf = new PerfRecord();
	private int intervalTimes = 5;
	private int baseInterval = 20;
	
	class PerfRecord {
		private long tranTime;
		private long tranCount;
		private Map<String, Date> sendTimeRecord = new HashMap<String, Date>(); 
		
		private void tranStart(String key, Date sendTime) {
			sendTimeRecord.put(key, sendTime);
		}
		
		private void tranEnd(String key, Date receiveTime) {
			Date sendTime = sendTimeRecord.remove(key);
			if(null == sendTime)
				log.error("Can't sendTime: " + key);
			
			tranTime += receiveTime.getTime() - sendTime.getTime();
			++tranCount;
			if(tranCount % 100 == 0)
				log.info("Transaction count: " + tranCount + ", average time: " + tranTime/tranCount + " ms");
		}
	}
	
	
	@Override
	public void init() throws Exception {
		super.init();
		log.info("intervalTimes: " + intervalTimes + ", baseInterval: " + baseInterval);
	}
	
	@Override
	public void processServerStatusEvent(String server, boolean up) {
		log.debug("Server: " + server + " is " + (up?"up":"down"));
	}
	
	@Override
	public void subscribeToEvents() {
		super.subscribeToEvents();
		subscribeToEvent(NodeInfoEvent.class, null);
		subscribeToEvent(AsyncTimerEvent.class, null);
		subscribeToEvent(ServerReadyEvent.class, null);
		subscribeToEvent(QuoteEvent.class, null);
		subscribeToEvent(EnterParentOrderReplyEvent.class, getId());
		subscribeToEvent(AmendParentOrderReplyEvent.class, getId());
		subscribeToEvent(CancelParentOrderReplyEvent.class, getId());
		subscribeToEvent(ParentOrderUpdateEvent.class, null);
		subscribeToEvent(ChildOrderUpdateEvent.class, null);
		subscribeToEvent(CreateUserReplyEvent.class, null);
		subscribeToEvent(UserLoginReplyEvent.class, null);
	}

	private void scheduleNextJob() {
		int n = random.nextInt(intervalTimes);
		// schedule next job between 20-1000 ms
		scheduleManager.scheduleTimerEvent((1+n)*this.baseInterval, eventProcessor, jobEvent);
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		super.processAsyncTimerEvent(event);
		if(event == jobEvent) {
			doJob();
		}
	}
	
	private void doJob() {
		int n = random.nextInt(100);
		if(n < 2) { // 2% of chance create new user
			createNextUser();
		} else if(n < 70) { // 68% of chance create new order
			createOrder();
		} else if(n < 95) { // 25% of chance amend a order
			amendOrder();
		} else { // 10% of chance cancel a order
			cancelOrder();
		}
		scheduleNextJob();
	}
	
	private void cancelOrder() {
		if(orders.size()<=0)
			return;

		int n = random.nextInt(orders.size());
		int i = 0;
		for(ParentOrder order: orders.values()) {
			if(i++ >= n) {
				if(order.getOrderType().equals(OrderType.Market))
					continue;
				
				String txId = IdGenerator.getInstance().getNextID();
				CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(getId(), null, 
						order.getId(), txId);
				tranPerf.tranStart(txId, Clock.getInstance().now());
				sendEvent(cancelEvent);
			}
		}
	}

	private void amendOrder() {
		if(orders.size()<=0)
			return;
		
		int n = random.nextInt(orders.size());
		int i = 0;
		for(ParentOrder order: orders.values()) {
			if(i++ >= n) {
				if(order.getOrderType().equals(OrderType.Market))
					continue;
				
				Map<String, Object> fields = new HashMap<String, Object>();
				
				double price = random.nextBoolean()?order.getPrice()+0.05:order.getPrice()-0.05;
				if(price > 0)
					fields.put(OrderField.PRICE.value(), price);
				
				double qty = order.getQuantity();
				if(order.getQuantity() > 10000)
					qty -= 5000;
				else
					qty += 5000;
				fields.put(OrderField.QUANTITY.value(), qty); 
				String txId = IdGenerator.getInstance().getNextID();
				AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(), null, 
						order.getId(), fields, txId);
				tranPerf.tranStart(txId, Clock.getInstance().now());
				sendEvent(amendEvent);
			}
		}
		
	}

	private void createOrder() {
		HashMap<String, Object> fields = new HashMap<String, Object>();
		String symbol = symbolList[random.nextInt(symbolList.length)];
		fields.put(OrderField.SYMBOL.value(), symbol);
		
		
		Quote quote = quotes.get(symbol);
		if(null == quote) {
			log.warn("No quote for: " + symbol);
			return;
		}
		
		OrderSide side = random.nextBoolean()?OrderSide.Buy:OrderSide.Sell;
		fields.put(OrderField.SIDE.value(), side);
		
		OrderType type = random.nextBoolean()?OrderType.Limit:OrderType.Market;
		fields.put(OrderField.TYPE.value(), type);
		
		double price = 0.0;
		if(type.equals(OrderType.Limit)) {
			price = side.equals(OrderSide.Buy)?quote.getBid():quote.getAsk();
			fields.put(OrderField.PRICE.value(), price);
		}
		
		int n = random.nextInt(4);
		double qty = 5000.0 * (1+n);
		fields.put(OrderField.QUANTITY.value(), qty); // 5000 - 20000
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		User user = users.get(random.nextInt(users.size()));
		fields.put(OrderField.USER.value(), user.getId());
		fields.put(OrderField.ACCOUNT.value(), user.getDefaultAccount());
		String txId = IdGenerator.getInstance().getNextID();
		EnterParentOrderEvent event = new EnterParentOrderEvent(getId(), null, fields, 
						txId, false);
		tranPerf.tranStart(txId, Clock.getInstance().now());
		log.debug("send EnterParentOrder: " + txId + ", " + symbol + ", " + qty + "@" + price);
		sendEvent(event);
	}

	public void processQuoteEvent(QuoteEvent event) {
		log.debug("Received QuoteEvent: " + event.getKey() + ", " + event.getQuote());
		quotes.put(event.getQuote().getSymbol(), event.getQuote());
	}

	public void processServerReadyEvent(ServerReadyEvent event) {
		log.debug("Received ServerReadyEvent: " + event.getSender() + ", " + event.isReady());
		if(event.isReady()) {
			preCreateUsers();
			preSubscribeQuotes();
		}
	}

	private void preSubscribeQuotes() {
		for(String symbol: symbolList) {
			sendEvent(new QuoteSubEvent(getId(), null, symbol));
		}
	}

	public void processCreateUserReplyEvent(CreateUserReplyEvent event) {
		log.debug("User created is " + event.isOk() + ", " + event.getMessage() + ", " + event.getUser());
		sendEvent(new UserLoginEvent(getId(), null, event.getUser().getId(), 
				password, IdGenerator.getInstance().getNextID()));
	}
	
	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		String msg = "User login is " + event.isOk() + ", " + event.getMessage() + ", lastLogin: " + event.getUser().getLastLogin();
		if(event.isOk()) {
			users.add(event.getUser());
			log.debug(msg);
			if(users.size() >= preCreateUserCount) {
				scheduleNextJob();
			}
		} else {
			log.error(msg);
		}
	}

	public void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Receive EnterParentOrderReplyEvent(ACK): " + event.getTxId());
			tranPerf.tranEnd(event.getTxId(), Clock.getInstance().now());
			orders.put(event.getOrder().getId(), event.getOrder());
		} else
			log.error("Receive EnterParentOrderReplyEvent(NACK): " + event.getMessage());
	}
	
	public void processAmendParentOrderReplyEvent(
			AmendParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Received AmendParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
			tranPerf.tranEnd(event.getTxId(), Clock.getInstance().now());
		} else {
			log.error("Received AmendParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public void processCancelParentOrderReplyEvent(
			CancelParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Received CancelParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
			tranPerf.tranEnd(event.getTxId(), Clock.getInstance().now());
		} else {
			log.error("Received CancelParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		log.debug("Received ParentOrderUpdateEvent: " + event.getExecType() + ", order: " + event.getOrder());
		ParentOrder order = event.getOrder();
		if(order.getOrdStatus().isCompleted())
			orders.remove(order.getId());
		else
			orders.put(order.getId(), order);
	}

	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		log.debug("Received ChildOrderUpdateEvent: " + event.getExecType() + 
				", Parent order id: " + event.getOrder().getParentOrderId() + 
				", child order: " + event.getOrder());
		// This is how you get trade
		if(event.getExecution() != null) {
			log.debug("Received trade: " + event.getExecution());
		}
	}
	
	void preCreateUsers() {
		for(int i=1; i<=preCreateUserCount; i++) {
			createNextUser();
		}
	}
	
	void createNextUser() {
		sendEvent(new CreateUserEvent(getId(), null, new User("test"+userCount++, password), "", "", 
				IdGenerator.getInstance().getNextID()));
	}
	
	public int getIntervalTimes() {
		return intervalTimes;
	}

	public void setIntervalTimes(int intervalTimes) {
		this.intervalTimes = intervalTimes;
	}

	public int getBaseInterval() {
		return baseInterval;
	}

	public void setBaseInterval(int baseInterval) {
		this.baseInterval = baseInterval;
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		String configFile = "conf/soak.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		SoakTest soakTest = (SoakTest)context.getBean("soakTest");
		soakTest.init();
	}
}
