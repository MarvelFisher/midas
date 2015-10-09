package com.cyanspring.cstw.trader.gui.composite.speeddepth.provider;

import com.cyanspring.cstw.trader.gui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.model.SpeedDepthModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		SpeedDepthModel model = (SpeedDepthModel) element;
		switch (columnIndex) {
		case 0:
			if (model.getAskQty() > 0) {
				return "" + model.getAskQty();
			}
			return "";
		case 1:
			if (model.getType() == SpeedDepthModel.BID && model.getVol() > 0) {
				return "" + model.getVol();
			}
			return "";
		case 2:
			return "" + model.getPrice();
		case 3:
			if (model.getType() == SpeedDepthModel.ASK && model.getVol() > 0) {
				return "" + model.getVol();
			}
			return "";
		case 4:
			if (model.getBidQty() > 0) {
				return "" + model.getBidQty();
			}
			return "";
		}
		return "";
	}
}
