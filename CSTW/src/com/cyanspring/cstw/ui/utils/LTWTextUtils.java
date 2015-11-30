package com.cyanspring.cstw.ui.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/22
 *
 */
public final class LTWTextUtils {

	public static Text createDoubleStyleText(final Composite parent, int style) {
		final Text text = new Text(parent, style);
		text.addKeyListener(new KeyListener() {
			String oldValue = "";

			@Override
			public void keyPressed(KeyEvent e) {
				oldValue = text.getText();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String value = text.getText();
				if (value != null && value.length() > 0
						&& !PatternUtils.isDoubleMaxTwo(value)) {
					MessageBox messageBox = new MessageBox(parent.getShell(),
							SWT.OK);
					messageBox.setMessage(value + "不是一个有效的数值");
					messageBox.open();
					text.setText(oldValue);
				}
			}
		});
		return text;
	}
	
	public static Text createIntegerStyleText(final Composite parent, int style) {
		final Text text = new Text(parent, style);
		text.addKeyListener(new KeyListener() {
			private String oldValue = "";
			@Override
			public void keyPressed(KeyEvent e) {
				oldValue = text.getText();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				String value = text.getText();
				if (value != null && value.length() > 0 && !PatternUtils.isNumeric(value)) {
					MessageDialog.openError(parent.getShell(), "", "不是一个有效的数值");
					text.setText(oldValue);
				}
				
			}
		});
		
		return text;
	}

}
