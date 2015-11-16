package com.cyanspring.cstw.ui.forms;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author Junfeng
 * @create 16 Nov 2015
 */
public class AssignedTableComposite extends BasicTableComposite {

	public AssignedTableComposite(Composite parent, int style) {
		super(parent, style, TableType.AssignedInfo);
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new AssignedTableLabelProvider();
	}

}

class AssignedTableLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		
		return null;
	}
	
}
