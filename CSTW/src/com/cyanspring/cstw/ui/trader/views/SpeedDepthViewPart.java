package com.cyanspring.cstw.ui.trader.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cyanspring.cstw.ui.trader.composite.speeddepth.SpeedDepthMainComposite;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthViewPart extends ViewPart {

	private SpeedDepthMainComposite mainComposite;

	public SpeedDepthViewPart() {
	}

	public static final String ID = "com.cyanspring.cstw.trader.gui.SpeedDepthViewPart"; //$NON-NLS-1$

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		mainComposite = new SpeedDepthMainComposite(parent, SWT.NONE);
	}

	@Override
	public void dispose() {
		mainComposite.dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

}
