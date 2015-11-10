/**
 * 
 */
package com.cyanspring.cstw.ui.rw.composite.table.provider;

import com.cyanspring.cstw.model.riskmgr.RCUserStatisticsModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;

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
			return LTWStringUtils.doubleToString(model.getRealizedProfit());
		case 2:
			return LTWStringUtils.doubleToString(model.getTurnover());
		default:
			return "";			
		}
	}

}
