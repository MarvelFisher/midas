package com.cyanspring.event.api.obj.reply;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.type.StrategyState;
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
public class ApiParentOrderUpdateEvent implements IApiReply{
    @Autowired
    ApiResourceManager resourceManager;
    @Override
    public void sendEventToClient(Object event) {
        ParentOrderUpdateEvent updateEvent = (ParentOrderUpdateEvent) event;
        ParentOrder parentOrder = updateEvent.getOrder();
        if(parentOrder.getState() == StrategyState.Terminated || parentOrder.getOrdStatus().isCompleted())
            resourceManager.removeOrder(parentOrder.getId());
        else if(resourceManager.hasUser(parentOrder.getAccount())) {
            resourceManager.putOrder(parentOrder.getId(), parentOrder);
        }

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

        com.cyanspring.apievent.reply.ParentOrderUpdateEvent sendEvent =
                new com.cyanspring.apievent.reply.ParentOrderUpdateEvent(
                        updateEvent.getKey(), updateEvent.getReceiver(),
                        updateEvent.getTxId(), order,
                        updateEvent.getInfo()
                );
        resourceManager.sendEventToUser(parentOrder.getUser(), sendEvent);
    }
}
