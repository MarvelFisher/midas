package com.cyanspring.id.Library.Lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class ReadLock implements AutoCloseable {
	static final int READLOCK = 1;
	private ReadWriteLock _rwl;
	private boolean isReadLockHeld = false;

	public ReadLock(ReadWriteLock rwl) {
		try {
			_rwl = rwl;
			if (_rwl.readLock().tryLock(READLOCK, TimeUnit.SECONDS)) {
				isReadLockHeld = true;
			}
		} catch (Exception e) {
			close();
		}
	}

	public ReadLock(ReadWriteLock rwl, int nSecond) {
		try {
			_rwl = rwl;
			if (_rwl.readLock().tryLock(nSecond, TimeUnit.SECONDS)) {
				isReadLockHeld = true;
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
				if (isReadLockHeld) // in case of timeout, lock is already
									// released
				{
					_rwl.readLock().unlock();
				}
				isReadLockHeld = false;
			} catch (Exception e) {
			} finally {
				_rwl = null;
			}
		}
	}
}
