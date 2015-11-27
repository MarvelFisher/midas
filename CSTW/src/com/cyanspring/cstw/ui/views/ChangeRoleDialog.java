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

import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.ChangeUserRoleEvent;
import com.cyanspring.common.event.account.ChangeUserRoleReplyEvent;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;

public class ChangeRoleDialog extends Dialog implements IAsyncEventListener{
	private static final Logger log = LoggerFactory
			.getLogger(ChangeRoleDialog.class);
	private Combo cbUserRole;
	private Label lblUserRoleId;
	private Label lblMessage;
	private Button btnOk, btnCancel;
	private String ID = IdGenerator.getInstance().getNextID();
	protected Shell shell;
	private Composite parent;
	private User user;
	
	protected ChangeRoleDialog(Shell parentShell,User user) {
		super(parentShell);
		this.user = user;
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
		lblUserId.setText(user.getId());
		
		Label lblUserRole = new Label(container, SWT.NONE);
		lblUserRole.setText("User Role:");
		
		lblUserRoleId = new Label(container, SWT.NONE);
		if( null == user.getRole()){
			lblUserRoleId.setText("");

		}else{
			lblUserRoleId.setText(user.getRole().desc());

		}
		Label lblchangeUserRole = new Label(container, SWT.NONE);
		lblchangeUserRole.setText("Change to:");
		
		cbUserRole = new Combo(container, SWT.BORDER);
		for(UserRole userRole: UserRole.values()) {
			cbUserRole.add(userRole.desc());
		}
		cbUserRole.setText(UserRole.Trader.desc());
		cbUserRole.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
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
		btnOk.setText("Change Role");
		//setButtonLayoutData(btnOk);
		btnOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				UserRole newRole = getRole(cbUserRole.getText());
				
				if(lblUserRoleId.getText().equals(newRole.name())){
					pushMessageToLabel("Role already is :"+newRole.name());
					return;
				}
				
				ChangeUserRoleEvent event = new ChangeUserRoleEvent(ID, Business.getBusinessService().getFirstServer(),user.getId(),newRole);
				sendRemoteEvent(event);
			}
		});
		
		btnCancel = new Button(buttonArea, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ChangeRoleDialog.this.close();
			}
		});

		return buttonArea;
	}
	
	public UserRole getRole(String desc){
		for(UserRole role : UserRole.values()){
			if(role.desc().equals(desc)){
				return role;
			}
		}
		return null;
	}
	
	@Override
	public int open() {
		subEvent(ChangeUserRoleReplyEvent.class);
		return super.open();
	}
	
	@Override
	public boolean close() {
		unSubEvent(ChangeUserRoleReplyEvent.class);
		return super.close();
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof ChangeUserRoleReplyEvent){
			ChangeUserRoleReplyEvent reply = (ChangeUserRoleReplyEvent)event;
			if(reply.isOk()){
				pushMessageToLabel("setting OK!");
				renewUser(reply.getUser());
			}else{
				pushMessageToLabel(getReplyErrorMessage(reply.getMessage()));
			}
		}
	}
	
	private void renewUser(final User user) {
		parent.getDisplay().asyncExec(new Runnable(){

			@Override
			public void run() {
				
				lblUserRoleId.setSize(100, 20);
				lblUserRoleId.setText(user.getRole().desc());
				
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
	
}
