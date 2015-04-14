package com.cyanspring.common.api.obj.reply;

import com.cyanspring.common.api.ApiResourceManager;
import com.cyanspring.common.api.obj.PendingRecord;
import com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyReplyEvent;
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
public class ApiNewSingleInstrumentStrategyReplyEvent implements IApiReply {

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        NewSingleInstrumentStrategyReplyEvent replyEvent = (NewSingleInstrumentStrategyReplyEvent) event;

        PendingRecord record = resourceManager.getPendingRecord(replyEvent.getTxId());
        if(null == record)
            return;

        if(record.ctx.isOpen())
            record.ctx.send(new com.cyanspring.apievent.reply.NewSingleInstrumentStrategyReplyEvent(
                    replyEvent.getKey(), replyEvent.getReceiver(), record.origTxId,
                    replyEvent.isSuccess(), replyEvent.getMessage()));
    }
}
