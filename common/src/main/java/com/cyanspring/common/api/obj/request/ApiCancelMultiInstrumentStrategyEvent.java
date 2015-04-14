package com.cyanspring.common.api.obj.request;

import com.cyanspring.apievent.request.CancelMultiInstrumentStrategyEvent;
import com.cyanspring.common.api.ApiResourceManager;
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
    @Autowired
    ApiResourceManager resourceManager;

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
}
