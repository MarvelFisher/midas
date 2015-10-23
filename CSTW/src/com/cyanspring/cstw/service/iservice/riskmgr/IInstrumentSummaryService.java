/**
 * 
 */
package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;

/**
 * @author Yu-Junfeng
 * @create 20 Aug 2015
 */
public interface IInstrumentSummaryService extends IBasicService {
	
	void queryInstrument();
	
	List<RCInstrumentModel> getInstrumentModelList();
	
}
