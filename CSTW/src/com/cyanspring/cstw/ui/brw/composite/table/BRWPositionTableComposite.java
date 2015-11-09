package com.cyanspring.cstw.ui.brw.composite.table;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.brw.composite.table.provider.BRWPositionLabelProvider;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BRWPositionTableComposite extends BasicTableComposite {

	public BRWPositionTableComposite(Composite parent, int style) {
		super(parent, style, TableType.BWPosition);
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new BRWPositionLabelProvider();
	}

}
