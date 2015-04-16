package com.cyanspring.event.api.obj.request;

import com.cyanspring.apievent.request.CancelMultiInstrumentStrategyEvent;
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
public class ApiCancelMultiInstrumentStrategyEvent implements IApiRequest {

    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        CancelMultiInstrumentStrategyEvent strategyEvent = (CancelMultiInstrumentStrategyEvent) event;
        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, strategyEvent.getTxId(), ctx);

        com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyEvent request =
                new com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyEvent(
                        strategyEvent.getKey(), strategyEvent.getReceiver(), txId);

        resourceManager.sendEventToManager(request);
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
