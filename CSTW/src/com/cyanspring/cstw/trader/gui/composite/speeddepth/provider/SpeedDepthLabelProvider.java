package com.cyanspring.cstw.trader.gui.composite.speeddepth.provider;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.wb.swt.SWTResourceManager;

import com.cyanspring.cstw.trader.gui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.model.SpeedDepthModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthLabelProvider extends DefaultLabelProviderAdapter
		implements ITableColorProvider {

	private int selectIndex = 0;

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
				try {
					int vol = (int) model.getVol();
					return "" + vol;
				} catch (Exception en) {
					return "";
				}
			}
			return "";
		case 2:
			return model.getFormatPrice();
		case 3:
			if (model.getType() == SpeedDepthModel.ASK && model.getVol() > 0) {
				try {
					int vol = (int) model.getVol();
					return "" + vol;
				} catch (Exception en) {
					return "";
				}
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

	@Override
	public Color getBackground(Object element, int columnIndex) {
		switch (columnIndex) {
		case 1:
			return SWTResourceManager.getColor(SWT.COLOR_DARK_RED);
		case 2:
			SpeedDepthModel model = (SpeedDepthModel) element;
			if (model.isLastPrice()) {
				return SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW);
			}
			return null;
		case 3:
			return SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN);
		}
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		SpeedDepthModel model = (SpeedDepthModel) element;
		switch (columnIndex) {
		case 1:
			if (model.getIndex() == selectIndex) {
				return SWTResourceManager.getColor(SWT.COLOR_BLACK);
			} else {
				return SWTResourceManager.getColor(SWT.COLOR_WHITE);
			}
		case 3:
			if (model.getIndex() == selectIndex) {
				return SWTResourceManager.getColor(SWT.COLOR_BLACK);
			} else {
				return SWTResourceManager.getColor(SWT.COLOR_WHITE);
			}
		}
		return null;
	}

	public void setSelectIndex(int selectIndex) {
		this.selectIndex = selectIndex;
	}
}
