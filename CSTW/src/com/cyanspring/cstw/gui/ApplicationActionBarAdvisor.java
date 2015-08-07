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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.system.SuspendServerEvent;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.common.StyledAction;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private final static Logger log = LoggerFactory.getLogger(ApplicationActionBarAdvisor.class);
	
	private Action suspendSystemAction;
	
	private Action userInfoAction;
	private ActionContributionItem  userInfoItem; 
	private final String ID_SUSPEND_SYSTEM_ACTION = "SUSPEND_SYSTEM_ACTION";
	private final String ID_USER_INFO_ACTION = "USER_INFO_ACTION";
	private ImageRegistry imageRegistry;
	
	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		imageRegistry = Activator.getDefault().getImageRegistry();
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		
		createSuspendSystemAction();
		createUserInfoAction();
		
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		ToolBarManager toolBarManager = new ToolBarManager();
		coolBar.add(toolBarManager);
		toolBarManager.add(userInfoItem);
		toolBarManager.add(suspendSystemAction);
		super.fillCoolBar(coolBar);
	}

	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		ServerStatusDisplay.getInstance().setStatusLineManager(statusLine);
	}
	
	public void createUserInfoAction(){
		userInfoAction = new StyledAction("",org.eclipse.jface.action.IAction.AS_UNSPECIFIED) {
		};
		userInfoAction.setId(ID_USER_INFO_ACTION);
		userInfoAction.setText(Business.getInstance().getAccount()+" - "+Business.getInstance().getUserGroup().getRole().toString());
		userInfoAction.setDescription("");
		userInfoAction.setToolTipText("");
		userInfoAction.setImageDescriptor(imageRegistry.getDescriptor(ImageID.USER_ICON.toString()));
		userInfoItem = new ActionContributionItem(userInfoAction);
		userInfoItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		userInfoAction.setEnabled(false);
	}
	
	public void createSuspendSystemAction(){
		suspendSystemAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX){
			public void run(){
				boolean suspend;
				if(this.isChecked()){
					suspend = true;
				} else {
					suspend = false;
				}
				
				try {
					Business.getInstance().getEventManager().sendRemoteEvent(
							new SuspendServerEvent(null, 
									Business.getInstance().getFirstServer(), suspend));
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		};
		suspendSystemAction.setId(ID_SUSPEND_SYSTEM_ACTION);
		suspendSystemAction.setImageDescriptor(imageRegistry.getDescriptor(ImageID.ALERT_ICON.toString()));
	}

}
