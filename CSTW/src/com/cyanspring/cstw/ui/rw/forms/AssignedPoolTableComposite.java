/**
 * 
 */
package com.cyanspring.cstw.ui.rw.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.InstrumentPoolModel;
import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author marve_000
 *
 */
public class AssignedPoolTableComposite extends BasicTableComposite {
	private ISubPoolManageService service;
	private InstrumentPoolModel subPoolModel;
	
	private Action addAction;
	private Action delAction;
	
	public AssignedPoolTableComposite(Composite parent, ISubPoolManageService service, int style) {
		super(parent, style, TableType.RWAssignedPool);
		this.service = service;
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new AssignedPoolTableLabelProvider(tableViewer);
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
				// TODO Auto-generated method stub
				super.run();
			}
		};
		
		delAction = new Action() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
			}
			
		};
	}
	
	public void setSubPoolModel(InstrumentPoolModel subPool) {
		this.subPoolModel = subPool;
	}
	
	class NewAssignedPoolDialog extends TrayDialog {
		
		private Combo combo;
		private Text txt;
		
		private AssignedModel createdAssignedModel;
		
		protected NewAssignedPoolDialog(Shell shell) {
			super(shell);
		}
		
		@Override
		protected Point getInitialSize() {
			return new Point(400, 300);
		}
		
		@Override
		protected void configureShell(Shell newShell) {
			newShell.setText("add assign for " + subPoolModel.getName());
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
			
			Label lblSelectUser = new Label(container, SWT.NONE);
			lblSelectUser.setText("Select a user to assign: ");
			
			combo = new Combo(container, SWT.NONE);
			combo.setLayoutData(gridData);
			initComboContent();
			createSpacer(container, 2);
			Label lblSelectUserRole = new Label(container, SWT.NONE);
			lblSelectUserRole.setText("User Role: ");
			txt = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txt.setLayoutData(gridData);
			txt.setEditable(false);
			return container;
		}
		
		private void initComboContent() {
			final List<Account> usrList = service.getAvailableAssigneeList(subPoolModel);
			List<String> selectList = new ArrayList<String>();
			for(Account u : usrList) {
				selectList.add(u.getId());
			}
			combo.setItems(selectList.toArray(new String[0]));
			combo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					txt.setText("Trader");
				}
			});
		}
		
		private void createSpacer(Composite parent, int span) {
			Label spacer = new Label(parent, SWT.NONE);
			GridData gd = new GridData();
			gd.horizontalSpan = span;
			spacer.setLayoutData(gd);
		}
		
		public AssignedModel getCreatedAssignModel() {
			return createdAssignedModel;
		}
		
		@Override
		protected void okPressed() {
			createdAssignedModel = new AssignedModel.Builder()
					.userId(combo.getText()).roleType(txt.getText()).build();
			super.okPressed();
		}
		
	}

}

class AssignedPoolTableLabelProvider extends DefaultLabelProviderAdapter {
	
	private TableViewer viewer;
	
	public AssignedPoolTableLabelProvider(TableViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		AssignedModel model = (AssignedModel) element;
		switch (columnIndex) {
		case 0:
			if (viewer != null) {
				List<?> input = (List<?>) viewer.getInput();
				int index = input.indexOf(element);
				return "" + (index + 1);
			}
			return "";
		case 1:
			return model.getUserId();
		case 2: 
			return model.getRoleType();
		default:
			return "";
		}
	}
	
}
