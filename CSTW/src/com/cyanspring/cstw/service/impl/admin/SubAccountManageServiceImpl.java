package com.cyanspring.cstw.service.impl.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.client.IInstrumentPoolKeeper.ModelType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.ExchangeAccount;
import com.cyanspring.common.pool.ExchangeSubAccount;
import com.cyanspring.common.pool.InstrumentPoolRecord;
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
public abstract class SubAccountManageServiceImpl extends BasicServiceImpl
		implements ISubAccountManagerService {

	private IInstrumentPoolKeeper instrumentPoolKeeper;

	@Override
	public void query() {
		// TODO Auto-generated method stub

	}

	public SubAccountManageServiceImpl() {

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

		return null;
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

}
