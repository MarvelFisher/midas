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
import com.cyanspring.common.server.event.ServerShutdownEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;

public class ConfirmPasswordDialog extends Dialog implements
		IAsyncEventListener {

	private static final Logger log = LoggerFactory
			.getLogger(ConfirmPasswordDialog.class);
	private Text txtPassword;
	private Text txtConfirmPassword;
	private Label lblMessage;
	private Button btnOk, btnCancel;
	private String id = IdGenerator.getInstance().getNextID();
	private ImageRegistry imageRegistry;
	private String username;
	private boolean loginOk;

	public ConfirmPasswordDialog(String username, Shell parentShell) {
		this(parentShell);
		this.username = username;
	}

	protected ConfirmPasswordDialog(Shell parentShell) {
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

		Label lblPwd = new Label(container, SWT.NONE);
		lblPwd.setText("Password:");
		txtPassword = new Text(container, SWT.PASSWORD | SWT.BORDER);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					txtConfirmPassword.setFocus();
				}
			}
		});

		Label lblConfirmPwd = new Label(container, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lblConfirmPwd.setLayoutData(gd_lblNewLabel);
		lblConfirmPwd.setText("Confirm Password:");

		txtConfirmPassword = new Text(container, SWT.PASSWORD | SWT.BORDER);
		txtConfirmPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		txtConfirmPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					doLogin();
				}
			}
		});

		lblMessage = new Label(container, SWT.NONE);
		GridData gdLblMessage = new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1);
		lblMessage.setLayoutData(gdLblMessage);
		lblMessage.setText("");
		Color red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		lblMessage.setForeground(red);
		return container;
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
				ConfirmPasswordDialog.this.close();
			}
		});

		return buttonArea;
	}

	@Override
	public int open() {
		Business.getInstance()
				.getEventManager()
				.subscribe(CSTWUserLoginReplyEvent.class, id,
						ConfirmPasswordDialog.this);
		return super.open();
	}

	@Override
	public boolean close() {
		Business.getInstance()
				.getEventManager()
				.unsubscribe(CSTWUserLoginReplyEvent.class, id,
						ConfirmPasswordDialog.this);

		return super.close();
	}

	private void doLogin() {

		if (!Business.getInstance().isFirstServerReady()) {
			lblMessage.setText("Server not ready");
			return;
		}

		if (!txtPassword.getText().equals(txtConfirmPassword.getText())) {
			lblMessage.setText("Password confirmation failed");
			return;
		}

		lblMessage.setText("");
		Business business = Business.getInstance();
		// need to review which server to get login
		String server = business.getFirstServer();
		CSTWUserLoginEvent event = new CSTWUserLoginEvent(id, server, username,
				txtPassword.getText());
		try {
			business.getEventManager().sendRemoteEvent(event);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof CSTWUserLoginReplyEvent) {
			final CSTWUserLoginReplyEvent reply = (CSTWUserLoginReplyEvent) event;
			log.info("loginOk:" + reply.isOk());

			loginOk = reply.isOk();

			if (loginOk
					&& (null == reply.getUserGroup() || null == reply
							.getUserGroup().getRole())) {
				loginOk = false;
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						lblMessage.setText("invalid user role");
					}
				});
				return;
			}

			if (loginOk) {
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						ConfirmPasswordDialog.this.close();
					}
				});
				Business business = Business.getInstance();
				String server = business.getFirstServer();
				try {
					business.getEventManager().sendRemoteEvent(new ServerShutdownEvent(id, server));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				this.getContents().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageBean bean = MessageLookup
								.getMsgBeanFromEventMessage(reply.getMessage());
						lblMessage.setText(bean.getLocalMsg());
					}
				});
			}
		}
	}

}
