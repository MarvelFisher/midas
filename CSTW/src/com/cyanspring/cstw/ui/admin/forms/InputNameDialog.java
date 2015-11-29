package com.cyanspring.cstw.ui.admin.forms;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class InputNameDialog extends TrayDialog {
	
	private String selectText;
	private String inputTitle;
	private Text txt;
	
	public InputNameDialog(Shell shell) {
		super(shell);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText("Please input a name");
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(300, 150);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);
		
		GridData gridData = new GridData(SWT.RIGHT, SWT.FILL, true, false);
		gridData.widthHint = 200;
		gridData.heightHint = SWT.DEFAULT;
		
		Label lblSelectUser = new Label(container, SWT.NONE);
		if ( inputTitle != null ) {
			lblSelectUser.setText(inputTitle);
		}
		txt = new Text(container, SWT.BORDER);
		txt.setLayoutData(gridData);
		if (selectText != null) {
			txt.setText(selectText);
		}
		
		return container;
	}
	
	public String getSelectText() {
		return selectText;
	}
	
	public void setSelectText(String txt)	{
		this.selectText = txt;
	}
	
	public void setInputTitle(String title) {
		
	}
	
	@Override
	protected void okPressed() {
		selectText = txt.getText();
		super.okPressed();
	}
	
}