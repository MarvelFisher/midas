package com.cyanspring.server.order;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;

public class SuspendSystemController {
	private volatile boolean suspendSystem = false;

	public boolean isSuspendSystem() {
		return suspendSystem;
	}

	public void setSuspendSystem(boolean suspendSystem) {
		this.suspendSystem = suspendSystem;
	}

	public boolean sendNewOrderRejectEvent(IRemoteEventManager eventManager, String key, String receiver, String txId,
			ParentOrder order, String user, String account) throws Exception {
		if (suspendSystem) {
			String msg = MessageLookup.buildEventMessage(ErrorMessage.SERVER_SUSPEND, "Server is suspend");
			EnterParentOrderReplyEvent replyEvent = new EnterParentOrderReplyEvent(key, receiver, false, msg, txId, order,
					user, account);
			eventManager.sendLocalOrRemoteEvent(replyEvent);
		}
		return suspendSystem;
	}

	public boolean sendAmendOrderRejectEvent(IRemoteEventManager eventManager, String key, String receiver, String txId,
			ParentOrder order) throws Exception {
		if (suspendSystem) {
			String msg = MessageLookup.buildEventMessage(ErrorMessage.SERVER_SUSPEND, "Server is suspend");
			AmendParentOrderReplyEvent replyEvent = new AmendParentOrderReplyEvent(key, receiver, false, msg, txId, order);
			eventManager.sendLocalOrRemoteEvent(replyEvent);
		}
		return suspendSystem;
	}

	public boolean sendCancelOrderRejectEvent(IRemoteEventManager eventManager, String key, String receiver, String txId,
			ParentOrder order) throws Exception {
		if (suspendSystem) {
			String msg = MessageLookup.buildEventMessage(ErrorMessage.SERVER_SUSPEND, "Server is suspend");
			CancelParentOrderReplyEvent reply = new CancelParentOrderReplyEvent(key, receiver, false, msg, txId, order);
			eventManager.sendLocalOrRemoteEvent(reply);
		}
		return suspendSystem;
		
	}

	public boolean sendClosePositionRejectEvent(IRemoteEventManager eventManager, String key, String receiver,
			String account, String symbol, String txId) throws Exception {
		if (suspendSystem) {
			String msg = MessageLookup.buildEventMessage(ErrorMessage.SERVER_SUSPEND, "Server is suspend");
			eventManager.sendLocalOrRemoteEvent(new ClosePositionReplyEvent(key, receiver, account, symbol, txId, false, msg));
		}
		return suspendSystem;
	}
}
