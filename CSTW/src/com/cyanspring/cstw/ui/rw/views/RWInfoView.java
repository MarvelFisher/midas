/**
 * 
 */
package com.cyanspring.cstw.ui.rw.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cyanspring.cstw.ui.rw.composite.RWMainComposite;

/**
 * @author Junfeng
 *
 */
public class RWInfoView extends ViewPart {
	
	public static String ID = "com.cyanspring.cstw.ui.rw.views.RWInfoView";
	
	private RWMainComposite composite;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		composite = new RWMainComposite(container, SWT.NONE);
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

}
