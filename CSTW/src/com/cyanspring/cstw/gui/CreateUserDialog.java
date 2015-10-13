package com.cyanspring.cstw.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.account.CreateUserEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;

public class CreateUserDialog extends Dialog implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(CreateUserDialog.class);
	private Text txtUser;
	private Text txtPassword;
	private Text txtEmail;
	private Text txtPhone;
	private Combo cbUserType;
	private Combo cbUserRole;
	private Label lblMessage;
	private Button btnOk, btnCancel;
	private String id = IdGenerator.getInstance().getNextID();

	public CreateUserDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 30;
		layout.marginLeft = 30;
		container.setLayout(layout);

		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setText("User Id:");

		txtUser = new Text(container, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		txtUser.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.CR) {
					txtPassword.setFocus();
				}
	        }
	      });

		Label lblPassword = new Label(container, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lblPassword.setLayoutData(gd_lblNewLabel);
		lblPassword.setText("Password:");

		txtPassword = new Text(container, SWT.BORDER);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.CR) {
					doCreate();
				}
	        }
	      });

		Label lblEmail = new Label(container, SWT.NONE);
		gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lblEmail.setLayoutData(gd_lblNewLabel);
		lblEmail.setText("Email:");
		txtEmail = new Text(container, SWT.BORDER);
		txtEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		
		Label lblPhone = new Label(container, SWT.NONE);
		gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lblPhone.setLayoutData(gd_lblNewLabel);
		lblPhone.setText("Phone:");
		txtPhone = new Text(container, SWT.BORDER);
		txtPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label lblUserType = new Label(container, SWT.NONE);
		gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lblUserType.setLayoutData(gd_lblNewLabel);
		lblUserType.setText("UserType:");
		cbUserType = new Combo(container, SWT.BORDER);
		for(UserType userType: UserType.values()) {
			cbUserType.add(userType.toString());
		}
		cbUserType.setText(UserType.NORMAL.toString());
		cbUserType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		
		Label lblUserRole = new Label(container, SWT.NONE);
		lblUserRole.setText("User Role:");
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
		
		return container;
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
		btnOk.setText("Create");
		//setButtonLayoutData(btnOk);
		btnOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCreate();
			}
		});
		
		btnCancel = new Button(buttonArea, SWT.PUSH);
		btnCancel.setText("Cancel");
		//setButtonLayoutData(btnCancel);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateUserDialog.this.close();
			}
		});

		return buttonArea;
	}

	private void doCreate() {
		lblMessage.setText("");
		Business business = Business.getInstance();
		// need to review which server to get login
		String server = business.getFirstServer();
		User user = new User(txtUser.getText(), txtPassword.getText());
		user.setEmail(txtEmail.getText());
		user.setPhone(txtPhone.getText());
		user.setUserType(UserType.valueOf(cbUserType.getText()));
		user.setRole(getRole(cbUserRole.getText()));
		user.setName(txtUser.getText());
		CreateUserEvent event = new CreateUserEvent(id, server, 
				user, "", "", IdGenerator.getInstance().getNextID());
		try {
			business.getEventManager().sendRemoteEvent(event);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
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
		Business.getInstance().getEventManager().subscribe(CreateUserReplyEvent.class, id, CreateUserDialog.this);
		return super.open();
	}
	
	@Override
	public boolean close() {
		Business.getInstance().getEventManager().unsubscribe(CreateUserReplyEvent.class, id, CreateUserDialog.this);
		return super.close();
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(300, 260);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof CreateUserReplyEvent) {
			final CreateUserReplyEvent reply = (CreateUserReplyEvent) event;
			if(reply.isOk()) {
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						CreateUserDialog.this.close();
					}
				});
			} else {
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						lblMessage.setText(reply.getMessage());							
					}
				});
			}
		}
	}
}
