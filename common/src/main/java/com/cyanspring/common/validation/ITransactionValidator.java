package com.cyanspring.common.validation;

import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;

public interface ITransactionValidator {
	void checkEnterOrder(EnterParentOrderEvent event) throws TransactionValidationException;
	void checkAmendOrder(AmendParentOrderEvent event) throws TransactionValidationException;
	void checkCancelOrder(CancelParentOrderEvent event) throws TransactionValidationException;
	void checkClosePosition(ClosePositionRequestEvent event) throws TransactionValidationException;
}
