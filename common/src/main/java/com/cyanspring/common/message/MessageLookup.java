package com.cyanspring.common.message;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.error.ErrorLookup;

public class MessageLookup {
	private static final Logger log = LoggerFactory
			.getLogger(ErrorLookup.class);
	private static final Map<Message, MessageBean> map = new HashMap<Message, MessageBean>();
	private	static final String MSG_SEPARATOR="|";

	private static void addAndCheck(Message m, MessageBean bean) throws Exception {
		if(map.put(m, bean) != null)
			throw new Exception("Critical error: error code duplicate: " + bean.getCode());
	}
	private static MessageBean getBean(int code,String msg){
		MessageBean bean = new MessageBean(code,msg,"");
		return bean;
	}
	static {
		try {	
			// exception messagg start with 900
			addAndCheck(Message.EXCEPTION_MESSAGE,getBean(900,"Unrecognized Error"));
			addAndCheck(Message.NONE_MATCHED_MESSAGE,getBean(901,"None matched message"));

			
			// system errors start with 100
			addAndCheck(Message.RECEIVE_HANDLE_ERROR,getBean(100,"Receive handle error"));
			addAndCheck(Message.ACTIVEMQ_CONNECT_FAILED,getBean(101, "ActiveMQ connect failed"));
			addAndCheck(Message.ACTIVEMQ_CLOSE_FAILED,getBean(102, "ActiveMQ close failed"));
			addAndCheck(Message.ACTIVEMQ_TIMEOUT,getBean(103, "ActiveMQ timeout"));
			addAndCheck(Message.ACTIVE_IDLE,getBean(104, "ActiveMQ idle"));
			addAndCheck(Message.PACKET_FORMAT_WRONG,getBean(105, "packet format is wrong"));
			addAndCheck(Message.CONNECTION_NOT_AVAILABLE,getBean(106, "connection is not available"));
			addAndCheck(Message.CONNECTION_BROKEN,getBean(107, "connection is broken"));
			addAndCheck(Message.INVALID_CMD_PACKET,getBean(108, "Invalid Cmd Packet"));
			addAndCheck(Message.UNSUPPORTED_CMD_PACKET,getBean(109, "Unsupported Cmd Packet"));			
			addAndCheck(Message.SERVER_NOT_AVAILABLE,getBean(110, "Server Not Available"));
			addAndCheck(Message.SEND_FRAME_FAILED,getBean(111, "send frame failed"));
			addAndCheck(Message.NONE_SET_USER,getBean(112, "doesn't set user"));
			addAndCheck(Message.NONE_SET_CALLBACK,getBean(113, "doesn't set callback"));
			addAndCheck(Message.NONE_SET_CONNECTION_CONFIG,getBean(114, "doesn't set connection configuration"));

			
			
			
			
			
			
			// business errors start with 200
			addAndCheck(Message.PREMIUM_FOLLOW_INFO_INCOMPLETE,getBean(200, "Premium follow info incomplete"));
			addAndCheck(Message.PREMIUM_FOLLOW_REQUEST_TIMEOUT,getBean(201, "Premium request time out"));
			addAndCheck(Message.SYMBOLIST_ERROR,getBean(202, "SymbolList error"));
			addAndCheck(Message.SEARCH_SYMBOL_ERROR,getBean(203, "Search Symbol error"));
			addAndCheck(Message.PRICE_ALERT_ERROR,getBean(204, "Price Alert error"));
			addAndCheck(Message.TRADE_ALERT_ERROR,getBean(205, "Trade Alert error"));
			addAndCheck(Message.NO_ALERT_DATA,getBean(206, "No Alert Data"));
			addAndCheck(Message.REF_SYMBOL_NOT_FOUND,getBean(207, "Ref Symbols not found"));
			addAndCheck(Message.REF_SYMBOL_DUPLICATED,getBean(208, "Ref Symbols duplicated"));			
			addAndCheck(Message.REF_SYMBOL_NOT_VALID,getBean(209, "Ref Symbols not valid"));
			addAndCheck(Message.REF_SYMBOL_RUNTIME_ERROR,getBean(210, "Ref Symbols runtime error"));
			addAndCheck(Message.USER_HAVE_NOT_SUBSCRIBE_SYMBOL,getBean(211, "User didn't subscribe"));
			addAndCheck(Message.USER_NONE_PERMISSION,getBean(212, "User doesn't have enough permission"));
			addAndCheck(Message.SUBSCRIBE_LIMITATION,getBean(213, "Subscribe limitation"));
			addAndCheck(Message.SEQUENCE_NUMBER_ERROR,getBean(214, "Sequence number wrong"));
			addAndCheck(Message.NOT_SUPPORT_FREQUENCE,getBean(215, "Frequence is not Support"));

			// api errors start with 300
			addAndCheck(Message.SEVER_NOT_CONNECTED,getBean(300, "Server isn't connected"));
			addAndCheck(Message.USER_NEED_LOGIN_BEFORE_EVENTS,getBean(301, "User must login before send any events"));
			addAndCheck(Message.EVENT_TYPE_NOT_SUPPORT,getBean(302, "Event type not support"));			
			addAndCheck(Message.ACCOUNT_NOT_MATCH,getBean(303, "Account & user not match"));			
			addAndCheck(Message.LOGIN_BLOCKED,getBean(304, "Can't login, blocked by existing connection"));

			
			
			
			
			
			// order errors start with 400
			addAndCheck(Message.ACTION_CANCELLED,getBean(400, "action is cancelled"));
			addAndCheck(Message.ORDER_PROCESSING,getBean(401, "order is processing"));
			addAndCheck(Message.ORDER_NOT_CAHNGE,getBean(402, "order didn't change"));
			addAndCheck(Message.ORDER_ID_NOT_FOUND,getBean(403, "order id is not found"));
			addAndCheck(Message.ORDER_SIDE_NOT_SUPPORT,getBean(404, "order side is not supported"));
			addAndCheck(Message.ORDER_TYPE_NOT_SUPPORT,getBean(405, "order type is not supported"));
			addAndCheck(Message.ACCOUNT_NOT_EXIST,getBean(406, "account doesn't exist"));
			addAndCheck(Message.USER_NOT_LOGIN,getBean(407, "user didn't login"));
			addAndCheck(Message.ENTER_ORDER_ERROR,getBean(408, "enter order error"));			
			addAndCheck(Message.AMEND_ORDER_ERROR,getBean(409, "amend order error"));
			addAndCheck(Message.CANCEL_ORDER_ERROR,getBean(410, "cancel order error"));
			addAndCheck(Message.AMEND_PARAM_SHORT,getBean(411, "amend parameters not enough"));
			addAndCheck(Message.CLOSE_ORDER_ERROR,getBean(412, "close order error"));
			addAndCheck(Message.TX_ID_ILLEGAL,getBean(413, "txid illegal error"));
			addAndCheck(Message.ORDER_REASON_NOT_SUPPORT,getBean(414, "order reason is not supported"));
			addAndCheck(Message.ALERT_TYPE_NOT_SUPPORT,getBean(415, "alert type is not supported"));
			addAndCheck(Message.NONE_ASSIGN_PRICE,getBean(416, "assign price please"));

			// user errors start with 500
			addAndCheck(Message.CREATE_USER_FAILED,getBean(500, "Create user failed"));			
			addAndCheck(Message.USER_LOGIN_FAILED,getBean(501, "User login failed"));
			addAndCheck(Message.CHANGE_USER_PWD_FAILED,getBean(502, "Change User Password failed"));
			addAndCheck(Message.USER_LOGIN_APPSERVER_FAILED,getBean(503, "User could not login this AppServer, try another"));
			addAndCheck(Message.WRONG_USER_TYPE,getBean(504, "Wrong User Type"));
			addAndCheck(Message.EMPTY_PWD,getBean(505, "Passwrod null or empty"));
			addAndCheck(Message.PREMIUM_FOLLOW_ERROR,getBean(506, "Premium follow error"));
			addAndCheck(Message.PREMIUM_FOLLOW_ERROR,getBean(507, "Account Reset error"));
			addAndCheck(Message.INVALID_USER_INFO,getBean(508, "Invalid user information"));

			
			// client errors start with 600
			addAndCheck(Message.NEED_RESTART_APP,getBean(600, "Please restart your App"));			
			addAndCheck(Message.SERVER_IN_MAINTAINING,getBean(601, "Server in Maintaining , Please wait"));
			addAndCheck(Message.ANOTHER_DEVICE_ALREADY_LOGIN,getBean(602, "Another device login your ID"));
			addAndCheck(Message.NEW_VERSION_NEED_DOWNLOAD,getBean(603, "New version is available now, please download it"));
			addAndCheck(Message.NEW_VERSION_AVAILABLE,getBean(604, "New version is available now"));
			addAndCheck(Message.MESSAGE_ARRIVED,getBean(605, "Message to Client , see http://forexmaster.io "));
			addAndCheck(Message.CLIENT_NOT_CONNECT,getBean(606, "client didn't connect"));
			addAndCheck(Message.CLIENT_NOT_DISCONNECT,getBean(607, "client didn't disconnect"));
			addAndCheck(Message.CLIENT_PARAM_ERROR,getBean(608, "client parameter is wrong"));
			addAndCheck(Message.CLIENT_IDLE,getBean(609, "Client idle"));
			addAndCheck(Message.CLIENT_SEND_DISCONNECT,getBean(610, "Client send disconnect"));
			addAndCheck(Message.CLIENT_VERSION_ERROR,getBean(611, "Client Version Error"));


			//DB errors start with 700
			addAndCheck(Message.SQL_SYNTAX_ERROR,getBean(700, "SQL syntax or Data gathering error"));
			addAndCheck(Message.DATA_NOT_FOUND,getBean(701, "Data Not Found"));
			addAndCheck(Message.DATA_ALREADY_EXIST,getBean(702, "Data Already Exist"));
			addAndCheck(Message.WRONG_ACTION,getBean(703, "Wrong Action"));


			//quote errors start with 800
			addAndCheck(Message.INVALID_QUOTE_ID,getBean(800, "Quote id is not valid"));
			addAndCheck(Message.QUOTE_NOT_SUBSCRIBE,getBean(801, "Quote is not subscribed"));
			addAndCheck(Message.NO_QUOTE_DATA,getBean(802, "There is no Quote data"));
			addAndCheck(Message.NO_DATA_ERROR,getBean(803, "No Such Data or Search Type/Key Error"));
			addAndCheck(Message.NO_MORE_DATA,getBean(804, "No More Data"));


		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	public static MessageBean getMsgBeanFromEventMessage(String eventMessage){
			MessageBean mb;
			try{
				if(null!= eventMessage && eventMessage.contains(MSG_SEPARATOR) && eventMessage.split("\\"+MSG_SEPARATOR).length>2){
					String tempMsgs[] = eventMessage.split("\\"+MSG_SEPARATOR);
					mb = new MessageBean(Integer.parseInt(tempMsgs[0]),tempMsgs[1],tempMsgs[2]);
				}else{
					mb = lookup(Message.NONE_MATCHED_MESSAGE);
				}
			}catch(Exception e){
				mb = lookup(Message.EXCEPTION_MESSAGE);
				log.error(e.getMessage(),e);
			}
		return mb;
		
	}
	public static String buildEventMessage(Message m,String localMessage){
		MessageBean mb = lookup(m);
		if(null==mb){			
			mb = lookup(Message.NONE_MATCHED_MESSAGE);
		}
		if(null==localMessage){
			localMessage = "";
		}
		return mb.getCode()+MSG_SEPARATOR+mb.getMsg()+MSG_SEPARATOR+localMessage;
		
	}
	public static MessageBean lookup(Message m) {
		return map.get(m);
	}
}
