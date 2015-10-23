/**
 * 
 */
package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.riskmgr.IInstrumentStatisticsService;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;

/**
 * @author Yu-Junfeng
 * @create 18 Aug 2015
 */
public class InstrumentStatisticsServiceImpl extends BasicServiceImpl implements
		IInstrumentStatisticsService {

	private double realizedProfit;

	private double consideration;

	private double productivity;

	private List<RCInstrumentModel> modelList;
	
	private UserRole roleType;

	public InstrumentStatisticsServiceImpl() {
		modelList = new ArrayList<RCInstrumentModel>();
	}

	@Override
	public void queryInstrument() {
		
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		if ( event.getKey() != roleType.name() ) {
			return RefreshEventType.Default;
		}
		
		return RefreshEventType.Default;
	}

	@Override
	public List<RCInstrumentModel> getInstrumentModelList() {
		if (modelList == null) {
			modelList = new ArrayList<RCInstrumentModel>();
		}
		return modelList;
	}

	@Override
	public String getAllRealizedProfit() {
		realizedProfit = 0.0;
		for ( RCInstrumentModel model : modelList ) {
			realizedProfit += model.getRealizedProfit();
		}
		return LTWStringUtils.cashDoubleToString(realizedProfit);
	}

	@Override
	public String getAllConsideraion() {
		consideration = 0.0;
		for ( RCInstrumentModel model : modelList ) {
			consideration += model.getTurnover();
		}			
		return LTWStringUtils.cashDoubleToStringWith10000(consideration);
	}

	@Override
	public String getProductivity() {
		productivity = realizedProfit / consideration;
		return LTWStringUtils.productivityDoubleToString(productivity);
	}
	
	public void setRoleType(UserRole type) {
		this.roleType = type;
	}

}
