package com.fdt.lts.client;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.apievent.request.AmendParentOrderEvent;
import com.cyanspring.apievent.request.EnterParentOrderEvent;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.util.IdGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class OrderUtil {
    public static EnterParentOrderEvent createLimitOrder(String symbol, OrderSide side, double price, long qty,
                                                         String user, String account) {
        // SDMA
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(OrderField.SYMBOL.value(), symbol);
        fields.put(OrderField.SIDE.value(), side);
        fields.put(OrderField.TYPE.value(), OrderType.Limit);
        fields.put(OrderField.PRICE.value(), price);
        fields.put(OrderField.QUANTITY.value(), qty);
        fields.put(OrderField.STRATEGY.value(), "SDMA");
        fields.put(OrderField.USER.value(), user);
        fields.put(OrderField.ACCOUNT.value(), account);
        EnterParentOrderEvent enterOrderEvent = new EnterParentOrderEvent(user, null, fields, IdGenerator.getInstance().getNextID(), false);
        return enterOrderEvent;
    }

    public static EnterParentOrderEvent createStopOrder(String symbol, OrderSide side, long qty,
                                                        String price, String user, String account) {
        // SDMA
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(OrderField.SYMBOL.value(), symbol);
        fields.put(OrderField.SIDE.value(), side);
        fields.put(OrderField.TYPE.value(), OrderType.Market);
        //fields.put(OrderField.PRICE.value(), 0.87980);
        fields.put(OrderField.QUANTITY.value(), qty);
        fields.put(OrderField.STRATEGY.value(), "STOP");
        fields.put(OrderField.STOP_LOSS_PRICE.value(), price);
        fields.put(OrderField.USER.value(), user);
        fields.put(OrderField.ACCOUNT.value(), account);
        EnterParentOrderEvent enterOrderEvent = new EnterParentOrderEvent(user, null, fields, IdGenerator.getInstance().getNextID(), false);
        return enterOrderEvent;
    }

    public static EnterParentOrderEvent createMarketOrder(String symbol, OrderSide side, long qty,
                                                          String user, String account) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(OrderField.SYMBOL.value(), symbol);
        fields.put(OrderField.SIDE.value(), side);
        fields.put(OrderField.TYPE.value(), OrderType.Market);
        fields.put(OrderField.QUANTITY.value(), qty);
        fields.put(OrderField.STRATEGY.value(), "SDMA");
        fields.put(OrderField.USER.value(), user);
        fields.put(OrderField.ACCOUNT.value(), account);
        EnterParentOrderEvent enterOrderEvent = new EnterParentOrderEvent(user, null, fields, IdGenerator.getInstance().getNextID(), false);
        return enterOrderEvent;
    }
    
    public static AmendParentOrderEvent amendOrder(String user, String id, double price, long qty) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(OrderField.PRICE.value(), price);
        fields.put(OrderField.QUANTITY.value(), qty);
        AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(user, null,
                id, fields, IdGenerator.getInstance().getNextID());
        return amendEvent;
    }
}
