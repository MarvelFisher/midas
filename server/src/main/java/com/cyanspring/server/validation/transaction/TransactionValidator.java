package com.cyanspring.server.validation.transaction;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.validation.ITransactionValidator;
import com.cyanspring.common.validation.TransactionValidationException;

public class TransactionValidator implements ITransactionValidator {
	private List<ITransactionValidator> validators = new ArrayList<ITransactionValidator>();

	@Override
	public void checkEnterOrder(EnterParentOrderEvent event)
			throws TransactionValidationException {
		for(ITransactionValidator validator: validators)
			validator.checkEnterOrder(event);
	}

	@Override
	public void checkAmendOrder(AmendParentOrderEvent event)
			throws TransactionValidationException {
		for(ITransactionValidator validator: validators)
			validator.checkAmendOrder(event);
	}

	@Override
	public void checkCancelOrder(CancelParentOrderEvent event)
			throws TransactionValidationException {
		for(ITransactionValidator validator: validators)
			validator.checkCancelOrder(event);
	}

	@Override
	public void checkClosePosition(ClosePositionRequestEvent event)
			throws TransactionValidationException {
		for(ITransactionValidator validator: validators)
			validator.checkClosePosition(event);
	}

	public List<ITransactionValidator> getValidators() {
		return validators;
	}

	public void setValidators(List<ITransactionValidator> validators) {
		this.validators = validators;
	}

}
