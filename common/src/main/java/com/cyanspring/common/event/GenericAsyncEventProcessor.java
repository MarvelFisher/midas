package com.cyanspring.common.event;

import com.cyanspring.common.IPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

    public boolean subscribeToEvent(Class<? extends AsyncEvent> event, Object handler, String key) {
        if (!isInit) {
            try {
                init();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }
        try {
            String eventName = event.getSimpleName();
            List<Subscribe> subs = events.get(eventName);
            if (subs == null)
                subs = new ArrayList<>();
            Method method = handler.getClass().getMethod(getEventName(eventName), event.getClasses());
            Subscribe pair = new Subscribe(method, handler, key);
            if (!subs.contains(pair))
                subs.add(pair);
            eventManager.subscribe(event, this);
            log.info("Class: " + handler.getClass().getSimpleName() + ", method: " + eventName + " added in method map");
        } catch (Exception e) {
            log.error("Can't subscribe event to " + handler.getClass().getSimpleName() +
                    ", please check this class");
            return false;
        }
        return true;
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
        public boolean equals(Object obj) {
            Subscribe c = (Subscribe) obj;
            return method.getName().equals(c.method.getName()) &&
                    sub.equals(c.sub);
        }
    }

    @Override
    public void onEvent(AsyncEvent event) {
        thread.addEvent(event);
    }

    private void onAsyncEvent(AsyncEvent event) {
        List<Subscribe> subs = events.get(event.getClass().getSimpleName());
        if (subs == null) {
            log.error("Unhandle event: " + event.getClass().getSimpleName());
            return;
        }

        String key = event.getKey();
        for (Subscribe sub : subs) {
            try {
                if (key != null) {
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
        thread.exit();
        eventManager = null;
        events = null;
    }

    public void setEventManager(IRemoteEventManager eventManager) {
        this.eventManager = eventManager;
    }

    private String getEventName(String event) {
        return "process" + event;
    }
}
