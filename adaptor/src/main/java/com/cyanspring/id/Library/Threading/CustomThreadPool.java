package com.cyanspring.id.Library.Threading;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Util.LogUtil;

public class CustomThreadPool {

	public static int CORE_POOLSIZE = 5; // always exist
	public static int MAX_POOLSIZE = 25; // max thread can used
	// keepalivetime if thread count more than core_poolsize and 1 thread is empty can be close  if timeout
	private static final Logger log = LoggerFactory.getLogger(CustomThreadPool.class);
	private BlockingQueue<Runnable> workQueue;
	private ThreadPoolExecutor poolExecutor;
	private static CustomThreadPool customThreadPool;

	/**
	 * constructor not available to public 
	 */
	private CustomThreadPool() {
		if (workQueue == null) {
			workQueue = new LinkedBlockingDeque<Runnable>();
		}

		if (poolExecutor == null) {
			poolExecutor = new ThreadPoolExecutor(CORE_POOLSIZE, MAX_POOLSIZE,
					5, TimeUnit.MINUTES, workQueue);
		}
	}

	/**
	 * run async if NO params
	 * @param workItem
	 */
	public static void queueUserWorkItem(Runnable workItem) {
		if (customThreadPool == null) {
			customThreadPool = new CustomThreadPool();
		}
		customThreadPool.poolExecutor.execute(workItem);
	}
	
	/**
	 * run method in async way
	 * @param method callback method
	 * @param data callback params
	 */
	public static void asyncMethod(Method method, Object... args) {

		CustomThreadPool.queueUserWorkItem(new Runnable() {
			Method _method;
			Object[] _args;

			public Runnable init(Method method, Object... args) {
				_method = method;
				_args = args;
				
				return this;
			}

			@Override
			public void run() {
				try {
					if (_method != null) {
						_method.invoke(null, _args);
					}
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					LogUtil.logException(log, e);
				}
			}
		}.init(method, args));
	}	
}
