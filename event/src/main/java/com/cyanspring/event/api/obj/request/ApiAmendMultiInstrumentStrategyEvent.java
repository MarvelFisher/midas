package com.cyanspring.event.api.obj.request;

import com.cyanspring.apievent.request.AmendMultiInstrumentStrategyEvent;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;

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
public class ApiAmendMultiInstrumentStrategyEvent implements IApiRequest {

    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        AmendMultiInstrumentStrategyEvent strategyEvent = (AmendMultiInstrumentStrategyEvent) event;
        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, strategyEvent.getTxId(), ctx);

        com.cyanspring.common.event.strategy.AmendMultiInstrumentStrategyEvent request =
                new com.cyanspring.common.event.strategy.AmendMultiInstrumentStrategyEvent(
                        strategyEvent.getKey(), strategyEvent.getReceiver(),
                        strategyEvent.getFields(), strategyEvent.getInstruments(), txId);

        resourceManager.sendEventToManager(request);
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
