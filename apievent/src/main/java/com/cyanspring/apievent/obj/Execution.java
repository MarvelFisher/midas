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
public class Execution { // need to check the fields
    // for thread safe
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Date created;
    private Date modified;
    private String id;
    private String serverID;
    private String symbol;
    private String side;
    private long quantity;
    private double price;
    private String orderID;
    private String parentOrderID;
    private String strategyID;
    private String execID;
    private String user;
    private String account;

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

    public Date getModified() {
        try {
            lock.readLock().lock();
            return modified;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setModified(Date modified) {
        try {
            lock.writeLock().lock();
            this.modified = modified;
        } finally {
            lock.writeLock().unlock();
        }

    }

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

    public String getServerID() {
        try {
            lock.readLock().lock();
            return serverID;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setServerID(String serverID) {
        try {
            lock.writeLock().lock();
            this.serverID = serverID;
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

    public String getSide() {
        try {
            lock.readLock().lock();
            return side;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setSide(String side) {
        try {
            lock.writeLock().lock();
            this.side = side;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public long getQuantity() {
        try {
            lock.readLock().lock();
            return quantity;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setQuantity(long quantity) {
        try {
            lock.writeLock().lock();
            this.quantity = quantity;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public double getPrice() {
        try {
            lock.readLock().lock();
            return price;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPrice(double price) {
        try {
            lock.writeLock().lock();
            this.price = price;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public String getOrderID() {
        try {
            lock.readLock().lock();
            return orderID;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setOrderID(String orderID) {
        try {
            lock.writeLock().lock();
            this.orderID = orderID;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public String getParentOrderID() {
        try {
            lock.readLock().lock();
            return parentOrderID;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setParentOrderID(String parentOrderID) {
        try {
            lock.writeLock().lock();
            this.parentOrderID = parentOrderID;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public String getStrategyID() {
        try {
            lock.readLock().lock();
            return strategyID;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setStrategyID(String strategyID) {
        try {
            lock.writeLock().lock();
            this.strategyID = strategyID;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public String getExecID() {
        try {
            lock.readLock().lock();
            return execID;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setExecID(String execID) {
        try {
            lock.writeLock().lock();
            this.execID = execID;
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
}
