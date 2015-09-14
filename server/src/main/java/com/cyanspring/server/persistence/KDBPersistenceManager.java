package com.cyanspring.server.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.TimeThrottler;
import com.cyanspring.common.util.TimeUtil;
import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QTimestamp;
import com.exxeleron.qjava.QConnection.MessageType;

public class KDBPersistenceManager implements IPlugin {
	public static final Logger log = LoggerFactory.getLogger(KDBPersistenceManager.class);
	private String ip = "localhost";
	private int port = 5001;
	private String user = "";
	private String pwd = "";
	private QConnection con;
	private boolean cleanCache = true;
	private int writeFileInterval = 60;
	private TimeThrottler throttler;
	
	@Override
	public void init() throws Exception {
		con = new QBasicConnection(ip, port, user, pwd);
		con.open();
		throttler = new TimeThrottler(writeFileInterval * 1000);
	}

	@Override
	public void uninit() {

	}

	public boolean saveQuote(Quote quote) {
		try {
			if (con.isConnected()) {
				final Object[] data = new Object[] {quote.getSymbol(), quote.getBid(), quote.getAsk(), quote.getBidVol(), quote.getAskVol(), quote.getLast(), 
						quote.getLastVol(), quote.getTurnover(), quote.getHigh(), quote.getLow(), quote.getOpen(), quote.getClose(), quote.getTotalVolume(), 
						new QTimestamp(quote.getTimeStamp())};
				con.sync("insert", "QuoteTable", data);				
			} else {
				log.info("QConnection is not initialized");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	public boolean saveQuotes(List<Quote> list) {
		Date now = Clock.getInstance().now();
		try {
			if (con.isConnected()) {
				int size = list.size();
				final Object[] data = new Object[] {new String[size], new Double[size], new Double[size], new Double[size], new Double[size]
						, new Double[size], new Double[size], new Double[size], new Double[size], new Double[size], new Double[size]
								, new Double[size], new Double[size], new QTimestamp[size]};
				for (int i=0; i < size; i++) {
					Quote quote = list.get(i);
					((String[]) data[0])[i] = quote.getSymbol();
		            ((Double[]) data[1])[i] = quote.getBid();
		            ((Double[]) data[2])[i] = quote.getAsk();
		            ((Double[]) data[3])[i] = quote.getBidVol();
		            ((Double[]) data[4])[i] = quote.getAskVol();
		            ((Double[]) data[5])[i] = quote.getLast();
		            ((Double[]) data[6])[i] = quote.getLastVol();
		            ((Double[]) data[7])[i] = quote.getTurnover();
		            ((Double[]) data[8])[i] = quote.getHigh();
		            ((Double[]) data[9])[i] = quote.getLow();
		            ((Double[]) data[10])[i] = quote.getOpen();
		            ((Double[]) data[11])[i] = quote.getClose();
		            ((Double[]) data[12])[i] = quote.getTotalVolume();
		            ((QTimestamp[]) data[13])[i] = new QTimestamp(quote.getTimeStamp());
				}				
				con.sync("insert", "QuoteTable", data);
				
				if(throttler.check()) {
					con.query(MessageType.SYNC, "`:QuoteTable insert (select from `QuoteTable)");
					if (cleanCache) {
						con.query(MessageType.SYNC, "delete from `QuoteTable");
					}
				}
				
			} else {
				log.info("QConnection is not initialized");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
		
		Date after = Clock.getInstance().now();
		long pTime = TimeUtil.getTimePass(after, now);
		if (pTime > 100)
			log.info("Process time:" + pTime + "(msc)");
		return true;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	public boolean isCleanCache() {
		return cleanCache;
	}

	public void setCleanCache(boolean cleanCache) {
		this.cleanCache = cleanCache;
	}

	public int getWriteFileInterval() {
		return writeFileInterval;
	}

	public void setWriteFileInterval(int writeFileInterval) {
		this.writeFileInterval = writeFileInterval;
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/common/mylog4j.xml");
		KDBPersistenceManager manager = new KDBPersistenceManager();
		manager.setIp("localhost");
		manager.setPort(5001);
		Quote quote = new Quote("AUDUSD", null, null);
		quote.setBid(123.3);
		quote.setAsk(123.5);
		quote.setBidVol(10000);
		quote.setAskVol(20000);
		quote.setLast(125);
		quote.setLastVol(15000);
		quote.setTurnover(300);
		quote.setHigh(129.2);
		quote.setLow(112.3);
		quote.setOpen(120.0);
		quote.setClose(119.5);
		quote.setTotalVolume(5000000);
		
		Quote quote2 = new Quote("AUDUSD", null, null);
		quote2.setBid(124.3);
		quote2.setAsk(123.5);
		quote2.setBidVol(10000);
		quote2.setAskVol(20000);
		quote2.setLast(125);
		quote2.setLastVol(15000);
		quote2.setTurnover(300);
		quote2.setHigh(129.2);
		quote2.setLow(112.3);
		quote2.setOpen(120.0);
		quote2.setClose(119.5);
		quote2.setTotalVolume(5000000);
		
		manager.init();
//		manager.saveQuote(new InnerQuote(QuoteSource.DEFAULT,quote));
		List<Quote> qList = new ArrayList<>();
		qList.add(quote);
		qList.add(quote2);
		manager.saveQuotes(qList);
	}
}
