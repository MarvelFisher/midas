package com.cyanspring.cstw.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cyanspring.cstw.ui.admin.composite.SubAccountManageComposite;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class SubAccountView extends ViewPart {
	public static String ID = "com.cyanspring.cstw.ui.views.SubAccountView";
	
	private SubAccountManageComposite composite;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		composite = new SubAccountManageComposite(container, SWT.NONE);
	}

	
	@Override
	public void setFocus() {
		composite.setFocus();
	}

}
