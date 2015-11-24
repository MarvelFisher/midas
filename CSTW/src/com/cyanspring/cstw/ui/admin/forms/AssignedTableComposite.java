package com.cyanspring.cstw.ui.admin.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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

import com.cyanspring.common.account.UserGroup;
import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author Junfeng
 * @create 16 Nov 2015
 */
public class AssignedTableComposite extends BasicTableComposite {
	
	private ISubAccountManagerService service;
	private SubAccountModel subAccountModel;
	
	private Action addAction;
	private Action delAction;
	
	public AssignedTableComposite(Composite parent, ISubAccountManagerService service, int style) {
		super(parent, style, TableType.AssignedInfo);
		this.service = service;
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new AssignedTableLabelProvider(tableViewer);
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
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				IStructuredSelection selected = (IStructuredSelection) tableViewer.getSelection();
				AssignedModel model = (AssignedModel) selected.getFirstElement();
				List<AssignedModel> input = (List<AssignedModel>) tableViewer.getInput();
				int index = input.indexOf(model);
				NewAssignedModelDialog dialog = new NewAssignedModelDialog(getShell(), service, subAccountModel);
				dialog.open();
				AssignedModel newAssignedModel = dialog.getCreatedAssignModel();
				service.createNewAssignedModel(subAccountModel, newAssignedModel, index);
				tableViewer.refresh();
			}
		};
		delAction = new Action() {
			@Override
			public void run() {
				IStructuredSelection selected = (IStructuredSelection) tableViewer.getSelection();
				AssignedModel model = (AssignedModel) selected.getFirstElement();
				service.removeAssignedUser(subAccountModel, model);
				tableViewer.refresh();
			}
		};
	}

	public void setSubAccountModel(SubAccountModel subAccountModel) {
		this.subAccountModel = subAccountModel;
	}
	
	/**
	 * Tray Dialog for create new assignment for current subAccount
	 * @author Junfeng
	 * @create 19 Nov 2015
	 */
	class NewAssignedModelDialog extends TrayDialog {
		
		private ISubAccountManagerService service;
		private SubAccountModel subAccount;
		private AssignedModel createAssignedModel;
		
		private Combo comboSelectUser;
		private Text txtSelectUserRole;
		
		protected NewAssignedModelDialog(Shell shell, ISubAccountManagerService service, SubAccountModel model) {
			super(shell);
			this.service = service;
			this.subAccount = model;
		}
		
		@Override
		protected Point getInitialSize() {
			return new Point(400, 300);
		}
		
		@Override
		protected void configureShell(Shell newShell) {
			newShell.setText("add new assignment for " + subAccount.getName());
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
			lblSelectUser.setText("Select a user/group to assign: ");
			comboSelectUser = new Combo(container, SWT.NONE);
			comboSelectUser.setLayoutData(gridData);
			initCombContent();			
			createSpacer(container, 2);
			Label lblSelectUserRole = new Label(container, SWT.NONE);
			lblSelectUserRole.setText("User Role: ");
			txtSelectUserRole = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txtSelectUserRole.setLayoutData(gridData);
			txtSelectUserRole.setEditable(true);
			return container;
		}

		private void initCombContent() {
			final List<UserGroup> usrList = service.getAvailableAssigneeList(subAccountModel);
			List<String> selectList = new ArrayList<String>();
			for (UserGroup ug : usrList) {
				selectList.add(ug.getUser());
			}
			comboSelectUser.setItems(selectList.toArray(new String[0]));
			comboSelectUser.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = comboSelectUser.getSelectionIndex();
					txtSelectUserRole.setText(usrList.get(index).getRole().name());
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
			return createAssignedModel;
		}
		
		@Override
		protected void okPressed() {
			createAssignedModel = new AssignedModel.Builder()
					.userId(comboSelectUser.getText())
					.roleType(txtSelectUserRole.getText()).build();
			super.okPressed();
		}
		
	}
	
}



class AssignedTableLabelProvider extends DefaultLabelProviderAdapter {

	private TableViewer viewer;
	
	public AssignedTableLabelProvider(TableViewer tableViewer) {
		this.viewer = tableViewer;
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
