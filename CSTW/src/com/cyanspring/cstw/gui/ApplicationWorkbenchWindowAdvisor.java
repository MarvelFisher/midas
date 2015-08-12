/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.cstw.gui;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.ISourceProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.event.SelectUserAccountEvent;
import com.cyanspring.cstw.gui.command.auth.AuthProvider;
import com.cyanspring.cstw.gui.command.auth.ViewAuthListener;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(ApplicationWorkbenchWindowAdvisor.class);
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1024, 768));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setTitle("Cyan Spring Trader Workstation");
		Business.getInstance().getEventManager().subscribe(SelectUserAccountEvent.class, this);
	}
	
	@Override
	public void postWindowOpen() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		
		try {
			
			//add listener
			ViewAuthListener authListener =new  ViewAuthListener();
			page.addPartListener(authListener);
			if(Business.getInstance().isLoginRequired()) {
				LoginDialog loginDialog = new LoginDialog(window.getShell());
				loginDialog.open();
			}
			
			setUserAccount(Business.getInstance().getUser(), Business.getInstance().getAccount());
			
			// check login role
			log.info("fire account login menu change :{}",Business.getInstance().getUserGroup().getRole().name());
			ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
			AuthProvider commandStateService = (AuthProvider) sourceProviderService
			        .getSourceProvider(Business.getInstance().getUserGroup().getRole().name());
			commandStateService.fireAccountChanged();

			// filter already open view 
			IWorkbenchPage pages[] =  getWindowConfigurer().getWindow().getPages();
			for(IWorkbenchPage activePage: pages){
				
				IViewReference vrs[] = activePage.getViewReferences();
				for(IViewReference vr : vrs){
					log.info("show view :{}",vr.getId());
					if(!Business.getInstance().hasViewAuth(vr.getPartName())){
						log.info("hide view :{}",vr.getId());
						IViewPart part = activePage.findView(vr.getId());
						log.info("part:{}",part.getViewSite().getId());
						part.getViewSite().getShell().setVisible(false);						
						activePage.hideView(vr);
						log.info("visible:{}",part.getViewSite().getShell().isVisible());

					}else{
						authListener.filterViewAllAction(vr.getPartName(), vr.getPart(true));
					}
					
				}
			}

			IActionBarConfigurer barConfig =  getWindowConfigurer().getActionBarConfigurer();
			ICoolBarManager coolBarManager = barConfig.getCoolBarManager();
			IContributionItem[] coolItems = coolBarManager.getItems();
			for(IContributionItem coolItem : coolItems){

				try {
					ToolBarContributionItem action = (ToolBarContributionItem)coolItem;
					IContributionItem[] toolItems = action.getToolBarManager().getItems();
					for(IContributionItem toolItem : toolItems){
						if(toolItem instanceof ActionContributionItem){
							ActionContributionItem actionItem =(ActionContributionItem) toolItem;
							if(actionItem.getId().equals("USER_INFO_ACTION")){
								actionItem.getAction().setText(Business.getInstance().getAccount()+" - "+Business.getInstance().getUserGroup().getRole().toString());
							}
							authListener.filterViewAction("Application View", actionItem);
						}
					}
				} catch (Exception e) {
					log.error("e:"+e.getMessage());
				}

			}
						
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean preWindowShellClose() {
		try {
			Business.getInstance().stop();
			Thread.sleep(300);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	private void setUserAccount(String user, String account) {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setTitle("Cyan Spring Trader Workstation - " + 
				user + "/" + account);
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof SelectUserAccountEvent) {
			setUserAccount(((SelectUserAccountEvent) event).getUser(), ((SelectUserAccountEvent) event).getAccount());
		}
		
	}

}
