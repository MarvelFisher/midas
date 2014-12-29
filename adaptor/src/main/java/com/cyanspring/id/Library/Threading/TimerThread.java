package com.cyanspring.id.Library.Threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.ResetEvent.IResetEvent;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;

/**
 * Define a timer thread that signals periodically.
 */
public class TimerThread extends WorkerThread implements AutoCloseable {
	private int m_nInterval = 1000; // (=1 sec)
	private int m_nFirstInterval = -1;

	private static final Logger log = LoggerFactory.getLogger(TimerThread.class);
	
	public interface TimerEventHandler {
		void onTimer(TimerThread objSender);
	}

	public TimerEventHandler TimerEvent = null;

	public final int getInterval() {
		return m_nInterval;
	}

	public final void setInterval(int value) {
		if (value > 0) {
			m_nInterval = value;
		}
	}

	/**
	 * Interval to trigger first-time OnTimer event
	 */
	public final int getFirstInterval() {
		if (m_nFirstInterval < 0) {
			return m_nInterval;
		} else {
			return m_nFirstInterval;
		}
	}

	public final void setFirstInterval(int value) {
		if (value >= 0) {
			m_nFirstInterval = value;
		}
	}

	public TimerThread() {
	}

	@Override
	public void onThreadProc() {
		// Wait for
		// - either a valid request
		// - stop thread request
		//
		IResetEvent[] arrHandle = new IResetEvent[1];
		arrHandle[0] = getStopEventHandle();

		boolean bFirst = true;
		while (true) {
			int nInterval;
			if (bFirst) {
				nInterval = this.getFirstInterval();
				bFirst = false;
			} else {
				nInterval = m_nInterval;
			}

			try {
				
				int nIndex = WaitHandle.waitAny(arrHandle, nInterval);
				if (nIndex == WaitHandle.WaitTimeout) {
					// Timeout
					//
					onTimer();
					if (isClose)
						break;
				} else if (nIndex == 0) {
					// stop thread request
					//
					break;
				}

			} catch (InterruptedException e) {
				LogUtil.logException(log, e);
			}
		}

		// do notify
		if (m_eventStopped != null)
			m_eventStopped.set();
	}

	// Signal client by sending event
	//
	private void onTimer() {
		try {
			if (TimerEvent != null) {
				TimerEvent.onTimer(this);
			}
		} catch (RuntimeException e) {
		}
	}

	@Override
	public void close() throws Exception {		
		super.stop();
		FinalizeHelper.suppressFinalize(this);
		
	}
}