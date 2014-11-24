package com.cyanspring.common.account;

import java.util.HashMap;


public enum UserType 
{
	ADMIN(0),
	NORMAL(1),
	SUPPORT(2),	
	TEST(101),
	;
	
	static HashMap<Integer, UserType> hmRecord = new HashMap<>();
	static 
	{
		for(UserType type : values())
			hmRecord.put(type.getCode(), type);
	}
	static public UserType fromCode(int nCode)
	{
		return hmRecord.get(nCode);
	}
	
	private int m_nCode;
	
	private UserType(int nCode)
	{
		m_nCode = nCode;
	}
	public int getCode()
	{
		return m_nCode;
	}
}
