package com.cyanspring.server.persistence;

import java.text.SimpleDateFormat;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteSource;
import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QConnection.MessageType;

public class KDBPersistenceManager implements IPlugin {
	public static final Logger log = LoggerFactory.getLogger(KDBPersistenceManager.class);
	private String ip = "localhost";
	private int port = 5001;
	private String user = "";
	private String pwd = "";
	private QConnection con;
	private String upsertQuote = "`:QuoteTable upsert "
			+ "`symbol`bid`ask`bidVol`askVol`lastPrice`lastVol`turnover`high`low`open`close`totalVolume`timeStamp!"
			+ "(`%s;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%s)";
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd'D'HH:mm:ss.SSS");
	
	@Override
	public void init() throws Exception {
		con = new QBasicConnection(ip, port, user, pwd);
		con.open();
	}

	@Override
	public void uninit() {

	}

	public boolean saveQuote(InnerQuote innerQuote) {
		try {
			if (con.isConnected()) {
				Quote quote = innerQuote.getQuote();
				String query = String.format(upsertQuote, quote.getSymbol(), quote.getBid(), quote.getAsk(), quote.getBidVol(),
						quote.getAskVol(), quote.getLast(), quote.getLastVol(), quote.getTurnover(), quote.getHigh(),
						quote.getLow(), quote.getOpen(), quote.getClose(), quote.getTotalVolume(), sdf.format(quote.getTimeStamp()));
				con.query(MessageType.SYNC, query);				
			} else {
				log.info("QConnection is not initialized");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
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
		manager.init();
		for (int i=0; i< 10; i++)
			manager.saveQuote(new InnerQuote(QuoteSource.DEFAULT,quote));
	}
}
