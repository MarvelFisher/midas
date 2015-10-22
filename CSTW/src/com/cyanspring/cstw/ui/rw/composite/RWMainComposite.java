package com.cyanspring.cstw.ui.rw.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWMainComposite extends Composite {
	
	private TabItem tbtmPosition;
	private TabItem tbtmRecord;
	private TabItem tbtmInstrument;
	private TabItem tbtmIndividual;
	private TabItem tbtmInstrumentSummary;
	private TabFolder tabFolder;
	
	public RWMainComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		initTabItem();
		initComposite();
	}
	
	private void initTabItem() {
		tabFolder = new TabFolder(this, SWT.NONE);
		
		tbtmPosition = new TabItem(tabFolder, SWT.NONE);
		
		tbtmPosition.setText("Current Position");
		
		tbtmRecord = new TabItem(tabFolder, SWT.NONE);
		
		tbtmRecord.setText("Trade Record");
		
		tbtmInstrument = new TabItem(tabFolder, SWT.NONE);
		
		tbtmInstrument.setText("Instrument Statistics");
		
		tbtmIndividual = new TabItem(tabFolder, SWT.NONE);
		
		tbtmIndividual.setText("User Statistics");
		
		tbtmInstrumentSummary = new TabItem(tabFolder, SWT.NONE);
		
		tbtmInstrumentSummary.setText("Instrument Summary");
		
	}
	
	private void initComposite() {
		
		RWPositionComposite position = new RWPositionComposite(tabFolder, SWT.NONE);
		tbtmPosition.setControl(position);
		
		RWTradeRecordComposite trade = new RWTradeRecordComposite(tabFolder, SWT.NONE);
		tbtmRecord.setControl(trade);
		
		RWInstrumentStatisticsComposite instrumentStatistics = new RWInstrumentStatisticsComposite(tabFolder, SWT.NONE);
		tbtmInstrument.setControl(instrumentStatistics);
		
		RWUserStatisticsComposite user = new RWUserStatisticsComposite(tabFolder, SWT.NONE);
		tbtmIndividual.setControl(user);
		
		RWInstrumentSummaryComposite instrumentSummary = new RWInstrumentSummaryComposite(tabFolder, SWT.NONE);
		tbtmInstrumentSummary.setControl(instrumentSummary);
		
	}

}
