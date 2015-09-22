package com.cyanspring.cstw.gui.assist;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.cstw.gui.SingleOrderStrategyView;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.09.21
 *
 */
public final class OpenSingleOrderStrategyViewAssist {

	private static final Logger log = LoggerFactory
			.getLogger(OpenSingleOrderStrategyViewAssist.class);

	public void run(int keyCode) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			SingleOrderStrategyView view = (SingleOrderStrategyView) page
					.showView(SingleOrderStrategyView.ID);
			view.openByKeyCode(keyCode);

		} catch (PartInitException e) {
			log.error(e.getMessage(), e);
		}
	}

}
