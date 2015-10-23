/**
 * 
 */
package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;

/**
 * @author Yu-Junfeng
 * @create 30 Jul 2015
 */
public interface IInstrumentStatisticsService extends IBasicService {
	
	void queryInstrument();
	
	public List<RCInstrumentModel> getInstrumentModelList();
	
	String getAllRealizedProfit();
	
	String getAllConsideraion();
	
	String getProductivity();
}
