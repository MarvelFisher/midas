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
package com.cyanspring.event;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncExecuteEvent;
import com.cyanspring.common.event.EventPriority;


public abstract class AsyncPriorityEventThread {
	private static final Logger log = LoggerFactory
			.getLogger(AsyncPriorityEventThread.class);
	
	protected List<AsyncEvent> highEvents = Collections.synchronizedList(new LinkedList<AsyncEvent>());
	protected List<AsyncEvent> normalEvents = Collections.synchronizedList(new LinkedList<AsyncEvent>());
	private boolean running = false;
	private boolean slow = false;
	private int slowThreshold = 200;
	private long eventCount;
	private PerfTracker perfTracker = new PerfTracker();
	
	@SuppressWarnings("rawtypes")
	protected void logStat(List<AsyncEvent> queue, String queueName) {
		if(queue.size() >= slowThreshold ) {
			if(!slow || (slow && queue.size() % slowThreshold == 0)) {
				slow = true;
				StringBuilder sb = new StringBuilder();
				if(AsyncThreadStat.isShowStat()) {
					HashMap<Class, Integer> stat = new HashMap<Class, Integer>();
					synchronized(queue) {
						Iterator<AsyncEvent> it = queue.iterator();
						while(it.hasNext()) {
							AsyncEvent e = it.next();
							if(e instanceof AsyncExecuteEvent) {
								e = ((AsyncExecuteEvent)e).getInnerEvent();
							}
							Integer count = stat.get(e.getClass());
							if(null == count)
								count = new Integer(0);
							stat.put(e.getClass(), count+1);
						}
						for(Entry<Class, Integer> entry: stat.entrySet()) {
							sb.append("\n" + entry.getKey() + " : " + entry.getValue());
						}
					}
				}
				log.warn("Slow consumer thread " + thread.getName() + " " + 
						queueName + " queue size: " + queue.size() + sb.toString());
			}
		} else if(queue.size() < slowThreshold && slow) {
			slow = false;
			log.info("Slow consumer thread " + thread.getName() + " " + 
					queueName + " back to normal, queue size: " + queue.size());
		}

	}
	
	class PerfStat {
		long time;
		long count;
	}
	
	@SuppressWarnings("rawtypes")
	class PerfTracker {
		HashMap<Class, PerfStat> map = new HashMap<Class, PerfStat>();
		void record(AsyncEvent event, Date before, Date after) {
			if(event instanceof AsyncExecuteEvent) {
				event = ((AsyncExecuteEvent)event).getInnerEvent();
			}
			PerfStat stat = map.get(event.getClass());
			if(null == stat) {
				stat = new PerfStat();
				map.put(event.getClass(), stat);
			}
			stat.time += after.getTime() - before.getTime();
			stat.count++;
		}
		
		void output() {
			if(eventCount % 1000 != 0) // only show every 1000 events processed
				return;
			StringBuilder sb = new StringBuilder();
			sb.append(thread.getName() + " PerfStat: ");
			for(Entry<Class, PerfStat> entry: map.entrySet()) {
				sb.append("\n" + entry.getKey() + ", " + entry.getValue().time + ", " + entry.getValue().count + ", " + 
						entry.getValue().time / entry.getValue().count);
			}
			log.debug(sb.toString());
		}
	}

	protected void processEvent(AsyncEvent event) {
		try {
			eventCount++;
			Date before = new Date();
			
			onEvent(event);
			
			Date after = new Date();
			if(AsyncThreadStat.isShowStat()) {
				perfTracker.record(event, before, after);
				perfTracker.output();
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}
	protected Thread thread = new Thread() {
		@Override
		public void run() {
			while(running) {
				try {
					synchronized (thread) {
						if(highEvents.size() == 0 && normalEvents.size() == 0)
							thread.wait();
					}
				} catch (InterruptedException e) {
					break;
				}
				
				if(highEvents.size()>0) {
					AsyncEvent event = highEvents.remove(0);
					logStat(highEvents, "high");
					processEvent(event);
				} else if(normalEvents.size()>0) {
					AsyncEvent event = normalEvents.remove(0);
					logStat(normalEvents, "normal");
					processEvent(event);
				} 
			}
		}		
	};
	
	public abstract void onEvent(AsyncEvent event);
	
	public void setName(String name) {
		thread.setName(name);
	}
	
	public void exit() {
		running = false;

		if (thread.isAlive())
			thread.interrupt();
		
	}
	
	public void addEvent(AsyncEvent event) {
		synchronized (thread) {
			if(event.getPriority().equals(EventPriority.HIGH))
				highEvents.add(event);
			else if(event.getPriority().equals(EventPriority.NORMAL))
				normalEvents.add(event);
			else
				throw new RuntimeException("Event priority not defined");
			
			thread.notify();
		}
	}
	
	public void start() {
		running = true;
		highEvents.clear();
		normalEvents.clear();
		thread.start();
	}

	public boolean isAlive() {
		return thread.isAlive();
	}
	
}
