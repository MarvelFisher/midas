package com.cyanspring.cstw.ui.rw.forms;


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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.cyanspring.cstw.model.admin.InstrumentPoolModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;

/**
 * @author Junfeng
 * @create 24 Nov 2015
 */
public class SubPoolManageMasterDetailBlock extends MasterDetailsBlock {
	
	private ISubPoolManageService service;
	
	private FormToolkit toolkit;
	private TreeViewer ediTree;
	private SectionPart spart;
	private Section dataSection;
	private Menu treeMenu;
	
	private Button btnAddAcc;
	private Button btnAddPool;
	private Button btnDelete;
	private Button btnUp;
	private Button btnDown;
	
	private Action addAccAction;
	private Action addPoolAction;
	private Action delAction;
	private Action upAction;
	private Action downAction;
	
	
	public SubPoolManageMasterDetailBlock(ISubPoolManageService service) {
		this.service = service;
	}


	@Override
	protected void createMasterPart(IManagedForm managedForm, Composite parent) {
		toolkit = managedForm.getToolkit();
		initComponent(managedForm, parent);
		initListener(managedForm);
		initAction();
	}

	
	private void initComponent(final IManagedForm managedForm, Composite parent) {
		dataSection = toolkit.createSection(parent, Section.COMPACT | Section.TITLE_BAR);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 1;
		dataSection.setLayoutData(td);
		dataSection.setText("Edit Tree");
		
		Composite sectionClient = toolkit.createComposite(dataSection, SWT.WRAP);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		sectionClient.setLayout(gridLayout);
		
		createSpacer(toolkit, sectionClient, 2);
		spart = new SectionPart(dataSection);
		managedForm.addPart(spart);
		
		// create tree
		GridData data = null;
		Tree memberTree = toolkit.createTree(sectionClient, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER );
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		data.widthHint = 200;
		memberTree.setLayoutData(data);
		ediTree = new TreeViewer(memberTree);
		ediTree.setContentProvider(new EditTreeContentProvider());
		ediTree.setLabelProvider(new EditTreeLabelProvider());
		initTreeMenu(sectionClient);
		refreshTree();
		
		// create buttons
		Composite btnComposite = toolkit.createComposite(sectionClient, SWT.NONE);
		GridData btnData = new GridData(SWT.FILL, SWT.FILL, false, true);
		btnData.widthHint = 100;
		btnComposite.setLayoutData(btnData);
		GridLayout btnLayout = new GridLayout(1, false);
		btnLayout.marginWidth = 5;
		btnLayout.marginHeight = 5;
		btnLayout.verticalSpacing = 0;
		btnLayout.horizontalSpacing = 0;
		btnComposite.setLayout(btnLayout);
		
		btnData = new GridData(SWT.FILL, SWT.FILL, true, false);
//		btnAddAcc = toolkit.createButton(btnComposite, "Add Sub", SWT.NONE);
		btnAddPool = toolkit.createButton(btnComposite, "Add SubPool", SWT.NONE);
		btnDelete = toolkit.createButton(btnComposite, "Delete", SWT.NONE);
		btnAddPool.setLayoutData(btnData);
		btnDelete.setLayoutData(btnData);
		btnAddPool.setEnabled(false);
		btnDelete.setEnabled(false);
		
		toolkit.createLabel(btnComposite, "");
		
		btnUp = toolkit.createButton(btnComposite, "Up", SWT.NONE);
		btnDown = toolkit.createButton(btnComposite, "Down", SWT.NONE);
		btnUp.setLayoutData(btnData);
		btnDown.setLayoutData(btnData);
		btnUp.setEnabled(false);
		btnDown.setEnabled(false);
		
		dataSection.setClient(sectionClient);
	}

	
	private void initTreeMenu(final Composite sectionClient) {
		treeMenu = new Menu(sectionClient.getShell(), SWT.POP_UP);
		final MenuItem item1 = new MenuItem(treeMenu, SWT.PUSH);
		item1.setText("Add SubPool");
		item1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPoolAction.run();
			}
		});
		final MenuItem item2 = new MenuItem(treeMenu, SWT.PUSH);
		item2.setText("Delete");
		item2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				delAction.run();
			}
		});
		
		ediTree.getTree().setMenu(treeMenu);
		
	}


	private void initListener(final IManagedForm managedForm) {
		ediTree.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(spart, event.getSelection());
			}
		});
		ediTree.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				changeUiElementState();
			}

			
		});
	}

	private void initAction() {
		
	}
	
	private void changeUiElementState() {
		IStructuredSelection selected = (IStructuredSelection) ediTree.getSelection();
		if (selected.isEmpty()) {
			
		} else if (selected.size() == 1) {
			
		} else {
			
		}
	}
	
	private void refreshTree() {
		ediTree.setInput(null);
		ediTree.refresh();
	}

	private void createSpacer(FormToolkit toolkit, Composite parent, int span) {
		Label spacer = toolkit.createLabel(parent, "");
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.registerPage(SubAccountModel.class, new SubAccountDetailsPage(service));
		detailsPart.registerPage(InstrumentPoolModel.class, new SubPoolDetailsPage(service));
	}

	
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		// TODO Auto-generated method stub

	}

}
