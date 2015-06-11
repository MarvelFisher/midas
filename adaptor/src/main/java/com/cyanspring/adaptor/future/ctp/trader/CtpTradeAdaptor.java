package com.cyanspring.adaptor.future.ctp.trader;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.stream.IStreamAdaptor;

public class CtpTradeAdaptor implements IStreamAdaptor<IDownStreamConnection> {
	private static final Logger log = LoggerFactory
			.getLogger(CtpTradeAdaptor.class);

	private String url;
	private String conLog;
	private String connectionPrefix = "CTP";
	private String user = "";
	private String password = "";	

	private String broker = "";
	private int connectionCount = 2;
	private List<IDownStreamConnection> connections = new ArrayList<IDownStreamConnection>();

	@Override
	public void init() throws Exception {
//		for (int i = 0 ; i < 2 ; i ++) {
			connections.add(new CtpTradeConnection(connectionPrefix + 1, url, broker, conLog, user, password));
//		}
		
		
		for(IDownStreamConnection connection: connections) {
			connection.init();
		}
			
	}

	@Override
	public void uninit() {
		for(IDownStreamConnection connection: connections)
			connection.uninit();
	}

	@Override
	public List<IDownStreamConnection> getConnections() {
		return connections;
	}
	
	public void setConnections(List<IDownStreamConnection> connections) {
		this.connections = connections;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public String getConLog() {
		return conLog;
	}

	public void setConLog(String conLog) {
		this.conLog = conLog;
	}

	public int getConnectionCount() {
		return connectionCount;
	}

	public void setConnectionCount(int connectionCount) {
		this.connectionCount = connectionCount;
	}

	
}
