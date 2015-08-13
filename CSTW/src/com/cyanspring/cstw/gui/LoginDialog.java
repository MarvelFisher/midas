
package com.cyanspring.cstw.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.account.CSTWUserLoginEvent;
import com.cyanspring.common.event.account.CSTWUserLoginReplyEvent;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;

public class LoginDialog extends Dialog implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(LoginDialog.class);
	private Text txtUser;
	private Text txtPassword;
	private Label lblMessage;
	private Button btnOk, btnCancel;
	private String id = IdGenerator.getInstance().getNextID();
	private boolean loginOk;
	private ImageRegistry imageRegistry;

	public LoginDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();
		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setBackgroundMode(SWT.INHERIT_FORCE);
		container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		ImageDescriptor backward_imageDesc = imageRegistry
				.getDescriptor(ImageID.LOGIN_BG.toString());
		container.setBackgroundImage(backward_imageDesc.createImage());
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 10;
		layout.marginLeft = 40;
		layout.marginTop = 30;
		container.setLayout(layout);

		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setText("User Id:");
		txtUser = new Text(container, SWT.BORDER);
		txtUser.setText("");
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

		txtPassword = new Text(container, SWT.PASSWORD | SWT.BORDER);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.CR) {
					doLogin();
				}
	        }
	      });
		

		lblMessage = new Label(container, SWT.NONE);
		GridData gdLblMessage = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		lblMessage.setLayoutData(gdLblMessage);
		lblMessage.setText("");
		Color red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		lblMessage.setForeground(red);
		return container;
	}
	
	private void doLogin() {
		
		if(!Business.getInstance().isFirstServerReady()){
			lblMessage.setText("Server not ready");
			return;
		}

		lblMessage.setText("");
		Business business = Business.getInstance();
		// need to review which server to get login
		String server = business.getFirstServer();
		CSTWUserLoginEvent event = new CSTWUserLoginEvent(id, server, 
				txtUser.getText(), txtPassword.getText());
		try {
			business.getEventManager().sendRemoteEvent(event);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonArea =  new Composite(parent, SWT.NONE);
		buttonArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridLayout layoutButtons = new GridLayout(2, true);
		layoutButtons.marginRight = 30;
		layoutButtons.marginLeft = 30;
		buttonArea.setLayout(layoutButtons);
		buttonArea.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		btnOk = new Button(buttonArea, SWT.PUSH);
		btnOk.setText("Login");
		//setButtonLayoutData(btnOk);
		btnOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doLogin();
			}
		});
		
		btnCancel = new Button(buttonArea, SWT.PUSH);
		btnCancel.setText("Cancel");
		//setButtonLayoutData(btnCancel);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LoginDialog.this.close();
				System.exit(0);
			}
		});

		return buttonArea;
	}

	@Override
	public int open() {
		Business.getInstance().getEventManager().subscribe(CSTWUserLoginReplyEvent.class, id, LoginDialog.this);
		return super.open();
	}
	
	@Override
	public boolean close() {
		Business.getInstance().getEventManager().unsubscribe(CSTWUserLoginReplyEvent.class, id, LoginDialog.this);
		if(loginOk) {
			return super.close();
		} else {
			System.exit(0);
		}
		return false;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(300, 160);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof CSTWUserLoginReplyEvent) {
			final CSTWUserLoginReplyEvent reply = (CSTWUserLoginReplyEvent) event;
			log.info("loginOk:"+reply.isOk());
			
			loginOk = reply.isOk();
			
			if(loginOk && ( null == reply.getUserGroup() || null == reply.getUserGroup().getRole())){
				loginOk = false;
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						lblMessage.setText("invalid user role");
						
					}
				});
				return ;
			}
			
			if(loginOk) {
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						
						LoginDialog.this.close();
					}
				});
			} else {
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageBean bean = MessageLookup.getMsgBeanFromEventMessage(reply.getMessage());
						lblMessage.setText(bean.getLocalMsg());							
					}
				});
			}
		}
	}
}
