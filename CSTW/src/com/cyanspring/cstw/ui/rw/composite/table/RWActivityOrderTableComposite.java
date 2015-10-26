package com.cyanspring.cstw.ui.rw.composite.table;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.iservice.riskmgr.IOrderRecordService;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.common.TableType;
import com.cyanspring.cstw.ui.rw.composite.table.provider.ActivityOrderLabelProvider;

/**
 * @author Junfeng
 * @create 26 Oct 2015
 */
public class RWActivityOrderTableComposite extends BasicTableComposite {
	
	private IOrderRecordService service;
	
	public RWActivityOrderTableComposite(Composite parent, int style,
			IOrderRecordService service) {
		super(parent, style, TableType.RWActivityOrder);
		this.service = service;
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new ActivityOrderLabelProvider();
	}

}
