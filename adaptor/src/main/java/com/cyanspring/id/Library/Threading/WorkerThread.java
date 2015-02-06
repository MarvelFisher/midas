package com.cyanspring.id.Library.Threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.ResetEvent.IResetEvent;
import com.cyanspring.id.Library.Threading.ResetEvent.ManualResetEvent;
import com.cyanspring.id.Library.Util.LogUtil;

/**
 * Define a background thread base class that provides the basic framework for
 * writing a background thread, including Start/Stop event between client and
 * thread. - Client derives from WorkerThread, - call Start()/Stop() to
 * start/stop thread, - overrides OnThreadProc(), and OnExitThread()
 */
public class WorkerThread {
	private static final Logger log = LoggerFactory.getLogger(WorkerThread.class);
	protected Thread m_thread = null;
	protected ManualResetEvent m_eventStop = null;
	protected ManualResetEvent m_eventStopped = null;
	private boolean m_fStart = false;
	protected Object m_objUserObject = null;
	protected boolean isClose = false;

	protected String name = "";
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public WorkerThread() {
	}

	protected void finalize() throws Throwable {
		stop();
	}

	public final IResetEvent getStopEventHandle() {
		return m_eventStop;
	}

	public final boolean isRunning() {
		return m_fStart;
	}

	public final Object getUserObject() {
		return m_objUserObject;
	}

	public final void setUserObject(Object value) {
		m_objUserObject = value;
	}

	// Create a thread to start job
	//
	public boolean start() {
		try {
			m_eventStop = new ManualResetEvent(false);
			m_eventStopped = new ManualResetEvent(false);
			m_thread = new Thread(new Runnable() {
				@Override
				public void run() {
					threadProc();
				}
				// new Thread(new ThreadStart(ThreadProc));
				// }
			});

			m_fStart = true;
			if (getName().isEmpty() == false) {
				m_thread.setName(getName());
			}			
			m_thread.start();
			return true;
		} catch (RuntimeException e) {
			m_fStart = false;
			m_thread = null;
			return false;
		}
	}

	public void stop() {
		stop(5 * 1000); // default wait for 5 seconds
	}

	@SuppressWarnings("deprecation")
	public void stop(int nTimeout) {
		if (!m_fStart) {
			return;
		}

		try {
			m_eventStop.set();
			if (nTimeout > 0) {
				IResetEvent[] arrHandle = new IResetEvent[1];
				arrHandle[0] = m_eventStopped;
				int nIndex = WaitHandle.waitAny(arrHandle, nTimeout);
				if (nIndex == WaitHandle.WaitTimeout) {
					m_thread.stop();
				}
			}
			else {
				//wait until thread end
				m_eventStopped.waitOne();							
			}
		} catch (Exception e) {
		} finally {
			m_eventStop = null;
			m_eventStopped = null;
			m_thread = null;
			m_fStart = false;
		}
	}

	/*
	 * @SuppressWarnings("deprecation") public void stop(int nTimeout) { if
	 * (!m_fStart) { return; } isClose = true; try { m_eventStop.set(); // if
	 * nTimeout == 0 -> wait until thread end if (nTimeout == 0) {
	 * m_eventStopped.waitOne(); } else {
	 * 
	 * Logger.logInfo("1"); boolean isStopped = m_eventStopped.waitOne(nTimeout,
	 * TimeUnit.MILLISECONDS); if (isStopped) { Logger.logInfo("2");
	 * m_thread.stop(); } else { Logger.logInfo("3"); } } } catch (Exception e)
	 * { } finally { m_eventStop = null; m_eventStopped = null; m_thread = null;
	 * m_fStart = false; } }
	 */

	protected final void threadProc() {
		try {
			onThreadProc(); // overrides to handle thread proc
		} catch (RuntimeException e) {
		} finally {
			// Signal cleanup
			//
			onExitThread();

			// Signal that thread has finished
			// note m_eventStopped can be null if thread is aborted
			//
			if (m_eventStopped != null) {
				m_eventStopped.set();
			}
		}
	}

	// Derived should override this function
	//
	public void onThreadProc() {
		try {
			getStopEventHandle().waitOne();
		} catch (InterruptedException e) {
			LogUtil.logException(log, e);
		}
	}

	//
	// This function is called from the 'finally' block of ThreadProc,
	// and will be called regardless whether thread is stopped gracefully
	// or thread is aborted.
	// Client can choose to perform final cleanup here, however
	// should note this function might be executed after client's
	// Stop() has finished
	//
	public void onExitThread() {
	}
}
