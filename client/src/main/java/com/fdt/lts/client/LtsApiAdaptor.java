package com.fdt.lts.client;


import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.reply.*;
import com.cyanspring.apievent.request.*;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.ClientSocketEventManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LtsApiAdaptor {
    private static Logger log = LoggerFactory.getLogger(LtsApiAdaptor.class);
    private String user = "test1";
    private String account = "test1-FX";
    private String password = "xxx";
    @Autowired
    private IRemoteEventManager eventManager = new ClientSocketEventManager();

    protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(ServerReadyEvent.class, null);
            subscribeToEvent(QuoteEvent.class, null);
            subscribeToEvent(EnterParentOrderReplyEvent.class, getId());
            subscribeToEvent(AmendParentOrderReplyEvent.class, getId());
            subscribeToEvent(CancelParentOrderReplyEvent.class, getId());
            subscribeToEvent(ParentOrderUpdateEvent.class, null);
            subscribeToEvent(StrategySnapshotEvent.class, null);
            subscribeToEvent(UserLoginReplyEvent.class, null);
            subscribeToEvent(AccountSnapshotReplyEvent.class, null);
            subscribeToEvent(AccountUpdateEvent.class, null);
            subscribeToEvent(OpenPositionUpdateEvent.class, null);
            subscribeToEvent(ClosedPositionUpdateEvent.class, null);
            subscribeToEvent(SystemErrorEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }

    };

    public void init() throws Exception {
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("LtsApiAdaptor");

        eventManager.init(null, null);
    }

    private void sendEvent(RemoteAsyncEvent event) {
        try {
            eventManager.sendRemoteEvent(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processServerReadyEvent(ServerReadyEvent event) {
        log.debug("Received ServerReadyEvent: " + event.getSender() + ", " + event.isReady());
        if (event.isReady()) {
            sendEvent(new UserLoginEvent(getId(), null, user, password, IdGenerator.getInstance().getNextID()));
        }
    }

    public void processAccountSnapshotReplyEvent(AccountSnapshotReplyEvent event) {
        log.debug("### Account Snapshot Start ###");
        log.debug("Account: " + event.getKey());
        log.debug("Open positions: " + event.getOpenPositions());
//        log.debug("Closed positions: " + event.getClosedPositions());
        log.debug("Trades :" + event.getExecutions());
        log.debug("### Account Snapshot End ###");
    }

    public void processAccountUpdateEvent(AccountUpdateEvent event) {
        log.debug("Account: " + event.getAccount());
    }

    public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
        log.debug("Position: " + event.getPosition());
    }

    public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
        log.debug("Closed Position: " + event.getPosition());
    }

    public void processQuoteEvent(QuoteEvent event) {
        log.debug("Received QuoteEvent: " + event.getKey() + ", " + event.getQuote());
    }

    public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
        log.debug("User login is " + event.isOk() + ", " + event.getMessage());

        if (!event.isOk())
            return;

        sendEvent(new QuoteSubEvent(getId(), null, "AUDUSD"));
        sendEvent(new QuoteSubEvent(getId(), null, "USDJPY"));
        sendEvent(new StrategySnapshotRequestEvent(account, null, null));
        sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
        sendEvent(OrderUtil.createLimitOrder("AUDUSD", OrderSide.Buy, 0.700, 20000, user, account));
    }

    public void processStrategySnapshotEvent(StrategySnapshotEvent event) {
        List<Order> orders = event.getOrders();
        log.debug("### Start order list ###");
        for (Order order : orders) {
            log.debug("Order: " + order);
        }
        log.debug("### End order list ###");
    }

    public void processEnterParentOrderReplyEvent(
            EnterParentOrderReplyEvent event) {
        if (!event.isOk()) {
            log.debug("Received EnterParentOrderReplyEvent(NACK): " + event.getMessage());
        } else {
            log.debug("Received EnterParentOrderReplyEvent(ACK)");
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put(OrderField.PRICE.value(), 0.81);
            fields.put(OrderField.QUANTITY.value(), (long)3000);
            AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(), null,
                    event.getOrder().getId(), fields, IdGenerator.getInstance().getNextID());
            sendEvent(amendEvent);
        }
    }

    public void processAmendParentOrderReplyEvent(
            AmendParentOrderReplyEvent event) {
        if (event.isOk()) {
            log.debug("Received AmendParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
            CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(getId(), null,
                    event.getOrder().getId(), false, IdGenerator.getInstance().getNextID());
            sendEvent(cancelEvent);
        } else {
            log.debug("Received AmendParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
        }
    }

    public void processCancelParentOrderReplyEvent(
            CancelParentOrderReplyEvent event) {
        if (event.isOk()) {
            log.debug("Received CancelParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
        } else {
            log.debug("Received CancelParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
        }
    }

    public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
        log.debug("Received ParentOrderUpdateEvent: " + ", order: " + event.getOrder());
    }

    public void processSystemErrorEvent(SystemErrorEvent event) {
        log.error("Error code: " + event.getErrorCode() + " - " + event.getMessage());
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String getId() {
        return user;
    }

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("conf/apilog4j.xml");
        String configFile = "conf/api.xml";
        if (args.length > 0)
            configFile = args[0];
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);

        // start server
        LtsApiAdaptor adaptor = (LtsApiAdaptor) context.getBean("apiAdaptor");
        adaptor.init();
    }
}
