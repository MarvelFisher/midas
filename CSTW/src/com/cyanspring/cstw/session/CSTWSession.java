package com.cyanspring.cstw.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.account.UserRole;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.05
 *
 */
public final class CSTWSession {

	private String inbox;
	private String channel;
	private String nodeInfoChannel;

	private HashMap<String, Boolean> servers;

	private String userId;
	private String accountId;
	private Account loginAccount;

	private UserGroup userGroup;
	private List<String> accountGroupList;
	private List<Account> accountList;

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

	private CSTWSession() {
		servers = new HashMap<String, Boolean>();
		userId = Default.getUser();
		accountId = Default.getAccount();
		accountGroupList = new ArrayList<String>();
		accountList = new ArrayList<Account>();
		userGroup = new UserGroup("Admin", UserRole.Admin);
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public Account getLoginAccount() {
		return loginAccount;
	}

	public void setLoginAccount(Account loginAccount) {
		this.loginAccount = loginAccount;
	}

	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	public List<String> getAccountGroupList() {
		return accountGroupList;
	}

	public void setAccountGroupList(List<String> accountGroupList) {
		this.accountGroupList = accountGroupList;
	}

	public List<Account> getAccountList() {
		return accountList;
	}

	public void setAccountList(List<Account> accountList) {
		this.accountList = accountList;
	}

	public void setXstream(XStream xstream) {
		this.xstream = xstream;
	}

	public HashMap<String, Boolean> getServers() {
		return servers;
	}

}
