package com.cyanspring.cstw.ui.rw.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.riskmgr.IInstrumentSummaryService;
import com.cyanspring.cstw.ui.basic.BasicComposite;
import com.cyanspring.cstw.ui.rw.composite.table.RWInstrumentSummaryTableComposite;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWInstrumentSummaryComposite extends BasicComposite {
	
	private IInstrumentSummaryService service;
	
	private RWInstrumentSummaryTableComposite tableComposite;
	
	public RWInstrumentSummaryComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
		initQuery();
	}

	private void initComponent() {
		setLayout(new FillLayout());
		tableComposite = new RWInstrumentSummaryTableComposite(this, SWT.NONE);
		
	}

	private void initQuery() {
		service.queryInstrument();
	}

	@Override
	protected void processByType(RefreshEventType type) {
		if ( type == RefreshEventType.RWInstrumentSummary) {
			tableComposite.setInput(service.getInstrumentModelList());
		}
	}

	@Override
	protected IBasicService createService() {
		service = ServiceFactory.createInstrumentSummaryService();
		return service;
	}

}
