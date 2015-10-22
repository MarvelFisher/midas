package com.cyanspring.cstw.ui.basic;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/22
 *	moved from Project S
 */
public abstract class BasicOperateDialog extends Dialog {

	/**
	 * Create the dialog.
	 * 
	 * @param parent.g
	 */
	public BasicOperateDialog(Composite parent) {
		super(parent.getShell());
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button buttonOK = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		buttonOK.setText("确定");
		Button buttonCancel = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		buttonCancel.setText("取消");
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

}
