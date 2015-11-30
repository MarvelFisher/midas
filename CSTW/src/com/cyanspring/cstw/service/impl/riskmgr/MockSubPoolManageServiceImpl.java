package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.client.IInstrumentPoolKeeper.ModelType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.ExchangeSubAccount;
import com.cyanspring.common.pool.InstrumentPool;
import com.cyanspring.common.pool.InstrumentPoolRecord;
import com.cyanspring.cstw.keepermanager.InstrumentPoolKeeperManager;
import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.model.admin.InstrumentPoolModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.iservice.admin.IInputChangeListener;
import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;
import com.cyanspring.cstw.session.CSTWSession;

/**
 * @author Junfeng
 * @create 24 Nov 2015
 */
public class MockSubPoolManageServiceImpl extends BasicServiceImpl implements
		ISubPoolManageService {
	private static final Logger log = LoggerFactory.getLogger(MockSubPoolManageServiceImpl.class);
	
	private IInstrumentPoolKeeper instrumentPoolKeeper;
	
	private List<IInputChangeListener> accountInputChange;
	private List<IInputChangeListener> poolInputChange;
	
	public MockSubPoolManageServiceImpl() {
		instrumentPoolKeeper = InstrumentPoolKeeperManager.getInstance().getInstrumentPoolKeeper();
		accountInputChange = new ArrayList<IInputChangeListener>();
		poolInputChange = new ArrayList<IInputChangeListener>();
	}
	
	@Override
	public List<SubAccountModel> getAllAssignedSubAccount() {
		List<SubAccountModel> subList = new ArrayList<SubAccountModel>();
		List<ExchangeSubAccount> exchangeSubAccount = instrumentPoolKeeper.getAssignedSubAccounts(CSTWSession.getInstance().getUserId());
		for (ExchangeSubAccount sub : exchangeSubAccount) {
			SubAccountModel model = ModelTransfer.parseSubAccountModel(sub);
			subList.add(model);
		}
		return subList;
	}
	
	@Override
	public List<AssignedModel> getAssignedModelListBySubPoolId(String id) {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public void addSubAccountInputChangeListener(
			IInputChangeListener iInputChangeListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<InstrumentInfoModel> getInstrumentInfoModelListBySubAccountId(String id) {
		List<InstrumentPoolRecord> recordList = instrumentPoolKeeper
				.getInstrumentPoolRecordList(id, ModelType.EXCHANGE_SUB_ACCOUNT);
		// merge record
		Map<String, Double> symbolQtyMap = new HashMap<String, Double>();
		for (InstrumentPoolRecord record : recordList) {
			if (!symbolQtyMap.containsKey(record.getSymbol())) {
				symbolQtyMap.put(record.getSymbol(), new Double(0));
			}
			Double qty = symbolQtyMap.get(record.getSymbol());
			qty = Double.valueOf(qty + record.getQty());
			symbolQtyMap.put(record.getSymbol(), qty);
		}
		List<InstrumentInfoModel> instrumentInfoModelList = new ArrayList<InstrumentInfoModel>();
		for (Map.Entry<String, Double> entry : symbolQtyMap.entrySet()) {
			InstrumentInfoModel infoModel = new InstrumentInfoModel.Builder()
					.symbolId(entry.getKey()).symbolName(entry.getKey())
					.qty(entry.getValue()).build();
			instrumentInfoModelList.add(infoModel);
		}
		return instrumentInfoModelList;
	}

	@Override
	public List<InstrumentInfoModel> getInstrumentInfoModelListBySubPoolId(String id) {
		List<InstrumentPoolRecord> recordList = instrumentPoolKeeper.getInstrumentPoolRecordList(id, ModelType.INSTRUMENT_POOL);
		// merge record
		Map<String, Double> symbolQtyMap = new HashMap<String, Double>();
		for (InstrumentPoolRecord record : recordList) {
			if (!symbolQtyMap.containsKey(record.getSymbol())) {
				symbolQtyMap.put(record.getSymbol(), new Double(0));
			}
			Double qty = symbolQtyMap.get(record.getSymbol());
			qty = Double.valueOf(qty + record.getQty());
			symbolQtyMap.put(record.getSymbol(), qty);
		}
		List<InstrumentInfoModel> instrumentInfoModelList = new ArrayList<InstrumentInfoModel>();
		for (Map.Entry<String, Double> entry : symbolQtyMap.entrySet()) {
			InstrumentInfoModel infoModel = new InstrumentInfoModel.Builder()
					.symbolId(entry.getKey()).symbolName(entry.getKey())
					.qty(entry.getValue()).build();
			instrumentInfoModelList.add(infoModel);
		}
		return instrumentInfoModelList;
	}

	@Override
	public List<InstrumentPoolModel> getSubPoolListByAccountId(String id) {
		List<InstrumentPoolModel> poolList = new ArrayList<InstrumentPoolModel>();
		List<InstrumentPool> instrumentPool = instrumentPoolKeeper.getInstrumentPoolList(id);
		for (InstrumentPool pool : instrumentPool) {
			poolList.add(ModelTransfer.parseInstrumentPoolModel(pool));
		}
		return poolList;
	}

	@Override
	public void createNewSubPool(String selectText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSubPool(InstrumentPoolModel obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createNewInstrumentInfoModel(InstrumentPoolModel subPoolModel,
			InstrumentInfoModel model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeInstrumentInfoModel(InstrumentPoolModel subPoolModel,
			InstrumentInfoModel model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Account> getAvailableAssigneeList(InstrumentPoolModel subPoolModel) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
