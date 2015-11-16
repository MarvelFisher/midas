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
 * @create 11 Nov 2015
 */
public class SubAccountManageServiceImpl extends BasicServiceImpl implements
		ISubAccountManagerService {

	private IInstrumentPoolKeeper instrumentPoolKeeper;

	@Override
	public void query() {
		// TODO Auto-generated method stub

	}

	// Mock
	private ExchangeAccountModel ex1;
	private ExchangeAccountModel ex2;

	// Mock
	public SubAccountManageServiceImpl() {
		ex1 = new ExchangeAccountModel.Builder().id("id1").name("ex1").build();
		ex2 = new ExchangeAccountModel.Builder().id("id2").name("ex2").build();
	}

	/*
	 * Mock Code
	 */
	@Override
	public List<ExchangeAccountModel> getExchangeAccountList() {
		List<ExchangeAccountModel> list = new ArrayList<ExchangeAccountModel>();

		list.add(ex1);
		list.add(ex2);
		return list;
	}

	/*
	 * Mock Code
	 */
	@Override
	public List<SubAccountModel> getSubAccountListByExchangeAccountName(
			String name) {
		if (name.equals("ex1")) {
			List<SubAccountModel> list = new ArrayList<SubAccountModel>();
			list.add(new SubAccountModel.Builder().id("sub1").name("account1")
					.exchangeAccount(ex1).useableMoney(10000)
					.commissionRate(0.01).build());
			list.add(new SubAccountModel.Builder().id("sub2").name("account2")
					.exchangeAccount(ex1).useableMoney(10000)
					.commissionRate(0.01).build());
			return list;
		} else {
			List<SubAccountModel> list = new ArrayList<SubAccountModel>();
			list.add(new SubAccountModel.Builder().id("sub1").name("account1")
					.exchangeAccount(ex2).useableMoney(10000)
					.commissionRate(0.01).build());
			list.add(new SubAccountModel.Builder().id("sub2").name("account2")
					.exchangeAccount(ex2).useableMoney(10000)
					.commissionRate(0.01).build());
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

}
