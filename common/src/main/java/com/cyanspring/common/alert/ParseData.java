package com.cyanspring.common.alert;

public class ParseData {
	private String strpushMessage = "";
	private String strUserId = "";
	private String strMsgId = "";
	private String strMsgType = "";
	private String strLocalTime = "";
	private String strKeyValue = "";
    private String strDeepLink = "";
    
    public ParseData(String UserId, String PushMessage, String MsgId, String MsgType, String LocalTime, String KeyValue, String DeepLink)
    {
    	setStrUserId(UserId);
        setStrpushMessage(PushMessage);
        setStrMsgId(MsgId);
        setStrMsgType(MsgType);
        setStrLocalTime(LocalTime);
        setStrKeyValue(KeyValue);
        setStrDeepLink(DeepLink);
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

    public String getStrDeepLink() {
        return strDeepLink;
    }

    public void setStrDeepLink(String strDeepLink) {
        this.strDeepLink = strDeepLink;
    }
}