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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.ISourceProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.event.SelectUserAccountEvent;
import com.cyanspring.cstw.gui.command.auth.AuthProvider;

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
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(true);
		configurer.setTitle("Cyan Spring Trader Workstation");
		Business.getInstance().getEventManager().subscribe(SelectUserAccountEvent.class, this);
	}
	
	@Override
	public void postWindowOpen() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		
		try {
			
//			page.showView(SingleOrderStrategyView.ID);
//			page.showView(SingleInstrumentStrategyView.ID);
//			page.showView(ChildOrderView.ID);
//			page.showView(PropertyView.ID);
//			page.showView(StrategyLogView.ID);
//			page.showView(PositionView.ID);
			if(Business.getInstance().isLoginRequired()) {
				LoginDialog loginDialog = new LoginDialog(window.getShell());
				loginDialog.open();
			}
			
			
			setUserAccount(Business.getInstance().getUser(), Business.getInstance().getAccount());
			
			
			log.info("fire account login menu change");
			ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
			AuthProvider commandStateService = (AuthProvider) sourceProviderService
			        .getSourceProvider(AuthProvider.ADMIN);
			commandStateService.fireAccountChanged();
			
//			Menu menu = window.getShell().getMenuBar();
//			MenuItem mi[] = menu.getItems();
//			log.info("get menu");
//			for(MenuItem m : mi){
//				log.info("menu:{}",m.getText());
//				Menu menu2 = m.getMenu();
//				if(menu2 != null){
//					MenuItem m2[] = menu2.getItems();
//					log.info("item size:{}",menu2.getItemCount());
//					for(MenuItem mi2 :m2){
//						log.info("menu2:{}",mi2.getText());
//					}
//				}else{
//					log.info("menu2 is null");
//				}
//			}
				
			IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
			

			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

			
//			MenuItem mi[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getMenuBar().getItems();
//			log.info("get menu");
//			for(MenuItem m : mi){
//				log.info("menu:{}",m.getText());
//				m.
//				Menu menu2 = m.getMenu();
//				if(menu2 != null){
//					MenuItem m2[] = menu2.getItems();
//					log.info("item size:{}",menu2.getItemCount());
//					for(MenuItem mi2 :m2){
//						log.info("menu2:{}",mi2.getText());
//					}
//				}else{
//					log.info("menu2 is null");
//				}
//			}
			
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
