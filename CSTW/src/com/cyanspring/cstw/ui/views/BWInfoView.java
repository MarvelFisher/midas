package com.cyanspring.cstw.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cyanspring.cstw.ui.bw.composite.BWMainDataComposite;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BWInfoView extends ViewPart {
	public static String ID = "com.cyanspring.cstw.ui.bw.views.BWInfoView";

	private BWMainDataComposite composite;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		composite = new BWMainDataComposite(container, SWT.NONE);
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

}
