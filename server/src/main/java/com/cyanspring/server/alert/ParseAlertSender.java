package com.cyanspring.server.alert;

import java.io.DataOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.alert.PriceAlert;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.util.IdGenerator;

public class ParseAlertSender implements IPriceAlertSender, ITradeAlertSender {
	private static final Logger log = LoggerFactory
			.getLogger(ParseAlertSender.class);

	//parse settings
	//********************************************************************************************
    //Dev
    //X-Parse-Application-Id = NSJUuK6ePPmFPaatYUuRCoySVcgVNMQbyZMrrN8D
    //X-Parse-REST-API-KEY = BW1YETnPzHgR3T4aTpWSIoOfqH2g68w7sq1PVfrO
    //Test
    //X-Parse-Application-Id = SzaPSKsOWCA0paXxKYLSOdruiQjfsKb2KiyNR073
    //X-Parse-REST-API-KEY = U5imDlpxABkXyha1AfEktKxuV3hwFArIuv6hMBwn
    //UAT
	//X-Parse-Application-Id = Ek9KqTvVcJYTtZPzsUF6KxqaZJ5vRrYJ88UkrIbX 
	//X-Parse-REST-API-KEY = 0ZQLbMl954IwK6D2JS3JPTinqikxY2JSRTepJnsE
    //Prod
    //X-Parse-Application-Id = G9wAmTe6AOaeGrks9rqbatPCxr0s9axnUeCvtUud 
    //X-Parse-REST-API-KEY = CO3Ht9wQcnpJ5nWogzC1OgiHoxEXPsZu8PEdRUmP
    //********************************************************************************************
	private String parseApplicationId;
	private String parseRestApiId;
	
	private static final String MSG_TYPE_PRICE = "1";
	private static final String MSG_TYPE_ORDER = "2";
	
	private class ParseData {
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
	}

	//********************************************************************************************
	//Example:	(Price Alert)
	//strUserId = "David" ; 
	//PushMessage = "USDJPY.FX just reached $108.290" ;
	//MsgId = "David.15:27:16:462" ;
	//MsgType = "1" ;
	//LocalTime = 2014-10-22 15:27:16
	//KeyValue = USDJPY.FX,108.290
	
	//MsgType=>
	//1�= Alert Notification
	//2�= Order Notification
	//3�= System Notification
	//4�= Being Followed Notification
	//5�= Post Liked Notification
	//"6�= Commented Notification
	//"7�= @mentioned (posts) Notification
	//"8�= @mentioned (comments) Notification
	//"9�= Create Group Notification
	//"10�= Group Invite Notification
	//"11�= Accepted to Group Notification
	//"12�= Kick out of the Group Notification
	//"13�= Delete Group Notification
	//"14�= New Post in a Group Notification
	//"15�= Stop Loss Notification
	 
	 // We only have 1:Alert, 2:Order
	//********************************************************************************************
	protected void sendPost(ParseData PD) throws Exception 
	{   	 		
		URL obj = new URL("https://api.parse.com/1/push") ;
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		
		String strPoststring = "{ \"where\": {\"userId\": \"" + PD.strUserId + "\" , \"deviceType\": { \"$in\": [ \"ios\", \"android\", \"winphone\", \"js\" ] }}, " +
                "\"data\": {\"alert\": \"" + PD.strpushMessage + "\",\"8004\":\"" + PD.strMsgId + "\",\"8006\":\"" + PD.strMsgType +
                "\",\"action\":\"" + "com.hkfdt.activity.UPDATE_STATUS" + ((PD.strLocalTime.length() > 0) ? ("\",\"1014\":\"" + PD.strLocalTime) : "") + 
                ((PD.strKeyValue.length() > 0) ? ("\",\"8007\":\"" + PD.strKeyValue) : "") + "\"" +
                ", \"badge\": \"Increment\"}}";
 
		log.debug("Parse message: " + strPoststring);
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("X-Parse-Application-Id", parseApplicationId);
		con.setRequestProperty("X-Parse-REST-API-KEY", parseRestApiId);
		con.setRequestProperty("Content-type", "application/json");
		con.setRequestProperty("Content-Length", Integer.toString(strPoststring.length()));
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(strPoststring);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		
		log.debug("Return code: " + responseCode);
	}

	@Override
	public void sendTradeAlert(Execution execution) {
		DecimalFormat qtyFormat = new DecimalFormat("#0");
		String strQty = qtyFormat.format(execution.getQuantity());
		DecimalFormat priceFormat = new DecimalFormat("#0.#####");
		String strPrice = priceFormat.format(execution.getPrice());
		String tradeMessage = "Trade " + execution.getSymbol() + " " + 
				execution.getSide().toString() + " " + strQty + "@" + strPrice;
		String account = execution.getAccount();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(Clock.getInstance().now());
		String keyValue = execution.getSymbol() + "," + strPrice + "," + strQty + "," + (execution.getSide().isBuy()?"BOUGHT":"SOLD");
		ParseData data = new ParseData(account, tradeMessage, account + IdGenerator.getInstance().getNextID(),
				MSG_TYPE_ORDER, strDate, keyValue);
		
		try {
			sendPost(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendPriceAlert(PriceAlert priceAlert) {
		// TODO Auto-generated method stub
		
	}

	// getters and setters
	public String getParseApplicationId() {
		return parseApplicationId;
	}

	public void setParseApplicationId(String parseApplicationId) {
		this.parseApplicationId = parseApplicationId;
	}

	public String getParseRestApiId() {
		return parseRestApiId;
	}

	public void setParseRestApiId(String parseRestApiId) {
		this.parseRestApiId = parseRestApiId;
	}


}
