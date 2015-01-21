package com.cyanspring.server.alert;


public class ThreadStatus {
	long lastUpdateTime ;
	ThreadState threadState ;
	public ThreadStatus()
	{
		lastUpdateTime = System.currentTimeMillis();
		threadState = ThreadState.IDLE ;
	}
	
	synchronized public int getThreadState()
	{
		return threadState.getState();
	}
	
	synchronized public void setThreadState(ThreadState threadState)
	{
		this.threadState = threadState ;
	}
	
	synchronized public long getTime()
	{
		return lastUpdateTime ;
	}
	
	synchronized public void UpdateTime()
	{
		this.lastUpdateTime = System.currentTimeMillis() ;
	}
}
