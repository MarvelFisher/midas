/**
 * 
 */
package com.cyanspring.cstw.ui.rw.forms;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author marve_000
 *
 */
public class AssignedPoolTableComposite extends BasicTableComposite {

	public AssignedPoolTableComposite(Composite parent, ISubPoolManageService service, int style) {
		super(parent, style, TableType.AssignedInfo);
		// TODO Auto-generated constructor stub
	}


	/* (non-Javadoc)
	 * @see com.cyanspring.cstw.ui.basic.BasicTableComposite#createLabelProvider()
	 */
	@Override
	protected IBaseLabelProvider createLabelProvider() {
		// TODO Auto-generated method stub
		return null;
	}

}
