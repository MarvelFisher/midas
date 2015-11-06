package com.cyanspring.cstw.ui.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.ChangeAccountStateReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountStateRequestEvent;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.GUIUtils;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class ChangeAccountStateDialog extends Dialog implements IAsyncEventListener{
	private static final Logger log = LoggerFactory
			.getLogger(ChangeRoleDialog.class);
	
	private Combo cbAccountState;
	private Label lblAccountStateId;
	private Label lblMessage;
	private Button btnOk, btnCancel;
	private String ID = IdGenerator.getInstance().getNextID();
	protected Shell shell;
	private Composite parent;
	private Account account;
	private DynamicTableViewer viewer;

	protected ChangeAccountStateDialog(Shell parentShell,Account account) {
		super(parentShell);
		this.account = account;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		this.parent = parent;
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 30;
		layout.marginLeft = 30;
		layout.marginTop = 30;
		layout.verticalSpacing = 20;
		container.setLayout(layout);
		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setText("User Id:");
		
		Label lblUserId = new Label(container, SWT.NONE);
		lblUserId.setText(account.getId());
		
		Label lblAccountState = new Label(container, SWT.NONE);
		lblAccountState.setText("State:");
		
		lblAccountStateId = new Label(container, SWT.NONE);
		lblAccountStateId.setText(account.getState().name());
		Label lblchangeUserRole = new Label(container, SWT.NONE);
		lblchangeUserRole.setText("Change to:");
		
		cbAccountState = new Combo(container, SWT.BORDER);
		for(AccountState accountState: AccountState.values()) {
			cbAccountState.add(accountState.toString());
		}
		cbAccountState.setText(AccountState.ACTIVE.toString());
		cbAccountState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label lblBlank = new Label(container, SWT.NONE);
		GridData gdLblBlank = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblBlank.horizontalIndent = 1;
		lblBlank.setLayoutData(gdLblBlank);
		lblBlank.setText("          ");

		lblMessage = new Label(container, SWT.NONE);
		GridData gdLblMessage = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblMessage.horizontalIndent = 1;
		lblMessage.setLayoutData(gdLblMessage);
		lblMessage.setText("");
		Color red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		lblMessage.setForeground(red);
		return super.createDialogArea(parent);
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonArea =  new Composite(parent, SWT.NONE);		
		GridLayout layoutButtons = new GridLayout(2, true);
		layoutButtons.marginRight = 30;
		layoutButtons.marginLeft = 30;
		buttonArea.setLayout(layoutButtons);
		buttonArea.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		btnOk = new Button(buttonArea, SWT.PUSH);
		btnOk.setText("Change State");
		//setButtonLayoutData(btnOk);
		btnOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lblMessage.setText("");
				AccountState newState = AccountState.valueOf(cbAccountState.getText());
				
				if(lblAccountStateId.getText().equals(newState.name())){
					pushMessageToLabel("State already is :"+newState.name());
					return;
				}
				
				ChangeAccountStateRequestEvent request = new ChangeAccountStateRequestEvent(ID, Business.getInstance().getFirstServer(),account.getId(),newState);
				sendRemoteEvent(request);
			}
		});
		
		btnCancel = new Button(buttonArea, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ChangeAccountStateDialog.this.close();
			}
		});

		return buttonArea;
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof ChangeAccountStateReplyEvent){
			ChangeAccountStateReplyEvent reply = (ChangeAccountStateReplyEvent)event;
			if(reply.isOk()){
				pushMessageToLabel("setting OK!");
				renewAccount(reply.getAccount());
				account = reply.getAccount();
			}else{
				GUIUtils.showMessageBox(getReplyErrorMessage(reply.getMessage()), parent);
			}
		}
	}

	
	
	private void renewAccount(final Account account) {
		parent.getDisplay().asyncExec(new Runnable(){

			@Override
			public void run() {
				
				lblAccountStateId.setSize(100, 20);
				lblAccountStateId.setText(account.getState().name());
				
			}
			
		});
		
	}

	private String getReplyErrorMessage(String msg){
		 MessageBean bean = MessageLookup.getMsgBeanFromEventMessage(msg);
		 return bean.getLocalMsg();
	}
	
	private void pushMessageToLabel(final String msg){
		parent.getDisplay().asyncExec(new Runnable(){

			@Override
			public void run() {
				lblMessage.setText(msg+"\n"+lblMessage.getText());	
			}
			
		});
	}
	
	@Override
	public int open() {
		subEvent(ChangeAccountStateReplyEvent.class);
		return super.open();
	}
	
	@Override
	public boolean close() {
		unSubEvent(ChangeAccountStateReplyEvent.class);
		return super.close();
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz,ID, this);		
	}
	
	private void unSubEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().unsubscribe(clazz,ID, this);		
	}
	
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Account getAccount() {
		return account;
	}
	
}
