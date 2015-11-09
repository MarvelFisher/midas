package com.cyanspring.cstw.ui.brw.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.cyanspring.cstw.ui.rw.composite.RWInstrumentSummaryComposite;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BRWMainDataComposite extends Composite {
	
	private TabItem tbtmPosition;
	private TabItem tbtmRecord;
	private TabItem tbtmInstrument;
	private TabItem tbtmInstrumentSummary;
	
	private TabFolder tabFolder;
	
	public BRWMainDataComposite(Composite parent, int style) {
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
		
		tbtmInstrumentSummary = new TabItem(tabFolder, SWT.NONE);
		
		tbtmInstrumentSummary.setText("Instrument Summary");
		
	}
	
	private void initComposite() {
		BRWPositionComposite position = new BRWPositionComposite(tabFolder, SWT.NONE);
		tbtmPosition.setControl(position);
		
		BRWTradeRecordComposite tradeRecord = new BRWTradeRecordComposite(tabFolder, SWT.NONE);
		tbtmRecord.setControl(tradeRecord);
		
		BRWInstrumentStatisticsComposite instrumentStatistics = new BRWInstrumentStatisticsComposite(tabFolder, SWT.NONE);
		tbtmInstrument.setControl(instrumentStatistics);
		
		RWInstrumentSummaryComposite instrumentSummary = new RWInstrumentSummaryComposite(tabFolder, SWT.NONE);
		tbtmInstrumentSummary.setControl(instrumentSummary);
		
	}

}
