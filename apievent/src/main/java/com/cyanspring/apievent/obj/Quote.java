package com.cyanspring.apievent.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
public class Quote implements Cloneable, Serializable {
    private static final Logger log = LoggerFactory
            .getLogger(Quote.class);
    String id;
    public int sourceId = 1;

    String symbol;
    double bid;
    double ask;
    double bidVol;
    double askVol;
    double last;
    double lastVol;
    double high;
    double low;
    double open;
    double close;
    double totalVolume;
    Date timeStamp;
    Date timeSent;
    boolean stale;
//	boolean opened;

    public String getId(){
        return id;
    }

    public String getSymbol() {
        return symbol;
    }
    public double getBid() {
        return bid;
    }
    public void setBid(double bid) {
        this.bid = bid;
    }
    public double getAsk() {
        return ask;
    }
    public void setAsk(double ask) {
        this.ask = ask;
    }
    public double getBidVol() {
        return bidVol;
    }
    public void setBidVol(double bidVol) {
        this.bidVol = bidVol;
    }
    public double getAskVol() {
        return askVol;
    }
    public void setAskVol(double askVol) {
        this.askVol = askVol;
    }
    public double getLast() {
        return last;
    }
    public void setLast(double last) {
        this.last = last;
    }
    public double getLastVol() {
        return lastVol;
    }
    public void setLastVol(double lastVol) {
        this.lastVol = lastVol;
    }
    public double getHigh() {
        return high;
    }
    public void setHigh(double high) {
        this.high = high;
    }
    public double getLow() {
        return low;
    }
    public void setLow(double low) {
        this.low = low;
    }
    public double getOpen() {
        return open;
    }
    public void setOpen(double open) {
        this.open = open;
    }
    public double getClose() {
        return close;
    }
    public void setClose(double close) {
        this.close = close;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Quote(String id, String symbol, Date timeStamp) {
        this.id = id;
        this.symbol = symbol;
        this.timeStamp = timeStamp;
        this.timeSent = this.timeStamp;
//		this.opened = true;
    }

    public Date getTimeSent() {
        return timeSent;
    }
    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    public double getTotalVolume() {
        return totalVolume;
    }
    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public boolean isStale() {
        return stale;
    }
    public void setStale(boolean stale) {
        this.stale = stale;
    }

    public String toString()
    {
        return id + " - " + symbol + " : [" + bidVol + "@" + bid + "," + askVol + "@" + ask + "," + timeSent + "]";
    }

    public void print()
    {
        System.out.printf("%n                 %s                      %n", getSymbol());
        System.out.printf("--------------------------------------------%n");
        System.out.printf("         Buy                  Sell          %n");
        System.out.printf("--------------------------------------------%n");
        System.out.printf("[%f, %f], [%f, %f], [%f, %f]%n", bidVol, bid, askVol, ask, lastVol, last);
        System.out.printf("--------------------------------------------%n%n");
    }

    public void cloneQuote(double bid, double ask, double bidVol, double askVol,
                           double last, double lastVol, double high, double low,
                           double open, double close, double totalVolume, boolean stale){
        this.bid = bid;
        this.ask = ask;
        this.bidVol = bidVol;
        this.askVol = askVol;
        this.last = last;
        this.lastVol = lastVol;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.totalVolume = totalVolume;
        this.stale = stale;
    }
}
