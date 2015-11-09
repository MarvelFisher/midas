package com.cyanspring.cstw.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cyanspring.cstw.ui.brw.composite.BRWMainDataComposite;

/**
 * @author Junfeng
 * @create 27 Oct 2015
 */
public class BRWInfoView extends ViewPart {
	public static String ID = "com.cyanspring.cstw.ui.bw.views.BWInfoView";

	private BRWMainDataComposite composite;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		composite = new BRWMainDataComposite(container, SWT.NONE);
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

}
