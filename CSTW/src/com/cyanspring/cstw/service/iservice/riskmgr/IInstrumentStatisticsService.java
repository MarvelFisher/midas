/**
 * 
 */
package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.service.iservice.IBasicService;

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
