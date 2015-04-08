package com.cyanspring.soak;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.Clock;
import com.cyanspring.common.account.User;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;

public class PerfTest extends ClientAdaptor {
	private static Logger log = LoggerFactory.getLogger(PerfTest.class);
	private AsyncTimerEvent jobEvent = new AsyncTimerEvent();
	private Random random = new Random();
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
	private PerfRecord tranPerf = new PerfRecord();
	private int baseInterval = 10;
	private List<ParentOrder> openPositionOrders = new LinkedList<ParentOrder>();
	private boolean ready = false;
	private int count;
	
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
		scheduleManager.scheduleRepeatTimerEvent(baseInterval, eventProcessor, jobEvent);
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
		subscribeToEvent(EnterParentOrderReplyEvent.class, getId());
		subscribeToEvent(AmendParentOrderReplyEvent.class, getId());
		subscribeToEvent(CancelParentOrderReplyEvent.class, getId());
		subscribeToEvent(ParentOrderUpdateEvent.class, null);
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		super.processAsyncTimerEvent(event);
		if(event == jobEvent && ready) {
			count++;
			doJob();
		}
		if(count > 10000) {
			log.info("############ THE END #############");
			System.exit(0);
		}
	}
	
	private void doJob() {
		if(!createCloseOrder())
			createOrder();
	}
	
	private String getUser() {
		return "test"+ (random.nextInt(10) + 1);
	}
	
	private String getAccount(String user) {
		return user + "-" + "FX";
	}
	
	private void createOrder() {
		HashMap<String, Object> fields = new HashMap<String, Object>();
		String symbol = symbolList[random.nextInt(symbolList.length)];
		fields.put(OrderField.SYMBOL.value(), symbol);
		
		OrderSide side = random.nextBoolean()?OrderSide.Buy:OrderSide.Sell;
		fields.put(OrderField.SIDE.value(), side);
		
		OrderType type = OrderType.Market;
		fields.put(OrderField.TYPE.value(), type);
		
		int n = random.nextInt(4);
		double qty = 5000.0 * (1+n);
		fields.put(OrderField.QUANTITY.value(), qty); // 5000 - 20000
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		String user = getUser();
		fields.put(OrderField.USER.value(), user);
		fields.put(OrderField.ACCOUNT.value(), getAccount(user));
		String txId = IdGenerator.getInstance().getNextID();
		EnterParentOrderEvent event = new EnterParentOrderEvent(getId(), null, fields, 
						txId, false);
		tranPerf.tranStart(txId, Clock.getInstance().now());
		log.debug("send EnterParentOrder: " + txId + ", " + symbol + ", " + qty);
		sendEvent(event);
	}
	
	private boolean createCloseOrder() {
		if(openPositionOrders.size()<=0)
			return false;
		
		ParentOrder order = openPositionOrders.remove(0);
		log.info("Sending Close order for: " + order.getId());
		
		HashMap<String, Object> fields = new HashMap<String, Object>();
		String symbol = order.getSymbol();
		fields.put(OrderField.SYMBOL.value(), symbol);
		
		OrderSide side = order.getSide().isSell()?OrderSide.Buy:OrderSide.Sell;
		fields.put(OrderField.SIDE.value(), side);
		
		OrderType type = OrderType.Market;
		fields.put(OrderField.TYPE.value(), type);
		
		fields.put(OrderField.QUANTITY.value(), order.getQuantity()); // 5000 - 20000
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		fields.put(OrderField.USER.value(), order.getUser());
		fields.put(OrderField.ACCOUNT.value(), order.getAccount());
		String txId = IdGenerator.getInstance().getNextID();
		EnterParentOrderEvent event = new EnterParentOrderEvent(getId(), null, fields, 
						txId, false);
		tranPerf.tranStart(txId, Clock.getInstance().now());
		log.debug("send close EnterParentOrder: " + txId + ", " + symbol + ", " + order.getQuantity());
		sendEvent(event);
		return true;
	}
	
	public void processQuoteEvent(QuoteEvent event) {
	}

	public void processServerReadyEvent(ServerReadyEvent event) {
		log.debug("Received ServerReadyEvent: " + event.getSender() + ", " + event.isReady());
		if(event.isReady()) {
			ready = true;
		}
	}

	public void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Receive EnterParentOrderReplyEvent(ACK): " + event.getTxId());
			tranPerf.tranEnd(event.getTxId(), Clock.getInstance().now());
			openPositionOrders.add(event.getOrder());
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
	}

	public int getBaseInterval() {
		return baseInterval;
	}

	public void setBaseInterval(int baseInterval) {
		this.baseInterval = baseInterval;
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		String configFile = "conf/perftest.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		PerfTest perfTest = (PerfTest)context.getBean("perfTest");
		perfTest.init();
	}
}
