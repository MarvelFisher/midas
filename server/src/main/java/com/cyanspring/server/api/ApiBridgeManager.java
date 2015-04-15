package com.cyanspring.server.api;

import java.util.Map;

import com.cyanspring.apievent.reply.ServerReadyEvent;
import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.event.api.ApiEventTranslator;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.event.api.obj.reply.IApiReply;
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
import com.cyanspring.event.AsyncPriorityEventThread;

public class ApiBridgeManager implements IPlugin, IAsyncEventBridge, IAsyncEventListener {
    private static Logger log = LoggerFactory.getLogger(ApiBridgeManager.class);

    @Autowired
    private IAsyncEventManager eventManager;

    @Autowired
    private ApiResourceManager resourceManager;

    private ApiEventTranslator translator = new ApiEventTranslator();

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
            IApiRequest tranObject = translator.translateRequest(obj);
            if (tranObject == null) {
                ctx.send(new SystemErrorEvent(null, null, 302, MessageLookup.buildEventMessage(ErrorMessage.EVENT_TYPE_NOT_SUPPORT, obj.getClass().toString())));
            } else if (ctx.getUser() == null) {
                ctx.send(new SystemErrorEvent(null, null, 301, MessageLookup.buildEventMessage(ErrorMessage.USER_NEED_LOGIN_BEFORE_EVENTS, "")));
            } else {
                tranObject.sendEventToLts(obj, ctx);
            }
        }

    };

    private AsyncPriorityEventThread thread = new AsyncPriorityEventThread() {

        @Override
        public void onEvent(AsyncEvent event) {
            IApiReply tranObject = translator.translateReply(event);
            if (tranObject != null)
                tranObject.sendEventToClient(event);
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
    }

    @Override
    public void uninit() {
        thread.exit();
    }
}
