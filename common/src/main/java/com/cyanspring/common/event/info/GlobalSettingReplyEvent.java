package com.cyanspring.common.event.info;

import com.cyanspring.common.business.GlobalSetting;
import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class GlobalSettingReplyEvent extends RemoteAsyncEvent{
	
	private GlobalSetting globalSetting;
	public GlobalSettingReplyEvent(String key, String receiver,GlobalSetting globalSetting) {
		super(key, receiver);
		this.globalSetting = globalSetting;
	}
	/**
	 * @return the globalSetting
	 */
	public GlobalSetting getGlobalSetting() {
		return globalSetting;
	}
}
