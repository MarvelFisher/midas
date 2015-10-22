package com.cyanspring.cstw.ui.rw.composite.table;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWUserStatisticsTableComposite extends BasicTableComposite {

	public RWUserStatisticsTableComposite(Composite parent, int style,
			TableType tableType) {
		super(parent, style, tableType);
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
