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
public class OpenPosition extends Position {
    // for thread safe
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private double price;
    private double margin;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        try {
            lock.writeLock().lock();
            this.price = price;
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
}
