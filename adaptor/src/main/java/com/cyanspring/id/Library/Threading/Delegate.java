package com.cyanspring.id.Library.Threading;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Util.LogUtil;

public final class Delegate<K, T> implements AutoCloseable {
	
	private static final Logger log = LoggerFactory.getLogger(Delegate.class);
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Method getMethod(String strMethod, Class callerClass, Class[] args) {
		Method m = null;
		try {
			m = callerClass.getMethod(strMethod, args);
		} catch (NoSuchMethodException | SecurityException e) {
			LogUtil.logException(log, e);
		}

		return m;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Method getMethod(String strMethod, String strCallerClass, String[] args) {
		
		Method m = null;
		try {
			Class caller = Class.forName(strCallerClass);
			if (caller == null)
				return null;
				
			ArrayList<Class> classes = new ArrayList<Class>();
			for (String s : args)
			{
				Class c = Class.forName(s);
				if (c == null)
					return null;
				classes.add(c);
			}
			
			Class[] arrClass = (Class[]) classes.toArray();
			
			m = caller.getMethod(strMethod,	arrClass);
		} catch (NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			LogUtil.logException(log, e);
		}

		return m;

	}

	Object _lock = new Object();
	ArrayList<Method> _list = new ArrayList<Method>();

	public int size() {
		synchronized (_lock) {
			return _list.size();
		}
	}

	protected void finalize() throws Throwable {
		close();
	}

	public void add(Method m) {
		synchronized (_lock) {
			_list.add(m);
		}
	}

	public void remove(Method m) {
		synchronized (_lock) {
			_list.remove(m);
		}
	}

	public void invoke(final K arg1, final T arg2)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		synchronized (_lock) {
			try {

				for (final Method m : _list) {
					/*
					 * CustomThreadPool.queueUserWorkItem(new Runnable() {
					 * 
					 * @Override public void run() { try { m.invoke(null, arg1,
					 * arg2); } catch (IllegalAccessException |
					 * IllegalArgumentException | InvocationTargetException e) {
					 * } } });
					 */
					m.invoke(null, arg1, arg2);
				}
			} catch (Exception ex) {
				LogUtil.logException(log, ex);
			}
		}
	}

	public void close() {
		synchronized (_lock) {
			if (_list != null) {
				_list.clear();
				_list = null;
			}
		}
	}
}
