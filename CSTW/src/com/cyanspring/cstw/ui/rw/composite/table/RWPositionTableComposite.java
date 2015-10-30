package com.cyanspring.cstw.ui.rw.composite.table;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.cstw.service.iservice.riskmgr.ICurrentPositionService;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.common.TableType;
import com.cyanspring.cstw.ui.rw.composite.table.provider.RWPositionLabelProvider;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWPositionTableComposite extends BasicTableComposite {
	private static final Logger log = LoggerFactory
			.getLogger(RWPositionTableComposite.class);
	
	private ICurrentPositionService service;
	
	public RWPositionTableComposite(Composite parent, int style, ICurrentPositionService service) {
		super(parent, style, TableType.RWPosition);
		this.service = service;
		initListener();
	}
	
	private void initListener() {
		tableViewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (!MessageDialog.openConfirm(getShell(), "Comfirm",
						"Are you sure to close the position?")) {
					return;
				}
				RCOpenPositionModel position = (RCOpenPositionModel) getSelectedObject();				
				log.info(
						"Close Position: Account:{}, Symbol:{}, Qty:{}, AcPnl:{}, Price:{}",
						new Object[] { position.getTrader(),
								position.getInstrumentCode(),
								position.getInstrumentQuality(),
								position.getPnl(), position.getAveragePrice() });				
				service.forceClosePosition(position.getTrader(), position.getInstrumentCode());
			}
		});
	}
	
	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new RWPositionLabelProvider();
	}

}
