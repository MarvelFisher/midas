package com.cyanspring.server.validation;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.PositionKeeper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class AvailablePositionValidator implements IOrderValidator {

    @Autowired
    AccountKeeper accountKeeper;

    @Autowired
    PositionKeeper positionKeeper;

    @Override
    public void validate(Map<String, Object> map, ParentOrder order) throws OrderValidationException {

        try {
            Double qty = (Double) map.get(OrderField.QUANTITY.value());
            if (null == qty) {
                return;
            }

            OrderSide orderSide;
            String symbol;
            String accountId;

            if(order == null) {
                orderSide = (OrderSide) map.get(OrderField.SIDE.value());
                symbol = (String) map.get(OrderField.SYMBOL.value());
                accountId = (String)map.get(OrderField.ACCOUNT.value());
            } else {
                orderSide = order.getSide();
                symbol = order.getSymbol();
                accountId = order.getAccount();
            }

            if (Default.getSettlementDays() > 0 && orderSide.isSell()) {

                Account account = accountKeeper.getAccount(accountId);

                double availableQty = positionKeeper.getOverallPosition(account, symbol).getAvailableQty();

                if (!checkQty(account, symbol, qty, availableQty, order)) {
                    throw new OrderValidationException("Sell quantity exceeded available position quantity", ErrorMessage.QUANTITY_EXCEED_AVAILABLE_QUANTITY);
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

        return availableQty - pendingSellQty + oldQty - qty >= 0;
    }
}
