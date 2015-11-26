package com.cyanspring.cstw.session;

import com.cyanspring.common.account.AccountSetting;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.05
 *
 */
public class CSTWSession {

	private String inbox;
	private String channel;
	private String nodeInfoChannel;

	private String configPath;

	private XStream xstream = new XStream(new DomDriver());

	private AccountSetting accountSetting;

	private static CSTWSession instance;

	public static CSTWSession getInstance() {
		if (instance == null) {
			instance = new CSTWSession();
		}
		return instance;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public XStream getXstream() {
		return xstream;
	}

	public AccountSetting getAccountSetting() {
		return accountSetting;
	}

	public void setAccountSetting(AccountSetting accountSetting) {
		this.accountSetting = accountSetting;
	}

	public String getInbox() {
		return inbox;
	}

	public void setInbox(String inbox) {
		this.inbox = inbox;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getNodeInfoChannel() {
		return nodeInfoChannel;
	}

	public void setNodeInfoChannel(String nodeInfoChannel) {
		this.nodeInfoChannel = nodeInfoChannel;
	}

}
