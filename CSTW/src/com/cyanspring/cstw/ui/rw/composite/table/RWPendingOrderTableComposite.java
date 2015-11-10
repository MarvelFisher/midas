package com.cyanspring.cstw.ui.rw.composite.table;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.model.riskmgr.RCOrderRecordModel;
import com.cyanspring.cstw.service.iservice.riskmgr.IOrderRecordService;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.common.TableType;
import com.cyanspring.cstw.ui.rw.composite.table.provider.PendingOrderLabelProvider;

/**
 * @author Junfeng
 * @create 26 Oct 2015
 */
public class RWPendingOrderTableComposite extends BasicTableComposite {
	
	private IOrderRecordService service;
	
	public RWPendingOrderTableComposite(Composite parent, int style,
			IOrderRecordService service) {
		super(parent, style, TableType.RWPendingOrder);
		this.service = service;
		initListener();
	}
	
	private void initListener() {
		tableViewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				RCOrderRecordModel model = (RCOrderRecordModel) getSelectedObject();
				if (model == null) {
					return;
				}
				if (!MessageDialog.openConfirm(getShell(), "Comfirm",
						"Are you sure to cancel the order?")) {
					return;
				}				
				service.cancelOrder(model.getOrderId());
			}
		});
	}
	
	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new PendingOrderLabelProvider();
	}

}
