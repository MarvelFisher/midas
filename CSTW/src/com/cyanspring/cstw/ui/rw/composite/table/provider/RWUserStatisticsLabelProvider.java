/**
 * 
 */
package com.cyanspring.cstw.ui.rw.composite.table.provider;

import com.cyanspring.cstw.service.model.riskmgr.RCUserStatisticsModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;

/**
 * @author Yu-Junfeng
 * @create 18 Aug 2015
 */
public class RWUserStatisticsLabelProvider extends
		DefaultLabelProviderAdapter {
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		RCUserStatisticsModel model = (RCUserStatisticsModel) element;
		
		switch (columnIndex) {
		case 0:
			return model.getTrader();
		case 1:
			return model.getRealizedProfit().toString();
		case 2:
			return model.getTurnover().toString();
		default:
			return "";			
		}
	}

}
