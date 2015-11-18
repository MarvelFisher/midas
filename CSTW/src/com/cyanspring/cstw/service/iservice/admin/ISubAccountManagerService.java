package com.cyanspring.cstw.service.iservice.admin;

import java.util.List;

import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.IBasicService;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public interface ISubAccountManagerService extends IBasicService {
	
	void query();
	
	List<ExchangeAccountModel> getExchangeAccountList();
	
	List<SubAccountModel> getSubAccountListByExchangeAccountName(String name);
	
	List<InstrumentInfoModel> getInstrumentInfoModelListByExchangeAccountName(String name);
	
	List<InstrumentInfoModel> getInstrumentInfoModelListBySubAccountId(String id);
	
	List<AssignedModel> getAssignedModelListBySubAccountId(String id);
	
	void createNewExchangeAccount();
	
	void createNewSubAccount(String exchangeId);
	
	void removeExchangeAccount(ExchangeAccountModel exchange);
	
	void removeSubAccount(SubAccountModel subAccount);
	
	void moveUpExchangeAccount(ExchangeAccountModel exchange);
	
	void moveDownExchangeAccount(ExchangeAccountModel exchange);
	
	void moveUpSubAccount(SubAccountModel subAccount);
	
	void moveDownSubAccount(SubAccountModel subAccount);
	
}
