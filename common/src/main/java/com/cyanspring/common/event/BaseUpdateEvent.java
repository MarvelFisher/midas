package com.cyanspring.common.event;

/**
 * @author GuoWei
 * @version 11/10/2015
 */
public class BaseUpdateEvent extends RemoteAsyncEvent {

	private static final long serialVersionUID = -8388425119053769914L;

	public BaseUpdateEvent(String key, String receiver) {
		super(key, receiver);
	}
}