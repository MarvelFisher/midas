/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.strategy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.event.strategy.ExecutionInstructionEvent;
import com.cyanspring.common.event.strategy.ExecutionInstructionResponseEvent;
import com.cyanspring.common.strategy.ExecutionInstruction;
import com.cyanspring.common.strategy.IExecutionManager;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrderAction;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

public class DefaultExecutionManager implements IExecutionManager, IAsyncExecuteEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(DefaultExecutionManager.class);
	
	protected IDownStreamSender sender;
	protected IStrategy strategy;
//	private Map<String, ChildOrder> orders = new HashMap<String, ChildOrder>();
	private TranTracker tranTracker;
	private long transactionTimeOut = 20 * 1000;
	private static enum STATE {Start, PendingCancel, CancelDone, PendingAmend, AmendDone, PendingNew, End}

	class TranTracker {
		STATE state = STATE.Start;
		String key;
		String id;
		boolean failed;
		String lastError;
		
		List<ExecutionInstruction> pendings = new ArrayList<ExecutionInstruction>();
		List<ExecutionInstruction> cancels = new ArrayList<ExecutionInstruction>();
		List<ExecutionInstruction> amends = new ArrayList<ExecutionInstruction>();
		List<ExecutionInstruction> news = new ArrayList<ExecutionInstruction>();
		
		TranTracker(ExecutionInstructionEvent event) {
			this.key = event.getKey();
			this.id = event.getId();
			for(ExecutionInstruction ei: event.getExecutionInstructions()) {
				if(ei.getAction().equals(OrderAction.NEW))
					news.add(ei);
				if(ei.getAction().equals(OrderAction.AMEND))
					amends.add(ei);
				if(ei.getAction().equals(OrderAction.CANCEL))
					cancels.add(ei);
			}
		}
		
		ExecutionInstruction findInstruction(String orderId) {
			for(ExecutionInstruction ei: pendings) {
				if(orderId.equals(ei.getOrder().getId()))
					return ei;
			}
			return null;
		}
	}
	
	public void init() {
		tranTracker = null;
		//strategy.getContainer().subscribe(UpdateChildOrderEvent.class, strategy.getId(), this);
		//strategy.getContainer().subscribe(ExecutionInstructionEvent.class, strategy.getId(), this);
	}

	public void uninit() {
		//strategy.getContainer().unsubscribe(UpdateChildOrderEvent.class, strategy.getId(), this);
		//strategy.getContainer().unsubscribe(ExecutionInstructionEvent.class, strategy.getId(), this);
	}
	
	private void processTransaction() {
		if(tranTracker == null) {
			log.error("TranTracker is null");
			return;
		}
			
		if(tranTracker.failed) {
			if(tranTracker.pendings.size() == 0) //all pending actions acked
				ackTransaction();
				
			return;
		}
		
		if (tranTracker.state == STATE.Start) {
			if(tranTracker.cancels.size() != 0) {
				tranTracker.state = STATE.PendingCancel;
				processCancelInstructions(tranTracker.key, tranTracker.id, tranTracker.cancels);
			} else {
				tranTracker.state = STATE.CancelDone;
				processTransaction();
			}
		} else if (tranTracker.state == STATE.PendingCancel) {
			if (tranTracker.pendings.size() == 0) {
				tranTracker.state = STATE.CancelDone;
				processTransaction();
			}
		} else if (tranTracker.state == STATE.CancelDone) {
			if(tranTracker.amends.size() != 0) {
				tranTracker.state = STATE.PendingAmend;
				processAmendInstructions(tranTracker.key, tranTracker.id, tranTracker.amends);
			} else {
				tranTracker.state = STATE.AmendDone;
				processTransaction();
			}
		} else if (tranTracker.state == STATE.PendingAmend) {
			if (tranTracker.pendings.size() == 0) {
				tranTracker.state = STATE.AmendDone;
				processTransaction();
			}
		} else if (tranTracker.state == STATE.AmendDone) {
			if(tranTracker.news.size() != 0) {
				tranTracker.state = STATE.PendingNew;
				processNewInstructions(tranTracker.key, tranTracker.id, tranTracker.news);
			} else {
				tranTracker.state = STATE.End;
				ackTransaction();
			}
		} else if (tranTracker.state == STATE.PendingNew) {
			if (tranTracker.pendings.size() == 0) {
				tranTracker.state = STATE.End;
				ackTransaction();
			}
		}
		
//		if(tranTracker != null) {
//			log.debug("Transaction state: " + tranTracker.state);
//			log.debug("pending actions: " + tranTracker.pendings);
//		}
	}

//	public void gc() {
//		Set<Entry<String, ChildOrder>> entries = orders.entrySet();
//		List<String> list = new ArrayList<String>();
//		for(Entry<String, ChildOrder> entry: entries) {
//			ChildOrder order = entry.getValue();
//			Date now = Clock.getInstance().now();
//			if(entry.getValue().getOrdStatus().isCompleted() && 
//				TimeUtil.getTimePass(order.getTimeModified()) > 120 * 1000) // last modified time is two minutes ago
//				list.add(entry.getKey());
//		}
//		
//		for(String key: list) {
//			log.debug("GC: remove completed order: " + key);
//			orders.remove(key);
//		}
//	}

	@Override
	public void onMaintenance() {
		//check transaction time out
		List<ExecutionInstruction> timeOuts = null;
		if(tranTracker != null) {
			for(ExecutionInstruction ei: tranTracker.pendings) {
				if(TimeUtil.getTimePass(ei.getTimeStamp()) > transactionTimeOut) {
					if(null == timeOuts)
						timeOuts = new ArrayList<ExecutionInstruction>();
					
					timeOuts.add(ei);
					String message = "Transaction time out for this: " + ei;
					tranTracker.failed = true;
					tranTracker.lastError = message;
					log.error(message);
				}
			}
		}
		
		if(null != timeOuts) {
			for (ExecutionInstruction ei: timeOuts) {
				tranTracker.pendings.remove(ei);
			}
			processTransaction();
		}
	}
	
	private void ackTransaction() {
		ExecutionInstructionResponseEvent event = 
			new ExecutionInstructionResponseEvent(tranTracker.key, tranTracker.id, !tranTracker.failed, tranTracker.lastError);
		tranTracker = null;
		strategy.getContainer().sendEvent(event);
	}
	
	public void processExecutionInstructionEvent(ExecutionInstructionEvent event) {
		if(tranTracker != null) {
			String info = "received transaction while another trancation is still pending";
			log.error(info);
			ExecutionInstructionResponseEvent repy = new ExecutionInstructionResponseEvent(event.getKey(), event.getId(), false, info);
			strategy.getContainer().sendEvent(repy);
			return;
		}
		log.debug("Receiving ExecutionInstructionEvent: " + event);
		tranTracker = new TranTracker(event);
		processTransaction();
	}

	private void processAmendInstructions(String key, String id, List<ExecutionInstruction> list) {
		for(ExecutionInstruction ei: list) {
			tranTracker.pendings.add(ei);
		}
		
		for(ExecutionInstruction ei: list) {
			log.debug("Amending child order: " + ei.getOrder() + "; Field chaning: " + ei.getChangeFields());
			ei.setTimeStamp(Clock.getInstance().now());
			try {
				sender.amendOrder(ei.getOrder(), ei.getChangeFields());
			} catch (DownStreamException e) {
				log.warn(e.getMessage(), e);
				e.printStackTrace();
				tranTracker.pendings.remove(ei);
				tranTracker.failed = true;
				tranTracker.lastError = "DownStreamException caught: " + e.getMessage();
				break;
			}
		}
		if(tranTracker != null && tranTracker.failed)
			processTransaction();
	}
	
	private void processCancelInstructions(String key, String id, List<ExecutionInstruction> list) {
		for(ExecutionInstruction ei: list) {
			tranTracker.pendings.add(ei);
		}
		
		for(ExecutionInstruction ei: list) {
			log.debug("Canceling child order: " + ei.getOrder());
			ei.setTimeStamp(Clock.getInstance().now());
			try {
				sender.cancelOrder(ei.getOrder());
			} catch (DownStreamException e) {
				log.warn(e.getMessage(), e);
				e.printStackTrace();
				tranTracker.pendings.remove(ei);
				tranTracker.failed = true;
				tranTracker.lastError = "DownStreamException caught: " + e.getMessage();
				break;
			}
		}
		if(tranTracker != null && tranTracker.failed)
			processTransaction();
	}
	
	private void processNewInstructions(String key, String id, List<ExecutionInstruction> list) {
		for(ExecutionInstruction ei: list) {
			tranTracker.pendings.add(ei);
		}
		
		for(ExecutionInstruction ei: list) {
			ChildOrder order = ei.getOrder();
			if (ei.getChangeFields() != null)
				order.getFields().putAll(ei.getChangeFields());
			
			ei.setOrder(order);
			log.debug("Sending new child order: " + ei.getOrder() + "; extra fields: " + ei.getChangeFields());
			ei.setTimeStamp(Clock.getInstance().now());
			try {
				sender.newOrder(order);
			} catch (DownStreamException e) {
				log.warn(e.getMessage(), e);
				e.printStackTrace();
				tranTracker.pendings.remove(ei);
				tranTracker.failed = true;
				tranTracker.lastError = "DownStreamException caught: " + e.getMessage();
				break;
			}
		}
		if(tranTracker != null && tranTracker.failed)
			processTransaction();
	}
	
	public void processUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		log.debug("Received child order update: " + event.getExecType() + " - " + event.getOrder());
		if(tranTracker == null)
			return;
		
		ExecType execType = event.getExecType();
		ChildOrder order = event.getOrder();
		if (execType.equals(ExecType.NEW) || 
				execType.equals(ExecType.PARTIALLY_FILLED) ||
				execType.equals(ExecType.FILLED)) {
			ExecutionInstruction ei = tranTracker.findInstruction(order.getId());
			if(ei != null && ei.getAction() == OrderAction.NEW) {
				strategy.logDebug("Child order new: " + order.getId());
				tranTracker.pendings.remove(ei);
				processTransaction();
			}
		} else if (execType.equals(ExecType.REPLACE)) {
			ExecutionInstruction ei = tranTracker.findInstruction(order.getId());
			if(ei != null && ei.getAction() == OrderAction.AMEND) {
				tranTracker.pendings.remove(ei);
				Double intendedQty = (Double)ei.getChangeFields().get(OrderField.QUANTITY.value());
				if(intendedQty != null && !PriceUtils.Equal(intendedQty, order.getQuantity())) {
					//order has been executed more, fail the transaction to avoid overfill
					tranTracker.failed = true;
					tranTracker.lastError = "Order quanity is not the same as what we wanted to amend, fail transaction:  "
						+ intendedQty + " vs " + order.getQuantity();
					log.warn(tranTracker.lastError);
				}
				processTransaction();
			} else { // unsolicited amend
				log.warn("Unsolicited child amend: " + order);
			}
		} else if (execType.equals(ExecType.CANCELED)) {
			ExecutionInstruction ei = tranTracker.findInstruction(order.getId());
			if(ei != null) {
				tranTracker.pendings.remove(ei);
				if(ei.getAction() == OrderAction.CANCEL) {
			
					Double intendedQty = (Double)ei.getOrder().getCumQty();
					if(intendedQty != null && !PriceUtils.Equal(intendedQty, order.getCumQty())) {
						//order has been executed more, fail the transaction to avoid overfill
						tranTracker.failed = true;
						tranTracker.lastError = "Order cumQty is more than the time we canceled, fail transaction: "
							+ intendedQty + " vs " + order.getCumQty();
						log.warn(tranTracker.lastError);
					}
				} else { // unsolicited cancel
					tranTracker.failed = true;
					tranTracker.lastError = "Child order has been unsolicitedly cancelled: " + order;
					log.warn("Unsolicited child cancel: " + order);
				}
				processTransaction();
			} else { // unsolicited cancel
				log.warn("Unsolicited child cancel: " + order);
			}
		} else if (execType.equals(ExecType.REJECTED)) {
			ExecutionInstruction ei = tranTracker.findInstruction(order.getId());
			if(ei != null) {
				tranTracker.pendings.remove(ei);
				tranTracker.failed = true;
				tranTracker.lastError = event.getMessage();
				processTransaction();
			} else { // unsolicited reject
				log.warn("unsolicited child reject: " + order);
			}
		} else {// rest we don't care
		}
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof ExecutionInstructionEvent) {
			processExecutionInstructionEvent((ExecutionInstructionEvent)event);
		} else if (event instanceof UpdateChildOrderEvent) {
			processUpdateChildOrderEvent((UpdateChildOrderEvent)event);
		}
		
	}


	@Override
	public boolean isPending() {
		return tranTracker != null;
	}

	@Override
	public IAsyncEventInbox getInbox() {
		return strategy.getContainer().getInbox();
	}

	@Override
	public IDownStreamSender getSender() {
		return this.sender;
	}

	@Override
	public void setSender(IDownStreamSender sender) {
		this.sender = sender;
	}

	@Override
	public IStrategy getStrategy() {
		return this.strategy;
	}

	@Override
	public void setStrategy(IStrategy strategy) {
		this.strategy = strategy;
		
	}


}
