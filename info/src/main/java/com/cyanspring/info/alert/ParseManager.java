package com.cyanspring.info.alert;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.alert.ParseData;
import com.cyanspring.common.alert.SendNotificationRequestEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.AsyncEventProcessor;

public class ParseManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(ParseManager.class);
//	@Autowired
//	private IRemoteEventManager eventManager;	
	@Autowired @Qualifier("eventManagerMD")
	private IRemoteEventManager eventManagerMD;
	@Autowired
	ScheduleManager scheduleManager;
	
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	public ConcurrentLinkedQueue<ParseData> ParseDataQueue;	
	private ArrayList<ParseThread> ParseThreadList;
	private int timeoutSecond;
	private int createThreadCount;
	private int maxRetrytimes;
	private long killTimeoutSecond;
	
	private String parseApplicationId;
	private String parseRestApiId;
	private String ParseAction;
	
	private int CheckThreadStatusInterval = 60000; // 60 seconds

	
//	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
//		@Override
//		public void subscribeToEvents() {
//		}
//
//		@Override
//		public IAsyncEventManager getEventManager() {
//			return eventManager;
//		}
//	};
	
	private AsyncEventProcessor eventProcessorMD = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(AsyncTimerEvent.class, null);
			subscribeToEvent(SendNotificationRequestEvent.class, null);			
		}
		
		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}		
	};
	
	public void processSendNotificationRequestEvent(SendNotificationRequestEvent event)
	{
		ParseDataQueue.add(event.getPD());
	}
	
	@SuppressWarnings("deprecation")
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if (event == timerEvent) {
			try {
				log.info("ParseDataQueue Size : " + ParseDataQueue.size());
				ThreadStatus TS;
				ParseThread PT;
				for (int i = ParseThreadList.size(); i > 0; i--) {
					PT = ParseThreadList.get(i - 1);
					TS = PT.getThreadStatus();
					if (TS.getThreadState() == ThreadState.IDLE.getState()) {
						continue;
					} else {
						long CurTime = System.currentTimeMillis();
						String Threadid = PT.getThreadId();
						if ((CurTime - TS.getTime()) > killTimeoutSecond) {
							log.warn(Threadid + " Timeout , ReOpen Thread.");
							ParseThreadList.remove(PT);
							try {
								PT.stop();
							} catch (Exception e) {
								log.warn("[processAsyncTimerEvent] Exception : "
										+ e.getMessage());
							} finally {
								PT = new ParseThread(Threadid, ParseDataQueue,
										timeoutSecond, maxRetrytimes,
										parseApplicationId, parseRestApiId, ParseAction);
								ParseThreadList.add(PT);
								PT.start();
							}
						}
					}
				}
			} catch (Exception e) {
				log.warn("[timerEvent] Exception : " + e.getMessage());
			}
		}
	}

	@Override
	public void init() throws Exception {
		String strThreadId = "";
		try {
			log.info("Initialising...");

			ParseDataQueue = new ConcurrentLinkedQueue<ParseData>();
			ParseThreadList = new ArrayList<ParseThread>();
			
			// subscribe to events
//			eventProcessor.setHandler(this);
//			eventProcessor.init();
//			if (eventProcessor.getThread() != null)
//				eventProcessor.getThread().setName("ParseManager");
			eventProcessorMD.setHandler(this);
			eventProcessorMD.init();
			if (eventProcessorMD.getThread() != null)
				eventProcessorMD.getThread().setName("ParseManager");
			
			scheduleManager.scheduleRepeatTimerEvent(CheckThreadStatusInterval,
					eventProcessorMD, timerEvent);
			
			if (getCreateThreadCount() > 0) {
				for (int i = 0; i < getCreateThreadCount(); i++) {
					strThreadId = "ParseThread" + String.valueOf(i);
					ParseThread PT = new ParseThread(strThreadId,
							ParseDataQueue, timeoutSecond, maxRetrytimes,
							parseApplicationId, parseRestApiId, ParseAction);
					log.info("[" + strThreadId + "] New.");
					ParseThreadList.add(PT);
					PT.start();
				}
			} else {
				log.warn("createThreadCount Setting error : "
						+ String.valueOf(getCreateThreadCount()));
			}
			
		} catch (Exception e) {
			log.warn("[" + strThreadId + "] Exception : " + e.getMessage());
		}
	}

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		for (ParseThread PT : ParseThreadList) {
			PT.setstartThread(false);
		}
		eventProcessorMD.uninit();
		scheduleManager.uninit();
	}
	
	public int getTimeoutSecond() {
		return timeoutSecond;
	}

	public void setTimeoutSecond(int timeoutSecond) {
		this.timeoutSecond = timeoutSecond;
	}

	public int getMaxRetrytimes() {
		return maxRetrytimes;
	}

	public void setMaxRetrytimes(int maxRetrytimes) {
		this.maxRetrytimes = maxRetrytimes;
	}

	public long getKillTimeoutSecond() {
		return killTimeoutSecond;
	}

	public void setKillTimeoutSecond(long killTimeoutSecond) {
		this.killTimeoutSecond = killTimeoutSecond;
	}

	public String getParseApplicationId() {
		return parseApplicationId;
	}

	public void setParseApplicationId(String parseApplicationId) {
		this.parseApplicationId = parseApplicationId;
	}

	public String getParseRestApiId() {
		return parseRestApiId;
	}

	public void setParseRestApiId(String parseRestApiId) {
		this.parseRestApiId = parseRestApiId;
	}

	public int getCreateThreadCount() {
		return createThreadCount;
	}

	public void setCreateThreadCount(int createThreadCount) {
		this.createThreadCount = createThreadCount;
	}

	public String getParseAction() {
		return ParseAction;
	}

	public void setParseAction(String parseAction) {
		ParseAction = parseAction;
	}
}
