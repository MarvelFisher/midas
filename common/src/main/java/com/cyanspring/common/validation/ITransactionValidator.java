package com.cyanspring.common.validation;

import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;

public interface ITransactionValidator {
	void checkEnterOrder(EnterParentOrderEvent event, String account) throws TransactionValidationException;
	void checkAmendOrder(AmendParentOrderEvent event, String account) throws TransactionValidationException;
	void checkCancelOrder(CancelParentOrderEvent event, String account) throws TransactionValidationException;
	void checkClosePosition(ClosePositionRequestEvent event, String account) throws TransactionValidationException;
}
