package com.cyanspring.soak;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
//import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Description....
 * <ul>
 * <li> Description
 * </ul>
 * <p/>
 * Description....
 * <p/>
 * Description....
 * <p/>
 * Description....
 *
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class IndexMarketSessionTest extends ClientAdaptor {
    private static Logger log = LoggerFactory.getLogger(ClientAdaptor.class);
    @Override
    public void subscribeToEvents() {
        super.subscribeToEvents();
        subscribeToEvent(ServerReadyEvent.class, null);
//        subscribeToEvent(IndexSessionEvent.class, null);
    }

//    public void processIndexSessionEvent(IndexSessionEvent event){
//        log.info(event.getDataMap().toString());
//    }

    public void processServerReadyEvent(ServerReadyEvent event) {
    }

    @Override
    public void processServerStatusEvent(String server, boolean up) {
    }

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("conf/log4j.xml");
        String configFile = "conf/indexMarketSession.xml";
        if(args.length>0)
            configFile = args[0];
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);

        // start server
        IndexMarketSessionTest bean = (IndexMarketSessionTest)context.getBean("indexMarketSessionTest");
        bean.init();
    }
}
