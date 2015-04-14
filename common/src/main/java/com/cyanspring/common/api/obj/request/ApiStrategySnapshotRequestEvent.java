package com.cyanspring.common.api.obj.request;

import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.apievent.request.StrategySnapshotRequestEvent;
import com.cyanspring.common.api.ApiResourceManager;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
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
public class ApiStrategySnapshotRequestEvent implements IApiRequest {

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        StrategySnapshotRequestEvent snapshotRequestEvent = (StrategySnapshotRequestEvent) event;

        if(!resourceManager.checkAccount(snapshotRequestEvent.getKey(), ctx.getUser())){
            MessageBean messageBean = MessageLookup.lookup(ErrorMessage.ACCOUNT_NOT_MATCH);
            String msg = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_MATCH, messageBean.getMsg() + ": " + snapshotRequestEvent.getKey() + ", " + ctx.getUser());

            ctx.send(new SystemErrorEvent(null, null, 303, msg));
        }
        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, snapshotRequestEvent.getTxId(), ctx);

        resourceManager.sendEventToManager(new com.cyanspring.common.event.order.StrategySnapshotRequestEvent(snapshotRequestEvent.getKey(), snapshotRequestEvent.getReceiver(), txId));
    }
}
