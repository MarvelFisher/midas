package com.cyanspring.cstw.service.iservice.admin;

import java.util.List;

import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
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
	
}
