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
import com.cyanspring.cstw.service.iservice.riskmgr.ICurrentPositionService;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel.RCPositionType;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;

/**
 * @author Yu-Junfeng
 * @create 29 Jul 2015
 */
public class CurrentPositionServiceImpl extends BasicServiceImpl implements
		ICurrentPositionService {

	private double marketCapital = 0.0;

	private double unrealizedPnl = 0.0;

	private double pnl = 0.0;

	private double allPnl = 0.0;

	private List<RCOpenPositionModel> currentPositionModelList;
	
	private List<RCOpenPositionModel> overallPositionModelList;
	
	private UserRole roleType;

	public CurrentPositionServiceImpl() {
	}

	@Override
	public void queryOpenPosition() {
		
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		//
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {

		return RefreshEventType.Default;
	}
	
	private List<RCOpenPositionModel> getOpenPositionFromOverallPosition(List<RCOpenPositionModel> overall){
		List<RCOpenPositionModel> result = new ArrayList<RCOpenPositionModel>();
		for (RCOpenPositionModel model : overall) {
			if (model.getType() == RCPositionType.Open) {
				result.add(model);
			}
		}
		return result;
	}
	
	public void setRoleType(UserRole roleType) {
		this.roleType = roleType;
	}

	@Override
	public List<RCOpenPositionModel> getOpenPositionModelList() {
		return currentPositionModelList;
	}

	@Override
	public String getAllMarketCapitalization() {	
		marketCapital = 0.0;
		for ( RCOpenPositionModel model : currentPositionModelList ) {
			marketCapital += model.getInstrumentQuality() * model.getAveragePrice();
		}
		return LTWStringUtils.cashDoubleToString(marketCapital);
	}

	@Override
	public String getUnrealizedPNL() {
		unrealizedPnl = 0.0;
		for ( RCOpenPositionModel model : currentPositionModelList ) {
			unrealizedPnl += model.getUrPnl();
		}
		return LTWStringUtils.cashDoubleToString(unrealizedPnl);
	}

	@Override
	public String getPNL() {
		pnl = 0.0;
		for ( RCOpenPositionModel model : overallPositionModelList ) {
			pnl += model.getPnl();
		}
		return LTWStringUtils.cashDoubleToString(pnl);
	}

	@Override
	public String getAllPNL() {
		allPnl = unrealizedPnl + pnl;
		return LTWStringUtils.cashDoubleToString(allPnl);
	}

}