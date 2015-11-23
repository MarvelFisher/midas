package com.cyanspring.cstw.service.impl.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.client.IInstrumentPoolKeeper.ModelType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.ExchangeAccount;
import com.cyanspring.common.pool.ExchangeSubAccount;
import com.cyanspring.common.pool.InstrumentPoolRecord;
import com.cyanspring.cstw.keepermanager.InstrumentPoolKeeperManager;
import com.cyanspring.cstw.localevent.InstrumentPoolUpdateLocalEvent;
import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 11 Nov 2015
 */
public class SubAccountManageServiceImpl extends BasicServiceImpl
		implements ISubAccountManagerService {

	private IInstrumentPoolKeeper instrumentPoolKeeper;

	@Override
	public void query() {
		// TODO Auto-generated method stub

	}

	public SubAccountManageServiceImpl() {
		instrumentPoolKeeper = InstrumentPoolKeeperManager.getInstance().getInstrumentPoolKeeper();
	}

	@Override
	public List<ExchangeAccountModel> getExchangeAccountList() {
		List<ExchangeAccountModel> exlist = new ArrayList<ExchangeAccountModel>();
		List<ExchangeAccount> exchangeAccountlist = instrumentPoolKeeper
				.getExchangeAccountList();
		for (ExchangeAccount account : exchangeAccountlist) {
			ExchangeAccountModel model = ModelTransfer
					.parseExchangeAccountModel(account);
			exlist.add(model);
		}
		return exlist;
	}

	@Override
	public List<SubAccountModel> getSubAccountListByExchangeAccountId(
			String id) {
		List<SubAccountModel> sublist = new ArrayList<SubAccountModel>();
		List<ExchangeSubAccount> exchangeSubAccountList = instrumentPoolKeeper
				.getExchangeSubAccountList(id);
		for (ExchangeSubAccount subAccount : exchangeSubAccountList) {
			SubAccountModel model = ModelTransfer
					.parseSubAccountModel(subAccount);
			sublist.add(model);
		}
		return sublist;
	}

	@Override
	public List<InstrumentInfoModel> getInstrumentInfoModelListByExchangeAccountId(
			String id) {
		Map<String, Double> symbolQtyMap = new HashMap<String, Double>();
		List<InstrumentPoolRecord> recordList = instrumentPoolKeeper
				.getInstrumentPoolRecordList(id, ModelType.EXCHANGE_ACCOUNT);
		for (InstrumentPoolRecord record : recordList) {
			if (symbolQtyMap.get(record.getSymbol()) == null) {
				symbolQtyMap.put(record.getSymbol(), new Double(0));
			}
			Double qty = symbolQtyMap.get(record.getSymbol());
			qty = Double.valueOf(qty.doubleValue() + record.getQty());
			symbolQtyMap.put(record.getSymbol(), qty);
		}
		List<InstrumentInfoModel> instrumentInfoModelList = new ArrayList<InstrumentInfoModel>();
		for (Map.Entry<String, Double> entry : symbolQtyMap.entrySet()) {
			InstrumentInfoModel infoModel = new InstrumentInfoModel.Builder()
					.build();
			infoModel.setSymbolName(entry.getKey());
			infoModel.setStockQuanity(entry.getValue());
			instrumentInfoModelList.add(infoModel);
		}
		return instrumentInfoModelList;
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(InstrumentPoolUpdateLocalEvent.class);
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createNewExchangeAccount() {

	}

	@Override
	public void createNewSubAccount(String exchange) {

	}

	@Override
	public void removeExchangeAccount(ExchangeAccountModel exchange) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSubAccount(SubAccountModel subAccount) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<InstrumentInfoModel> getInstrumentInfoModelListBySubAccountId(
			String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AssignedModel> getAssignedModelListBySubAccountId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserGroup> getAvailableAssigneeList(SubAccountModel subAccount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createNewAssignedModel(SubAccountModel subAccount,
			AssignedModel assigned, int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAssignedUser(SubAccountModel subAccount,
			AssignedModel assign) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveUpExchangeAccount(ExchangeAccountModel exchange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveDownExchangeAccount(ExchangeAccountModel exchange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveUpSubAccount(SubAccountModel subAccount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveDownSubAccount(SubAccountModel subAccount) {
		// TODO Auto-generated method stub
		
	}

}
