package com.cyanspring.common.error;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLookup {
	private static final Logger log = LoggerFactory
			.getLogger(ErrorLookup.class);
	private static final Map<Integer, String> map = new HashMap<Integer, String>();
	
	private static void addAndCheck(int code, String message) throws Exception {
		if(map.put(code, message) != null)
			throw new Exception("Critical error: error code duplicate: " + code);
	}
	static {
		try {	
			// system errors start with 100
			addAndCheck(100, "Receive handle error");
			addAndCheck(101, "ActiveMQ connect failed");
			addAndCheck(102, "ActiveMQ close failed");
			addAndCheck(103, "ActiveMQ timeout");
			addAndCheck(104, "ActiveMQ idle");	
			addAndCheck(105, "packet format is wrong");
			addAndCheck(106, "connection is not available");
			addAndCheck(107, "connection is broken");
			addAndCheck(108, "Invalid Cmd Packet");
			addAndCheck(109, "Unsupported Cmd Packet");
			addAndCheck(110, "Server Not Available");
			addAndCheck(111, "send frame failed");			
			addAndCheck(112, "doesn't set user");
			addAndCheck(113, "doesn't set callback");
			addAndCheck(114, "doesn't set connection configuration");	
			
			// business errors start with 200
			addAndCheck(200, "Premium follow info incomplete");
			addAndCheck(201, "Premium request time out");
			addAndCheck(202, "SymbolList error");
			addAndCheck(203, "Search Symbol error");
			addAndCheck(204, "Price Alert error");
			addAndCheck(205, "Trade Alert error");
			addAndCheck(206, "No Alert Data");		
			addAndCheck(207, "Ref Symbols not found"); // Ref packet's symbols not found
			addAndCheck(208, "Ref Symbols duplicated"); // Ref packet's symbols duplicated
			addAndCheck(209, "Ref Symbols not valid"); // Ref packet's symbols not valid
			addAndCheck(210, "Ref Symbols runtime error"); // Ref packet's symbols runtime error
			addAndCheck(211, "User didn't subscribe");// User didn't subscribe symbol
			addAndCheck(212, "User doesn't have enough permission"); // User doesn't have enough permission
			addAndCheck(213, "Subscribe limitation"); // Subscribe count is limited
			addAndCheck(214, "Sequence number wrong");
			addAndCheck(215, "Frequence is not Support");//Chart Error
			
			
			
			// api errors start with 300
			addAndCheck(300, "Server isn't connected");
			addAndCheck(301, "User must login before send any events");
			addAndCheck(302, "Event type not support");
			addAndCheck(303, "Account & user not match");
			addAndCheck(304, "Can't login, blocked by existing connection");
			
			
			
			// order errors start with 400
			addAndCheck(400, "action is cancelled");
			addAndCheck(401, "order is processing");
			addAndCheck(402, "order didn't change");
			addAndCheck(403, "order id is not found");
			addAndCheck(404, "order side is not supported");
			addAndCheck(405, "order type is not supported");
			addAndCheck(406, "account doesn't exist");
			addAndCheck(407, "user didn't login");
			addAndCheck(408, "enter order error");
			addAndCheck(409, "amend order error");
			addAndCheck(410, "cancel order error");
			addAndCheck(411, "amend parameters not enough");
			addAndCheck(412, "close order error");
			addAndCheck(413, "txid illegal error");
			addAndCheck(414, "order reason is not supported");
			addAndCheck(415, "alert type is not supported");
			addAndCheck(416, "txid illegal error");
			addAndCheck(417, "assign price please");
	
			// user errors start with 500
			addAndCheck(500, "Create user failed");
			addAndCheck(501, "User login failed");
			addAndCheck(502, "Change User Password failed");
			addAndCheck(503, "User could not login this AppServer, try another");
			addAndCheck(504, "Wrong User Type");
			addAndCheck(505, "Passwrod null or empty");
			addAndCheck(506, "Premium follow error");
			addAndCheck(507, "Account Reset error");
			addAndCheck(508, "Invalid user information"); // Connect fail due to invalid user information
			addAndCheck(509, "user didn't login");

			// client errors start with 600
			addAndCheck(600, "Please restart your App");
			addAndCheck(601, "Server in Maintaining , Please wait");
			addAndCheck(602, "Another device login your ID");
			addAndCheck(603, "New version is available now, please download it");
			addAndCheck(604, "New version is available now");
			addAndCheck(605, "Message to Client , see http://forexmaster.io ");
			addAndCheck(606, "Message to Client , see http://forexmaster.io ");
			addAndCheck(607, "Message to Client , see http://forexmaster.io ");
//			addAndCheck(608, "client didn't connect");
//			addAndCheck(609, "client didn't disconnect");
			addAndCheck(610, "client parameter is wrong");
//			addAndCheck(611, "Client idle");// Status Disconnect: client is idle.
//			addAndCheck(612, "Client send disconnect"); // Status Disconnect: client has sent Cmd Disconnect
//			addAndCheck(613, "Client Version Error");
			
			//DB errors start with 700
			addAndCheck(700, "SQL syntax or Data gathering error");
			addAndCheck(701, "Data Not Found");
			addAndCheck(702, "Data Already Exist");
			addAndCheck(703, "Wrong Action");
			
			//quote errors start with 800
			addAndCheck(800, "Quote id is not valid");
			addAndCheck(801, "Quote is not subscribed");
			addAndCheck(802, "There is no Quote data");
			addAndCheck(803, "No Such Data or Search Type/Key Error");
			addAndCheck(804, "No More Data");
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	public static String lookup(int code) {
		return map.get(code);
	}
}
