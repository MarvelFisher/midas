package com.cyanspring.cstw.ui.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.order.ManualClosePositionRequestEvent;
import com.cyanspring.common.event.order.UpdateOpenPositionPriceEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;

import org.eclipse.swt.layout.GridData;

public class SetPriceDialog extends Dialog {
	private static final Logger log = LoggerFactory.getLogger(SetPriceDialog.class);
	private Text price;
	double inputPrice;
	private OpenPosition position;
	private Mode mode;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public SetPriceDialog(Shell parentShell, OpenPosition position, Mode mode) {
		super(parentShell);
		this.position = position;
		this.mode = mode;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(container, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginRight = 20;
		gl_composite.marginLeft = 20;
		composite.setLayout(gl_composite);

		Label priceLabel = new Label(composite, SWT.NONE);
		priceLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
		priceLabel.setText("price: ");

		price = new Text(composite, SWT.BORDER);
		price.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent select) {
				try {
					String server = Business.getInstance().getFirstServer();
					RemoteAsyncEvent request = null;
					if (mode == Mode.CLOSE_POSITION)
						request = new ManualClosePositionRequestEvent(position.getAccount(), server, position,
								IdGenerator.getInstance().getNextID(), inputPrice);
					else if (mode == Mode.CHANGE_POSITION_PRICE)
						request = new UpdateOpenPositionPriceEvent(position.getAccount(), server, 
								inputPrice, position.getSymbol(), position.getAccount());
					Business.getInstance().getEventManager().sendRemoteEvent(request);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(220, 150);
	}

	@Override
	protected void okPressed() {
		try {
			if (price.getText() == null || price.getText().trim() == "")
				inputPrice = 0.0;
			else
				inputPrice = Double.parseDouble(price.getText());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		super.okPressed();
	}

	enum Mode {
		CLOSE_POSITION, CHANGE_POSITION_PRICE
	}
}
