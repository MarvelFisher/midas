package com.cyanspring.cstw.gui.command.auth;

import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.cstw.business.Business;

public class ViewAuthListener implements IPartListener2 {
	private static final Logger log = LoggerFactory
			.getLogger(ViewAuthListener.class);
	
	
	public void filterViewAllAction(String partName,IWorkbenchPart part){
		filterToolbarAction(partName,part);
		filterMenuAction(partName);
	}
	
	public void filterViewAction(String partName,ActionContributionItem actionItem){
		
		if(!Business.getInstance().hasAuth(partName, actionItem.getId())){
			actionItem.getAction().setEnabled(false);
		}else{
			actionItem.getAction().setEnabled(true);
		}
	}
	
	public void filterToolbarAction(String partName,IWorkbenchPart part){
		IViewSite site= (IViewSite) part.getSite();
		ToolBarManager bar = (ToolBarManager) site.getActionBars().getToolBarManager();
		IContributionItem items []=  bar.getItems();
		for(IContributionItem item : items){
			ActionContributionItem actionItem = (ActionContributionItem)item;
			if(!Business.getInstance().hasAuth(partName, actionItem.getId())){
				actionItem.setVisible(false);
			}else{
				actionItem.setVisible(true);
			}
		}
		bar.update(true);
	}
	
	public void filterMenuAction(String partName){
		List <ActionContributionItem> list = AuthMenuManager.getViewMenuActions(partName);

		for(ActionContributionItem actionItem : list ){
//			log.info("menu actionItem:{} hasAuth:{}",actionItem.getId(),Business.getInstance().hasAuth(partName, actionItem.getId()));
			if(!Business.getInstance().hasAuth(partName, actionItem.getId())){
				actionItem.setVisible(false);
			}else{
				actionItem.setVisible(true);
			}
		}
	}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		
		UserRole role = Business.getInstance().getUserGroup().getRole();
		String partName = partRef.getPartName();	
//		log.info("partActviated:{}",partRef.getPartName());
		filterToolbarAction(partName,partRef.getPart(true));
		filterMenuAction(partName);
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
