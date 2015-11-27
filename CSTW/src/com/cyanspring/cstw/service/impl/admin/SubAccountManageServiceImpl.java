package com.cyanspring.cstw.service.impl.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.User;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.client.IInstrumentPoolKeeper.ModelType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.event.pool.ExchangeAccountOperationRequestEvent;
import com.cyanspring.common.event.pool.ExchangeSubAccountOperationRequestEvent;
import com.cyanspring.common.event.pool.UserExchangeSubAccountOperationRequestEvent;
import com.cyanspring.common.pool.ExchangeAccount;
import com.cyanspring.common.pool.ExchangeSubAccount;
import com.cyanspring.common.pool.InstrumentPoolRecord;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.business.CSTWEventManager;
import com.cyanspring.cstw.keepermanager.InstrumentPoolKeeperManager;
import com.cyanspring.cstw.localevent.InstrumentPoolUpdateLocalEvent;
import com.cyanspring.cstw.localevent.SubAccountStructureUpdateLocalEvent;
import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.iservice.admin.IInputChangeListener;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 11 Nov 2015
 */
public class SubAccountManageServiceImpl extends BasicServiceImpl implements
		ISubAccountManagerService {
	private static final Logger log = LoggerFactory
			.getLogger(SubAccountManageServiceImpl.class);

	private IInstrumentPoolKeeper instrumentPoolKeeper;

	private List<IInputChangeListener> exchangeInputChange;
	private List<IInputChangeListener> subInputChange;

	@Override
	public void query() {
		// TODO Auto-generated method stub

	}

	public SubAccountManageServiceImpl() {
		instrumentPoolKeeper = InstrumentPoolKeeperManager.getInstance()
				.getInstrumentPoolKeeper();
		exchangeInputChange = new ArrayList<IInputChangeListener>();
		subInputChange = new ArrayList<IInputChangeListener>();
	}

	@Override
	public ExchangeAccountModel getExchangeAccoutById(String id) {
		ExchangeAccount exchangeAccount = instrumentPoolKeeper
				.getExchangeAccount(id);
		return ModelTransfer.parseExchangeAccountModel(exchangeAccount);
	}

	@Override
	public SubAccountModel getSubAccountById(String id) {

		return null;
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
	public List<SubAccountModel> getSubAccountListByExchangeAccountId(String id) {
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
	public List<InstrumentInfoModel> getInstrumentInfoModelListBySubAccountId(
			String id) {
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
					.symbolName(entry.getKey()).symbolName(entry.getKey())
					.qty(entry.getValue()).build();
			instrumentInfoModelList.add(infoModel);
		}
		return instrumentInfoModelList;
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(InstrumentPoolUpdateLocalEvent.class);
		list.add(SubAccountStructureUpdateLocalEvent.class);
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		if (event instanceof InstrumentPoolUpdateLocalEvent) {
			fireExchangeInputChange();
			return RefreshEventType.Default;
		}
		if (event instanceof SubAccountStructureUpdateLocalEvent) {
			return RefreshEventType.InstrumentPoolUpdate;
		}
		return RefreshEventType.Default;
	}

	@Override
	public void createNewExchangeAccount(String name) {
		ExchangeAccountOperationRequestEvent request = new ExchangeAccountOperationRequestEvent(
				IdGenerator.getInstance().getNextID(), Business
						.getBusinessService().getFirstServer(), IdGenerator
						.getInstance().getNextID());
		ExchangeAccount ex = new ExchangeAccount();
		ex.setName(name);
		request.setExchangeAccount(ex);
		request.setOperationType(OperationType.CREATE);
		log.info("Create New Exchange Account: " + name);
		CSTWEventManager.sendEvent(request);
	}

	@Override
	public void createNewSubAccount(String exchange, String name) {
		ExchangeSubAccountOperationRequestEvent request = new ExchangeSubAccountOperationRequestEvent(
				IdGenerator.getInstance().getNextID(), Business
						.getBusinessService().getFirstServer(), IdGenerator
						.getInstance().getNextID());
		ExchangeSubAccount sub = new ExchangeSubAccount();
		sub.setExchangeAccount(exchange);
		sub.setName(name);
		request.setExchangeSubAccount(sub);
		request.setOperationType(OperationType.CREATE);
		log.info("Create Sub Account: " + name);
		CSTWEventManager.sendEvent(request);
	}

	@Override
	public void removeExchangeAccount(ExchangeAccountModel exchange) {
		ExchangeAccountOperationRequestEvent request = new ExchangeAccountOperationRequestEvent(
				IdGenerator.getInstance().getNextID(), Business
						.getBusinessService().getFirstServer(), IdGenerator
						.getInstance().getNextID());
		ExchangeAccount ex = new ExchangeAccount();
		ex.setId(exchange.getId());
		ex.setName(exchange.getName());
		request.setExchangeAccount(ex);
		request.setOperationType(OperationType.DELETE);
		log.info("Delete Exchange Account: " + exchange.getName());
		CSTWEventManager.sendEvent(request);
	}

	@Override
	public void removeSubAccount(SubAccountModel subAccount) {
		ExchangeSubAccountOperationRequestEvent request = new ExchangeSubAccountOperationRequestEvent(
				IdGenerator.getInstance().getNextID(), Business
						.getBusinessService().getFirstServer(), IdGenerator
						.getInstance().getNextID());
		ExchangeSubAccount sub = new ExchangeSubAccount();
		sub.setExchangeAccount(subAccount.getExchangeAccountName());
		sub.setId(subAccount.getId());
		request.setExchangeSubAccount(sub);
		request.setOperationType(OperationType.DELETE);
		log.info("Delete Sub Account: " + subAccount.getName());
		CSTWEventManager.sendEvent(request);

	}

	@Override
	public void updateExchangeAccountName(ExchangeAccountModel exchange,
			String name) {
		ExchangeAccountOperationRequestEvent request = new ExchangeAccountOperationRequestEvent(
				IdGenerator.getInstance().getNextID(), Business
						.getBusinessService().getFirstServer(), IdGenerator
						.getInstance().getNextID());
		ExchangeAccount ex = new ExchangeAccount();
		ex.setId(exchange.getId());
		ex.setName(name);
		request.setExchangeAccount(ex);
		request.setOperationType(OperationType.UPDATE);

		log.info("Rename Exchange Accout " + exchange.getName() + " -> " + name);
		CSTWEventManager.sendEvent(request);
	}

	@Override
	public void updateSubAccountName(SubAccountModel subAccount, String name) {
		ExchangeSubAccountOperationRequestEvent request = new ExchangeSubAccountOperationRequestEvent(
				IdGenerator.getInstance().getNextID(), Business
						.getBusinessService().getFirstServer(), IdGenerator
						.getInstance().getNextID());
		ExchangeSubAccount sub = new ExchangeSubAccount();
		sub.setExchangeAccount(subAccount.getExchangeAccountName());
		sub.setId(subAccount.getId());
		sub.setName(name);
		request.setExchangeSubAccount(sub);
		request.setOperationType(OperationType.UPDATE);

		log.info("Rename SubAccount " + subAccount.getName() + " -> " + name);
		CSTWEventManager.sendEvent(request);
	}

	@Override
	public List<AssignedModel> getAssignedModelListBySubAccountId(String id) {
		List<AssignedModel> assList = new ArrayList<AssignedModel>();
		List<String> userIdList = instrumentPoolKeeper
				.getAssignedAdminsBySubAccount(id);
		for (String usrId : userIdList) {
			User usr = InstrumentPoolKeeperManager.getInstance()
					.getRiskManagerOrGroupUser(usrId);
			if (usr != null) {
				assList.add(new AssignedModel.Builder().userId(usr.getId())
						.roleType(usr.getRole().desc()).build());
			}
		}

		return assList;
	}

	@Override
	public List<User> getAvailableAssigneeList(SubAccountModel subAccount) {
		return InstrumentPoolKeeperManager.getInstance()
				.getRiskManagerNGroupUser();
	}

	@Override
	public void createNewAssignedModel(SubAccountModel subAccount,
			AssignedModel assigned, int index) {
		UserExchangeSubAccountOperationRequestEvent request = new UserExchangeSubAccountOperationRequestEvent(
				IdGenerator.getInstance().getNextID(), Business
						.getBusinessService().getFirstServer(), IdGenerator
						.getInstance().getNextID());

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

	@Override
	public void addExchangeInputChangeListener(IInputChangeListener listener) {
		exchangeInputChange.add(listener);
	}

	@Override
	public void addSubAccInputChangeListener(IInputChangeListener listener) {
		subInputChange.add(listener);
	}

	private void fireExchangeInputChange() {
		for (IInputChangeListener listener : exchangeInputChange) {
			listener.inputChanged();
		}
	}

	private void fireSubAccInputChange() {
		for (IInputChangeListener listener : subInputChange) {
			listener.inputChanged();
		}
	}

}
