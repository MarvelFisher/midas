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

import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class ExchangeAccountDetailsPage implements IDetailsPage {

	private static final int TEXT_WIDTH_HINT = 150;
	
	private ExchangeAccountModel input;
	private ISubAccountManagerService service;
	
	private IManagedForm mform;
	private Section dataSection;
	private Text txtName;
	private InstrumentInfoTableComposite tableComposite;
	
	public ExchangeAccountDetailsPage(ISubAccountManagerService service) {
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
		dataSection = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		dataSection.marginWidth = 10;
		dataSection.setText("Exchange Account Details: ");
		
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		dataSection.setLayoutData(td);
		
		Composite client = toolkit.createComposite(dataSection);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 0;
		glayout.numColumns = 2;
		client.setLayout(glayout);
		
		createSpacer(toolkit, client, 2);
		
		createComponent(toolkit, client);
		
		toolkit.paintBordersFor(dataSection);
		dataSection.setClient(client);
	}
	
	private void createComponent(FormToolkit toolkit, Composite client) {
		toolkit.createLabel(client, "Name: ");
		txtName = toolkit.createText(client, "", SWT.BORDER);
		GridData gd1 = new GridData();
		gd1.widthHint = TEXT_WIDTH_HINT;
		txtName.setLayoutData(gd1);
		txtName.setEditable(true);
		txtName.setTextLimit(1023);
		
		createSpacer(toolkit, client, 2);
		
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
		gd2.horizontalSpan = 2;
		Label lblTable = toolkit.createLabel(client, "Summary of symbol: ");
		lblTable.setLayoutData(gd2);
		tableComposite = new InstrumentInfoTableComposite(client, SWT.NONE);
		GridData gd3 = new GridData();
		gd3.horizontalSpan = 2;
		gd3.heightHint = 150;
		tableComposite.setLayoutData(gd3);
		toolkit.adapt(tableComposite);
		
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
			input = (ExchangeAccountModel) selected.getFirstElement();
		} else {
			input = null;
		}
		update();
	}
	
	private void update() {
		if ( input != null ) {
			txtName.setText(input.getName());
			tableComposite.setInput(service.getInstrumentInfoModelListByExchangeAccountId(input.getId()));
		}
	}

	

}
