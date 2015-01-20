package com.cyanspring.common.transport;

public interface IServerSocketListener {
	void onConnected(boolean connected, IUserSocketContext ctx);
	void onMessage(Object obj, IUserSocketContext ctx);
}
