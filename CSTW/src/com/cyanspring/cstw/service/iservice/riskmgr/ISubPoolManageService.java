package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.model.admin.InstrumentPoolModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.admin.IInputChangeListener;

/**
 * @author Junfeng
 * @create 24 Nov 2015
 */
public interface ISubPoolManageService extends IBasicService{
	
	List<SubAccountModel> getAllAssignedSubAccount();
	
	void addSubAccountInputChangeListener(
			IInputChangeListener iInputChangeListener);

	List<InstrumentInfoModel> getInstrumentInfoModelListBySubAccountId(String id);

	List<InstrumentInfoModel> getInstrumentInfoModelListBySubPoolId(String id);

	List<InstrumentPoolModel> getSubPoolListByAccountId(String id);
	
	List<AssignedModel> getAssignedModelListBySubPoolId(String id);

	void createNewSubPool(String selectText);

	void removeSubPool(InstrumentPoolModel obj);

	void createNewInstrumentInfoModel(InstrumentPoolModel subPoolModel,
			InstrumentInfoModel model);

	void removeInstrumentInfoModel(InstrumentPoolModel subPoolModel,
			InstrumentInfoModel model);

	List<Account> getAvailableAssigneeList(InstrumentPoolModel subPoolModel);

	
	
}
