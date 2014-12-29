package com.cyanspring.id.Library.Threading;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.ResetEvent.IResetEvent;
import com.cyanspring.id.Library.Threading.ResetEvent.ManualResetEvent;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;

/**
 * Request-based thread.
 */
public class RequestThread implements AutoCloseable {
	private static final Logger log = LoggerFactory
			.getLogger(RequestThread.class);

	protected Thread m_thread;
	protected ManualResetEvent m_eventExited = new ManualResetEvent(false);

	protected Object m_lock = new Object();
	private boolean m_fDisposing = false;

	private RequestQueue m_queue = new RequestQueue();
	private String privateName;

	IReqThreadCallback reqThreadCallback = null;

	public final String getName() {
		return privateName;
	}

	public final void setName(String value) {
		privateName = value;
	}

	public RequestThread() {
	}

	public RequestThread(IReqThreadCallback callback) {
		this(callback, "");
	}

	public RequestThread(IReqThreadCallback callback, String strThreadName) {
		reqThreadCallback = callback;
		setName(strThreadName);
	}

	protected void finalize() throws Throwable {
		log.info(String.format("[%s] finalize", this.getName()));
		uninit();
	}

	public final int getQueueSize() {
		return m_queue.size();
	}

	public final Object[] getAllRequests() {
		return m_queue.getAllRequests();
	}

	private void uninit() {

		m_fDisposing = true;
		reqThreadCallback = null;

		stop(3000);
		log.info(String.format("[%s] close", this.getName()));

		if (m_queue != null) {
			m_queue.close();
			m_queue = null;
		}
	}

	public final void close() {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	public final void start() {

		m_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					onRequestThreadProc();
				} catch (IllegalAccessException | InvocationTargetException e) {
					LogUtil.logException(log, e);
				}
			}

		});
		m_thread.setName(getName());
		m_thread.start();
	}

	/**
	 * Stop the thread. nWait is # of milli-second to wait for thread to exit.
	 * 
	 * @param nWait
	 * @throws InterruptedException
	 */
	public final void stop(int nWait) {
		m_fDisposing = true;
		if (nWait <= 0) {
			nWait = 500; // default 0.5 second
		}

		if (m_thread != null && m_thread.isAlive()) {
			try {
				// m_eventExit.set();
				try {
					m_eventExited.waitOne();
					log.info(String.format("RequestThread %s exit",
							m_thread == null ? "null" : m_thread.getName()));
				} catch (InterruptedException e) {
					LogUtil.logException(log, e);
				}
			} catch (RuntimeException ex) {
				log.error(String.format("RequestThread Stop Exception: %s",
						ex.getMessage()));
			} finally {
				m_thread = null;
			}
		}
	}

	private void onRequestThreadProc() throws IllegalAccessException,
			InvocationTargetException {
		fireOnStartEvent();

		boolean fContinue = true;

		IResetEvent[] waitHandles = new IResetEvent[1]; // [2];;
		// waitHandles[0] = m_eventExit;
		waitHandles[0] = m_queue.getQueueEvent();

		while (fContinue) {
			try {
				int nIndex = WaitHandle.waitAny(waitHandles, 10);

				if (m_fDisposing) {
					break;
				}

				switch (nIndex) {
				case 0: {
					Object objRequest = m_queue.dequeueRequest();
					if (objRequest == null) {
						continue;
					}

					if (m_fDisposing) {
						break;
					}

					fireOnRequestEvent(objRequest);

					// See if there is outstanding request
					if (m_queue != null) {
						m_queue.checkPendingRequest();
					}
				}
					break;

				case WaitHandle.WaitTimeout:
				default:
					break;
				}
			} catch (Exception ex) {
				LogUtil.logException(log, ex);
				log.error(
						"RequestThread %s OnRequestThreadProc Exception ex: %s",
						m_thread == null ? "null" : m_thread.getName(),
						ex.getMessage());
			}
		}

		fireOnEndEvent();

		m_eventExited.set();
		log.info(String.format(
				"RequestThread %s ThreadProc Fire m_eventExited",
				m_thread == null ? "null" : m_thread.getName()));
	}

	public final void addRequest(Object objRequest) {

		if (m_fDisposing) {
			return;
		}

		m_queue.queueRequest(objRequest, true);
	}

	/**
	 * Add multiple requests at once. This is used if the Request handler can
	 * handle multiple requests at once.
	 * 
	 * @param arrRequest
	 */
	public final void addRequests(Object[] arrRequest) {

		if (m_fDisposing) {
			return;
		}

		for (Object objRequest : arrRequest) {
			m_queue.queueRequest(objRequest, false);
		}

		m_queue.checkPendingRequest();
	}

	public final boolean removeRequest(Object objRequest) {
		return m_queue.removeRequest(objRequest);
	}

	public final Object RemoveFirstRequest() {
		return m_queue.dequeueRequest();
	}

	private void fireOnStartEvent() throws IllegalAccessException,
			InvocationTargetException {
		try {
			if (reqThreadCallback != null) {
				reqThreadCallback.onStartEvent(this);
			}
		} catch (RuntimeException ex) {
			log.error(String.format(
					"RequestThread %s FireOnStartEvent Exception: %s",
					m_thread == null ? "null" : m_thread.getName(),
					ex.getMessage()));
		}
	}

	private void fireOnEndEvent() {
		try {
			if (reqThreadCallback != null) {// && OnEndEvent.size() > 0) {
				reqThreadCallback.onStopEvent(this);
			}

		} catch (Exception ex) {
			log.error(String.format(
					"RequestThread FireOnEndEvent Exception: %s",
					ex.getMessage()));
			LogUtil.logException(log, ex);
		}
	}

	private void fireOnRequestEvent(Object objRequest) {
		try {
			if (objRequest == null) {
				return;
			}

			if (reqThreadCallback != null) {
				reqThreadCallback.onRequestEvent(this, objRequest);

			}
		} catch (Exception ex) {
			log.error(String.format(
					"RequestThread FireOnRequestEvent Exception: %s",
					ex.getMessage()));
			LogUtil.logException(log, ex);
		}
	}
}
