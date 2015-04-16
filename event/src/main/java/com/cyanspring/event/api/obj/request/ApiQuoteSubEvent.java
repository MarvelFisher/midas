package com.cyanspring.event.api.obj.request;

import com.cyanspring.apievent.request.QuoteSubEvent;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.transport.IUserSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class ApiQuoteSubEvent implements IApiRequest {
    private Logger log = LoggerFactory.getLogger(ApiQuoteSubEvent.class);

    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        QuoteSubEvent subEvent = (QuoteSubEvent) event;

        log.info("User: " + ctx.getUser() + ", Symbol: " + subEvent.getSymbol());
        if(null == ctx.getUser())
            return;

        Map<String, String> userSymbol = resourceManager.getSubscriptionMap(subEvent.getSymbol());
        if(null == userSymbol) {
            userSymbol = new ConcurrentHashMap<String, String>();
            resourceManager.putQuoteSubscription(subEvent.getSymbol(), userSymbol);
        }

        userSymbol.put(ctx.getUser(), subEvent.getSymbol());

        resourceManager.sendEventToManager(
                new com.cyanspring.common.event.marketdata.QuoteSubEvent(ctx.getUser(), resourceManager.getBridgeId(), subEvent.getSymbol()));
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
