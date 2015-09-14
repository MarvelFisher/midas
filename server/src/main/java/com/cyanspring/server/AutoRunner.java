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
package com.cyanspring.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Clock.Mode;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.CreateUserEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class AutoRunner implements IPlugin, IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(AutoRunner.class);
	@Autowired
	IAsyncEventManager eventManager;
	
	@Autowired
	ScheduleManager scheduleManager;
	
	AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	Thread thread;
	DateTimeRoller dateTimeRoller;
	boolean threadSentinel = false;

	static class DateTimeRoller {
		int countToRollDate;
		int count;
		DateTimeRoller(Date startDate, int countToRollDate) {
			Clock.getInstance().setMode(Mode.MANUAL);
			Clock.getInstance().setManualClock(startDate);
			this.countToRollDate = countToRollDate;
		}
		
		Date getNextTime() {
			Date now = Clock.getInstance().now();
			if(count >= countToRollDate) {
				now.setTime(now.getTime() + 24 * 60 * 60 * 1000);
				count = 0;
			} else {
				now.setTime(now.getTime() + 1000);
				count++;
			}
			Clock.getInstance().setManualClock(now);
			
			return now;
		}
	}
	
	public void init() throws ParseException {
		log.info("Initialising AutoRunner");
		eventManager.subscribe(ServerReadyEvent.class, this);

		// String dateStr = "2014-07-21 10:20:25.234";
		// SimpleDateFormat dateFormat = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		// Date now = dateFormat.parse(dateStr);
		// dateTimeRoller = new DateTimeRoller(now, 100);
		// scheduleManager.scheduleRepeatTimerEvent(1000, this, timerEvent);

		threadSentinel = true;
		thread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized(this) {
						this.wait();
					}
					asyncRun();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

			}
		};
		
		thread.start();

	}
	
//	private void asyncRun() throws InterruptedException {
//		Thread.sleep(20000);
//		Random ran = new Random();
//		
//		for(int i=0; i<1000; i++) {
//			EnterParentOrderEvent sdma = createSDMA();
//			if(ran.nextBoolean())
//				sdma.getFields().put(OrderField.PRICE.value(), "68.3");
//			else
//				sdma.getFields().put(OrderField.PRICE.value(), "68.2");
//			sdma.getFields().put(OrderField.NOTE.value(), ""+i);
//			eventManager.sendEvent(sdma);
//		}
//	}
	
	private void asyncRun() {

		for (int i = 1; i <= 10; i++) {
			if (threadSentinel) {
				User user = new User("test" + i, "xxx");
				user.setName("test" + i);
				user.setEmail("test" + i + "@test.com");
				user.setPhone("12345678");
				user.setUserType(UserType.TEST);
				CreateUserEvent event = new CreateUserEvent(null, null, user,
						"", "", "123");
				eventManager.sendEvent(event);
			}
		}
	}

	@Override
	public void uninit() {
		threadSentinel = false;
		eventManager.clearAllSubscriptions();
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof ServerReadyEvent) {
			ServerReadyEvent e = (ServerReadyEvent)event;
			if(!e.isReady())
				return;
			
			synchronized(thread) {
				thread.notify();
			}
		} else if(event == timerEvent) {
			processAsyncTimerEvent((AsyncTimerEvent)event);
		}
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		dateTimeRoller.getNextTime();
	}

	static EnterParentOrderEvent createSDMA() {
		// SDMA 
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
		fields.put(OrderField.SYMBOL.value(), "0005.HK");
		fields.put(OrderField.SIDE.value(), OrderSide.Buy.toString());
		fields.put(OrderField.TYPE.value(), OrderType.Limit.toString());
		fields.put(OrderField.PRICE.value(), "68.25");
		fields.put(OrderField.QUANTITY.value(), "2000");
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		enterOrderEvent = new EnterParentOrderEvent(null, null, fields, IdGenerator.getInstance().getNextID(), false,true);
		return enterOrderEvent;
	}
	
	static EnterParentOrderEvent createPOV() {
		// POV
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
		fields.put(OrderField.SYMBOL.value(), "0005.HK");
		fields.put(OrderField.SIDE.value(), OrderSide.Buy.toString());
		fields.put(OrderField.TYPE.value(), OrderType.Limit.toString());
		fields.put(OrderField.PRICE.value(), "70.4");
		fields.put(OrderField.QUANTITY.value(), "80000");
		fields.put(OrderField.POV.value(), "30");
		fields.put(OrderField.POV_LIMIT.value(), "30");
		fields.put(OrderField.STRATEGY.value(), "POV");
		enterOrderEvent = new EnterParentOrderEvent(null, null, fields, IdGenerator.getInstance().getNextID(), false,true);
		return enterOrderEvent;
	}
	
	static NewMultiInstrumentStrategyEvent createDollarNeutral() {
		// DOLLAR_NEUTRAL
		Map<String, Object> strategyLevelParams = new HashMap<String, Object>();
		strategyLevelParams.put(OrderField.STRATEGY.value(), "DOLLAR_NEUTRAL");
		strategyLevelParams.put("Value", "50000");
		strategyLevelParams.put("Allow diff", "100");
		strategyLevelParams.put("High stop", "0.05");
		strategyLevelParams.put("High take", "0.02");
		strategyLevelParams.put("High flat", "0.01");
		strategyLevelParams.put("Low flat", "-0.01");
		strategyLevelParams.put("Low take", "-0.02");
		strategyLevelParams.put("Low stop", "-0.05");
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> instr1 = new HashMap<String, Object>();
		instr1.put(OrderField.SYMBOL.value(), "RIO.AX");
		instr1.put("Leg", "1");
		instr1.put("Weight", "1");
		instr1.put("Ref price", "55");
		list.add(instr1);
		
		Map<String, Object> instr2 = new HashMap<String, Object>();
		instr2.put(OrderField.SYMBOL.value(), "WBC.AX");
		instr2.put("Leg", "1");
		instr2.put("Weight", "2");
		instr2.put("Ref price", "20.5");
		list.add(instr2);
		
		Map<String, Object> instr3 = new HashMap<String, Object>();
		instr3.put(OrderField.SYMBOL.value(), "BHP.AX");
		instr3.put("Leg", "2");
		instr3.put("Weight", "1");
		instr3.put("Ref price", "37");
		list.add(instr3);

		Map<String, Object> instr4 = new HashMap<String, Object>();
		instr4.put(OrderField.SYMBOL.value(), "ANZ.AX");
		instr4.put("Leg", "2");
		instr4.put("Weight", "2");
		instr4.put("Ref price", "21.6");
		list.add(instr4);
		
		NewMultiInstrumentStrategyEvent newMultiInstrumentStrategyEvent 
			= new NewMultiInstrumentStrategyEvent(null, null, strategyLevelParams, list);
		
		return newMultiInstrumentStrategyEvent;
	}
	
	static NewMultiInstrumentStrategyEvent createLowHigh() {
		// LOW_HIGH
		Map<String, Object> lowHigh = new HashMap<String, Object>();
		lowHigh.put(OrderField.STRATEGY.value(), "LOW_HIGH");
		
		List<Map<String, Object>> listLowHigh = new ArrayList<Map<String, Object>>();
		Map<String, Object> instrLowHigh1 = new HashMap<String, Object>();
		instrLowHigh1.put(OrderField.SYMBOL.value(), "0001.HK");
		instrLowHigh1.put("Qty", "2000");
		instrLowHigh1.put("Low flat", "88");
		instrLowHigh1.put("Low take", "87");
		instrLowHigh1.put("Low stop", "82");
		listLowHigh.add(instrLowHigh1);
		
		Map<String, Object> instrLowHigh2 = new HashMap<String, Object>();
		instrLowHigh2.put(OrderField.SYMBOL.value(), "0005.HK");
		instrLowHigh2.put("Qty", "2000");
		instrLowHigh2.put("High stop", "69");
		instrLowHigh2.put("High take", "68.5");
		instrLowHigh2.put("High flat", "68.2");
		instrLowHigh2.put("Low flat", "68.2");
		instrLowHigh2.put("Low take", "67.9");
		instrLowHigh2.put("Low stop", "67.4");
		instrLowHigh2.put("Shortable", "true");
		listLowHigh.add(instrLowHigh2);
		
		NewMultiInstrumentStrategyEvent newLowHighStrategyEvent 
			= new NewMultiInstrumentStrategyEvent(null, null, lowHigh, listLowHigh);
		
		return newLowHighStrategyEvent;
	}
	
	static NewSingleInstrumentStrategyEvent createStopWinLoss() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(OrderField.SYMBOL.value(), "0005.HK");
		map.put(OrderField.STRATEGY.value(), "STOP_WIN_LOSS");
		map.put(OrderField.POSITION.value(), 2000.0);
		map.put(OrderField.POS_AVGPX.value(), 68.3);
		map.put("Min win", 3.0);
		map.put("High fall", 1.0);
		map.put("Low fall", 3.0);
		NewSingleInstrumentStrategyEvent event = new NewSingleInstrumentStrategyEvent(null, null, "", map);
		
		return event;
	}
	
	static void saveXML(String name, AsyncEvent event, XStream xstream) throws IOException {
		File file = new File(name);
		file.createNewFile();
		FileOutputStream os = new FileOutputStream(file);
		xstream.toXML(event, os);
	}
	
	public static void main(String[] args) throws IOException {
		XStream xstream = new XStream(new DomDriver());
		AutoRunner.saveXML("templates/SDMA.xml", AutoRunner.createSDMA(), xstream);
		AutoRunner.saveXML("templates/POV.xml", AutoRunner.createPOV(), xstream);
		AutoRunner.saveXML("templates/LOW_HIGH.xml", AutoRunner.createLowHigh(), xstream);
		AutoRunner.saveXML("templates/DOLLAR_NEUTRAL.xml", AutoRunner.createDollarNeutral(), xstream);
		
	}

}
