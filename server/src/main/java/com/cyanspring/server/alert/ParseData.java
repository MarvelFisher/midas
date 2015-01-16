package com.cyanspring.server.alert;

public class ParseData {
	String strpushMessage = "";
	String strUserId = "";
	String strMsgId = "";
	String strMsgType = "";
	String strLocalTime = "";
	String strKeyValue = "";		
    
    ParseData(String UserId, String PushMessage, String MsgId, String MsgType, String LocalTime, String KeyValue)
    {
    	strUserId = UserId;
        strpushMessage = PushMessage;
        strMsgId = MsgId;
        strMsgType = MsgType;
        strLocalTime = LocalTime;
        strKeyValue = KeyValue;	        
    }
    
    public String getMsg()
    {
    	return strpushMessage ;
    }
}