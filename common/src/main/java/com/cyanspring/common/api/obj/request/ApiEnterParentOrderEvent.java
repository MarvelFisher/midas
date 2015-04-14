package com.cyanspring.common.api.obj.request;

import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.apievent.request.EnterParentOrderEvent;
import com.cyanspring.common.api.ApiResourceManager;
import com.cyanspring.common.business.OrderField;
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
public class ApiEnterParentOrderEvent implements IApiRequest {

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        EnterParentOrderEvent orderEvent = (EnterParentOrderEvent) event;

        String account = (String) orderEvent.getFields().get(OrderField.ACCOUNT.value());
        if (!resourceManager.checkAccount(account, ctx.getUser())) {
            MessageBean messageBean = MessageLookup.lookup(ErrorMessage.ACCOUNT_NOT_MATCH);
            String msg = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_MATCH, messageBean.getMsg() + ": " + orderEvent.getKey() + ", " + ctx.getUser());

            ctx.send(new SystemErrorEvent(null, null, 303,
                    msg));
        }

        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, orderEvent.getTxId(), ctx);

        com.cyanspring.common.event.order.EnterParentOrderEvent request =
                new com.cyanspring.common.event.order.EnterParentOrderEvent(
                        orderEvent.getKey(), orderEvent.getReceiver(), orderEvent.getFields(),
                        txId, false);
        request.getFields().put(OrderField.USER.value(), ctx.getUser());

        resourceManager.sendEventToManager(request);
    }
}
