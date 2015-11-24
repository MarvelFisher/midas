package com.cyanspring.cstw.ui.admin.forms;


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

import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class SubAccountDetailsPage implements IDetailsPage {
	
	private static final int TEXT_WIDTH_HINT = 150;
	
	private SubAccountModel input;
	private ISubAccountManagerService service;
	
	private IManagedForm mform;
	private Section dataSection1;
	private Section dataSection2;
	private Text txtName1;
	private Text txtName2;
	private InstrumentInfoTableComposite exTableComposite;
	private InstrumentInfoTableComposite subTableComposite;
	private AssignedTableComposite assTableComposite;
	
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
		FormToolkit toolkit = mform.getToolkit();
		
		//init Top Part
		dataSection1 = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		dataSection1.marginWidth = 10;
		dataSection1.setText("Exchange Account Details: ");
		
		TableWrapData td1 = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td1.grabHorizontal = true;
		dataSection1.setLayoutData(td1);
		
		Composite client1 = toolkit.createComposite(dataSection1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 0;
		glayout.numColumns = 2;
		client1.setLayout(glayout);
		createSpacer(toolkit, client1, 2);
		createComponent1(toolkit, client1);
		toolkit.paintBordersFor(dataSection1);
		dataSection1.setClient(client1);
		
		// init Bottom Part
		dataSection2 = toolkit.createSection(parent,  Section.TITLE_BAR | Section.EXPANDED);
		dataSection2.marginWidth = 10;
		dataSection2.setText("SubAccount Details: ");
		TableWrapData td2 = new TableWrapData(TableWrapData.FILL, TableWrapData.BOTTOM);
		td2.grabHorizontal = true;
		dataSection2.setLayoutData(td2);
		
		Composite client2 = toolkit.createComposite(dataSection2);
		GridLayout gLayout2 = new GridLayout();
		gLayout2.marginWidth = gLayout2.marginHeight = 0;
		gLayout2.numColumns = 2;
		client2.setLayout(gLayout2);
		createSpacer(toolkit, client2, 2);
		createComponent2(toolkit, client2);
		toolkit.paintBordersFor(dataSection2);
		dataSection2.setClient(client2);
	}
	
	private void createComponent1(FormToolkit toolkit, Composite client) {
		toolkit.createLabel(client, "Name: ");
		txtName1 = toolkit.createText(client, "", SWT.BORDER);
		GridData gd1 = new GridData();
		gd1.widthHint = TEXT_WIDTH_HINT;
		txtName1.setLayoutData(gd1);
		txtName1.setEditable(true);
		txtName1.setTextLimit(1023);
		
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
	
	private void createComponent2(FormToolkit toolkit, Composite client) {
		toolkit.createLabel(client, "Name: ");
		txtName2 = toolkit.createText(client, "", SWT.BORDER);
		GridData gd1 = new GridData();
		gd1.widthHint = TEXT_WIDTH_HINT;
		txtName2.setLayoutData(gd1);
		txtName2.setEditable(true);
		txtName2.setTextLimit(1023);
		
		createSpacer(toolkit, client, 2);
		
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
		gd2.horizontalSpan = 2;
		Label lblTable = toolkit.createLabel(client, "Summary of symbol: ");
		lblTable.setLayoutData(gd2);
		subTableComposite = new InstrumentInfoTableComposite(client, SWT.NONE);
		GridData gd3 = new GridData(); //SWT.LEFT, SWT.FILL, false, true
		gd3.horizontalSpan = 2;
		subTableComposite.setLayoutData(gd3);
		toolkit.adapt(subTableComposite);
		
		createSpacer(toolkit, client, 2);
		
		Label lblAssign = toolkit.createLabel(client, "Assign to RW / Group: ");
		lblAssign.setLayoutData(gd2);
		assTableComposite = new AssignedTableComposite(client, service, SWT.NONE);
		GridData gd4 = new GridData(); //SWT.LEFT, SWT.FILL, false, true
		gd4.horizontalSpan = 2;
		assTableComposite.setLayoutData(gd4);
		toolkit.adapt(assTableComposite);
		
	}

	private void createSpacer(FormToolkit toolkit, Composite parent, int span) {
		Label spacer = toolkit.createLabel(parent, "");
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void commit(boolean onSave) {

	}

	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	@Override
	public void setFocus() {

	}

	@Override
	public boolean isStale() {
		return false;
	}

	@Override
	public void refresh() {
		update();
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
			txtName1.setText(input.getExchangeAccountName());
			exTableComposite.setInput(service.getInstrumentInfoModelListByExchangeAccountId(input.getRelativeExchAccount()));
			
			txtName2.setText(input.getName());
			subTableComposite.setInput(service.getInstrumentInfoModelListBySubAccountId(input.getId()));
			
			assTableComposite.setSubAccountModel(input);
			assTableComposite.setInput(service.getAssignedModelListBySubAccountId(input.getId()));
		}
	}
	

}
