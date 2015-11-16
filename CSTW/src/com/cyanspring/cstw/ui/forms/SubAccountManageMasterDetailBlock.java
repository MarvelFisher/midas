package com.cyanspring.cstw.ui.forms;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
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
	
	private Button btnAdd;
	private Button btnDelete;
	private Button btnUp;
	private Button btnDown;
	
	private Action addAction;
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
		
		GridData data = new GridData(SWT.LEFT, SWT.FILL, false, false);
		Tree memberTree = toolkit.createTree(sectionClient, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER );
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		data.widthHint = 200;
		memberTree.setLayoutData(data);
		editTree = new TreeViewer(memberTree);
		editTree.setContentProvider(new EditTreeContentProvider(service));
		editTree.setLabelProvider(new EditTreeLabelProvider());
		
		refreshTree();
		
		// create buttons
		Composite btnComposite = toolkit.createComposite(sectionClient, SWT.NONE);
		GridData btnData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		btnComposite.setLayoutData(btnData);
		GridLayout btnLayout = new GridLayout();
		btnComposite.setLayout(btnLayout);
		
		btnData = new GridData(SWT.CENTER, SWT.FILL, false, false);
		btnAdd = toolkit.createButton(parent, "Add", SWT.NONE);
		btnDelete = toolkit.createButton(parent, "Delete", SWT.NONE);
		btnAdd.setEnabled(false);
		btnDelete.setEnabled(false);
		btnAdd.setLayoutData(btnData);
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
		
		btnAdd.addSelectionListener(new SelectionAdapter() {
		});
		
		btnDelete.addSelectionListener(new SelectionAdapter() {
		});
		
		btnUp.addSelectionListener(new SelectionAdapter() {
		});
		
		btnDown.addSelectionListener(new SelectionAdapter() {
		});
		
	}
	
	private void initAction() {
		addAction = new Action() {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection)editTree.getSelection()).getFirstElement();
				if (obj instanceof ExchangeAccountModel) {
//					service
				} else if (obj instanceof SubAccountModel) {
					
				}
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
			btnAdd.setEnabled(false);
			btnDelete.setEnabled(false);
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
		} else if (selected.size() == 1) {
			btnAdd.setEnabled(true);
			btnDelete.setEnabled(true);
			Object object = selected.getFirstElement();
			
		} else {
			btnAdd.setEnabled(false);
			btnDelete.setEnabled(true);
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
