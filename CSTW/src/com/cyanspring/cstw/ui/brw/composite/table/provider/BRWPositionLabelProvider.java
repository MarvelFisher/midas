package com.cyanspring.cstw.ui.brw.composite.table.provider;

import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BRWPositionLabelProvider extends DefaultLabelProviderAdapter {

	
	@Override
	public String getColumnText(Object element, int columnIndex) {
RCOpenPositionModel model = (RCOpenPositionModel) element;
		
		switch (columnIndex) {
		case 0:
			return model.getInstrumentCode();
		case 1:
			return model.getPositionDirection().toString();
		case 2:
			return LTWStringUtils.doubleToString(model.getInstrumentQuality());
		case 3:
			return LTWStringUtils.doubleToString(model.getUrPnl());
		case 4:
			return LTWStringUtils.doubleToString(model.getAveragePrice());
		default:
			return "";			
		}
	}

}
