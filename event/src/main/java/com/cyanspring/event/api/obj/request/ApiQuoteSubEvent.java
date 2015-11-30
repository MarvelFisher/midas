package com.cyanspring.event.api.obj.request;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.apievent.request.QuoteSubEvent;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.event.api.ApiResourceManager;

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
    	if (event == null || ctx == null) {
			return;
		}
        QuoteSubEvent subEvent = (QuoteSubEvent) event;
        String symbol = subEvent.getSymbol();
        String ctxUser = ctx.getUser();

        log.info("User: " + ctxUser + ", Symbol: " + symbol);
        if (null == ctxUser) {
			return;
		}

        // Restrict maximum number of quote per user to be <= 5
        List<String> lstSymbol = resourceManager.getQuoteSubsSymbolList(ctxUser);
        if (lstSymbol != null && lstSymbol.contains(symbol)) {
			return;
		}

        if (lstSymbol == null) {
        	lstSymbol = new ArrayList<>();
		}

        while (lstSymbol.size() > 4) {
	        Iterator<String> iterator = lstSymbol.iterator();
        	iterator.next();
        	iterator.remove();
        }

        resourceManager.putQuoteSubsSymbol(ctxUser, symbol);

        resourceManager.sendEventToManager(
                new com.cyanspring.common.event.marketdata.QuoteSubEvent(ctx.getUser(), resourceManager.getBridgeId(), subEvent.getSymbol()));
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

}
