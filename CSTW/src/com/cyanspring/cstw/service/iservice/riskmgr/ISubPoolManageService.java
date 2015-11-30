package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

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
	
	

}
