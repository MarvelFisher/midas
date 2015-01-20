package com.cyanspring.common.transport;

import java.util.List;

import com.cyanspring.common.IPlugin;

public interface IClientSocketService extends IPlugin {
	boolean sendMessage(Object obj);
	boolean addListener(IClientSocketListener listener);
	boolean removeListener(IClientSocketListener listener);
}
