package com.cyanspring.server.alert;

public enum ThreadState {
	SENDDING(1),
	IDLE(2),
	;
	
	private int State;
	ThreadState(int threadstate)
	{
		this.State = threadstate ;
	}
	
	public int getState()
	{
		return State ;
	}	
}
