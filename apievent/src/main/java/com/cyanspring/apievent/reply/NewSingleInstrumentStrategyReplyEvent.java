package com.cyanspring.apievent.reply;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class NewSingleInstrumentStrategyReplyEvent extends
		StrategyChangeReplyEvent {

	public NewSingleInstrumentStrategyReplyEvent(String key, String receiver,
												 String txId, boolean success, String message) {
		super(key, receiver, txId, success, message);
	}

}
