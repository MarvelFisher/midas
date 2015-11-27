package com.cyanspring.cstw.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.cstw.auth.IAuthChecker;
import com.cyanspring.common.cstw.kdb.SignalManager;
import com.cyanspring.common.cstw.kdb.SignalType;
import com.cyanspring.common.cstw.tick.TickManager;
import com.cyanspring.common.cstw.tick.Ticker;
import com.cyanspring.common.fx.IFxConverter;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.cstw.session.CSTWSession;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.05
 *
 */
public final class CSTWBeanPool implements IBusinessService {

	private IAuthChecker authManager;
	private TickManager tickManager;
	private SignalManager signalManager;

	private IFxConverter rateConverter;

	private List<String> symbolList = new ArrayList<String>();

	public CSTWBeanPool(BeanHolder beanHolder) {
		authManager = beanHolder.getAuthManager();
		tickManager = beanHolder.getTickManager();
		signalManager = beanHolder.getSignalManager();
		tickManager = new TickManager(beanHolder.getEventManager());
	}

	public IAuthChecker getAuthManager() {
		return authManager;
	}

	public TickManager getTickManager() {
		return tickManager;
	}

	public SignalManager getSignalManager() {
		return signalManager;
	}

	public List<String> getSymbolList() {
		if ((null == symbolList || symbolList.isEmpty()) && null != tickManager)
			symbolList = tickManager.getSymbolList();

		return symbolList;
	}

	public List<RefData> getRefDataList() {
		return tickManager.getRefDataList();
	}

	public RefData getRefData(String symbol) {
		return tickManager.getRefDataMap().get(symbol);
	}

	public Map<String, RefData> getRefDataMap() {
		return tickManager.getRefDataMap();
	}

	public SignalType getSignal(String symbol, double scale) {
		return signalManager.getSignal(symbol, scale);
	}

	public Ticker getTicker(String symbol) {
		Ticker ticker = tickManager.getTicker(symbol);
		if (null == ticker && StringUtils.hasText(symbol)) {
			tickManager.requestTickTableInfo(symbol);
		}
		return ticker;
	}

	public boolean hasAuth(String view, String action) {
		return this.authManager.hasAuth(CSTWSession.getInstance()
				.getUserGroup().getRole(), view, action);
	}

	public boolean hasViewAuth(String view) {
		return this.authManager.hasViewAuth(CSTWSession.getInstance()
				.getUserGroup().getRole(), view);
	}

	public IFxConverter getRateConverter() {
		return rateConverter;
	}

	public void setRateConverter(IFxConverter rateConverter) {
		this.rateConverter = rateConverter;
	}

}
