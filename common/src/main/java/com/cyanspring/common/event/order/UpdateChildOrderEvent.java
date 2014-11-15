package com.cyanspring.common.event.order;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.type.ExecType;

public class UpdateChildOrderEvent extends AsyncEvent {
	private ExecType execType; 
	private ChildOrder order;
	private Execution execution; 
	private String message;
	
	public UpdateChildOrderEvent(String strategyId, ExecType execType, ChildOrder order, Execution execution,
			String message) {
		super(strategyId);
		this.execType = execType;
		this.order = order;
		this.execution = execution;
		this.message = message;
	}

	public ExecType getExecType() {
		return execType;
	}
	
	public ChildOrder getOrder() {
		return order;
	}
	
	public Execution getExecution() {
		return execution;
	}

	public String getMessage() {
		return message;
	}

}
