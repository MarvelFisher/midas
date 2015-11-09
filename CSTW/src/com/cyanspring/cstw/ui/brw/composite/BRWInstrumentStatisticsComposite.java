package com.cyanspring.cstw.ui.brw.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.riskmgr.IInstrumentStatisticsService;
import com.cyanspring.cstw.ui.basic.BasicComposite;
import com.cyanspring.cstw.ui.brw.composite.table.BRWInstrumentStatisticsTableComposite;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BRWInstrumentStatisticsComposite extends BasicComposite {
	
	private IInstrumentStatisticsService service;
	
	private BRWInstrumentStatisticsTableComposite tableComposite;
	
	private Label lblRealizedProfit;		// 实现盈利
	private Label lblConsiderartion;		// 总交易额
	private Label lblProductivity;			// 效率
	
	public BRWInstrumentStatisticsComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
		initQuery();
	}

	private void initComponent() {
		GridLayout grid = new GridLayout(1, false);
		grid.verticalSpacing = 0;
		grid.horizontalSpacing = 0;
		grid.marginWidth = 0;
		grid.marginHeight = 0;
		setLayout(grid);
		
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(6, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lbl1 = new Label(composite, SWT.NONE);
		lbl1.setAlignment(SWT.RIGHT);
		GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1 );
		lbl1.setLayoutData(gd1);
		lbl1.setText("Realized PNL: ");
		
		lblRealizedProfit = new Label(composite, SWT.NONE);
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		lblRealizedProfit.setLayoutData(gd2);
		
		Label lbl2 = new Label(composite, SWT.NONE);
		lbl2.setAlignment(SWT.RIGHT);
		lbl2.setLayoutData(gd1);
		lbl2.setText("Total Consideration: ");
		
		lblConsiderartion = new Label(composite, SWT.NONE);
		lblConsiderartion.setLayoutData(gd2);
		
		Label lbl3 = new Label(composite, SWT.NONE);
		lbl3.setAlignment(SWT.RIGHT);
		lbl3.setLayoutData(gd1);
		lbl3.setText("Productivity: ");
		
		lblProductivity = new Label(composite, SWT.NONE);
		lblProductivity.setLayoutData(gd2);
		
		tableComposite = new BRWInstrumentStatisticsTableComposite(this, SWT.NONE);
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableComposite.setLayoutData(gd_table);
	}

	private void initQuery() {
		service.queryInstrument();
	}

	@Override
	protected void processByType(RefreshEventType type) {
		if (type == RefreshEventType.RWInstrumentStatistics) {
			tableComposite.setInput(service.getInstrumentModelList());
			lblRealizedProfit.setText(service.getAllRealizedProfit());
			lblConsiderartion.setText(service.getAllConsideraion());
			lblProductivity.setText(service.getProductivity());
		}
	}

	@Override
	protected IBasicService createService() {
		service = ServiceFactory.createInstrumentStatisticsService();
		return service;
	}

}
