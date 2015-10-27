package com.cyanspring.cstw.ui.bw.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.riskmgr.ITradeRecordService;
import com.cyanspring.cstw.ui.basic.BasicComposite;
import com.cyanspring.cstw.ui.bw.composite.table.BWTradeRecordTableComposite;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BWTradeRecordComposite extends BasicComposite {
	
	private ITradeRecordService service;
	
	private BWTradeRecordTableComposite tableComposite;
	
	public BWTradeRecordComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
		initQuery();
	}

	private void initComponent() {
		setLayout(new FillLayout());
		tableComposite = new BWTradeRecordTableComposite(this, SWT.NONE);
	}

	private void initQuery() {
		service.queryTradeRecord();
	}

	@Override
	protected void processByType(RefreshEventType type) {
		if (type == RefreshEventType.RWTradeRecordList) {
			tableComposite.setInput(service.getTradeRecordModelList());
		}
	}

	@Override
	protected IBasicService createService() {
		service = ServiceFactory.createTradeRecordService();
		return service;
	}

}
