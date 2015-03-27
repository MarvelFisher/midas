package com.cyanspring.common.message;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.error.ErrorLookup;

public class MessageLookup {
	private static final Logger log = LoggerFactory
			.getLogger(MessageLookup.class);
	private static final Map<ErrorMessage, MessageBean> map = new HashMap<ErrorMessage, MessageBean>();
	private	static final String MSG_SEPARATOR="|&|";

	private static void addAndCheck(ErrorMessage m, MessageBean bean) throws Exception {
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
			addAndCheck(ErrorMessage.EXCEPTION_MESSAGE,getBean(900,"Unrecognized Error"));
			addAndCheck(ErrorMessage.NONE_MATCHED_MESSAGE,getBean(901,"None matched message"));
			addAndCheck(ErrorMessage.EMPTY_MESSAGE,getBean(902,"Empty message"));
			
			// system errors start with 100
			addAndCheck(ErrorMessage.RECEIVE_HANDLE_ERROR,getBean(100,"Receive handle error"));
			addAndCheck(ErrorMessage.ACTIVEMQ_CONNECT_FAILED,getBean(101, "ActiveMQ connect failed"));
			addAndCheck(ErrorMessage.ACTIVEMQ_CLOSE_FAILED,getBean(102, "ActiveMQ close failed"));
			addAndCheck(ErrorMessage.ACTIVEMQ_TIMEOUT,getBean(103, "ActiveMQ timeout"));
			addAndCheck(ErrorMessage.ACTIVE_IDLE,getBean(104, "ActiveMQ idle"));
			addAndCheck(ErrorMessage.PACKET_FORMAT_WRONG,getBean(105, "packet format is wrong"));
			addAndCheck(ErrorMessage.CONNECTION_NOT_AVAILABLE,getBean(106, "connection is not available"));
			addAndCheck(ErrorMessage.CONNECTION_BROKEN,getBean(107, "connection is broken"));
			addAndCheck(ErrorMessage.INVALID_CMD_PACKET,getBean(108, "Invalid Cmd Packet"));
			addAndCheck(ErrorMessage.UNSUPPORTED_CMD_PACKET,getBean(109, "Unsupported Cmd Packet"));			
			addAndCheck(ErrorMessage.SERVER_NOT_AVAILABLE,getBean(110, "Server Not Available"));
			addAndCheck(ErrorMessage.SEND_FRAME_FAILED,getBean(111, "send frame failed"));
			addAndCheck(ErrorMessage.NONE_SET_USER,getBean(112, "doesn't set user"));
			addAndCheck(ErrorMessage.NONE_SET_CALLBACK,getBean(113, "doesn't set callback"));
			addAndCheck(ErrorMessage.NONE_SET_CONNECTION_CONFIG,getBean(114, "doesn't set connection configuration"));
			addAndCheck(ErrorMessage.SYSTEM_NOT_READY,getBean(115, "System not yet Ready"));
			addAndCheck(ErrorMessage.SERVER_NOT_READY_FOR_LOGIN,getBean(116, "Server is not set up for login"));
			
			
			// business errors start with 200
			addAndCheck(ErrorMessage.PREMIUM_FOLLOW_INFO_INCOMPLETE,getBean(200, "Premium follow info incomplete"));
			addAndCheck(ErrorMessage.PREMIUM_FOLLOW_REQUEST_TIMEOUT,getBean(201, "Premium request time out"));
			addAndCheck(ErrorMessage.SYMBOLIST_ERROR,getBean(202, "SymbolList error"));
			addAndCheck(ErrorMessage.SEARCH_SYMBOL_ERROR,getBean(203, "Search Symbol error"));
			addAndCheck(ErrorMessage.PRICE_ALERT_ERROR,getBean(204, "Price Alert error"));
			addAndCheck(ErrorMessage.TRADE_ALERT_ERROR,getBean(205, "Trade Alert error"));
			addAndCheck(ErrorMessage.NO_ALERT_DATA,getBean(206, "No Alert Data"));
			addAndCheck(ErrorMessage.REF_SYMBOL_NOT_FOUND,getBean(207, "Ref Symbols not found"));
			addAndCheck(ErrorMessage.REF_SYMBOL_DUPLICATED,getBean(208, "Ref Symbols duplicated"));			
			addAndCheck(ErrorMessage.REF_SYMBOL_NOT_VALID,getBean(209, "Ref Symbols not valid"));
			addAndCheck(ErrorMessage.REF_SYMBOL_RUNTIME_ERROR,getBean(210, "Ref Symbols runtime error"));
			addAndCheck(ErrorMessage.USER_HAVE_NOT_SUBSCRIBE_SYMBOL,getBean(211, "User didn't subscribe"));
			addAndCheck(ErrorMessage.USER_NONE_PERMISSION,getBean(212, "User doesn't have enough permission"));
			addAndCheck(ErrorMessage.SUBSCRIBE_LIMITATION,getBean(213, "Subscribe limitation"));
			addAndCheck(ErrorMessage.SEQUENCE_NUMBER_ERROR,getBean(214, "Sequence number wrong"));
			addAndCheck(ErrorMessage.NOT_SUPPORT_FREQUENCE,getBean(215, "Frequence is not Support"));
			addAndCheck(ErrorMessage.SYMBOL_NOT_FOUND,getBean(216, "Can't find symbol"));
			addAndCheck(ErrorMessage.POSITION_NOT_FOUND,getBean(217, "Account doesn't have a position at this symbol"));
			addAndCheck(ErrorMessage.POSITION_PENDING,getBean(218, "pending close position action on symbol"));
			addAndCheck(ErrorMessage.STRATEGY_NOT_REGISTERD,getBean(219, "Strategy hasn't been registered"));
			addAndCheck(ErrorMessage.STRATEGY_ERROR,getBean(220, "Strategy Error"));
			addAndCheck(ErrorMessage.STRATEGY_NOT_PRESENT_IN_SINGLE_INSTRUMENT,getBean(221, "Strategy field not present in NewSingleInstrumentStrategyEvent"));

			

			// api errors start with 300
			addAndCheck(ErrorMessage.SEVER_NOT_CONNECTED,getBean(300, "Server isn't connected"));
			addAndCheck(ErrorMessage.USER_NEED_LOGIN_BEFORE_EVENTS,getBean(301, "User must login before send any events"));
			addAndCheck(ErrorMessage.EVENT_TYPE_NOT_SUPPORT,getBean(302, "Event type not support"));			
			addAndCheck(ErrorMessage.ACCOUNT_NOT_MATCH,getBean(303, "Account & user not match"));			
			addAndCheck(ErrorMessage.LOGIN_BLOCKED,getBean(304, "Can't login, blocked by existing connection"));
		
			// order errors start with 400
			addAndCheck(ErrorMessage.ACTION_CANCELLED,getBean(400, "action is cancelled"));
			addAndCheck(ErrorMessage.ORDER_PROCESSING,getBean(401, "order is processing"));
			addAndCheck(ErrorMessage.ORDER_NOT_CAHNGE,getBean(402, "order didn't change"));
			addAndCheck(ErrorMessage.ORDER_ID_NOT_FOUND,getBean(403, "order id is not found"));
			addAndCheck(ErrorMessage.ORDER_SIDE_NOT_SUPPORT,getBean(404, "order side is not supported"));
			addAndCheck(ErrorMessage.ORDER_TYPE_NOT_SUPPORT,getBean(405, "order type is not supported"));
			addAndCheck(ErrorMessage.ACCOUNT_NOT_EXIST,getBean(406, "account doesn't exist"));
			addAndCheck(ErrorMessage.USER_NOT_LOGIN,getBean(407, "user didn't login"));
			addAndCheck(ErrorMessage.ENTER_ORDER_ERROR,getBean(408, "enter order error"));			
			addAndCheck(ErrorMessage.AMEND_ORDER_ERROR,getBean(409, "amend order error"));
			addAndCheck(ErrorMessage.CANCEL_ORDER_ERROR,getBean(410, "cancel order error"));
			addAndCheck(ErrorMessage.AMEND_PARAM_SHORT,getBean(411, "amend parameters not enough"));
			addAndCheck(ErrorMessage.CLOSE_ORDER_ERROR,getBean(412, "close order error"));
			addAndCheck(ErrorMessage.TX_ID_ILLEGAL,getBean(413, "txid illegal error"));
			addAndCheck(ErrorMessage.ORDER_REASON_NOT_SUPPORT,getBean(414, "order reason is not supported"));
			addAndCheck(ErrorMessage.ALERT_TYPE_NOT_SUPPORT,getBean(415, "alert type is not supported"));
			addAndCheck(ErrorMessage.NONE_ASSIGN_PRICE,getBean(416, "assign price please"));
			addAndCheck(ErrorMessage.AMEND_ORDER_NOT_FOUND,getBean(417, "Can't find order to amend"));
			addAndCheck(ErrorMessage.ORDER_ALREADY_COMPLETED,getBean(418, "Order already completed"));
			addAndCheck(ErrorMessage.ORDER_ALREADY_TERMINATED,getBean(419, "Order already terminated"));
			addAndCheck(ErrorMessage.ORDER_VALIDATION_ERROR,getBean(420, "Order Field Validation Error"));
			addAndCheck(ErrorMessage.FIELD_DEFINITION_NOT_FOUND,getBean(421, "Can't find field definition for strategy"));
			addAndCheck(ErrorMessage.CANCEL_ORDER_NOT_FOUND,getBean(422, "Can't find order to cancel"));
			addAndCheck(ErrorMessage.ORDER_ID_EXIST,getBean(423, "this order id already exist"));
			addAndCheck(ErrorMessage.ORDER_IS_PENDING,getBean(424, "Order is pending on instruction/amendment/cancellation"));
			addAndCheck(ErrorMessage.OVER_FILLED,getBean(425, "This would cause overfilled"));
			addAndCheck(ErrorMessage.NO_ORDER_IN_ACTIVE_CHILD_ORDER,getBean(426, "cant find order in active child orders"));
			addAndCheck(ErrorMessage.CUM_QTY_GREATER_THAN_INTENTED_QTY,getBean(427, "CumQty is greater than intended quantity"));
			addAndCheck(ErrorMessage.PRICE_NOT_PERMITTED,getBean(428, "Price is not permitted by parentOrder"));
			addAndCheck(ErrorMessage.PARENT_ORDER_IS_PENDING,getBean(429, "Parent order is pending on action"));
			addAndCheck(ErrorMessage.MARKET_CLOSED,getBean(430, "Market closed,order couldn't be placed"));
			addAndCheck(ErrorMessage.MARKET_VALIDATION_ERROR,getBean(431, "Market validation error"));
			addAndCheck(ErrorMessage.ORDER_QTY_OVER_MAX_HOLD,getBean(432, "The order quantity is over maximum hold."));
			addAndCheck(ErrorMessage.ORDER_QTY_OVER_MAX_LOT,getBean(433, "The order quantity is over maximun lot"));
			addAndCheck(ErrorMessage.VALIDATION_ERROR,getBean(434, "validation error"));

			
			

			
			
			
			// user errors start with 500
			addAndCheck(ErrorMessage.CREATE_USER_FAILED,getBean(500, "Create user failed"));			
			addAndCheck(ErrorMessage.USER_LOGIN_FAILED,getBean(501, "User login failed"));
			addAndCheck(ErrorMessage.CHANGE_USER_PWD_FAILED,getBean(502, "Change User Password failed"));
			addAndCheck(ErrorMessage.USER_LOGIN_APPSERVER_FAILED,getBean(503, "User could not login this AppServer, try another"));
			addAndCheck(ErrorMessage.WRONG_USER_TYPE,getBean(504, "Wrong User Type"));
			addAndCheck(ErrorMessage.EMPTY_PWD,getBean(505, "Passwrod null or empty"));
			addAndCheck(ErrorMessage.PREMIUM_FOLLOW_ERROR,getBean(506, "Premium follow error"));
			addAndCheck(ErrorMessage.ACCOUNT_RESET_ERROR,getBean(507, "Account Reset error"));
			addAndCheck(ErrorMessage.INVALID_USER_INFO,getBean(508, "Invalid user information"));
			addAndCheck(ErrorMessage.NO_TRADING_ACCOUNT,getBean(509, "No trading account available"));
			addAndCheck(ErrorMessage.INVALID_USER_ACCOUNT_PWD,getBean(510, "userid or password invalid"));
			addAndCheck(ErrorMessage.USER_ALREADY_EXIST,getBean(511, "User already exists"));
			addAndCheck(ErrorMessage.CREATE_DEFAULT_ACCOUNT_ERROR,getBean(512, "Cannot create default account for user"));
			addAndCheck(ErrorMessage.USER_EMAIL_EXIST,getBean(513, "This email already exists"));

			
			
			// client errors start with 600
			addAndCheck(ErrorMessage.NEED_RESTART_APP,getBean(600, "Please restart your App"));			
			addAndCheck(ErrorMessage.SERVER_IN_MAINTAINING,getBean(601, "Server in Maintaining , Please wait"));
			addAndCheck(ErrorMessage.ANOTHER_DEVICE_ALREADY_LOGIN,getBean(602, "Another device login your ID"));
			addAndCheck(ErrorMessage.NEW_VERSION_NEED_DOWNLOAD,getBean(603, "New version is available now, please download it"));
			addAndCheck(ErrorMessage.NEW_VERSION_AVAILABLE,getBean(604, "New version is available now"));
			addAndCheck(ErrorMessage.MESSAGE_ARRIVED,getBean(605, "Message to Client , see http://forexmaster.io"));
			addAndCheck(ErrorMessage.CLIENT_NOT_CONNECT,getBean(606, "client didn't connect"));
			addAndCheck(ErrorMessage.CLIENT_NOT_DISCONNECT,getBean(607, "client didn't disconnect"));
			addAndCheck(ErrorMessage.CLIENT_PARAM_ERROR,getBean(608, "client parameter is wrong"));
			addAndCheck(ErrorMessage.CLIENT_IDLE,getBean(609, "Client idle"));
			addAndCheck(ErrorMessage.CLIENT_SEND_DISCONNECT,getBean(610, "Client send disconnect"));
			addAndCheck(ErrorMessage.CLIENT_VERSION_ERROR,getBean(611, "Client Version Error"));


			//DB errors start with 700
			addAndCheck(ErrorMessage.SQL_SYNTAX_ERROR,getBean(700, "SQL syntax or Data gathering error"));
			addAndCheck(ErrorMessage.DATA_NOT_FOUND,getBean(701, "Data Not Found"));
			addAndCheck(ErrorMessage.DATA_ALREADY_EXIST,getBean(702, "Data Already Exist"));
			addAndCheck(ErrorMessage.WRONG_ACTION,getBean(703, "Wrong Action"));


			//quote errors start with 800
			addAndCheck(ErrorMessage.INVALID_QUOTE_ID,getBean(800, "Quote id is not valid"));
			addAndCheck(ErrorMessage.QUOTE_NOT_SUBSCRIBE,getBean(801, "Quote is not subscribed"));
			addAndCheck(ErrorMessage.NO_QUOTE_DATA,getBean(802, "There is no Quote data"));
			addAndCheck(ErrorMessage.NO_DATA_ERROR,getBean(803, "No Such Data or Search Type/Key Error"));
			addAndCheck(ErrorMessage.NO_MORE_DATA,getBean(804, "No More Data"));


		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	public static MessageBean getMsgBeanFromEventMessage(String eventMessage){
			MessageBean mb;
			try{
				if(null!= eventMessage && eventMessage.contains(MSG_SEPARATOR) && eventMessage.split("\\|"+"&"+"\\|").length>2){
					String tempMsgs[] = eventMessage.split("\\|"+"&"+"\\|");
					mb = new MessageBean(Integer.parseInt(tempMsgs[0]),tempMsgs[1],tempMsgs[2]);
				}else{
					if(!StringUtils.hasText(eventMessage)){
						mb = lookup(ErrorMessage.EMPTY_MESSAGE);
						mb.setLocalMsg(eventMessage);
						log.info("Empty event message");
					}else{
						mb = lookup(ErrorMessage.NONE_MATCHED_MESSAGE);
						mb.setLocalMsg(eventMessage);
						log.warn("NONE MATCHED MESSAGE:"+eventMessage);
					}
				}
			}catch(Exception e){
				mb = lookup(ErrorMessage.EXCEPTION_MESSAGE);
				mb.setLocalMsg(e.getMessage());
				log.error(e.getMessage(),e);
			}
		return mb;
		
	}
	public static String buildEventMessage(ErrorMessage m,String localMessage){
		MessageBean mb = lookup(m);
		if(null==mb){			
			mb = lookup(ErrorMessage.NONE_MATCHED_MESSAGE);
		}
		if(null==localMessage){
			localMessage = "";
		}
		String eventMsg = mb.getCode()+MSG_SEPARATOR+mb.getMsg()+MSG_SEPARATOR+localMessage;
		log.info(eventMsg);
		return eventMsg;
		
	}
	public static MessageBean lookup(ErrorMessage m) {
		return map.get(m);
	}
}
