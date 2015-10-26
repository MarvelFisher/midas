package com.cyanspring.cstw.ui.rw.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.riskmgr.IUserStatisticsService;
import com.cyanspring.cstw.ui.basic.BasicComposite;
import com.cyanspring.cstw.ui.rw.composite.table.RWUserStatisticsTableComposite;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWUserStatisticsComposite extends BasicComposite {
	
	private IUserStatisticsService service;
	
	private RWUserStatisticsTableComposite tableComposite;
	
	public RWUserStatisticsComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
		initQuery();
	}

	private void initComponent() {
		setLayout(new FillLayout());
		tableComposite = new RWUserStatisticsTableComposite(this, SWT.NONE);
	}

	private void initQuery() {
		service.queryIndividualRecord();
	}

	@Override
	protected void processByType(RefreshEventType type) {
		if (type == RefreshEventType.RWUserStatistics) {
			tableComposite.setInput(service.getIndividualRecordModelList());
		}
	}

	@Override
	protected IBasicService createService() {
		service = ServiceFactory.createUserStatisticsService();
		return service;
	}

}
