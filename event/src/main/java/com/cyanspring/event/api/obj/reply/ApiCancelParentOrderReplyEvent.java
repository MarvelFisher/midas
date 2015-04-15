package com.cyanspring.event.api.obj.reply;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.event.api.obj.PendingRecord;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
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
public class ApiCancelParentOrderReplyEvent implements IApiReply {
    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        CancelParentOrderReplyEvent replyEvent = (CancelParentOrderReplyEvent) event;
        PendingRecord record = resourceManager.getPendingRecord(replyEvent.getTxId());
        if(null == record)
            return;

        if(record.ctx.isOpen()){
            ParentOrder parentOrder = replyEvent.getOrder();
            Order order = new Order();
            order.setId(parentOrder.getId());
            order.setState(parentOrder.getState().toString());
            order.setStatus(parentOrder.getOrdStatus().toString());
            order.setPrice(parentOrder.getPrice());
            order.setQuantity(new Double(parentOrder.getQuantity()).longValue());
            order.setSide(OrderSide.valueOf(parentOrder.getSide().toString()));
            order.setType(OrderType.valueOf(parentOrder.getOrderType().toString()));
            order.setStopLossPrice(parentOrder.get(double.class,
                    OrderField.STOP_LOSS_PRICE.value()));
            order.setSymbol(parentOrder.getSymbol());

            record.ctx.send(new com.cyanspring.apievent.reply.CancelParentOrderReplyEvent(
                    replyEvent.getKey(), null, replyEvent.isOk(),
                    replyEvent.getMessage(), record.origTxId, order));
        }

    }
}
