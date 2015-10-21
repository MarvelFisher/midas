package com.cyanspring.cstw.ui.trader.basic;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/19
 *
 */
public abstract class DefaultContentProviderAdapter implements
		IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			return ((List<?>) inputElement).toArray();
		}
		return null;
	}

	@Override
	public void dispose() {
		// do nothing.
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing.
	}

}
