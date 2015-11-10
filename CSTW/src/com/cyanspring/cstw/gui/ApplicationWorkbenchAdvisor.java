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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.gui.assist.OpenSingleOrderStrategyViewAssist;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "CSTW.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);
	}

	@Override
	public void preStartup() {
		try {
			Business.getInstance().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.preStartup();
	}

	@Override
	public void postStartup() {
		Display.getDefault().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (e.keyCode == SWT.F1 || e.keyCode == SWT.F2
						|| e.keyCode == SWT.ESC || e.keyCode == SWT.F3
						|| e.keyCode == SWT.F4) {
					new OpenSingleOrderStrategyViewAssist().run(e.keyCode);
				}
			}
		});
	}

	@Override
	public boolean preShutdown() {
		// try {
		// Business.getInstance().stop();
		// Thread.sleep(100);
		// } catch (Exception e) {
		// log.error(e.getMessage(), e);
		// }
		return super.preShutdown();
	}
}
