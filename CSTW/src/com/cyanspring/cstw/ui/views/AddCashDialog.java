package com.cyanspring.cstw.ui.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.AddCashEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;

public class AddCashDialog extends Dialog {
	private static final Logger log = LoggerFactory.getLogger(AddCashDialog.class);
	private Text textAccountID;
	private Text textCash;
	private Account currentAccount;
	
	private String accountID;
	private double cash;
	private String id = IdGenerator.getInstance().getNextID();

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public AddCashDialog(Shell parentShell, Account currentAccount) {
		super(parentShell);
		this.currentAccount = currentAccount;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		FillLayout fl_container = new FillLayout(SWT.HORIZONTAL);
		fl_container.marginHeight = 3;
		fl_container.marginWidth = 6;
		container.setLayout(fl_container);

		Composite composite = new Composite(container, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		composite.setLayout(gl_composite);

		Label lblAccountid = new Label(composite, SWT.NONE);
		lblAccountid.setText("AccountID: ");

		textAccountID = new Text(composite, SWT.BORDER);
		textAccountID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (currentAccount != null) {
			textAccountID.setText(currentAccount.getId());
		}

		Label lblCash = new Label(composite, SWT.NONE);
		lblCash.setText("Cash: ");

		textCash = new Text(composite, SWT.BORDER);
		textCash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		final Shell shell = parent.getShell();
		Button button = createButton(parent, IDialogConstants.OK_ID, "ADD", true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent select) {			
				boolean confirm = MessageDialog.openConfirm(shell, "Confirm", "Are you sure to add?");
				if (!confirm)
					return;
				log.info("accountID:{}, cash:{}", accountID, cash);
				if (StringUtils.hasText(accountID) && !PriceUtils.isZero(cash)) {
					IRemoteEventManager eventManager = Business.getInstance().getEventManager();
					try {
						String server = Business.getInstance().getFirstServer();
						eventManager.sendRemoteEvent(new AddCashEvent(id, server, accountID, cash));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(220, 160);
	}
	
	@Override
	protected void okPressed(){
	    accountID = textAccountID.getText();
	    cash = Double.parseDouble(textCash.getText());
	    super.okPressed();
	}
}
