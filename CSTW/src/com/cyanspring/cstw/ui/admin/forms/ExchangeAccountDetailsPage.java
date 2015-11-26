package com.cyanspring.cstw.ui.admin.forms;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import com.cyanspring.cstw.service.iservice.admin.IInputChangeListener;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class ExchangeAccountDetailsPage implements IDetailsPage {

	private static final int TEXT_WIDTH_HINT = 250;
	private static final int BUTTON_WIDTH_HINT = 80;
	private static final int TABLE_WIDTH_HINT = 400;
	private static final int TABLE_HEIGHT_HINT = 150;
	
	private ExchangeAccountModel input;
	private ISubAccountManagerService service;
	
	private IManagedForm mform;
	private Section dataSection;
	private Text txtName;
	private Button btnEdit;
	private InstrumentInfoTableComposite tableComposite;
	
	private Action updateAction;
	
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
		glayout.numColumns = 3;
		client.setLayout(glayout);
		
		createSpacer(toolkit, client, 3);
		
		createComponent(toolkit, client);
		initListener();
		initAction();
		
		toolkit.paintBordersFor(dataSection);
		dataSection.setClient(client);
	}
	
	private void createComponent(FormToolkit toolkit, Composite client) {
		toolkit.createLabel(client, "Name: ");
		txtName = toolkit.createText(client, "", SWT.BORDER);
		GridData gd1 = new GridData();
		gd1.widthHint = TEXT_WIDTH_HINT;
		txtName.setLayoutData(gd1);
		txtName.setEditable(false);
		txtName.setTextLimit(1023);
		
		btnEdit = toolkit.createButton(client, "Edit...", SWT.NONE);		
		GridData gd4 = new GridData();
		gd4.widthHint = BUTTON_WIDTH_HINT;
		gd4.heightHint = SWT.DEFAULT;
		btnEdit.setLayoutData(gd4);
		
		createSpacer(toolkit, client, 3);
		
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
		gd2.horizontalSpan = 3;
		Label lblTable = toolkit.createLabel(client, "Summary of symbol: ");
		lblTable.setLayoutData(gd2);
		tableComposite = new InstrumentInfoTableComposite(client, SWT.NONE);
		GridData gd3 = new GridData();
		gd3.horizontalSpan = 3;
		gd3.heightHint = TABLE_HEIGHT_HINT;
		gd3.widthHint = TABLE_WIDTH_HINT;
		tableComposite.setLayoutData(gd3);
		toolkit.adapt(tableComposite);
		
	}
	
	private void initListener() {
		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateAction.run();
			}
		});
		
		service.addExchangeInputChangeListener(new IInputChangeListener() {
			@Override
			public void inputChanged() {
				input = service.getExchangeAccoutById(input.getId());
				update();
			}
		});
		
	}

	private void initAction() {
		updateAction = new Action() {
			@Override
			public void run() {
				InputNameDialog inputDialog = new InputNameDialog(mform.getForm().getShell());
				inputDialog.setInputTitle("Exchange Account: ");
				inputDialog.setSelectText(input.getName());
				if (TrayDialog.OK == inputDialog.open()) {
					String txt = inputDialog.getSelectText();
					service.updateExchangeAccountName(input, txt);
				}
			}
		};
		
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
