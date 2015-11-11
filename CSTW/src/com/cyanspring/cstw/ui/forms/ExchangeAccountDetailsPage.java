package com.cyanspring.cstw.ui.forms;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.cyanspring.cstw.model.admin.ExchangeAccountModel;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class ExchangeAccountDetailsPage implements IDetailsPage {

	private ExchangeAccountModel input;
	
	private IManagedForm mform;
	private Section dataSection;
	
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
		
		toolkit.paintBordersFor(dataSection);
		dataSection.setClient(client);
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
			dataSection.setText("Exchange Account Details: " + input.getName());
		}
	}

	

}
