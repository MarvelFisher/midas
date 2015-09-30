package com.cyanspring.info;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cyanspring.common.alert.SendNotificationRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertRequestEvent;
import com.cyanspring.common.event.alert.QueryPriceAlertRequestEvent;
import com.cyanspring.common.event.alert.SetPriceAlertRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.info.alert.Compute;
public class InfoGateway implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(InfoGateway.class);
	@Autowired
	private IRemoteEventManager eventManager;

//	@Autowired
//	ScheduleManager scheduleManager;

	@Autowired
	private IRemoteEventManager eventManagerMD;

	private int createThreadCount;
	private ExecutorService service;
	private List<Compute> Computes;
	private ConcurrentHashMap<String, RefData> refDataMap;
//	private ConcurrentLinkedQueue<AsyncEvent> sendRemoteEventQueue;
//	private ConcurrentLinkedQueue<AsyncEvent> sendEventQueue;
//	private AsyncTimerEvent timerEvent1min = new AsyncTimerEvent();
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			for (Compute compute : Computes) {
				for (Class<? extends AsyncEvent> event : compute
						.getSubscirbetoEventList()) {
					subscribeToEvent(event, null);
				}
			}
		}
		
		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	private AsyncEventProcessor eventProcessorMD = new AsyncEventProcessor() {
		@Override
		public void subscribeToEvents() {
			for (Compute compute : Computes) {
				for (Class<? extends AsyncEvent> event : compute
						.getSubscirbetoEventMDList()) {
					subscribeToEvent(event, null);
				}
			}
			subscribeToEvent(RefDataEvent.class, null);
			subscribeToEvent(RefDataUpdateEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}
	};

	public void processMarketSessionEvent(final MarketSessionEvent event) {
		MarketSessionType mst = event.getSession();
		if (MarketSessionType.PREMARKET != mst && MarketSessionType.CLOSE != mst) {
			return;
		}
		log.info("[MarketSessionEvent] : " + mst);
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + " " + oldName);
                    compute.processMarketSessionEvent(event, Computes);
                    currentThread.setName(oldName);
                }
			});
		}		
	}

	public void processChildOrderUpdateEvent(final ChildOrderUpdateEvent event) {
		Execution execution = event.getExecution();
		if (null == execution)
			return;
		log.info("[processUpdateChildOrderEvent] " + execution.toString());
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processChildOrderUpdateEvent(event, Computes);
                    currentThread.setName(oldName);
				}
			});
		}
	}

    public void processParentOrderUpdateEvent(final ParentOrderUpdateEvent event) {
        if (null == event)
            return;
        log.info("[processUpdateChildOrderEvent] " + event.getInfo() + ":" + event.getTxId());
        for (final Compute compute : Computes) {
            service.submit(new Runnable() {
                public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processParentOrderUpdateEvent(event, Computes);
                    currentThread.setName(oldName);
                }
            });
        }
    }

	public void processResetAccountRequestEvent(
			final ResetAccountRequestEvent event) {
		log.info("[processResetAccountRequestEvent] : AccountId :"
				+ event.getAccount() + " Coinid : " + event.getCoinId());
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processResetAccountRequestEvent(event, Computes);
                    currentThread.setName(oldName);
				}
			});
		}
	}

	public void processQueryOrderAlertRequestEvent(
			final QueryOrderAlertRequestEvent event) {
		log.info("[receiveQueryOrderAlertRequestEvent] : UserId :"
				+ event.getuserId());
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processQueryOrderAlertRequestEvent(event, Computes);
                    currentThread.setName(oldName);
				}
			});
		}
	}

	public void processQuoteEvent(final QuoteEvent event) {
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processQuoteEvent(event, Computes);
                    currentThread.setName(oldName);
				}
			});
		}
	}

	public void processSetPriceAlertRequestEvent(
			final SetPriceAlertRequestEvent event) {
		log.info("[processSetPriceAlertRequestEvent] " + event.toString());
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processSetPriceAlertRequestEvent(event, Computes);
                    currentThread.setName(oldName);
				}
			});
		}
	}

	public void processQueryPriceAlertRequestEvent(
			final QueryPriceAlertRequestEvent event) {
		log.info("[processQueryPriceAlertRequestEvent] " + event.toString());
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processQueryPriceAlertRequestEvent(event, Computes);
                    currentThread.setName(oldName);
				}
			});
		}
	}

    public void processSendNotificationRequestEvent(final SendNotificationRequestEvent event)
    {
        log.info("[processSendNotificationRequestEvent] " + event.toString());
        for (final Compute compute : Computes) {
            service.submit(new Runnable() {
                public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processSendNotificationRequestEvent(event, Computes);
                    currentThread.setName(oldName);
                }
            });
        }
    }

	@SuppressWarnings("deprecation")
	public void processAsyncTimerEvent(final AsyncTimerEvent event) {
		for (final Compute compute : Computes) {
			service.submit(new Runnable() {
				public void run() {
                    final Thread currentThread = Thread.currentThread();
                    final String oldName = currentThread.getName();
                    currentThread.setName(compute.getThreadName() + "-" + oldName);
                    compute.processAsyncTimerEvent(event, Computes);
                    currentThread.setName(oldName);
				}
			});
		}
	}
	
	public void processRefDataEvent(RefDataEvent event) {
		if (refDataMap == null) {
			refDataMap = new ConcurrentHashMap<String, RefData>();
		}
		refDataMap.clear();
		for (RefData refdata : event.getRefDataList()) {
			refDataMap.put(refdata.getSymbol(), refdata);
		}
	}
	
	public void processRefDataUpdateEvent(RefDataUpdateEvent event) {
		RefDataUpdateEvent.Action act = event.getAction();
		for (RefData refdata : event.getRefDataList()) {
			if (act == RefDataUpdateEvent.Action.DEL) {
				refDataMap.remove(refdata.getSymbol());
			}
			else {
				refDataMap.put(refdata.getSymbol(), refdata);
			}
		}
	}
	
	public RefData getRefData(String symbol) {
		if (refDataMap == null)
		{
			return null;
		}
		return refDataMap.get(symbol);
	}

	@Override
	public void init() throws Exception {
		try {
			log.info("Initialising...");
			service = Executors.newFixedThreadPool(createThreadCount);
			// subscribe to events
			eventProcessor.setHandler(this);
			eventProcessor.init();
			if (eventProcessor.getThread() != null)
				eventProcessor.getThread().setName("InfoGateway");

			// subscribe to events
			eventProcessorMD.setHandler(this);
			eventProcessorMD.init();
			if (eventProcessorMD.getThread() != null)
				eventProcessorMD.getThread().setName("InfoGateway-MD");
			
//			scheduleManager.scheduleRepeatTimerEvent(60000, eventProcessorMD,
//					timerEvent1min);
//			scheduleManager.scheduleRepeatTimerEvent(300000, eventProcessorMD,
//					timerEvent5min);
			
//			sendRemoteEventQueue = new ConcurrentLinkedQueue<AsyncEvent>(); 
//			sendEventQueue = new ConcurrentLinkedQueue<AsyncEvent>();
			for (Compute compute : Computes) {
				compute.initial(eventProcessor, eventProcessorMD, this);
			}
			
		} catch (Exception e) {
			log.warn("Exception : " + e.getMessage());
		}
	}
	
	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
		eventProcessorMD.uninit();
	}

	public List<Compute> getComputes() {
		return Computes;
	}

	public void setComputes(List<Compute> computes) {
		Computes = computes;
	}

	public int getCreateThreadCount() {
		return createThreadCount;
	}

	public void setCreateThreadCount(int createThreadCount) {
		this.createThreadCount = createThreadCount;
	}

	public Map<String, RefData> getRefDataMap() {
		return refDataMap;
	}

	public void setRefDataMap(ConcurrentHashMap<String, RefData> refDataMap) {
		this.refDataMap = refDataMap;
	}
}
