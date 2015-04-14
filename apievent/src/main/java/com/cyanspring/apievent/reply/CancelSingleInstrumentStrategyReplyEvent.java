package com.cyanspring.apievent.reply;

public class CancelSingleInstrumentStrategyReplyEvent extends
		StrategyChangeReplyEvent {

	public CancelSingleInstrumentStrategyReplyEvent(String key,
													String receiver, String txId, boolean success, String message) {
		super(key, receiver, txId, success, message);
	}

}
