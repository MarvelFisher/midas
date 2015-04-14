package com.cyanspring.common.api.obj.request;

import com.cyanspring.apievent.request.AmendSingleInstrumentStrategyEvent;
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
public class ApiAmendSingleInstrumentStrategyEvent implements IApiRequest {
    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        AmendSingleInstrumentStrategyEvent strategyEvent = (AmendSingleInstrumentStrategyEvent) event;

        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, strategyEvent.getTxId(), ctx);

        com.cyanspring.common.event.strategy.AmendSingleInstrumentStrategyEvent request =
                new com.cyanspring.common.event.strategy.AmendSingleInstrumentStrategyEvent(
                        strategyEvent.getKey(), strategyEvent.getReceiver(),
                        strategyEvent.getFields(), txId);

        resourceManager.sendEventToManager(request);
    }
}
