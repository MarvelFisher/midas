package com.cyanspring.common.message;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


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
			addAndCheck(ErrorMessage.SNIPER_STRATEGY_PRICE_EMPTY,getBean(222, "price can not be empty for Sniper stratgy"));
			addAndCheck(ErrorMessage.STRATEGY_NOT_DEFINED_IN_REGISTRY,getBean(223, "field not defined in registry"));
			addAndCheck(ErrorMessage.FIX_EXCEPTION,getBean(224, "fix adaptor error"));
			addAndCheck(ErrorMessage.DOWN_STREAM_EXCEPTION,getBean(225, "down stream error"));
			addAndCheck(ErrorMessage.DOWN_STREAM_NULL_LISTENER,getBean(226, "down stream null listener"));
			addAndCheck(ErrorMessage.DOWN_STREAM_NULL_FIELDS,getBean(227, "down stream null fields"));
			addAndCheck(ErrorMessage.FIX_FIELD_NOT_DEFINED_STRATEGY,getBean(228, "'Strategy' field is not defined in fixToOrderMap"));
			addAndCheck(ErrorMessage.FIX_STRATEGY_FIELD_IS_NOT_PRESENTED,getBean(229, "Strategy field is not presented in this message"));
			addAndCheck(ErrorMessage.FIX_SIDE_NOT_HANDLED,getBean(230, "Fix side not handled"));
			addAndCheck(ErrorMessage.FIX_SIDE_CANT_CONVERT,getBean(231, "Can't convert value to FIX side"));
			addAndCheck(ErrorMessage.FIX_CANT_MAP_SIDE,getBean(232, "cant map side"));
			addAndCheck(ErrorMessage.FX_CONVERTER_CANT_FIND_SYMBOL,getBean(233, "FxConverter can not find symbol"));
			addAndCheck(ErrorMessage.FX_CONVERTER_RATE_IS_ZERO,getBean(234, "FxConverter rate is 0"));
			addAndCheck(ErrorMessage.DOWN_STREAM_TOO_MANY_LISTENER,getBean(235, "Only support one listener per downstream connection"));
			addAndCheck(ErrorMessage.POSITION_CONTAINS_DIFF_ACCOUNT,getBean(236, "Position list contains different account"));
			addAndCheck(ErrorMessage.POSITION_CONTAINS_DIFF_SYMBOL,getBean(237, "Position list contains different symbol"));
			addAndCheck(ErrorMessage.POSITION_CONTAINS_DIFF_SIDE,getBean(238, "Position list contains different side"));
			addAndCheck(ErrorMessage.POSITION_CONTAINS_ZERO,getBean(239, "Position list contains 0"));
			addAndCheck(ErrorMessage.UPSTREAM_EXCEPTION,getBean(240, "UpStream Exception"));
			addAndCheck(ErrorMessage.UPSTREAM_CONNECTION_EXIST,getBean(241, "This connection id already exists"));
			addAndCheck(ErrorMessage.DATA_CONVERT_EXCEPTION,getBean(242, "Data convert exception"));
			addAndCheck(ErrorMessage.OVER_SET_MAX_PRICEALERTS,getBean(243, "You can only set 20 Price Alerts"));
			addAndCheck(ErrorMessage.DATA_CONVERT_CLASS_NULL,getBean(244, "Data convert fail"));
			addAndCheck(ErrorMessage.DATA_CONVERT_UNKNOWN_DATE_FORMAT,getBean(245, "Unknown date format"));
			addAndCheck(ErrorMessage.DATA_CONVERT_CONVERT_FIELD_FAIL,getBean(246, "Cant convert field"));
			addAndCheck(ErrorMessage.DOWN_STREAM_CONN_ID_EXIST,getBean(247, "This connection id already exists"));
			addAndCheck(ErrorMessage.DOWN_STREAM_CONN_DOWN,getBean(248, "Down Stream connection is down"));
			addAndCheck(ErrorMessage.DOWN_STREAM_SENDER_NOT_AVAILABLE,getBean(249, "DownStreamSender not available"));
			addAndCheck(ErrorMessage.TICK_DATA_LESS_THAN_ONE_TOKEN,getBean(250, "The tick data is less than one token"));
			addAndCheck(ErrorMessage.TICK_DATA_TAG_VALUE_MALFORMATTED,getBean(251, "Tag Value malformatted"));
			addAndCheck(ErrorMessage.TICK_DATA_FIRST_FIELD_MUST_SYMBOL,getBean(252, "The first field must be symbol"));
			addAndCheck(ErrorMessage.TICK_DATA_EXCEPTION,getBean(253, "tick data error"));
			addAndCheck(ErrorMessage.TICK_DATA_ASK_VOL_OUT_OF_SEQ,getBean(254, "depth ask vol out of sequence"));
			addAndCheck(ErrorMessage.TICK_DATA_BID_VOL_OUT_OF_SEQ,getBean(255, "depth bid vol out of sequence"));

			
			
			
			
			
			

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
			addAndCheck(ErrorMessage.MARKET_CLOSED,getBean(430, "When market close, pop out a notification when user make an order in market closed pair."));
			addAndCheck(ErrorMessage.MARKET_VALIDATION_ERROR,getBean(431, "Market validation error"));
			addAndCheck(ErrorMessage.ORDER_QTY_OVER_MAX_HOLD,getBean(432, "The order quantity is over maximum hold."));
			addAndCheck(ErrorMessage.ORDER_QTY_OVER_MAX_LOT,getBean(433, "The order quantity is over maximun lot"));
			addAndCheck(ErrorMessage.VALIDATION_ERROR,getBean(434, "validation error"));
			addAndCheck(ErrorMessage.ORDER_FIELD_EMPTY,getBean(435, "order field empty"));
			addAndCheck(ErrorMessage.ORDER_FIELD_MUST_GREATER_THAN_ZERO,getBean(436, "order field must be greater than 0"));
			addAndCheck(ErrorMessage.ORDER_FIELD_MUST_BE_INTEGER,getBean(437, "order field must be must be an integer"));
			addAndCheck(ErrorMessage.LIVE_TRADING_STOP_TRADING, getBean(438, "Live trading on stop trading time"));

			addAndCheck(ErrorMessage.ORDER_SYMBOL_LOT_SIZE_ERROR,getBean(439, "Can not determine symbol for quantity lot size validation"));
			addAndCheck(ErrorMessage.ORDER_SYMBOL_NOT_FOUND,getBean(440, "Can't find symbol in refdata"));
			addAndCheck(ErrorMessage.INVALID_QUANTITY,getBean(441, "Invalid Quantity! Quantity should be the multiple of 1000."));
			addAndCheck(ErrorMessage.ORDER_ACCOUNT_OVER_CREDIT_LIMIT,getBean(442, "This order would have caused account over credit limit"));
			addAndCheck(ErrorMessage.AMEND_ORDER_OVER_CREDIT_LIMIT,getBean(443, "Amendment would have caused the account over credit limit"));
			addAndCheck(ErrorMessage.ENDTIME_IN_THE_PASS,getBean(444, "end time is in the pass"));
			addAndCheck(ErrorMessage.STARTTIME_SAME_AS_ENDTIME,getBean(445, "end time is is the same or before start time"));
			addAndCheck(ErrorMessage.ICEBERG_STRATEGY_QTY_EMPTY,getBean(446, "Display quantity can not be empty for Iceberg stratgy"));
			addAndCheck(ErrorMessage.DAILY_ORDERS_EXCEED_LIMIT,getBean(447, "Daily allowed number of orders exceeded limit"));
			addAndCheck(ErrorMessage.DAILY_ORDERS_EXCEED_LIMIT_CAN_AMEND_QTY,getBean(448, "Daily allowed number of orders exceeded limit, can only amend down qty"));
			addAndCheck(ErrorMessage.STRATEGY_IS_NOT_DEFINED,getBean(449, "Strategy is not defined in strategyFields map"));
			addAndCheck(ErrorMessage.STRATEGY_PARAMS_IS_MISSING,getBean(450, "Required parameter is missing for this strategy"));
			addAndCheck(ErrorMessage.ORDER_FIELD_VALUE_IS_EMPTY,getBean(451, "order field value is null"));
			addAndCheck(ErrorMessage.ORDER_FIELD_OUT_OF_RANGE,getBean(452, "order field out of range of (0, 100]"));
			addAndCheck(ErrorMessage.STOP_LOSS_PRICE_EMPTY,getBean(453, "Stop Loss price can't be empty or 0"));
			addAndCheck(ErrorMessage.STOP_LOSS_PRICE_CANT_OVER_THAN_LIMIT_PRICE,getBean(454, "Stop loss price can not be more aggressive than limit price"));
			addAndCheck(ErrorMessage.ORDER_NOT_IN_READY_STATUS,getBean(455, "order isn't in ready status"));
			addAndCheck(ErrorMessage.ORDER_CANT_CONVERT_TO_FIX_TYPE,getBean(456, "Can't convert to FIX OrdType"));
			addAndCheck(ErrorMessage.ORDER_OVER_CEIL_PRICE,getBean(457, "Order price over than ceil price"));
			addAndCheck(ErrorMessage.ORDER_LOWER_FLOOR_PRICE,getBean(458, "Order price lower than floor price"));
			addAndCheck(ErrorMessage.ORDER_CANT_FIND_QUOTEEXT_FILE,getBean(459, "Missing QuoteExt file"));
			addAndCheck(ErrorMessage.QUANTITY_EXCEED_AVAILABLE_QUANTITY, getBean(460, "Sell quantity exceeded available position quantity"));
			addAndCheck(ErrorMessage.MARKET_WILL_TAKE_ORDER_AFTER_OPEN, getBean(461, "Your order can’t be placed. We start to take orders at 9:10 am."));
			addAndCheck(ErrorMessage.MARKET_WILL_TAKE_ORDER_BEFORE_OPEN_ONE_HOUR, getBean(462, "Your order can’t be placed. We start taking orders at 1 hour before market open."));

			
			
			
			
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
			addAndCheck(ErrorMessage.ACCOUNT_AND_USER_NOT_MATCH,getBean(514, "Account and user not match"));
			addAndCheck(ErrorMessage.USER_IS_TERMINATED, getBean(515, "User is terminated"));
			addAndCheck(ErrorMessage.TERMINATE_USER_FAILED, getBean(516, "Change user termination status failed"));
			addAndCheck(ErrorMessage.ACCOUNT_FROZEN,getBean(517, "Exceed Daily Maximun Loss!  Your account will be frozen for the rest of the day."));
			addAndCheck(ErrorMessage.ACCOUNT_TERMINATED,getBean(518, "Exceed Account Stop Loss!  Your account is terminated."));
			addAndCheck(ErrorMessage.THIRD_PARTY_ID_NOT_MATCH_USER_ID, getBean(519, "Third party id is not match with the user id"));
			addAndCheck(ErrorMessage.THIRD_PARTY_ID_REGISTER_FAILED, getBean(520, "Register third party id failed"));
			addAndCheck(ErrorMessage.DETACH_THIRD_PARTY_ID_FAILED, getBean(521, "Detach third party id failed"));
			addAndCheck(ErrorMessage.USER_PHONE_EXIST, getBean(522, "This phone already exists"));
			addAndCheck(ErrorMessage.THIRD_PARTY_ID_USED_IN_NEW_APP, getBean(523, "This third party id is already used in the new version app"));
			addAndCheck(ErrorMessage.LIVE_TRADING_SETTING_NOT_OVER_FROZEN_DAYS, getBean(524, "cant change live trading setting , because not over frozen setted days"));
			addAndCheck(ErrorMessage.LIVE_TRADING_NO_RULE_IN_MAP, getBean(525, "can't find this rule in live trading"));
			addAndCheck(ErrorMessage.ATTACH_THIRD_PARTY_ID_FAILED, getBean(526, "Attach third party id failed"));

			
			
			
			
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
			addAndCheck(ErrorMessage.CANT_CONNECT_TO_CENTRAL_DATABASE,getBean(704, "can't connect to central database"));

			

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
				if(null!= eventMessage && eventMessage.contains(MSG_SEPARATOR) && eventMessage.split("\\|"+"&"+"\\|").length>1){
					int length = eventMessage.split("\\|"+"&"+"\\|").length;
					String tempMsgs[] = eventMessage.split("\\|"+"&"+"\\|");
					if(length==2){
						
						mb = new MessageBean(Integer.parseInt(tempMsgs[0]),tempMsgs[1],"");

					}else{
						
						mb = new MessageBean(Integer.parseInt(tempMsgs[0]),tempMsgs[1],tempMsgs[2]);

					}
					
					
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
	public static String buildEventMessageWithCode(ErrorMessage m,String localMessage){
		MessageBean mb = lookup(m);
		if(null==mb){			
			mb = lookup(ErrorMessage.NONE_MATCHED_MESSAGE);
		}
		if(null==localMessage){
			localMessage = "";
		}
		String eventMsg = mb.getCode()+MSG_SEPARATOR+localMessage+MSG_SEPARATOR+localMessage;
		log.info(eventMsg);
		return eventMsg;
		
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
