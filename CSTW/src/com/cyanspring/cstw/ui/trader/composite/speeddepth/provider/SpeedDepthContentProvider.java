package com.cyanspring.cstw.ui.trader.composite.speeddepth.provider;

import java.util.List;

import com.cyanspring.cstw.ui.basic.DefaultContentProvider;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthContentProvider extends
		DefaultContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			return ((List<?>) inputElement).toArray();
		}
		return null;
	}

}
