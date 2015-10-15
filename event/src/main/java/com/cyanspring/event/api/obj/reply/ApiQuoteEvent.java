package com.cyanspring.event.api.obj.reply;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cyanspring.apievent.obj.Quote;
import com.cyanspring.common.event.marketdata.QuoteEvent;
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
public class ApiQuoteEvent implements IApiReply {

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

        if (quoteEvent.getReceiver() != null) {
            resourceManager.sendEventToUser(quoteEvent.getKey(), sendEvent); //in this case key is user
        } else {
        	String symbol = quoteEvent.getQuote().getSymbol();
            Map<String, List<String>> mapQuoteSubs = resourceManager.getQuoteSubs();
            if (mapQuoteSubs != null && mapQuoteSubs.size() > 0) {
	            Set<Map.Entry<String, List<String>>> entries = mapQuoteSubs.entrySet();
	            for (Map.Entry<String, List<String>> entry : entries) {
	            	String user = entry.getKey();
	            	List<String> lstSymbol = entry.getValue();
	            	while (lstSymbol != null && lstSymbol.contains(symbol)) {
						lstSymbol.remove(symbol);
						resourceManager.sendEventToUser(user, sendEvent);
					}
	            }
            }
        }
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

}
