package com.cyanspring.apievent.obj;

import java.util.Date;
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
public class Position {
    // for thread safe
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private String id;
    private String account;
    private String user;
    private String symbol;
    private double qty;
    private double PnL;
    private Date created;
    private double acPnL;

    public String getId() {
        try {
            lock.readLock().lock();
            return id;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setId(String id) {
        try {
            lock.writeLock().lock();
            this.id = id;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getAccount() {
        try {
            lock.readLock().lock();
            return account;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setAccount(String account) {
        try {
            lock.writeLock().lock();
            this.account = account;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public String getUser() {
        try {
            lock.readLock().lock();
            return user;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setUser(String user) {
        try {
            lock.writeLock().lock();
            this.user = user;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public String getSymbol() {
        try {
            lock.readLock().lock();
            return symbol;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setSymbol(String symbol) {
        try {
            lock.writeLock().lock();
            this.symbol = symbol;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public double getQty() {
        try {
            lock.readLock().lock();
            return qty;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setQty(double qty) {
        try {
            lock.writeLock().lock();
            this.qty = qty;
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

    public Date getCreated() {
        try {
            lock.readLock().lock();
            return created;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setCreated(Date created) {
        try {
            lock.writeLock().lock();
            this.created = created;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public double getAcPnL() {
        try {
            lock.readLock().lock();
            return acPnL;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setAcPnL(double acPnL) {
        try {
            lock.writeLock().lock();
            this.acPnL = acPnL;
        } finally {
            lock.writeLock().unlock();
        }

    }
}
