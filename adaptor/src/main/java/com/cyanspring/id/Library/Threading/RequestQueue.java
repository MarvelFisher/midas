package com.cyanspring.id.Library.Threading;

import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.ResetEvent.AutoResetEvent;
import com.cyanspring.id.Library.Threading.ResetEvent.IResetEvent;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;




/**
 * Request queue. Each request is put into this queue, first-in-first-out.
 * (cannot use System.Collections.Generic.Queue<T> as a base class since it does
 * not support Remove method)
 */
public class RequestQueue extends LinkedList<Object> implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(RequestQueue.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -3961732439095463197L;
	protected Object m_lock = new Object();
	protected AutoResetEvent m_eventSignal = new AutoResetEvent(false);

	/**
	 * This event will be signaled if there is outstanding request in this
	 * queue.
	 */
	public final IResetEvent getQueueEvent() {
		return m_eventSignal;
	}

	public RequestQueue() {
	}

	protected void finalize() throws Throwable {
		close();
	}

	/**
	 * Add a request. This will signal the QueueEvent.
	 * 
	 * @param objRequest
	 */
	public final void queueRequest(Object objRequest, boolean fSignalNow) {
		synchronized (m_lock) {
			this.addLast(objRequest);
			if (fSignalNow) {
				m_eventSignal.set();
			}
		}
	}

	/**
	 * Return the next request for execution.
	 * 
	 * @return
	 */
	public final Object dequeueRequest() {
		synchronized (m_lock) {
			if (this.size() > 0) {
				Object objRequest = this.getFirst();
				this.removeFirst();
				try {
					m_eventSignal.waitOne();
				} catch (InterruptedException e) {
					LogUtil.logException(log, e);
				}
				return objRequest;
			} else {
				return null;
			}
		}
	}

	/**
	 * Check if there is more pending requests. If so, signal the QueueEvent.
	 */
	public final void checkPendingRequest() {
		synchronized (m_lock) {
			if (this.size() > 0) {
				m_eventSignal.set();
			}
		}
	}

	/**
	 * Remove any arbitray request from this queue.
	 * 
	 * @param objRequest
	 * @return
	 */
	public final boolean removeRequest(Object objRequest) {
		synchronized (m_lock) {
			return this.remove(objRequest);
		}
	}

	public final void clearRequest() {
		if (this.size() > 0) {
			for (Object obj : this) {
				if (obj instanceof AutoCloseable) {
					AutoCloseable auto = (AutoCloseable) obj;
					try {
						auto.close();
					} catch (Exception e) {
						LogUtil.logException(log, e);
					}
				}
			}
			clear();
		}
	}

	public final Object[] getAllRequests()
	{
		ArrayList<Object> list = new ArrayList<Object>();
		list.addAll(this);
		return list.toArray();
	}
	
	@Override
	public void close() {
		log.info("RequestQueue close");
		clearRequest();
		FinalizeHelper.suppressFinalize(this);

	}
}