/**
 * 
 */
package com.cyanspring.cstw.ui.rw.composite.table.provider;

import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;

/**
 * @author Yu-Junfeng
 * @create 25 Aug 2015
 */
public class ActivityOrderLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		RCOrderRecordModel model = (RCOrderRecordModel) element;
		switch (columnIndex) {
		case 0:
			return model.getOrderId();
		case 1:
			return model.getSymbol();
		case 2:
			return model.getSide();
		case 3:
			return model.getPrice().toString();
		case 4:
			return model.getVolume().toString();
		case 5:
			return model.getOrderStatus();
		case 6:
			return model.getCumQty().toString();
		case 7:
			return model.getCreateTime();
		case 8:
			return model.getTrader();
		default:
			return "";
		}
	}

}
