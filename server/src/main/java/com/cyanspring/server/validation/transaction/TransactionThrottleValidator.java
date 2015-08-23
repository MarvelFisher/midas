package com.cyanspring.server.validation.transaction;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.Clock;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.validation.ITransactionValidator;
import com.cyanspring.common.validation.TransactionValidationException;

public class TransactionThrottleValidator implements ITransactionValidator {
	private long interval = 300;
	private ConcurrentHashMap<String, Date> trans = new ConcurrentHashMap<String, Date>();

	private void check(String account) throws TransactionValidationException {
		Date last = trans.get(account);
		if(null != last && TimeUtil.getTimePass(last) < interval) {
			throw new TransactionValidationException("Transaction sent too fast one after another", ErrorMessage.FAST_REJECT);
		}
		trans.put(account, Clock.getInstance().now());
	}

	@Override
	public void checkEnterOrder(EnterParentOrderEvent event, String account)
			throws TransactionValidationException {
		check(account);
	}

	@Override
	public void checkAmendOrder(AmendParentOrderEvent event, String account)
			throws TransactionValidationException {
		check(account);
	}

	@Override
	public void checkCancelOrder(CancelParentOrderEvent event, String account)
			throws TransactionValidationException {
		check(account);
	}

	@Override
	public void checkClosePosition(ClosePositionRequestEvent event,
			String account) throws TransactionValidationException {
		check(account);
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

}
