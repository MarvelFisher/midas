package com.cyanspring.apievent.reply;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class NewMultiInstrumentStrategyReplyEvent extends
		StrategyChangeReplyEvent {

	public NewMultiInstrumentStrategyReplyEvent(String key, String receiver,
												String txId, boolean success, String message) {
		super(key, receiver, txId, success, message);
	}

}
