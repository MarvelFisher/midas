package com.cyanspring.id.Library.Threading;
import com.cyanspring.id.Library.Threading.ResetEvent.IResetEvent;

public class WaitHandle {
	public final static int WaitTimeout = 258;

	public static int waitAny(IResetEvent[] handlers, int nWait)
			throws InterruptedException {
		long t = System.currentTimeMillis();
		long due = t + nWait;
		int nInterval = nWait / 10;
		if (nInterval < 10)
			nInterval = 10;
		while (true) {
			for (int i = 0; i < handlers.length; i++) {
				if (handlers[i].isSignalled())
					return i;
			}
			
			Thread.sleep(1); //nInterval);
			
			if (System.currentTimeMillis() >= due)
				break;	
		}
		return WaitTimeout;

	}

}
