package com.cyanspring.cstw.ui.basic;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/28
 *
 */
public abstract class BasicEditingSupport extends EditingSupport {

	public BasicEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

}
