package com.cyanspring.event.api.obj.request;

import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.apievent.request.EnterParentOrderEvent;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

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

    private ApiResourceManager resourceManager;

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

        translateOrder(orderEvent.getFields());
        com.cyanspring.common.event.order.EnterParentOrderEvent request =
                new com.cyanspring.common.event.order.EnterParentOrderEvent(
                        orderEvent.getKey(), orderEvent.getReceiver(), orderEvent.getFields(),
                        txId, false);
        request.getFields().put(OrderField.USER.value(), ctx.getUser());

        resourceManager.sendEventToManager(request);
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    private void translateOrder(Map<String, Object> order){
        Long qty = (Long) order.get(OrderField.QUANTITY.value());
        if (qty != null)
            order.put(OrderField.QUANTITY.value(), qty.doubleValue());
        OrderSide side = (OrderSide) order.get(OrderField.SIDE.value());
        if (side != null)
            order.put(OrderField.SIDE.value(), com.cyanspring.common.type.OrderSide.valueOf(side.toString()));
        OrderType type = (OrderType) order.get(OrderField.TYPE.value());
        if (type != null)
            order.put(OrderField.TYPE.value(), com.cyanspring.common.type.OrderType.valueOf(type.toString()));
    }
}
