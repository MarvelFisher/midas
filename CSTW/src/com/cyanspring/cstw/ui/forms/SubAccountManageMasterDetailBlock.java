package com.cyanspring.cstw.ui.forms;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

/**
 * @author Junfeng
 * @create 9 Nov 2015
 */
public class SubAccountManageMasterDetailBlock extends MasterDetailsBlock {
	
	private FormToolkit toolkit;
	private TreeViewer editTree;
	private Object rootList;
	
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		toolkit = managedForm.getToolkit();
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
		
		final SectionPart spart = new SectionPart(dataSection);
		managedForm.addPart(spart);
		
		GridData data = new GridData(SWT.LEFT, SWT.FILL, false, false);
		Tree memberTree = toolkit.createTree(sectionClient, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER );
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		data.widthHint = 200;
		memberTree.setLayoutData(data);
		editTree = new TreeViewer(memberTree);
		editTree.setContentProvider(new EditTreeContentProvider());
		editTree.setLabelProvider(new EditTreeLabelProvider());
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
		refreshTree();
		
		dataSection.setClient(sectionClient);
	}

	private void refreshTree() {
		editTree.setInput(rootList);		
		editTree.expandToLevel(3);
		editTree.refresh();
	}

	private void changeUiElementState() {
		// TODO Auto-generated method stub
		
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
