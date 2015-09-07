package com.cyanspring.server.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.apievent.reply.ServerReadyEvent;
import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.event.api.ApiEventTranslator;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.event.api.IEventTranslatror;
import com.cyanspring.event.api.obj.SpamController;
import com.cyanspring.event.api.obj.reply.IApiReply;
import com.cyanspring.event.api.obj.request.ApiUserLoginEvent;
import com.cyanspring.event.api.obj.request.IApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventBridge;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.transport.IServerSocketListener;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.event.AsyncPriorityEventThread;

public class ApiBridgeManager implements IPlugin, IAsyncEventBridge, IAsyncEventListener {
    private static Logger log = LoggerFactory.getLogger(ApiBridgeManager.class);

    @Autowired
    private IAsyncEventManager eventManager;

    @Autowired
    private ApiResourceManager resourceManager;

    private IEventTranslatror translator = new ApiEventTranslator();
    private Map<String, String> requestMap = new HashMap<String, String>();
    private Map<String, String> replyMap = new HashMap<String, String>();
    private Map<String, SpamController> spamMap = new HashMap<>();
    private int restrict = 1000;

    private IServerSocketListener listener = new IServerSocketListener() {
        @Override
        public void onConnected(boolean connected, IUserSocketContext ctx) {
            if (connected) {
                ctx.send(new ServerReadyEvent(connected));
            } else {
                for (Map<String, String> map : resourceManager.getAllSubscriptionbyList()) {
                    String symbol = map.remove(ctx.getId());
                    log.info("Remove symbol subscription: " + ctx.getUser() + ", " + symbol);
                }
            }
        }

        @Override
        public void onMessage(Object obj, IUserSocketContext ctx) {
        	if (ctx.getUser() != null) {
        		SpamController con = spamMap.get(ctx.getUser());
                if (con == null) {
                	con = new SpamController(ctx.getUser(), restrict);
                	spamMap.put(ctx.getUser(), con);
                }
                
                if (!con.checkAndCount(Calendar.getInstance())) {
                	log.info("Account: " + con.getAccount() + " reach max access limit.");
                    ctx.send(new SystemErrorEvent(null, null, 305, MessageLookup.buildEventMessage(ErrorMessage.REACH_MAX_ACCESS_LIMIT, "")));
                    return;
                }
        	}
            
            IApiRequest tranObject = translator.translateRequest(obj);
            if (tranObject == null) {
                ctx.send(new SystemErrorEvent(null, null, 302, MessageLookup.buildEventMessage(ErrorMessage.EVENT_TYPE_NOT_SUPPORT, obj.getClass().toString())));
            } else if (tranObject instanceof ApiUserLoginEvent) {
                tranObject.setResourceManager(resourceManager);
                tranObject.sendEventToLts(obj, ctx);
            } else if (ctx.getUser() == null) {
                ctx.send(new SystemErrorEvent(null, null, 301, MessageLookup.buildEventMessage(ErrorMessage.USER_NEED_LOGIN_BEFORE_EVENTS, "")));
            } else {
                tranObject.setResourceManager(resourceManager);
                tranObject.sendEventToLts(obj, ctx);
            }
        }

    };

    private AsyncPriorityEventThread thread = new AsyncPriorityEventThread() {

        @Override
        public void onEvent(AsyncEvent event) {
            IApiReply tranObject = translator.translateReply(event);
            if (tranObject != null){
                tranObject.setResourceManager(resourceManager);
                tranObject.sendEventToClient(event);
            }
        }

    };

    @Override
    public void onEvent(AsyncEvent event) {
        thread.addEvent(event);
    }

//	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
//		sendEventToUser(event.getPosition().getUser(), event);
//	}

//	public void processAccountDynamicUpdateEvent(AccountDynamicUpdateEvent event) {
//
//	}

//	public void processOpenPositionDynamicUpdateEvent(OpenPositionDynamicUpdateEvent event) {
//
//	}

//	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
//
//	}

    @Override
    public String getBridgeId() {
        return resourceManager.getBridgeId();
    }

    @Override
    public void onBridgeEvent(RemoteAsyncEvent event) {
        thread.addEvent(event);
    }

    @Override
    public void init() throws Exception {
        thread.setName("ApiBridgeManager");
        thread.start();

        resourceManager.init(this.listener);
        translator.init(requestMap, replyMap);
    }

    @Override
    public void uninit() {
        thread.exit();
    }

    public void setReplyMap(Map<String, String> replyMap) {
        this.replyMap = replyMap;
    }

    public void setRequestMap(Map<String, String> requestMap) {
        this.requestMap = requestMap;
    }
    
    public void setRestrict(int restrict) {
    	this.restrict = restrict;
    }
}
