/**
 * 
 */
package com.cyanspring.cstw.service.iservice.riskmgr;


import java.util.List;

import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;

/**
 * @author Yu-Junfeng
 * @create 29 Jul 2015
 */
public interface ICurrentPositionService extends IBasicService {

	void queryOpenPosition();
	
	void forceClosePosition(String user, String symbol);
	
	List<RCOpenPositionModel> getOpenPositionModelList();
	
	String getAllMarketCapitalization();
	
	String getUnrealizedPNL();
	
	String getPNL();
	
	String getAllPNL();
	
}
