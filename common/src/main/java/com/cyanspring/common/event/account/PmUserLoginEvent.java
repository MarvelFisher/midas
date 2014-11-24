package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmUserLoginEvent extends RemoteAsyncEvent
{
	Object m_userKeeper;
	Object m_accountKeeper;
	UserLoginEvent m_originalEvent;
	
	public PmUserLoginEvent(String key, String receiver, Object userKeeper, Object accountKeeper, UserLoginEvent orgEvent) 
	{
		super(key, receiver);
		m_userKeeper = userKeeper;
		m_accountKeeper = accountKeeper;
		m_originalEvent = orgEvent;
	}
	public Object getUserKeeper()
	{
		return m_userKeeper;
	}
	public Object getAccountKeeper()
	{
		return m_accountKeeper;
	}
	public UserLoginEvent getOriginalEvent()
	{
		return m_originalEvent;
	}
}
