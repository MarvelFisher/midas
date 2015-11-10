/**
 * 
 */
package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.service.iservice.IBasicService;

/**
 * @author Yu-Junfeng
 * @create 20 Aug 2015
 */
public interface IInstrumentSummaryService extends IBasicService {
	
	void queryInstrument();
	
	List<RCInstrumentModel> getInstrumentModelList();
	
}
