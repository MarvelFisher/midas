package com.cyanspring.cstw.ui.rw.forms;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

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
		initTreeMenu();
		refreshTree();
		
		// create buttons
		Composite btnComposite = toolkit.createComposite(sectionClient, SWT.NONE);
		GridData btnData = new GridData(SWT.FILL, SWT.FILL, false, true);
		btnData.widthHint = SWT.DEFAULT;
		btnData.heightHint = SWT.DEFAULT;
		btnComposite.setLayoutData(btnData);
		GridLayout btnLayout = new GridLayout(1, false);
		btnLayout.marginWidth = 5;
		btnLayout.marginHeight = 5;
		btnLayout.verticalSpacing = 0;
		btnLayout.horizontalSpacing = 0;
		btnComposite.setLayout(btnLayout);
		
		btnData = new GridData(SWT.FILL, SWT.FILL, false, true);
//		btnAddAcc = toolkit.createButton(btnComposite, "Add Sub", SWT.NONE);
		btnAddPool = toolkit.createButton(btnComposite, "Add SubPool", SWT.NONE);
		btnDelete = toolkit.createButton(btnComposite, "Delete", SWT.NONE);
		btnAddPool.setLayoutData(btnData);
		btnDelete.setLayoutData(btnData);
		btnAddPool.setEnabled(false);
		btnDelete.setEnabled(false);
		
		
		dataSection.setClient(sectionClient);
	}

	private void refreshTree() {
		// TODO Auto-generated method stub
		
	}


	private void initTreeMenu() {
		// TODO Auto-generated method stub
		
	}


	private void initListener(IManagedForm managedForm) {
		// TODO Auto-generated method stub

	}

	private void initAction() {
		// TODO Auto-generated method stub

	}
	
	private void createSpacer(FormToolkit toolkit, Composite parent, int span) {
		Label spacer = toolkit.createLabel(parent, "");
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		// TODO Auto-generated method stub

	}

	
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		// TODO Auto-generated method stub

	}

}
