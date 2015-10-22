package com.cyanspring.cstw.ui.basic;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.IUiListener;



/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/22
 *	moved from Project S
 */
public abstract class BasicComposite extends Composite {

	private IBasicService basicService;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public BasicComposite(Composite parent, int style) {
		super(parent, style);
		initService();
		initListener();
	}

	private void initService() {
		//basicService = createService();
	}

	private void initListener() {
		if (basicService != null) {
			basicService.setRefreshListener(new IUiListener() {
				@Override
				public void refreshByType(final RefreshEventType type) {
					BasicComposite.this.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							processByType(type);
						}
					});
				}

				@Override
				public void handleErrorMessage(final String errorMessage) {
					BasicComposite.this.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openConfirm(BasicComposite.this.getShell(), "Confirm", errorMessage);							
						}
					});

				}
			});

			this.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					basicService.clear();
					basicService = null;
				}
			});
		}
	}

	protected abstract void processByType(RefreshEventType type);

	protected abstract IBasicService createService();

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
