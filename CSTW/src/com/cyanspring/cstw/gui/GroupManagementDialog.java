package com.cyanspring.cstw.gui;

import java.awt.BorderLayout;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;

public class GroupManagementDialog extends Dialog implements IAsyncEventListener{

	private Text  txtUser;
	private Label lblUser;
	
	
	protected GroupManagementDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
//		Composite container = (Composite) super.createDialogArea(parent);
//		container.setLayout(new BorderLayout(0, 0));
//		
//		
//		Label lblUser = new Label(container, SWT.NONE);
//		lblUser.setLayoutData(BorderLayout.NORTH);
//		lblUser.setText("User: Test");
//		
//		ListViewer listViewer = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL);
//		List list = listViewer.getList();
//		list.setLayoutData(BorderLayout.WEST);
//		
//		ListViewer listViewer_1 = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL);
//		List list_1 = listViewer_1.getList();
//		list_1.setLayoutData(BorderLayout.EAST);
//		
//		Composite composite = new Composite(container, SWT.NONE);
//		composite.setLayoutData(BorderLayout.CENTER);
//		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
//		
//		Button button = new Button(composite, SWT.NONE);
//		button.setText("<---");
//		
//		Button button_1 = new Button(composite, SWT.NONE);
//		button_1.setText("--->");
		return super.createDialogArea(parent);
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		
	}

}
