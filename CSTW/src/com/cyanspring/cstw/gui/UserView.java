package com.cyanspring.cstw.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AllUserSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllUserSnapshotRequestEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.event.account.UserUpdateEvent;
import com.cyanspring.common.util.ArrayMap;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.GUIUtils;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;

public class UserView extends ViewPart implements IAsyncEventListener{

	private static final Logger log = LoggerFactory
			.getLogger(UserView.class);
	public static final String ID = "com.cyanspring.cstw.gui.UserViewer";
	private DynamicTableViewer viewer;
	private ImageRegistry imageRegistry;
	private Composite parentComposite = null;
	
	private AsyncTimerEvent refreshEvent = new AsyncTimerEvent();
	private long maxRefreshInterval = 10000;
	private final int autoRefreshLimitUser = 1000;

	private List<User> users = new ArrayList<User>();
	private ArrayMap <String,User> userMap = new ArrayMap<String,User>();
	private boolean columnCreated = false;
	
	private Action createGroupManagementAction;
	private Action createChangeRoleAction;
	private Action createManualRefreshAction;

	private GroupManagementDialog createGroupDialog;

	private final String ID_GROUP_MANAGEMENT_ACTION = "GROUP_MANAGEMENT";
	private final String ID_CHANGE_ROLE = "CHANGE_ROLE";
	private final String ID_MANUAL_REFRESH_ACTION = "MANUAL_REFRESH";

	@Override
	public void onEvent(AsyncEvent event) {
		if( event instanceof AllUserSnapshotReplyEvent){
			AllUserSnapshotReplyEvent reply = (AllUserSnapshotReplyEvent) event;
			users = reply.getUsers();
			setUserMap(reply.getUsers());
			
			if(users.size()<=autoRefreshLimitUser){
				if(!createManualRefreshAction.isChecked())
					createManualRefreshAction.setChecked(true);
				createManualRefreshAction.run();
			}
			
			showUsers();
		}else if(event instanceof UserUpdateEvent){
			UserUpdateEvent reply = (UserUpdateEvent) event;
			log.info("userupdate reply:{}",reply.getUser().getId());
			updateUser(reply.getUser());
		} else if (event instanceof AsyncTimerEvent) {
			showUsers();
		}
	}

	private void setUserMap(List<User> users) {
		for(User user:users){
			userMap.put(user.getId(), user);
		}
	}

	private void updateUser(User user) {
		userMap.put(user.getId(), user);
	}

	protected User findUser(TableItem item) {
		String id = item.getText(0);
		for(User user:users){
			if(id.equals(user.getId())){
				return user;
			}
		}
		return null;
	}
	
	private void showUsers() {
	
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (viewer) {
					if (viewer.isViewClosing())
						return;
					
					if (!columnCreated) {
						ArrayList <User> tempList = userMap.toArray();
						Object obj = tempList.get(0);
						List<ColumnProperty> properties = viewer
								.setObjectColumnProperties(obj);
						viewer.setSmartColumnProperties(obj.getClass().getName(),
								properties);
						viewer.setInput(tempList);
						columnCreated = true;
					}

					viewer.refresh();
				}
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		parentComposite = parent;
		imageRegistry = Activator.getDefault().getImageRegistry();
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		
		String strFile = Business.getInstance().getConfigPath()
				+ "UserTable.xml";
		viewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL, Business.getInstance()
				.getXstream(), strFile, BeanHolder.getInstance()
				.getDataConverter());
		viewer.init();

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		
		final Table table = viewer.getTable();
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {

				
			}
		});
		
		createManualRefreshToggleAction(parent);
		createGroupManagementAction(parent);
		createChangeRoleAction(parent);
		
		sendAllUserRequest();	
		subEvent(AllUserSnapshotReplyEvent.class);
		subEvent(UserUpdateEvent.class);
	}

	@Override
	public void dispose() {
		super.dispose();
		unSubEvent(AllUserSnapshotReplyEvent.class);
		unSubEvent(UserUpdateEvent.class);
		cancelScheduleJob(refreshEvent);
	}
	
	private void sendAllUserRequest() {
		AllUserSnapshotRequestEvent request = new AllUserSnapshotRequestEvent(ID, Business.getInstance().getFirstServer());
		sendRemoteEvent(request);
	}
	
	private TableItem getSelection(final Composite parent){
		
		TableItem items [] = viewer.getTable().getSelection();
		
		if( null == items ||  items.length == 0 ){
			showMessageBox("You have to select one user!", parent);
			return null;
		}
		
		if(items.length > 1){
			showMessageBox("Select just one user!", parent);
			return null;
		}
		
		return items[0];
	}
	
	private void createChangeRoleAction(final Composite parent) {
		
		createChangeRoleAction = new Action() {
			public void run() {
				
				TableItem item = getSelection(parent);			
				if( null == item)
					return ; 
				
				User user = (User)item.getData();		
				if( null == user){
					showMessageBox("can't find user", parent);
					return;
				}
				
				ChangeRoleDialog dialog = new ChangeRoleDialog(parent.getShell(),user);
				dialog.open();
			}
		};
		createChangeRoleAction.setId(ID_CHANGE_ROLE);
		createChangeRoleAction.setText("Change Role");
		createChangeRoleAction.setToolTipText("Change Role");
		
		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.ROLE_ICON.toString());
		createChangeRoleAction.setImageDescriptor(imageDesc);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createChangeRoleAction);		
	}

	private void createGroupManagementAction(final Composite parent) {

		createGroupManagementAction = new Action() {
			public void run() {
				
				TableItem item = getSelection(parent);
				if( null == item)
					return ;
				
				User user = (User)item.getData();
				if(null == user.getRole()){
					GUIUtils.showMessageBox(""+user.getId()+" role is empty.", parent);
					return ;
				}
				
				if( UserRole.Trader.equals(user.getRole())){
					GUIUtils.showMessageBox(""+user.getId()+" is a Trader that can't manage someone else.", parent);
					return ;
				}
				
				createGroupDialog = new GroupManagementDialog(parent.getShell(),item.getText(0),userMap.toArray());
				createGroupDialog.open();
			}
		};
		createGroupManagementAction.setId(ID_GROUP_MANAGEMENT_ACTION);
		createGroupManagementAction.setText("Group Management");
		createGroupManagementAction.setToolTipText("Group Management");
		
		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.PEOPLE_ICON.toString());
		createGroupManagementAction.setImageDescriptor(imageDesc);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createGroupManagementAction);
		
	}
	
	private void createManualRefreshToggleAction(final Composite parent) {

		createManualRefreshAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				if(!createManualRefreshAction.isChecked()) {
					cancelScheduleJob(refreshEvent);
				} else { 
					scheduleJob(refreshEvent, maxRefreshInterval);
				}

			}
		};
		createManualRefreshAction.setId(ID_MANUAL_REFRESH_ACTION);
		createManualRefreshAction.setChecked(false);		
		createManualRefreshAction.setText("AutoRefresh");
		createManualRefreshAction.setToolTipText("AutoRefresh");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.REFRESH_ICON.toString());
		createManualRefreshAction.setImageDescriptor(imageDesc);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createManualRefreshAction);
	}
	
	@Override
	public void setFocus() {
		
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz, this);		
	}
	
	private void unSubEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().unsubscribe(clazz,ID, this);		
	}
	
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void scheduleJob(AsyncTimerEvent timerEvent,
			long maxRefreshInterval) {
		Business.getInstance().getScheduleManager().scheduleRepeatTimerEvent(maxRefreshInterval,
				UserView.this, timerEvent);
	}

	private void cancelScheduleJob(AsyncTimerEvent timerEvent) {
		Business.getInstance().getScheduleManager().cancelTimerEvent(timerEvent);		
	}
	
	private void showMessageBox(final String msg, Composite parent){
		parent.getDisplay().asyncExec(new Runnable(){

			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(parentComposite.getShell(),
						SWT.ICON_INFORMATION);
				messageBox.setText("Info");
				messageBox.setMessage(msg);
				messageBox.open();				
			}
			
		});
	}
}
