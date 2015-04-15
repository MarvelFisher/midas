package com.cyanspring.event.api.obj.request;

import com.cyanspring.apievent.request.NewSingleInstrumentStrategyEvent;
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
public class ApiNewSingleInstrumentStrategyEvent implements IApiRequest {

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        NewSingleInstrumentStrategyEvent strategyEvent = (NewSingleInstrumentStrategyEvent) event;

        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, strategyEvent.getTxId(), ctx);

        com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyEvent request =
                new com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyEvent(
                        strategyEvent.getKey(), strategyEvent.getReceiver(),
                        txId, strategyEvent.getInstrument());

        resourceManager.sendEventToManager(request);
    }
}
