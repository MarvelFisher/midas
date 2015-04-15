package com.cyanspring.event.api.obj.reply;

import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.event.api.obj.PendingRecord;
import com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyReplyEvent;
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
public class ApiCancelMultiInstrumentStrategyReplyEvent implements IApiReply{

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        CancelMultiInstrumentStrategyReplyEvent replyEvent = (CancelMultiInstrumentStrategyReplyEvent) event;
        PendingRecord record = resourceManager.getPendingRecord(replyEvent.getTxId());
        if(null == record)
            return;

        if(record.ctx.isOpen())
            record.ctx.send(new com.cyanspring.apievent.reply.CancelMultiInstrumentStrategyReplyEvent(
                    replyEvent.getKey(), replyEvent.getReceiver(), record.origTxId,
                    replyEvent.isSuccess(), replyEvent.getMessage()));
    }
}
