package com.cyanspring.common.api.obj.request;

import com.cyanspring.apievent.reply.AmendParentOrderReplyEvent;
import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.apievent.request.AmendParentOrderEvent;
import com.cyanspring.common.api.ApiResourceManager;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.EventPriority;
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
public class ApiAmendParentOrderEvent implements IApiRequest{

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        AmendParentOrderEvent orderEvent = (AmendParentOrderEvent) event;


        ParentOrder prev = resourceManager.getOrder(orderEvent.getId());

        if(null == prev) {
            String message = MessageLookup.buildEventMessage(ErrorMessage.AMEND_ORDER_NOT_FOUND, "Can't find order to amend");
            ctx.send(new AmendParentOrderReplyEvent(orderEvent.getKey(), null, false,
                    message, orderEvent.getTxId(), null));
        }

        if(!resourceManager.checkAccount(prev.getAccount(), ctx.getUser())){
            MessageBean messageBean =MessageLookup.lookup(ErrorMessage.ACCOUNT_NOT_MATCH);
            String msg = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_MATCH, messageBean.getMsg() + ": " + orderEvent.getKey() + ", " + ctx.getUser());

            ctx.send(new SystemErrorEvent(null, null, 303,
                    msg));
        }

        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, orderEvent.getTxId(), ctx);

        com.cyanspring.common.event.order.AmendParentOrderEvent request =
                new com.cyanspring.common.event.order.AmendParentOrderEvent(orderEvent.getKey(),
                orderEvent.getReceiver(), orderEvent.getId(), orderEvent.getFields(), txId);
        request.setPriority(EventPriority.HIGH);;
        resourceManager.sendEventToManager(request);
    }
}
