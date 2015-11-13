package com.cyanspring.cstw.ui.forms;


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.omg.CORBA.PRIVATE_MEMBER;

import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class SubAccountDetailsPage implements IDetailsPage {
	
	private SubAccountModel input;
	private ISubAccountManagerService service;
	
	private IManagedForm mform;
	private Section dataSection1;
	private Section dataSection2;
	private Text txtName;
	private InstrumentInfoTableComposite exTableComposite;
	
	public SubAccountDetailsPage(ISubAccountManagerService service) {
		this.service = service;
	}
	
	@Override
	public void initialize(IManagedForm form) {
		this.mform = form;
	}
	
	@Override
	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 5;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);
		
		//init head
		FormToolkit toolkit = mform.getToolkit();
		dataSection1 = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		dataSection1.marginWidth = 10;
		dataSection1.setText("Exchange Account Details: ");
		
		TableWrapData td1 = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td1.grabHorizontal = true;
		dataSection1.setLayoutData(td1);
		
		Composite client = toolkit.createComposite(dataSection1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 0;
		glayout.numColumns = 2;
		client.setLayout(glayout);
		
		createSpacer(toolkit, client, 2);
		
		createComponent(toolkit, client);
		
		toolkit.paintBordersFor(dataSection1);
		dataSection1.setClient(client);
		
		dataSection2 = toolkit.createSection(parent,  Section.TITLE_BAR | Section.EXPANDED);
		dataSection2.marginWidth = 10;
		dataSection2.setText("SubAccount Details: ");
		TableWrapData td2 = new TableWrapData(TableWrapData.FILL, TableWrapData.BOTTOM);
		td2.grabHorizontal = true;
		dataSection2.setLayoutData(td2);
		
		toolkit.paintBordersFor(dataSection2);
		//dataSection2.setClient(client);
	}
	
	private void createComponent(FormToolkit toolkit, Composite client) {
		toolkit.createLabel(client, "Name: ");
		txtName = toolkit.createText(client, "", SWT.BORDER);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
		txtName.setLayoutData(gd1);
		txtName.setEditable(true);
		txtName.setTextLimit(1023);
		
		createSpacer(toolkit, client, 2);
		
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
		gd2.horizontalSpan = 2;
		Label lblTable = toolkit.createLabel(client, "Summary of symbol: ");
		lblTable.setLayoutData(gd2);
		exTableComposite = new InstrumentInfoTableComposite(client, SWT.NONE);
		GridData gd3 = new GridData();
		gd3.horizontalSpan = 2;
		exTableComposite.setLayoutData(gd3);
		toolkit.adapt(exTableComposite);
		
	}

	private void createSpacer(FormToolkit toolkit, Composite parent, int span) {
		Label spacer = toolkit.createLabel(parent, "");
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void commit(boolean onSave) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setFormInput(Object input) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStale() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection selected = (IStructuredSelection) selection; 
		if ( selected.size() == 1 ) {
			input = (SubAccountModel) selected.getFirstElement();
		} else {
			input = null;
		}
		update();

	}
	
	private void update() {
		if ( input != null ) {
			dataSection1.setText("Exchange Account Details: " + input.getName());
			txtName.setText(input.getName());
			exTableComposite.setInput(service.getInstrumentInfoModelListByExchangeAccountName(input.getName()));
		}
	}
	

}
