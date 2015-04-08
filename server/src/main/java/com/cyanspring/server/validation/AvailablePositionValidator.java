package com.cyanspring.server.validation;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.PositionKeeper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class AvailablePositionValidator implements IOrderValidator {

    @Autowired
    PositionKeeper positionKeeper;

    @Override
    public void validate(Map<String, Object> map, ParentOrder order) throws OrderValidationException {

        try {
            OrderSide orderSide = (OrderSide) map.get(OrderField.SIDE.value());

            if (Default.getSettlementDays() > 0 && orderSide.isSell()) {

                double qty = (Double) map.get(OrderField.QUANTITY.value());

                String symbol;
                if(order == null) {
                    symbol = (String) map.get(OrderField.SYMBOL.value());
                } else {
                    symbol = order.getSymbol();
                }

                String accountId = (String)map.get(OrderField.ACCOUNT.value());
                String userId = (String)map.get(OrderField.USER.value());

                Account account = new Account(accountId, userId);

                double availableQty = positionKeeper.getOverallPosition(account, symbol).getAvailableQty();

                if (!checkQty(account, symbol, qty, availableQty, order)) {
                    throw new OrderValidationException("Quantity exceeded available quantity", ErrorMessage.QUANTITY_EXCEED_AVAILABLE_QUANTITY);
                }
            }

        } catch (OrderValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderValidationException(e.getMessage(), ErrorMessage.VALIDATION_ERROR);
        }
    }

    private boolean checkQty(Account account, String symbol, double qty, double availableQty, ParentOrder order) {

        List<ParentOrder> orders = positionKeeper.getParentOrders(account.getId(), symbol);

        double pendingSellQty = 0;

        for (ParentOrder o : orders) {

            if (o.getOrdStatus().isCompleted() || !o.getSide().isSell()) {
                continue;
            }

            pendingSellQty += o.getRemainingQty();
        }

        double oldQty = 0;
        if (null != order) {
            oldQty = order.getRemainingQty();
        }

        // Sell quantity is negative value.
        return availableQty + pendingSellQty - oldQty + qty >= 0;
    }
}
