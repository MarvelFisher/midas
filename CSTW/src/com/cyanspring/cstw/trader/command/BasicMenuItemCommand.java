package com.cyanspring.cstw.trader.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.util.IdGenerator;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/14
 *
 */
public abstract class BasicMenuItemCommand extends AbstractHandler {

	private static final Logger log = LoggerFactory
			.getLogger(BasicMenuItemCommand.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			if (isMultiple()) {
				page.showView(getViewId(), IdGenerator.getInstance()
						.getNextID(), IWorkbenchPage.VIEW_ACTIVATE);
			} else {
				page.showView(getViewId());
			}
		} catch (PartInitException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	protected abstract String getViewId();

	protected boolean isMultiple() {
		return false;
	}
}
