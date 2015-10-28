package com.cyanspring.common.event;

import com.cyanspring.common.IPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author elviswu
 */

public class GenericAsyncEventProcessor implements IPlugin, IAsyncEventListener {
    private static final Logger log = LoggerFactory.getLogger(GenericAsyncEventProcessor.class);
    private IRemoteEventManager eventManager;
    private Map<String, List<Subscribe>> events;
    private AsyncPriorityEventThread thread;
    private volatile boolean isInit;
    private ScheduleManager scheduleManager = new ScheduleManager();
    private List<Schedule> schedules;

    public boolean subscribeToEvent(Class<? extends AsyncEvent> event, Object handler, String key) {
        if (!isInit) {
            try {
                init();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }
        
        String eventName = event.getSimpleName();
        List<Subscribe> subs = events.get(eventName);
        if (subs == null) {
        	subs = new ArrayList<>();
        	events.put(eventName, subs);
        }
        
        try {
            Method method = handler.getClass().getMethod(getEventName(eventName), event);
            Subscribe pair = new Subscribe(method, handler, key);
            if (!subs.contains(pair))
                subs.add(pair);
            eventManager.subscribe(event, this);
            log.info("Class: " + handler.getClass().getSimpleName() + ", method: " + eventName + " added in method map");
        } catch (NoSuchMethodException e) {
            log.error("Can't subscribe event: " + eventName + " to " + handler.getClass().getSimpleName());
            return false;
        }
        return true;
    }

    @Override
    public void onEvent(AsyncEvent event) {
        thread.addEvent(event);
    }

    private void onAsyncEvent(AsyncEvent event) {
    	if (event instanceof AsyncTimerEvent) {
    		processScheduleEvent(event);
    	} else {
    		processEvent(event);
    	}      
    }

    private void processScheduleEvent(AsyncEvent event) {
		Schedule schedule = null;
		synchronized (schedules) {
			for (Schedule s : schedules) {
				if (s.event == event) {
					schedule = s;
					break;
				}
			}
		}
		if (schedule != null) {
			String eventName = "AsyncTimerEvent";
			try {
				if (schedule.once)
					removeFromSchedule(schedule);
				Method method = schedule.sub.getClass().getMethod(getEventName(eventName), event.getClass());
				method.invoke(schedule.sub, event);
			} catch (Exception e) {
				if (e instanceof NoSuchMethodException) {
					log.error("Can't subscribe event: " + eventName + " to " + schedule.sub.getClass().getSimpleName());
					return;
				}
				log.error(e.getMessage(), e);
			}
		}
	}
    
	private void processEvent(AsyncEvent event) {
		List<Subscribe> subs = events.get(event.getClass().getSimpleName());
		if (subs == null) {
		    log.error("Unhandle event: " + event.getClass().getSimpleName());
		    return;
		}
		
		String key = event.getKey();
		for (Subscribe sub : subs) {
		    try {
		        if (sub.key != null) {
		            if (sub.key.equals(key)) {
		                sub.method.invoke(sub.sub, event);
		            }
		        } else {
		            sub.method.invoke(sub.sub, event);
		        }
		    } catch (IllegalAccessException | InvocationTargetException e) {
		        log.error(e.getMessage(), e);
		    }
		}
	}

    @Override
    public void init() throws Exception {
        if (isInit)
            return;
        log.info("Initializing...");
        if (eventManager == null)
            throw new Exception("eventManager not set");
        events = new HashMap<>();
        schedules = new ArrayList<>();

        if (thread == null) {
            thread = new AsyncPriorityEventThread() {
                @Override
                public void onEvent(AsyncEvent event) {
                    GenericAsyncEventProcessor.this.onAsyncEvent(event);
                }
            };
            thread.start();
        }

        isInit = true;
    }

    @Override
    public void uninit() {
    	scheduleManager.uninit();
        thread.exit();
        eventManager = null;
        schedules = null;
        events = null;
    }

    public void setEventManager(IRemoteEventManager eventManager) {
        this.eventManager = eventManager;
    }

    private String getEventName(String event) {
        return "process" + event;
    }
    
	public void scheduleTimerEvent(long time, Object handler, AsyncEvent event) {
		addToSchedule(new Schedule(handler, event, true));
		scheduleManager.scheduleTimerEvent(time, this, event);
	}

	public void scheduleTimerEvent(Date time, Object handler, AsyncEvent event) {
		addToSchedule(new Schedule(handler, event, true));
		scheduleManager.scheduleTimerEvent(time, this, event);
	}

	public void scheduleRepeatTimerEvent(long time, Object handler, AsyncEvent event) {
		addToSchedule(new Schedule(handler, event, false));
		scheduleManager.scheduleRepeatTimerEvent(time, this, event);
	}
	
	public void cancelTimerEvent(AsyncEvent event, Object handler) {
		removeFromSchedule(new Schedule(handler, event));
		scheduleManager.cancelTimerEvent(event);
	}
	
	private void addToSchedule(Schedule schedule) {
		synchronized (schedules) {
			if (schedules.contains(schedule)) {
				schedules.remove(schedule);
			}
			schedules.add(schedule);
		}		
	}
	
	private void removeFromSchedule(Schedule schedule) {
		synchronized (schedules) {
			if (schedules.contains(schedule))
				schedules.remove(schedule);
		}
	}
	
	private class Subscribe {
        private Method method;
        private Object sub;
        private String key;

        public Subscribe(Method method, Object sub, String key) {
            this.method = method;
            this.sub = sub;
            this.key = key;
        }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result
					+ ((method == null) ? 0 : method.hashCode());
			result = prime * result + ((sub == null) ? 0 : sub.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Subscribe other = (Subscribe) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			if (sub == null) {
				if (other.sub != null)
					return false;
			} else if (!sub.equals(other.sub))
				return false;
			return true;
		}

		private GenericAsyncEventProcessor getOuterType() {
			return GenericAsyncEventProcessor.this;
		}

    }
    
    private class Schedule {
    	private Object sub;
    	private AsyncEvent event;
    	private boolean once;
		
    	public Schedule(Object sub, AsyncEvent event, boolean once) {
			super();
			this.sub = sub;
			this.event = event;
			this.once = once;
		}
    	
    	public Schedule(Object sub, AsyncEvent event) {
			super();
			this.sub = sub;
			this.event = event;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((event == null) ? 0 : event.hashCode());
			result = prime * result + ((sub == null) ? 0 : sub.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Schedule other = (Schedule) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (event == null) {
				if (other.event != null)
					return false;
			} else if (!event.equals(other.event))
				return false;
			if (sub == null) {
				if (other.sub != null)
					return false;
			} else if (!sub.equals(other.sub))
				return false;
			return true;
		}

		private GenericAsyncEventProcessor getOuterType() {
			return GenericAsyncEventProcessor.this;
		}
    		
    }

}
