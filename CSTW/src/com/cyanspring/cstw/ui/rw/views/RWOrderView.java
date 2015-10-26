package com.cyanspring.cstw.ui.rw.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cyanspring.cstw.ui.rw.composite.RWMainOrderComposite;

/**
 * @author Junfeng
 * @create 26 Oct 2015
 */
public class RWOrderView extends ViewPart {
	public static String ID = "com.cyanspring.cstw.ui.rw.views.RWOrderView";
	
	private RWMainOrderComposite composite;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		composite = new RWMainOrderComposite(container, SWT.NONE);
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

}
