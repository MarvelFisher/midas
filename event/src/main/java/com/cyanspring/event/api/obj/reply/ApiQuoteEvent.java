package com.cyanspring.event.api.obj.reply;

import com.cyanspring.apievent.obj.Quote;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

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
public class ApiQuoteEvent implements IApiReply {

    @Autowired
    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        QuoteEvent quoteEvent = (QuoteEvent) event; // not translate to client quote yet
        com.cyanspring.common.marketdata.Quote ltsQuote = quoteEvent.getQuote();

        Quote quote = new Quote(ltsQuote.getId(), ltsQuote.getSymbol(), ltsQuote.getTimeStamp());
        quote.cloneQuote(ltsQuote.getBid(), ltsQuote.getAsk(),
                ltsQuote.getBidVol(), ltsQuote.getAskVol(),
                ltsQuote.getLast(), ltsQuote.getLastVol(),
                ltsQuote.getHigh(), ltsQuote.getLow(),
                ltsQuote.getOpen(), ltsQuote.getClose(),
                ltsQuote.getTotalVolume(), ltsQuote.isStale());

        com.cyanspring.apievent.reply.QuoteEvent sendEvent =
                new com.cyanspring.apievent.reply.QuoteEvent(quoteEvent.getKey(), quoteEvent.getReceiver(), quote);

        if(quoteEvent.getReceiver() != null) {
            resourceManager.sendEventToUser(quoteEvent.getKey(), sendEvent); //in this case key is user
        } else {
            Map<String, String> userSymbol = resourceManager.getSubscriptionMap(quoteEvent.getQuote().getSymbol());
            if(null != userSymbol) {
                for(String user: userSymbol.keySet())
                    resourceManager.sendEventToUser(user, sendEvent);
            }
        }
    }
}
