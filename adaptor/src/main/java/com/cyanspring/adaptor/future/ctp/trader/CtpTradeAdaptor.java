package com.cyanspring.adaptor.future.ctp.trader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bridj.BridJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.ISymbolConverter;
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
	private String libPath;
	private List<IDownStreamConnection> connections = new ArrayList<IDownStreamConnection>();
	private ISymbolConverter symbolConverter;

	@Override
	public void init() throws Exception {
		initNativeLibrary();
		
		for (int i = 0 ; i < connectionCount ; i ++) {
			connections.add(new CtpTradeConnection(connectionPrefix + i, url, broker, conLog, user, password, symbolConverter));
		}
		
		for(IDownStreamConnection connection: connections) {
			connection.init();
		}
		log.info("Ctp connection initialized: " + connectionPrefix + " " + connectionCount);	
	}

	private void initNativeLibrary() {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		if(null != libPath) {
			BridJ.setNativeLibraryFile("Trader", new File(libPath));
		} else if ( os.toLowerCase().contains("win") ) {
			if ( arch.contains("x64") ) {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\win64\\thosttraderapi.dll"));
			} else {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\win32\\thosttraderapi.dll"));
			}			
		} else if ( os.toLowerCase().contains("linux") ) {
			if ( arch.contains("x64") ) {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\linux64\\thosttraderapi.so"));
			} else {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\linux32\\thosttraderapi.so"));
			}
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

	public String getLibPath() {
		return libPath;
	}

	public void setLibPath(String libPath) {
		this.libPath = libPath;
	}

	public ISymbolConverter getSymbolConverter() {
		return symbolConverter;
	}

	public void setSymbolConverter(ISymbolConverter symbolConverter) {
		this.symbolConverter = symbolConverter;
	}

	
}
