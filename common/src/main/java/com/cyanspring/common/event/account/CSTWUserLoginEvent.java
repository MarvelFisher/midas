package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CSTWUserLoginEvent extends RemoteAsyncEvent {

	private static final long serialVersionUID = 1L;
	private String id;
	private String password;
	private boolean shutdownServer = false;

	public CSTWUserLoginEvent(String key, String receiver, String id,
			String password) {
		this(key, receiver, id, password, false);
	}

	public CSTWUserLoginEvent(String key, String receiver, String id,
			String password, boolean shutdownServer) {
		super(key, receiver);
		this.id = id;
		this.password = password;
		this.shutdownServer = shutdownServer;
	}

	public String getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public boolean getShutdownServer() {
		return shutdownServer;
	}

}
