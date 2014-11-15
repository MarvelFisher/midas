package com.cyanspring.common.strategy;

import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.event.strategy.ExecutionInstructionEvent;
import com.cyanspring.common.strategy.IStrategy;

public interface IExecutionManager {
	
	IDownStreamSender getSender();
	void setSender(IDownStreamSender sender);
	IStrategy getStrategy();
	void setStrategy(IStrategy strategy);
	void processUpdateChildOrderEvent(UpdateChildOrderEvent event);
	void processExecutionInstructionEvent(ExecutionInstructionEvent event);
	boolean isPending();
	void init();
	void uninit();
	void onMaintenance();
}
