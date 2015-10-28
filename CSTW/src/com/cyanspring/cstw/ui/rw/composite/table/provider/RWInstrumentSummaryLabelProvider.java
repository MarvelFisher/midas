/**
 * 
 */
package com.cyanspring.cstw.ui.rw.composite.table.provider;

import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;

/**
 * @author Yu-Junfeng
 * @create 20 Aug 2015
 */
public class RWInstrumentSummaryLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		RCInstrumentModel model = (RCInstrumentModel) element;
		switch (columnIndex) {
		case 0:
			return model.getSymbol();
		case 1:
			return model.getSymbol();
		case 2:
			return model.getRealizedProfit().toString();
		case 3:
			return model.getTrades().toString();
		case 4:
			return model.getVolume().toString();
		case 5:
			return model.getTurnover().toString();
		case 6:
			return model.getCommission().toString();
		default:
			return "";			
		}
	}

}
