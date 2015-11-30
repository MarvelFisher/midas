/**
 * 
 */
package com.cyanspring.cstw.ui.rw.forms;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author marve_000
 *
 */
public class AssignedPoolTableComposite extends BasicTableComposite {

	public AssignedPoolTableComposite(Composite parent, ISubPoolManageService service, int style) {
		super(parent, style, TableType.RWAssignedPool);
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new AssignedPoolTableLabelProvider();
	}

}

class AssignedPoolTableLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
