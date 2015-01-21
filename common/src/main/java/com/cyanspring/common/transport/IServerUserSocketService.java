package com.cyanspring.common.transport;

import java.util.List;

import com.cyanspring.common.IPlugin;

public interface IServerUserSocketService extends IPlugin {
	IUserSocketContext getContext(String key);
	List<IUserSocketContext> getContextByUser(String user);
	void setUserContext(String user, IUserSocketContext ctx);
	boolean addListener(IServerSocketListener listener);
	boolean removeListener(IServerSocketListener listener);
}
