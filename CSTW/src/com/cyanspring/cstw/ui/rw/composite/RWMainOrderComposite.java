package com.cyanspring.cstw.ui.rw.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.riskmgr.IOrderRecordService;
import com.cyanspring.cstw.ui.basic.BasicComposite;
import com.cyanspring.cstw.ui.rw.composite.table.RWActivityOrderTableComposite;
import com.cyanspring.cstw.ui.rw.composite.table.RWPendingOrderTableComposite;

/**
 * @author Junfeng
 * @create 26 Oct 2015
 */
public class RWMainOrderComposite extends BasicComposite {
	
	private IOrderRecordService service;
	
	private RWPendingOrderTableComposite pendingOrderTable;
	private RWActivityOrderTableComposite activityOrderTable;
	
	public RWMainOrderComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
		initQuery();
	}

	private void initComponent() {
		setLayout(new GridLayout(1, false));

		Label lblPending = new Label(this, SWT.NONE);
		lblPending.setText("Pending Order: ");

		pendingOrderTable = new RWPendingOrderTableComposite(this, SWT.NONE, service);
		GridData gd_pendingOrderHistoryComposite = new GridData(SWT.FILL,
				SWT.FILL, true, true, 1, 1);
		pendingOrderTable.setLayoutData(gd_pendingOrderHistoryComposite);

		Label lblActivity = new Label(this, SWT.NONE);
		lblActivity.setText("Active Log: ");

		activityOrderTable = new RWActivityOrderTableComposite(this, SWT.NONE, service);
		GridData gd_activityOrderHistoryComposite = new GridData(SWT.FILL,
				SWT.FILL, true, true, 1, 1);
		activityOrderTable.setLayoutData(gd_activityOrderHistoryComposite);
		
	}

	private void initQuery() {
		service.queryOrder();
	}

	@Override
	protected void processByType(RefreshEventType type) {
		if (type == RefreshEventType.RWOrderRecordList) {
			pendingOrderTable.setInput(service.getPendingOrderList());
			activityOrderTable.setInput(service.getActivityOrderList());
		}
	}

	@Override
	protected IBasicService createService() {
		service = ServiceFactory.createOrderRecordService();
		return service;
	}

}
