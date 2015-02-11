package com.cyanspring.common.account;

import java.util.HashMap;


public enum UserType 
{
	ADMIN(0),
	NORMAL(1),
	SUPPORT(2),	
	TRADER(3),
	RISKMANAGER(4),
	FRONTMANAGER(5),
	BACKMANAGER(6),
	
	// Third-Party Authentication
	FACEBOOK(50),
	QQ(51),
	WECHAT(52),
	TWITTER(53),
	
	TEST(101),
	GROUPUSER(102),
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
