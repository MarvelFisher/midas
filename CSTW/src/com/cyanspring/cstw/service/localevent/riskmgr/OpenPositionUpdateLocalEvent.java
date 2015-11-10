/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel;

/**
 * @author Yu-Junfeng
 * @create 27 Aug 2015
 */
public final class OpenPositionUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 1L;

	private List<RCOpenPositionModel> allPositionModelList;

	private String PNL;

	public OpenPositionUpdateLocalEvent(String key) {
		super(key);
		allPositionModelList = new ArrayList<RCOpenPositionModel>();
	}

	public List<RCOpenPositionModel> getAllPositionModelList() {
		return allPositionModelList;
	}

	public void setAllPositionModelList(
			List<RCOpenPositionModel> allPositionModelList) {
		this.allPositionModelList = allPositionModelList;
	}

	public String getPNL() {
		return PNL;
	}

	public void setPNL(String pNL) {
		PNL = pNL;
	}

}
