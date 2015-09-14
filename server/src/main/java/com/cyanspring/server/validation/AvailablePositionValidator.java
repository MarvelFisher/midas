package com.cyanspring.server.validation;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.PositionKeeper;

public class AvailablePositionValidator implements IOrderValidator {
	private static final Logger log = LoggerFactory.getLogger(AvailablePositionValidator.class);
    @Autowired
    AccountKeeper accountKeeper;

    @Autowired
    PositionKeeper positionKeeper;
    
    @Autowired
    IRefDataManager refDataManager;
    
    private Set <IType> futureITypeSet;

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
            
            RefData refData = refDataManager.getRefData(symbol);
            if (refData == null) {
            	log.warn("Can't find refData");
            } else {
            	String type = refData.getIType();         
            	if (type != null && isFutureIType(type))
            		return;
            }

            if (Default.getSettlementDays() > 0 && orderSide.isSell()) {

                Account account = accountKeeper.getAccount(accountId);
                double availableQty = positionKeeper.getOverallPosition(account, symbol).getAvailableQty();
                if (!checkQty(qty, availableQty, order)) {
                    throw new OrderValidationException("Sell quantity exceeded available position quantity", ErrorMessage.QUANTITY_EXCEED_AVAILABLE_QUANTITY);
                }
            }

        } catch (OrderValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderValidationException(e.getMessage(), ErrorMessage.VALIDATION_ERROR);
        }
    }

    private boolean isFutureIType(String type) {
    	if( null == getFutureITypeSet() || getFutureITypeSet().isEmpty())
    		return false;
    	
    	Iterator <IType>itypeIte = getFutureITypeSet().iterator();
    	while(itypeIte.hasNext()){
    		IType iType = itypeIte.next();
    		if(iType.getValue().equals(type))
    			return true;  		
    	}	
    	return false;
	}

	private boolean checkQty(double qty, double availableQty, ParentOrder order) {

        double oldQty = 0;
        if (null != order) {
            oldQty = order.getRemainingQty();
        }

        return availableQty + oldQty - qty >= 0;
    }

	public Set<IType> getFutureITypeSet() {
		return futureITypeSet;
	}

	public void setFutureITypeSet(Set<IType> futureITypeSet) {
		this.futureITypeSet = futureITypeSet;
	}
}
