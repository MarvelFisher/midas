package com.cyanspring.server.validation.transaction;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.system.SuspendServerEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.validation.ITransactionValidator;
import com.cyanspring.common.validation.TransactionValidationException;

public class SystemSuspendValidator implements ITransactionValidator, IPlugin, IAsyncEventListener {
    @Autowired
    private IRemoteEventManager eventManager;
    
    private boolean suspended;

    private void check() throws TransactionValidationException {
    	if(this.suspended) {
			throw new TransactionValidationException("System is suspended from trading", ErrorMessage.SERVER_SUSPEND);
    	}
    }
    
	@Override
	public void checkEnterOrder(EnterParentOrderEvent event, String account)
			throws TransactionValidationException {
		check();
	}

	@Override
	public void checkAmendOrder(AmendParentOrderEvent event, String account)
			throws TransactionValidationException {
		check();
	}

	@Override
	public void checkCancelOrder(CancelParentOrderEvent event, String account)
			throws TransactionValidationException {
		check();
	}

	@Override
	public void checkClosePosition(ClosePositionRequestEvent event, String account)
			throws TransactionValidationException {
		check();
	}

	@Override
	public void init() throws Exception {
		eventManager.subscribe(SuspendServerEvent.class, this);
		
	}

	@Override
	public void uninit() {
		eventManager.unsubscribe(SuspendServerEvent.class, this);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof SuspendServerEvent) {
			this.suspended = ((SuspendServerEvent) event).isSuspendServer();
		}
	}

	public boolean isSuspended() {
		return suspended;
	}

}
