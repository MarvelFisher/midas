package com.cyanspring.cstw.service.impl.admin;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.keepermanager.InstrumentPoolKeeperManager;
import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 16 Nov 2015
 */
public class MockSubAccountManageServiceImpl extends BasicServiceImpl implements
		ISubAccountManagerService {
	private static final Logger log = LoggerFactory.getLogger(MockSubAccountManageServiceImpl.class);
	
	private IInstrumentPoolKeeper instrumentPoolKeeper;

	@Override
	public void query() {
		// TODO Auto-generated method stub

	}

	// Mock
	private ExchangeAccountModel ex1;
	private ExchangeAccountModel ex2;
	
	private List<ExchangeAccountModel> exlist;
	private List<SubAccountModel> sub1list;
	private List<SubAccountModel> sub2list;
	
	private List<InstrumentInfoModel> instruList;
	private List<AssignedModel> assList;

	// Mock
	public MockSubAccountManageServiceImpl() {
		instrumentPoolKeeper = InstrumentPoolKeeperManager.getInstance().getInstrumentPoolKeeper();
		
		ex1 = new ExchangeAccountModel.Builder().id("id1").name("ex1").build();
		ex2 = new ExchangeAccountModel.Builder().id("id2").name("ex2").build();

		exlist = new ArrayList<ExchangeAccountModel>();
		exlist.add(ex1);
		exlist.add(ex2);

		sub1list = new ArrayList<SubAccountModel>();
		sub1list.add(new SubAccountModel.Builder().id("sub1.1")
				.name("account1.1").exchangeAccount(ex1).useableMoney(10000)
				.commissionRate(0.01).build());
		sub1list.add(new SubAccountModel.Builder().id("sub1.2")
				.name("account1.2").exchangeAccount(ex1).useableMoney(10000)
				.commissionRate(0.01).build());
		sub1list.add(new SubAccountModel.Builder().id("sub1.3")
				.name("account1.3").exchangeAccount(ex1).useableMoney(10000)
				.commissionRate(0.01).build());

		sub2list = new ArrayList<SubAccountModel>();
		sub2list.add(new SubAccountModel.Builder().id("sub2.1")
				.name("account2.1").exchangeAccount(ex2).useableMoney(10000)
				.commissionRate(0.01).build());
		sub2list.add(new SubAccountModel.Builder().id("sub2.2")
				.name("account2.2").exchangeAccount(ex2).useableMoney(10000)
				.commissionRate(0.01).build());
		
		instruList = new ArrayList<InstrumentInfoModel>();
		instruList.add(new InstrumentInfoModel.Builder().symbolId("AUDUSD")
				.symbolName("AUDUSD").qty(10000).build());
		instruList.add(new InstrumentInfoModel.Builder().symbolId("AUDCAD")
				.symbolName("AUDCAD").qty(10000).build());
		instruList.add(new InstrumentInfoModel.Builder().symbolId("AUDCAD")
				.symbolName("AUDCAD").qty(10000).build());
		instruList.add(new InstrumentInfoModel.Builder().symbolId("AUDCAD")
				.symbolName("AUDCAD").qty(10000).build());
		
		assList = new ArrayList<AssignedModel>();
		assList.add(new AssignedModel.Builder().userId("front_risk1").roleType("FrontRiskManager").build());
		assList.add(new AssignedModel.Builder().userId("group1").roleType("Group").build());
		assList.add(new AssignedModel.Builder().userId("front_risk2").roleType("FrontRiskManager").build());
		
	}

	/*
	 * Mock Code
	 */
	@Override
	public List<ExchangeAccountModel> getExchangeAccountList() {

		return exlist;
	}

	/*
	 * Mock Code
	 */
	@Override
	public List<SubAccountModel> getSubAccountListByExchangeAccountId(
			String name) {
		if (name.equals("id1")) {

			return sub1list;
		} else if (name.equals("id2")) {

			return sub2list;
		} else {
			List<SubAccountModel> list = new ArrayList<SubAccountModel>();
			return list;
		}

	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		
		return null;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Mock
	 */
	@Override
	public List<InstrumentInfoModel> getInstrumentInfoModelListByExchangeAccountId(
			String name) {
		
		return instruList;
	}
	
	/**
	 * Mock
	 */
	@Override
	public List<InstrumentInfoModel> getInstrumentInfoModelListBySubAccountId(
			String id) {
		return instruList.subList(0, 1);
	}
	
	@Override
	public List<AssignedModel> getAssignedModelListBySubAccountId(String id) {
		
		return assList;
	}


	@Override
	public void createNewExchangeAccount() {
		exlist.add(new ExchangeAccountModel.Builder().id("id3").name("ex3")
				.build());
	}

	@Override
	public void createNewSubAccount(String exchange) {
		if (exchange.equals("id1")) {
			sub1list.add(new SubAccountModel.Builder().id("sub1.3")
					.name("account1.3").exchangeAccount(ex1)
					.useableMoney(10000).commissionRate(0.01).build());

		} else if (exchange.equals("id2")) {
			sub2list.add(new SubAccountModel.Builder().id("sub2.3")
					.name("account2.3").exchangeAccount(ex1)
					.useableMoney(10000).commissionRate(0.01).build());

		}
	}

	@Override
	public void removeExchangeAccount(ExchangeAccountModel exchange) {
		exlist.remove(exchange);
	}

	@Override
	public void removeSubAccount(SubAccountModel subAccount) {
		sub1list.remove(subAccount);
		sub2list.remove(subAccount);
	}

	
	@Override
	public void moveUpExchangeAccount(ExchangeAccountModel exchange) {
		int index = exlist.indexOf(exchange);
		if (index > 0) {
			exlist.remove(exchange);
			exlist.add(index-1, exchange);
		}
	}

	@Override
	public void moveDownExchangeAccount(ExchangeAccountModel exchange) {
		int index = exlist.indexOf(exchange);
		if (index >= 0 && index < exlist.size()) {
			exlist.remove(exchange);
			exlist.add(index+1, exchange);
		}
	}

	@Override
	public void moveUpSubAccount(SubAccountModel subAccount) {
		int index = sub1list.indexOf(subAccount);
		if (index > 0) {
			sub1list.remove(subAccount);
			sub1list.add(index-1, subAccount);
		}
		
		int index2 = sub2list.indexOf(subAccount);
		if (index2 > 0) {
			sub2list.remove(subAccount);
			sub2list.add(index2-1, subAccount);
		}
		
	}

	@Override
	public void moveDownSubAccount(SubAccountModel subAccount) {
		int index1 = sub1list.indexOf(subAccount);
		if (index1 >= 0 && index1 < sub1list.size()) {
			sub1list.remove(subAccount);
			sub1list.add(index1+1, subAccount);
		}
		
		int index2 = sub2list.indexOf(subAccount);
		if (index2 >= 0 && index2 < sub2list.size()) {
			sub2list.remove(subAccount);
			sub2list.add(index2+1, subAccount);
		}
		
	}

	@Override
	public void createNewAssignedModel(SubAccountModel subAccount,
			AssignedModel assigned, int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAssignedUser(SubAccountModel subAccount, AssignedModel assign) {
		log.info("removeAssignedUser: " + subAccount.getName() + ", " + assign.getUserId());
		
	}

	@Override
	public List<UserGroup> getAvailableAssigneeList(SubAccountModel subAccount) {
		List<UserGroup> usrGroups = new ArrayList<UserGroup>();
		usrGroups.add(new UserGroup("front_risk8", UserRole.RiskManager));
		usrGroups.add(new UserGroup("group7", UserRole.Group));
		return usrGroups;
	}

	

}
