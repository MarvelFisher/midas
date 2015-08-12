package com.cyanspring.event.api.obj.request;

import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.apievent.reply.AmendParentOrderReplyEvent;
import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.apievent.request.AmendParentOrderEvent;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.EventPriority;
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
public class ApiAmendParentOrderEvent implements IApiRequest{

    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        AmendParentOrderEvent orderEvent = (AmendParentOrderEvent) event;


        ParentOrder prev = resourceManager.getOrder(orderEvent.getId());

        if(null == prev) {
            String message = MessageLookup.buildEventMessage(ErrorMessage.AMEND_ORDER_NOT_FOUND, "Can't find order to amend");
            ctx.send(new AmendParentOrderReplyEvent(orderEvent.getKey(), null, false,
                    message, orderEvent.getTxId(), null));
            return;
        }

        if(!resourceManager.checkAccount(prev.getAccount(), ctx.getUser())){
            MessageBean messageBean =MessageLookup.lookup(ErrorMessage.ACCOUNT_NOT_MATCH);
            String msg = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_MATCH, messageBean.getMsg() + ": " + orderEvent.getKey() + ", " + ctx.getUser());

            ctx.send(new SystemErrorEvent(null, null, 303,
                    msg));
            return;
        }

        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, orderEvent.getTxId(), ctx);

        translateOrder(orderEvent.getFields());
        com.cyanspring.common.event.order.AmendParentOrderEvent request =
                new com.cyanspring.common.event.order.AmendParentOrderEvent(orderEvent.getKey(),
                orderEvent.getReceiver(), orderEvent.getId(), orderEvent.getFields(), txId);
        request.setPriority(EventPriority.HIGH);;
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
