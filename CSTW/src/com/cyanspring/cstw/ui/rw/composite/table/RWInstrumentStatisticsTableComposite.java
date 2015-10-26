package com.cyanspring.cstw.ui.rw.composite.table;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.common.TableType;
import com.cyanspring.cstw.ui.rw.composite.table.provider.RWInstrumentStatisticsLabelProvider;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWInstrumentStatisticsTableComposite extends BasicTableComposite {

	public RWInstrumentStatisticsTableComposite(Composite parent, int style) {
		super(parent, style, TableType.RWInstrumentStatistics);
		
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new RWInstrumentStatisticsLabelProvider();
	}

}
