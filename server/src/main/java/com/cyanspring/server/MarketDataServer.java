package com.cyanspring.server;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.system.DuplicateSystemIdEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.event.system.ServerHeartBeatEvent;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.server.event.ServerShutdownEvent;
import com.cyanspring.common.util.IdGenerator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.net.InetAddress;
import java.util.List;

/**
 * This server is used to collect market data from adaptor
 * and also can be query market data through activeMQ. The
 * server is a stand alone server which can execute for a
 * single process.
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class MarketDataServer {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    @Autowired
    private IRemoteEventManager eventManager;

    @Autowired
    private SystemInfo systemInfo;

    @Autowired
    ScheduleManager scheduleManager;

    private String inbox;
    private String uid;
    private String channel;
    private String nodeInfoChannel;
    private int heartBeatInterval = 3000; // milliseconds
    private ServerHeartBeatEvent heartBeat = new ServerHeartBeatEvent(null, null);
    private AsyncTimerEvent shutdownEvent = new AsyncTimerEvent();
    private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private List<IPlugin> plugins;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(NodeInfoEvent.class, null);
            subscribeToEvent(DuplicateSystemIdEvent.class, null);
            subscribeToEvent(MarketDataReadyEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }

    };

    public void processNodeInfoEvent(NodeInfoEvent event) throws Exception {
        if(event.getFirstTime() &&
                !event.getUid().equals(MarketDataServer.this.uid)) { // not my own message
            //check duplicate system id
            if (event.getServer() && event.getInbox().equals(MarketDataServer.this.inbox)) {
                log.error("Duplicated system id detected: " + event.getSender());
                DuplicateSystemIdEvent de =
                        new DuplicateSystemIdEvent(null, null, event.getUid());
                de.setSender(MarketDataServer.this.uid);
                eventManager.publishRemoteEvent(nodeInfoChannel, de);
            } else {
                // publish my node info
                NodeInfoEvent myInfo =
                        new NodeInfoEvent(null, null, true, false,
                                MarketDataServer.this.inbox, MarketDataServer.this.uid);
                eventManager.publishRemoteEvent(nodeInfoChannel, myInfo);
                log.info("Replied my nodeInfo");
            }
            eventManager.publishRemoteEvent(channel, new ServerReadyEvent(true));
        }
    }

    public void processDuplicateSystemIdEvent(DuplicateSystemIdEvent event) {
        if(event.getUid().equals(MarketDataServer.this.uid)) {
            log.error("System id duplicated: " + systemInfo.getId());
            log.error("Fatal error, existing system");
            System.exit(1);
        }
    }

    public void processMarketDataReadyEvent(MarketDataReadyEvent event) {
        log.info("Market data is ready: " + event.isReady());
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) throws Exception {
        if(event == timerEvent) {
            eventManager.publishRemoteEvent(nodeInfoChannel, heartBeat);
        } else if(event == shutdownEvent) {
            log.info("System hits end time, shutting down...");
            System.exit(0);
        }
    }

    public void init() throws Exception {
        IdGenerator.getInstance().setPrefix(systemInfo.getId()+"-");

        // create node.info subscriber and publisher
        log.info("SystemInfo: " + systemInfo);
        this.channel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "channel";
        this.nodeInfoChannel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "node";
        InetAddress addr = InetAddress.getLocalHost();
        String hostName = addr.getHostName();
        this.inbox = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + systemInfo.getId();
        IdGenerator.getInstance().setSystemId(this.inbox);
        this.uid = hostName + "." + IdGenerator.getInstance().getNextID();
        eventManager.init(channel, inbox);
        eventManager.addEventChannel(nodeInfoChannel);

        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if(eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("MarketDataServer");

        // ScheduleManager initialization
        log.debug("ScheduleManager initialized");
        scheduleManager.init();

        if(null != plugins) {
            for(IPlugin plugin: plugins) {
                plugin.init();
            }
        }

        // publish my node info
        NodeInfoEvent nodeInfo = new NodeInfoEvent(null, null, true, true, inbox, uid);
        // Set sender as uid. This is to cater the situation when
        // duplicate inbox happened, the other node can receive the NodeInfoEvent and detect it.
        // For this reason, one should never use NodeInfoEvent.getSender() to reply anything for this event
        nodeInfo.setSender(uid);

        eventManager.publishRemoteEvent(nodeInfoChannel, nodeInfo);
        log.info("Published my node info");


        // start heart beat
        scheduleManager.scheduleRepeatTimerEvent(heartBeatInterval, eventProcessor, timerEvent);
    }

    public void uninit() {
        log.info("uninitialising server");
        if(null != plugins) {
            //uninit in reverse order
            for(int i=plugins.size(); i>0; i--) {
                plugins.get(i-1).uninit();
            }
        }
        scheduleManager.uninit();
        eventProcessor.uninit();
        eventManager.uninit();

    }

    public void shutdown() {
        log.debug("");
        log.debug(">>>> CLOSING DOWN SERVER, PLEASE WAIT FOR ALL COMPONENTS CLEAN SHUTDOWN <<<");
        log.debug("");
        // stop heart beat
        scheduleManager.cancelTimerEvent(timerEvent);
        eventManager.sendEvent(new ServerShutdownEvent());
        try { //give it 2 seconds for an opportunity of clean shutdown
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        uninit();
        System.exit(0);
    }

    public void setPlugins(List<IPlugin> plugins) {
        this.plugins = plugins;
    }

    public static void main(String[] args) throws Exception {
        String settingURL = "conf/mdserver.xml";
        String logURL = "conf/common/mylog4j.xml";
        if (args.length == 2){
            settingURL = args[0];
            logURL = args[1];
        }

        DOMConfigurator.configure(logURL);
        ApplicationContext context = new FileSystemXmlApplicationContext(settingURL);

        MarketDataServer mdServer = (MarketDataServer) context.getBean("mdServer");
        mdServer.init();
    }
}
