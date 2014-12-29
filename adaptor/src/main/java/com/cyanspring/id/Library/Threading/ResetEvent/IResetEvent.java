package com.cyanspring.id.Library.Threading.ResetEvent;

import java.util.concurrent.TimeUnit;

/**
 * from http://code.google.com/p/pelops/ 
 * @author dominicwilliams
 */
public interface IResetEvent {
	public void set();
	
	public void reset();
	
	public void waitOne() throws InterruptedException;
	
	public boolean waitOne(int timeout, TimeUnit unit) throws InterruptedException;
	
	public boolean isSignalled();
}
