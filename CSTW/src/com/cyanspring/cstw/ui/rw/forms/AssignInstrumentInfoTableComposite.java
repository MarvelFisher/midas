/**
 * 
 */
package com.cyanspring.cstw.ui.rw.forms;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.model.admin.InstrumentPoolModel;
import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.common.TableType;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;
import com.cyanspring.cstw.ui.utils.LTWTextUtils;

/**
 * @author marve_000
 *
 */
public class AssignInstrumentInfoTableComposite extends BasicTableComposite {
	
	private ISubPoolManageService service;
	private InstrumentPoolModel subPoolModel;
	
	private Action addAction;
	private Action delAction;
	
	public AssignInstrumentInfoTableComposite(Composite parent, ISubPoolManageService service, int style) {
		super(parent, style, TableType.RWAssignedInstrument);
		this.service = service;
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new AssignInstrumentInfoTableLabelProvider();
	}
	
	@Override
	protected void initBodyMenu(Menu menu) {
		final MenuItem item1 = new MenuItem(menu, SWT.PUSH);
		item1.setText("add...");
		item1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addAction.run();
			}
		});
		final MenuItem item2 = new MenuItem(menu, SWT.PUSH);
		item2.setText("delete");
		item2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				delAction.run();
			}
		});
		super.initBodyMenu(menu);
	}
	
	@Override
	protected void initOthers() {
		addAction = new Action() {
			@Override
			public void run() {
				NewInstrumentInfoModelDialog dialog = new NewInstrumentInfoModelDialog(getShell());
				if (TrayDialog.OK == dialog.open()) {
					InstrumentInfoModel model = dialog.getCreatedInstrumentModel();
					service.createNewInstrumentInfoModel(subPoolModel, model);
				}
				//tableViewer.refresh();
			}
		};
		
		delAction = new Action() {
			@Override
			public void run() {
				IStructuredSelection selected = (IStructuredSelection) tableViewer.getSelection();
				InstrumentInfoModel model = (InstrumentInfoModel) selected.getFirstElement();
				service.removeInstrumentInfoModel(subPoolModel, model);
				//tableViewer.refresh();
			}
		};
	}
	
	public void setSubPoolModel(InstrumentPoolModel subPoolModel) {
		this.subPoolModel = subPoolModel;
	}
	
	class NewInstrumentInfoModelDialog extends TrayDialog {
		private InstrumentInfoModel createdInstrumentModel;
		
		private Text txtSymbol;
		private Text txtQty;
		
		protected NewInstrumentInfoModelDialog(Shell shell) {
			super(shell);
		}
		
		@Override
		protected Point getInitialSize() {
			return new Point(400, 300);
		}
		
		@Override
		protected void configureShell(Shell newShell) {
			newShell.setText("add new instrument for " + subPoolModel.getName());
			super.configureShell(newShell);
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginWidth = 5;
			gridLayout.marginHeight = 5;
			gridLayout.verticalSpacing = 0;
			gridLayout.horizontalSpacing = 0;
			container.setLayout(gridLayout);
			
			GridData gridData = new GridData(SWT.RIGHT, SWT.FILL, true, false);
			gridData.widthHint = 200;
			gridData.heightHint = SWT.DEFAULT;
			
			Label lblSelectSymbol = new Label(container, SWT.NONE);
			lblSelectSymbol.setText("Input an instrument symbol:");
			txtSymbol = new Text(container, SWT.BORDER);
			txtSymbol.setLayoutData(gridData);
			createSpacer(container, 2);
			Label lblSelectQty = new Label(container, SWT.NONE);
			lblSelectQty.setText("Input allocate quanity:");
			txtQty = LTWTextUtils.createIntegerStyleText(container, SWT.BORDER);
			txtQty.setLayoutData(gridData);
			
			initListeners();
			
			return container;
		}
		
		private void initListeners() {
			txtSymbol.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					if(isEmptyInput(txtSymbol) || isEmptyInput(txtQty)) {
						getButton(OK).setEnabled(false);
					} else {
						getButton(OK).setEnabled(true);
					}
				}
			});		
			txtQty.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					if(isEmptyInput(txtSymbol) || isEmptyInput(txtQty)) {
						getButton(OK).setEnabled(false);
					} else {
						getButton(OK).setEnabled(true);
					}
				}
			});
		}
		
		private boolean isEmptyInput(Text txt) {
			return txt.getText() == null ? true : (txt.getText().equals(""));
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			getButton(OK).setEnabled(false);
		}
		
		private void createSpacer(Composite parent, int span) {
			Label spacer = new Label(parent, SWT.NONE);
			GridData gd = new GridData();
			gd.horizontalSpan = span;
			spacer.setLayoutData(gd);
		}
		
		public InstrumentInfoModel getCreatedInstrumentModel() {
			return createdInstrumentModel;
		}
		
		@Override
		protected void okPressed() {
			
			super.okPressed();
		}
		
	}

}



class AssignInstrumentInfoTableLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		InstrumentInfoModel model = (InstrumentInfoModel) element;
		switch (columnIndex) {
		case 0:
			return model.getSymbolId();
		case 1:
			return model.getSymbolName();
		case 2:
			return LTWStringUtils.doubleToString(model.getStockQuanity());	
		default:
			return "";
		}
	}
	
}

