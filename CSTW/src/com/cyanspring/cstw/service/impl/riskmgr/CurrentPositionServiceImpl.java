/**
 * 
 */
package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel.RCPositionType;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.riskmgr.ICurrentPositionService;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionSnapshotListReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionSnapshotListRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionUpdateLocalEvent;
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
		OpenPositionSnapshotListRequestLocalEvent event = new OpenPositionSnapshotListRequestLocalEvent(
				roleType.name());
		sendEvent(event);
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(OpenPositionSnapshotListReplyLocalEvent.class);
		list.add(OpenPositionUpdateLocalEvent.class);
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		if (event instanceof OpenPositionSnapshotListReplyLocalEvent) {
			OpenPositionSnapshotListReplyLocalEvent replyEvent = (OpenPositionSnapshotListReplyLocalEvent) event;
			overallPositionModelList = replyEvent.getPositionList();
			currentPositionModelList = getOpenPositionFromOverallPosition(overallPositionModelList);
			return RefreshEventType.RWCurrentPositionList;
		}
		if (event instanceof OpenPositionUpdateLocalEvent) {
			OpenPositionUpdateLocalEvent replyEvent = (OpenPositionUpdateLocalEvent) event;
			overallPositionModelList = replyEvent.getAllPositionModelList();
			currentPositionModelList = getOpenPositionFromOverallPosition(overallPositionModelList);
			return RefreshEventType.RWCurrentPositionList;
		}
		return RefreshEventType.Default;
	}

	private List<RCOpenPositionModel> getOpenPositionFromOverallPosition(
			List<RCOpenPositionModel> overall) {
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
		for (RCOpenPositionModel model : currentPositionModelList) {
			double pricePerUnit = Business.getBusinessService()
					.getRefData(model.getInstrumentCode()).getPricePerUnit();
			if (pricePerUnit == 0) {
				pricePerUnit = 1.0;
			}
			marketCapital += model.getInstrumentQuality()
					* model.getAveragePrice() * pricePerUnit;
		}
		return LTWStringUtils.cashDoubleToString(marketCapital);
	}

	@Override
	public String getUnrealizedPNL() {
		unrealizedPnl = 0.0;
		for (RCOpenPositionModel model : currentPositionModelList) {
			unrealizedPnl += model.getUrPnl();
		}
		return LTWStringUtils.cashDoubleToString(unrealizedPnl);
	}

	@Override
	public String getPNL() {
		pnl = 0.0;
		for (RCOpenPositionModel model : overallPositionModelList) {
			pnl += model.getPnl();
		}
		return LTWStringUtils.cashDoubleToString(pnl);
	}

	@Override
	public String getAllPNL() {
		allPnl = unrealizedPnl + pnl;
		return LTWStringUtils.cashDoubleToString(allPnl);
	}

	@Override
	public void forceClosePosition(String account, String symbol) {
		String server = Business.getBusinessService().getFirstServer();

		ClosePositionRequestEvent request = new ClosePositionRequestEvent(
				account, server, account, symbol, 0.0, OrderReason.ManualClose,
				IdGenerator.getInstance().getNextID());
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

}
