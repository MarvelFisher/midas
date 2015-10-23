package com.cyanspring.cstw.ui.rw.composite.table.provider;

import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWPositionLabelProvider extends DefaultLabelProviderAdapter{

	@Override
	public String getColumnText(Object element, int columnIndex) {
		RCOpenPositionModel model = (RCOpenPositionModel) element;
		
		switch (columnIndex) {
		case 0:
			return model.getSubAccount();
		case 1:
			return model.getInstrumentCode();
		case 2:
			return model.getInstrumentName();
		case 3:
			return model.getPositionDirection().toString();
		case 4:
			return LTWStringUtils.doubleToString(model.getInstrumentQuality());
		case 5:
			return LTWStringUtils.doubleToString(model.getUrPnl());
		case 6:
			return LTWStringUtils.doubleToString(model.getAveragePrice());
		case 7:
			return model.getTrader();
		default:
			return "";			
		}
	}

}
