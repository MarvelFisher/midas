package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

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

	List<?> getInstrumentInfoModelListBySubAccountId(String id);

	List<?> getInstrumentInfoModelListBySubPoolId(String id);

	List<?> getSubPoolListByAccountId(String id);
	
	List<?> getAssignedModelListBySubPoolId(String id);

	void createNewSubPool(String selectText);

	void removeSubPool(InstrumentPoolModel obj);

	void createNewInstrumentInfoModel(InstrumentPoolModel subPoolModel,
			InstrumentInfoModel model);

	void removeInstrumentInfoModel(InstrumentPoolModel subPoolModel,
			InstrumentInfoModel model);

	
	
}
