package com.cyanspring.cstw.gui.command.auth;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.event.system.SuspendServerEvent;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.GUIUtils;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.gui.common.StyledAction;
import com.cyanspring.cstw.ui.views.ConfirmPasswordDialog;

public class WorkbenchActionProvider {
	
	private static final Logger log = LoggerFactory.getLogger(WorkbenchActionProvider.class);
	private static WorkbenchActionProvider wp;
	private Map <String,ToolBarManager> tbMap = new HashMap <String,ToolBarManager>();
	
	public final static String ID_TOOLBARMANAGER_COOLBAR = "TOOLBARMANAGER_COOLBAR";
	private final String ID_SUSPEND_SYSTEM_ACTION = "SUSPEND_SYSTEM_ACTION";
	private final String ID_SERVER_SHUTDOWN_ACTION = "SERVER_SHUTDOWN_ACTION";
	private final String ID_USER_INFO_ACTION = "USER_INFO_ACTION";
	
	private Action suspendSystemAction;
	private Action serverShutdownAction;
	private Action userInfoAction;
	public static ActionContributionItem  userInfoItem;
	
	private ImageRegistry imageRegistry;
	
	
	private WorkbenchActionProvider() {
		imageRegistry = Activator.getDefault().getImageRegistry();
	}
	
	public static WorkbenchActionProvider getInstance(){
		if(null == wp)
			wp = new WorkbenchActionProvider();
		
		return wp;
	}
	
	public ToolBarManager produceToolBarManager(String id){
		if(!StringUtils.hasText(id)){
			log.info("id is empty");
			return null;
		}
		
		ToolBarManager tempTB = new ToolBarManager();
		tbMap.put(id, tempTB);
		return tempTB;
	}
	
	public ToolBarManager getWorkbenchCoolBarActions(){
		
		createSuspendSystemAction();
		createUserInfoAction();
		createServerShutdownAction();	
		
		ToolBarManager toolBarManager = WorkbenchActionProvider.getInstance().produceToolBarManager(ID_TOOLBARMANAGER_COOLBAR);
		if(null == toolBarManager){
			return null;
		}
		toolBarManager.add(userInfoItem);
		toolBarManager.add(new Separator());
		toolBarManager.add(suspendSystemAction);
		toolBarManager.add(serverShutdownAction);
		return toolBarManager;
	}
	
	
	public ToolBarManager getToolBarManager(String id){
		if(tbMap.containsKey(id)){
			return tbMap.get(id);
		}else{
			return null;
		}
	}
	
	
	public void createUserInfoAction() {
		userInfoAction = new StyledAction("",org.eclipse.jface.action.IAction.AS_UNSPECIFIED) {
		};
		userInfoAction.setId(ID_USER_INFO_ACTION);
		userInfoAction.setText(Business.getInstance().getUser()+" - "+Business.getInstance().getUserGroup().getRole().toString());
		userInfoAction.setDescription("");
		userInfoAction.setToolTipText("");
		userInfoAction.setImageDescriptor(imageRegistry.getDescriptor(ImageID.USER_ICON.toString()));
		userInfoItem = new ActionContributionItem(userInfoAction);
		userInfoItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		userInfoAction.setEnabled(false);
	}

	public void createSuspendSystemAction() {
		suspendSystemAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				
				if(!Business.getInstance().getUserGroup().isAdmin()){
					GUIUtils.showMessageBox("Only Admin can do this action", PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					return;
				}						
				boolean suspend;
				if (this.isChecked()) {
					suspend = true;
					boolean isOk = MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "", "suspend server?");
					if (!isOk) {
						suspendSystemAction.setChecked(false);
						return;
					}
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

	public void createServerShutdownAction() {
		serverShutdownAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_PUSH_BUTTON) {
			public void run() {
				if(!Business.getInstance().getUserGroup().isAdmin()){
					GUIUtils.showMessageBox("Only Admin can do this action", PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					return;
				}	
				
				boolean isOk = MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "", "Shutdown server?");
				if (!isOk) {
					return;
				}

				try {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					ConfirmPasswordDialog confirmPasswordDialog =
							new ConfirmPasswordDialog(window.getShell());
					confirmPasswordDialog.open();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		};
		serverShutdownAction.setId(ID_SERVER_SHUTDOWN_ACTION);
		serverShutdownAction.setImageDescriptor(imageRegistry.getDescriptor(ImageID.POWER_ICON.toString()));
	}
}
