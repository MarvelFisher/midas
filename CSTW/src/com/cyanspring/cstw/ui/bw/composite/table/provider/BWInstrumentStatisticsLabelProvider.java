package com.cyanspring.cstw.ui.bw.composite.table.provider;

import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BWInstrumentStatisticsLabelProvider extends
		DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		RCInstrumentModel model = (RCInstrumentModel) element;
		switch (columnIndex) {
		case 0:
			return model.getSymbol();
		case 1:
			return LTWStringUtils.doubleToString(model.getRealizedProfit());
		case 2:
			return model.getTrades().toString();
		case 3:
			return model.getVolume().toString();
		case 4:
			return LTWStringUtils.doubleToString(model.getTurnover());
		default:
			return "";			
		}
	}

}
