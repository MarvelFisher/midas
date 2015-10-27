package com.cyanspring.cstw.ui.bw.composite.table;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.bw.composite.table.provider.BWPositionLabelProvider;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BWPositionTableComposite extends BasicTableComposite {

	public BWPositionTableComposite(Composite parent, int style) {
		super(parent, style, TableType.BWPosition);
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new BWPositionLabelProvider();
	}

}
