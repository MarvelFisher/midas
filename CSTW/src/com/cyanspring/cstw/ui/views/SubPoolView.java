package com.cyanspring.cstw.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cyanspring.cstw.ui.rw.composite.SubPoolManageComposite;

/**
 * @author Junfeng
 * @create 19 Nov 2015
 */
public class SubPoolView extends ViewPart {
	
	public static String ID = "com.cyanspring.cstw.ui.views.SubPoolView";
	
	private SubPoolManageComposite composite;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		composite = new SubPoolManageComposite(container, SWT.NONE);
	}

	@Override
	public void setFocus() {
		composite.forceFocus();
	}

}
