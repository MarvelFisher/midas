package com.cyanspring.event.api.obj.reply;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.event.api.obj.PendingRecord;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.order.StrategySnapshotEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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
public class ApiStrategySnapshotEvent implements IApiReply{

    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        StrategySnapshotEvent snapshotEvent = (StrategySnapshotEvent) event;
        PendingRecord record = resourceManager.getPendingRecord(snapshotEvent.getTxId());
        if(null == record)
            return;

        if(record.ctx.isOpen()){
            List<Order> orders = new ArrayList<Order>();
            for (ParentOrder parentOrder : snapshotEvent.getOrders()){
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
                orders.add(order);
            }

            record.ctx.send(new com.cyanspring.apievent.reply.StrategySnapshotEvent(
                    snapshotEvent.getKey(), snapshotEvent.getReceiver(),
                    orders, record.origTxId));
        }
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
