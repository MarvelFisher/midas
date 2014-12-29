package com.cyanspring.id.Library.Lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class WriteLock implements AutoCloseable {
	static final int WRITELOCK = 5;
	private ReadWriteLock _rwl;
	private boolean isWriteLockHeld = false;

	public WriteLock(ReadWriteLock rwl) {
		try {
			_rwl = rwl;
			if (_rwl.writeLock().tryLock(WRITELOCK, TimeUnit.SECONDS)) {
				isWriteLockHeld = true;
			}
		} catch (Exception e) {
			close();
		}
	}

	public WriteLock(ReadWriteLock rwl, int nSecond) {
		try {
			_rwl = rwl;
			if (_rwl.writeLock().tryLock(nSecond, TimeUnit.SECONDS)) {
				isWriteLockHeld = true;
			}
		} catch (Exception e) {
			close();
		}
	}

	protected void finalize() {
		close();
	}

	public void close() {
		if (_rwl != null) {
			try {
				if (isWriteLockHeld) // in case of timeout, lock is already
										// released
				{
					_rwl.writeLock().unlock();
				}
				isWriteLockHeld = false;
			} catch (Exception e) {
			} finally {
				_rwl = null;
			}
		}
	}
}
