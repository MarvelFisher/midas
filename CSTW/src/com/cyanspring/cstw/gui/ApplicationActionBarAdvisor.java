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

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.action.Action;
import org.eclipse.wb.swt.ResourceManager;
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
	private StyledAction action;
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

		action = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX){
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
		action.setImageDescriptor(imageRegistry.getDescriptor(ImageID.ALERT_ICON.toString()));
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		super.fillCoolBar(coolBar);

		ToolBarManager toolBarManager = new ToolBarManager();
		coolBar.add(toolBarManager);
		toolBarManager.add(action);
	}

	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		ServerStatusDisplay.getInstance().setStatusLineManager(statusLine);
	}

}
