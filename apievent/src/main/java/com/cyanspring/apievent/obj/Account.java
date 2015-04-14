package com.cyanspring.apievent.obj;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Description....
 * <ul>
 * <li> Description
 * </ul>
 * <p/>
 * Description....
 * <p/>
 * Description....
 * <p/>
 * Description....
 *
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class Account {
    // for thread safe
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private double dailyPnL;
    private double PnL;
    private double urPnL;
    private double allTimePnL;
    private String currency;
    private double cash;
    private double margin;
    private double value;

    public double getDailyPnL() {
        try {
            lock.readLock().lock();
            return dailyPnL;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setDailyPnL(double dailyPnL) {
        try {
            lock.writeLock().lock();
            this.dailyPnL = dailyPnL;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getMargin() {
        try {
            lock.readLock().lock();
            return margin;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setMargin(double margin) {
        try {
            lock.writeLock().lock();
            this.margin = margin;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getValue() {
        try {
            lock.readLock().lock();
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setValue(double value) {
        try {
            lock.writeLock().lock();
            this.value = value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getPnL() {
        try {
            lock.readLock().lock();
            return PnL;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPnL(double pnL) {
        try {
            lock.writeLock().lock();
            PnL = pnL;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getUrPnL() {
        try {
            lock.readLock().lock();
            return urPnL;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setUrPnL(double urPnL) {
        try {
            lock.writeLock().lock();
            this.urPnL = urPnL;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getAllTimePnL() {
        try {
            lock.readLock().lock();
            return allTimePnL;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setAllTimePnL(double allTimePnL) {
        try {
            lock.writeLock().lock();
            this.allTimePnL = allTimePnL;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getCurrency() {
        try {
            lock.readLock().lock();
            return currency;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setCurrency(String currency) {
        try {
            lock.writeLock().lock();
            this.currency = currency;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getCash() {
        try {
            lock.readLock().lock();
            return cash;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setCash(double cash) {
        try {
            lock.writeLock().lock();
            this.cash = cash;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
