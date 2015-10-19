package com.cyanspring.server;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author elviswu
 */
public class PositionForwarder implements IPlugin{
    private static final Logger log = LoggerFactory.getLogger(PositionForwarder.class);
    private IRemoteEventManager eventManager;
    private IRemoteEventManager globalEventManager;
    private Queue<RemoteAsyncEvent> eventQueue = new LinkedList<>();
    private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private long throttle = 10 * 1000;
    private int size = 100;

    @Autowired
    private ScheduleManager scheduleManager;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
        @Override
        public void subscribeToEvents() {
            subscribeToEvent(OpenPositionUpdateEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
        putEventInQueue(event);
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        for (int i = 0; i < size; i++) {
            if (eventQueue.size() > 0) {
                RemoteAsyncEvent evn = eventQueue.poll();
                try {
                    globalEventManager.sendRemoteEvent(evn);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void init() throws Exception {
        log.info("initializing...");
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("PositionForwarder");

        scheduleManager.scheduleRepeatTimerEvent(throttle, eventProcessor, timerEvent);
    }

    @Override
    public void uninit() {
        eventProcessor.uninit();
    }

    private void putEventInQueue(RemoteAsyncEvent event) {
        eventQueue.offer(event);
    }

    public IRemoteEventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(IRemoteEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public IRemoteEventManager getGlobalEventManager() {
        return globalEventManager;
    }

    public void setGlobalEventManager(IRemoteEventManager globalEventManager) {
        this.globalEventManager = globalEventManager;
    }

    public void setThrottle(long throttle) {
        this.throttle = throttle;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
