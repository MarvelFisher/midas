package com.cyanspring.cstw.gui.command.auth;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.cstw.business.Business;

public class ViewAuthListener implements IPartListener2 {
	private static final Logger log = LoggerFactory
			.getLogger(ViewAuthListener.class);
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		
		UserRole role = Business.getInstance().getUserGroup().getRole();
		String partName = partRef.getPartName();
		
		log.info("partActviated:{}",partRef.getPartName());
		
		IViewSite site= (IViewSite) partRef.getPart(true).getSite();
		ToolBarManager bar = (ToolBarManager) site.getActionBars().getToolBarManager();
		log.info("bar size:{}",bar.getSize());
		
		IContributionItem items []=  bar.getItems();
		for(IContributionItem item : items){
			ActionContributionItem actionItem = (ActionContributionItem)item;
			if(!Business.getInstance().hasAuth(partName, actionItem.getId())){
				actionItem.setVisible(false);
			}
		}
		bar.update(true);
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {

	}

}
