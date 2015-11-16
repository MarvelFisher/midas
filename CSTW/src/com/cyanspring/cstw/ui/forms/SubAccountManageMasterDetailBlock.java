package com.cyanspring.cstw.ui.forms;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 9 Nov 2015
 */
public class SubAccountManageMasterDetailBlock extends MasterDetailsBlock {
	
	private ISubAccountManagerService service;
	
	private FormToolkit toolkit;
	private TreeViewer editTree;
	private SectionPart spart;
	
	private Button btnAddExch;
	private Button btnAddSub;
	private Button btnDelete;
	private Button btnUp;
	private Button btnDown;
	
	private Action addExchAction;
	private Action addSubAction;
	private Action delAction;
	private Action upAction;
	private Action downAction;
	
	public SubAccountManageMasterDetailBlock(ISubAccountManagerService service) {
		this.service = service;
	}
	
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		toolkit = managedForm.getToolkit();
		initComponent(managedForm, parent);
		initListener(managedForm);
		initAction();
	}

	private void initComponent(final IManagedForm managedForm, Composite parent) {
		Section dataSection = toolkit.createSection(parent, Section.COMPACT | Section.TITLE_BAR | Section.EXPANDED);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 1;
		dataSection.setLayoutData(td);
		dataSection.setText("Edit Tree");
		
		Composite sectionClient = toolkit.createComposite(dataSection, SWT.WRAP);
		GridLayout clientLayout = new GridLayout();
		clientLayout.numColumns = 2;
		sectionClient.setLayout(clientLayout);
		
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		Label label = toolkit.createLabel(sectionClient, "");
		label.setLayoutData(gd);
		
		spart = new SectionPart(dataSection);
		managedForm.addPart(spart);
		
		GridData data = null;
		Tree memberTree = toolkit.createTree(sectionClient, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER );
		data = new GridData(SWT.FILL, SWT.FILL, false, true);
		data.heightHint = 200;
		data.widthHint = 200;
		memberTree.setLayoutData(data);
		editTree = new TreeViewer(memberTree);
		editTree.setContentProvider(new EditTreeContentProvider(service));
		editTree.setLabelProvider(new EditTreeLabelProvider());
		
		refreshTree();
		
		// create buttons
		Composite btnComposite = toolkit.createComposite(sectionClient, SWT.NONE);
		GridData btnData = new GridData(SWT.FILL, SWT.FILL, false, true);
		btnData.widthHint = 100;
		btnComposite.setLayoutData(btnData);
		GridLayout btnLayout = new GridLayout();
		btnComposite.setLayout(btnLayout);
		
		btnData = new GridData(SWT.FILL, SWT.FILL, true, false);
		btnAddExch = toolkit.createButton(btnComposite, "Add Exchange", SWT.NONE);
		btnAddSub = toolkit.createButton(btnComposite, "Add Sub", SWT.NONE);
		btnDelete = toolkit.createButton(btnComposite, "Delete", SWT.NONE);
		btnAddExch.setEnabled(true);
		btnAddSub.setEnabled(false);
		btnDelete.setEnabled(false);
		btnAddExch.setLayoutData(btnData);
		btnAddSub.setLayoutData(btnData);
		btnDelete.setLayoutData(btnData);
		
		toolkit.createLabel(btnComposite, "");
		
		btnUp = toolkit.createButton(btnComposite, "Up", SWT.NONE);
		btnDown = toolkit.createButton(btnComposite, "Down", SWT.NONE);
		btnUp.setEnabled(false);
		btnDown.setEnabled(false);
		btnUp.setLayoutData(btnData);
		btnDown.setLayoutData(btnData);
		
		
		dataSection.setClient(sectionClient);
	}
	
	private void initListener(final IManagedForm managedForm) {
		// register SelectionListener
		editTree.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(spart, event.getSelection());
			}
		});

		editTree.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				changeUiElementState();
			}
		});
		
		btnAddExch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addExchAction.run();
			}
		});
		
		btnAddSub.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSubAction.run();
			}
		});
		
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				delAction.run();
			}
		});
		
		btnUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
			}
		});
		
		btnDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
			}
		});
		
	}
	
	private void initAction() {
		addExchAction = new Action() {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection)editTree.getSelection()).getFirstElement();
				if (obj instanceof ExchangeAccountModel) {
					service.createNewExchangeAccount();
				}
				refreshTree();
			}
		};
		
		addSubAction = new Action() {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) editTree.getSelection())
						.getFirstElement();
				if (obj instanceof SubAccountModel) {
					SubAccountModel subAccountModel = (SubAccountModel) obj;
					service.createNewSubAccount(subAccountModel
							.getExchangeAccountModel().getName());
				}
				refreshTree();
			}
		};
		
		delAction = new Action() {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) editTree.getSelection())
						.getFirstElement();
				if (obj instanceof ExchangeAccountModel) {
					service.removeExchangeAccount((ExchangeAccountModel) obj);
				} else if (obj instanceof SubAccountModel) {
					service.removeSubAccount((SubAccountModel) obj);
				}
			}
		};
		
		upAction = new Action() {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) editTree.getSelection())
						.getFirstElement();
			}
		};
		
		downAction = new Action() {
			@Override
			public void run() {
				
			}
		};
		
	}

	private void refreshTree() {
		editTree.setInput(service.getExchangeAccountList());		
		editTree.expandToLevel(3);
		editTree.refresh();
	}

	private void changeUiElementState() {
		IStructuredSelection selected = ((IStructuredSelection) editTree.getSelection());
		if (selected.isEmpty()) {
			btnAddExch.setEnabled(true);
			btnAddSub.setEnabled(false);
			btnDelete.setEnabled(false);
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
		} else if (selected.size() == 1) {
			btnDelete.setEnabled(true);
			Object object = selected.getFirstElement();
			if (object instanceof ExchangeAccountModel) {
				btnAddExch.setEnabled(true);
				btnAddSub.setEnabled(true);
				int index = service.getExchangeAccountList().indexOf(object);
				setArrowButtonsFlag(index, 0, service.getExchangeAccountList().size()-1);
			} else if (object instanceof SubAccountModel) {
				btnAddExch.setEnabled(false);
				btnAddSub.setEnabled(true);
				TreeItem treeItem = editTree.getTree().getSelection()[0];
				TreeItem parent = treeItem.getParentItem();
				if (parent != null) {
					TreeItem[] children = parent.getItems();
					setArrowButtonsFlag(parent.indexOf(treeItem), 0, children.length-1);
				}
			}
			
		} else {
			btnAddExch.setEnabled(true);
			btnDelete.setEnabled(false);
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
		}
	}

	private void setArrowButtonsFlag(int index, int begin, int end) {
		if (begin < end) {
			// not found
			if (index == -1) {
				btnUp.setEnabled(false);
				btnDown.setEnabled(false);
			}
			else if (index == begin) {
				btnUp.setEnabled(false);
				btnDown.setEnabled(true);
			}
			else if (index == end) {
				btnUp.setEnabled(true);
				btnDown.setEnabled(false);
			}
			else {
				btnUp.setEnabled(true);
				btnDown.setEnabled(true);
			}
		} else {
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
		}
		
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.registerPage(ExchangeAccountModel.class, new ExchangeAccountDetailsPage(service));
		detailsPart.registerPage(SubAccountModel.class, new SubAccountDetailsPage(service));
	}

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		// Do Nothing
	}

}
