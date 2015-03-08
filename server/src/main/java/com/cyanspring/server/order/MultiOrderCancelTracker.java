package com.cyanspring.server.order;

import java.util.HashSet;
import java.util.Set;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.order.CancelStrategyOrderEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.type.StrategyState;

public class MultiOrderCancelTracker {
	private IAsyncEventManager eventManager;
	private IAsyncEventListener listener;
	private ResetAccountRequestEvent event;
	private Set<String> orders = new HashSet<String>();
	
	public MultiOrderCancelTracker(IAsyncEventManager eventManager,
			IAsyncEventListener listener,
			ResetAccountRequestEvent event) {
		this.eventManager = eventManager;
		this.listener = listener;
		this.event = event;
	}
	
	public void add(ParentOrder order) {
		orders.add(order.getId());
		eventManager.subscribe(UpdateParentOrderEvent.class, order.getId(), listener);
		String source = order.get(String.class, OrderField.SOURCE.value());
		String txId = order.get(String.class, OrderField.CLORDERID.value());
		CancelStrategyOrderEvent cancel = 
				new CancelStrategyOrderEvent(order.getId(), order.getSender(), txId, source, null, false);
		eventManager.sendEvent(cancel);
	}
	
	public boolean checkParentOrderUpdate(UpdateParentOrderEvent event) {
		ParentOrder order = event.getParent();
		if(order.getOrdStatus().isCompleted() && order.getState().equals(StrategyState.Terminated)) {
			eventManager.unsubscribe(UpdateParentOrderEvent.class, order.getId(), listener);
			orders.remove(order.getId());
		}
		
		return orders.size() <= 0;
	}
	
	public ResetAccountRequestEvent getEvent() {
		return event;
	}
}