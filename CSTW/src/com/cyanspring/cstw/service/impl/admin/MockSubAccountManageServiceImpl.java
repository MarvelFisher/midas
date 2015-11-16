package com.cyanspring.cstw.service.impl.admin;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.event.AsyncEvent;
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

	// Mock
	public MockSubAccountManageServiceImpl() {
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

		sub2list = new ArrayList<SubAccountModel>();
		sub2list.add(new SubAccountModel.Builder().id("sub2.1")
				.name("account2.1").exchangeAccount(ex2).useableMoney(10000)
				.commissionRate(0.01).build());
		sub2list.add(new SubAccountModel.Builder().id("sub2.2")
				.name("account2.2").exchangeAccount(ex2).useableMoney(10000)
				.commissionRate(0.01).build());

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
	public List<SubAccountModel> getSubAccountListByExchangeAccountName(
			String name) {
		if (name.equals("ex1")) {

			return sub1list;
		} else if (name.equals("ex2")) {

			return sub2list;
		} else {
			List<SubAccountModel> list = new ArrayList<SubAccountModel>();
			return list;
		}

	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		// TODO Auto-generated method stub
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
	public List<InstrumentInfoModel> getInstrumentInfoModelListByExchangeAccountName(
			String name) {
		List<InstrumentInfoModel> list = new ArrayList<InstrumentInfoModel>();
		list.add(new InstrumentInfoModel.Builder().symbolId("AUDUSD")
				.symbolName("AUDUSD").qty(10000).build());
		list.add(new InstrumentInfoModel.Builder().symbolId("AUDCAD")
				.symbolName("AUDCAD").qty(10000).build());
		return list;
	}

	@Override
	public void createNewExchangeAccount() {
		exlist.add(new ExchangeAccountModel.Builder().id("id3").name("ex3")
				.build());
	}

	@Override
	public void createNewSubAccount(String exchange) {
		if (exchange.equals("ex1")) {
			sub1list.add(new SubAccountModel.Builder().id("sub1.3")
					.name("account1.3").exchangeAccount(ex1)
					.useableMoney(10000).commissionRate(0.01).build());

		} else if (exchange.equals("ex2")) {
			sub2list.add(new SubAccountModel.Builder().id("sub2.3")
					.name("account2.3").exchangeAccount(ex1)
					.useableMoney(10000).commissionRate(0.01).build());

		}
	}

}
