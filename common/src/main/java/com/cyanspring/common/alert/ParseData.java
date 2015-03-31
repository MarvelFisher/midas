package com.cyanspring.common.alert;

public class ParseData {
	private String strpushMessage = "";
	private String strUserId = "";
	private String strMsgId = "";
	private String strMsgType = "";
	private String strLocalTime = "";
	private String strKeyValue = "";		
    
    public ParseData(String UserId, String PushMessage, String MsgId, String MsgType, String LocalTime, String KeyValue)
    {
    	setStrUserId(UserId);
        setStrpushMessage(PushMessage);
        setStrMsgId(MsgId);
        setStrMsgType(MsgType);
        setStrLocalTime(LocalTime);
        setStrKeyValue(KeyValue);	        
    }

	public String getStrpushMessage() {
		return strpushMessage;
	}

	public void setStrpushMessage(String strpushMessage) {
		this.strpushMessage = strpushMessage;
	}

	public String getStrUserId() {
		return strUserId;
	}

	public void setStrUserId(String strUserId) {
		this.strUserId = strUserId;
	}

	public String getStrMsgId() {
		return strMsgId;
	}

	public void setStrMsgId(String strMsgId) {
		this.strMsgId = strMsgId;
	}

	public String getStrMsgType() {
		return strMsgType;
	}

	public void setStrMsgType(String strMsgType) {
		this.strMsgType = strMsgType;
	}

	public String getStrLocalTime() {
		return strLocalTime;
	}

	public void setStrLocalTime(String strLocalTime) {
		this.strLocalTime = strLocalTime;
	}

	public String getStrKeyValue() {
		return strKeyValue;
	}

	public void setStrKeyValue(String strKeyValue) {
		this.strKeyValue = strKeyValue;
	}
}