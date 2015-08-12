package com.cyanspring.cstw.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.CreateGroupManagementEvent;
import com.cyanspring.common.event.account.CreateGroupManagementReplyEvent;
import com.cyanspring.common.event.account.DeleteGroupManagementEvent;
import com.cyanspring.common.event.account.DeleteGroupManagementReplyEvent;
import com.cyanspring.common.event.account.GroupManageeReplyEvent;
import com.cyanspring.common.event.account.GroupManageeRequestEvent;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;

public class GroupManagementDialog extends Dialog implements IAsyncEventListener{
	
	private static final Logger log = LoggerFactory
			.getLogger(GroupManagementDialog.class);

	private Button btnOk;
	private Button btnCancel;
	private Button btnAssign;
	private Button btnDeAssign;
	private Label lblManager;
	private Label lblMessage;
	private ImageRegistry imageRegistry;
	private String accountId;
	private String ID = IdGenerator.getInstance().getNextID();
	private UserGroup userGroup;
	private java.util.List<User> users = new ArrayList();
	private ArrayList <String>manageeList = new ArrayList<String>();
	private ArrayList <String>nonManageeList = new ArrayList<String>();
	private TableViewer manageeListView;
	private Table manageeTable;	
	private TableViewer nonManageeListView;
	private Table nonManageeTable;
	private TableViewer messageTableView;
	private Table messageTable;
	private Stack <String> messageStack = new Stack<String>();
	private Composite parent;
	private Composite container;
	private ReentrantLock displayLock = new ReentrantLock();
	
	private final RGB PURPLE = new RGB(171,130,255);
	private final RGB GREEN = new RGB(27,154,13);
	private final RGB RED = new RGB(179,0,0);
	private final Color RISK_MANAGER_COLOR = new Color(Display.getCurrent(), RED);
	private final Color TRADER_COLOR = new Color(Display.getCurrent(), GREEN);
	private final Color ADMIN_COLOR = new Color(Display.getCurrent(), PURPLE);

	protected GroupManagementDialog(Shell parentShell,String accountId,java.util.List<User> users) {
		super(parentShell);
		this.accountId = accountId;
		this.users = users;
		for(User user:users){
			if(null != user.getRole() && !UserRole.Admin.equals(user.getRole())){
				nonManageeList.add(user.getId());
			}
		}
		log.info("GroupManagementDialog set user:{}",accountId);
	}
	
	private void setManagerLabel(Label lbl,String account){
		lbl.setText("Manager : "+account);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
	
		imageRegistry = Activator.getDefault().getImageRegistry();
		this.parent = parent;
		Composite container = new Composite(parent, SWT.NONE);
		this.container = container;
		container.setLayout(new GridLayout(3, false));
		container.getShell().setText("Managee Assign");
		Label lblManagee = new Label(container, SWT.NONE);
		lblManagee.setText("Managee");
		
		lblManager = new Label(container, SWT.NONE);
		setManagerLabel(lblManager,accountId);
		
		Label lblNonAssignAccont = new Label(container, SWT.NONE);
		lblNonAssignAccont.setText("Non Assign User");
		
		manageeListView = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.SCROLLBAR_OVERLAY);
		manageeTable = manageeListView.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.widthHint=70;
		gd_table.heightHint=150;		
		manageeTable.setLayoutData(gd_table);
		
		Composite composite = new Composite(container, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 150;
		gd_composite.heightHint = 150;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		
		btnDeAssign = new Button(composite, SWT.NONE);
		btnAssign = new Button(composite, SWT.NONE);	
		ImageDescriptor backward_imageDesc = imageRegistry
				.getDescriptor(ImageID.BACKWARD_ICON.toString());
		ImageDescriptor forward_imageDesc = imageRegistry
				.getDescriptor(ImageID.FORWARD_ICON.toString());
		btnDeAssign.setImage(forward_imageDesc.createImage());
		btnAssign.setImage(backward_imageDesc.createImage());
		
		btnDeAssign.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doDeAssign();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		btnAssign.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAssign();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
			
		nonManageeListView= new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.SCROLLBAR_OVERLAY);
		nonManageeTable = nonManageeListView.getTable();
		
		GridData gd_table2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table2.widthHint=70;
		gd_table2.heightHint=150;
		nonManageeTable.setLayoutData(gd_table2);
		
		Label lblroleColor = new Label(container, SWT.NONE);
		GridData gdLblMessage4 = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblMessage4.horizontalIndent = 1;
		lblroleColor.setLayoutData(gdLblMessage4);
		lblroleColor.setText("Role Color :");
		
		Label lbladminColor = new Label(container, SWT.NONE);
		GridData gdLblMessage7 = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblMessage7.horizontalIndent = 1;
		lbladminColor.setLayoutData(gdLblMessage7);
		lbladminColor.setText("Admin");
		lbladminColor.setForeground(ADMIN_COLOR);
		
		Label lblriskColor = new Label(container, SWT.NONE);
		GridData gdLblMessage3 = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblMessage3.horizontalIndent = 1;
		lblriskColor.setLayoutData(gdLblMessage3);
		lblriskColor.setText("Risk Manager");
		lblriskColor.setForeground(RISK_MANAGER_COLOR);
		
		Label lbltraderColor = new Label(container, SWT.NONE);
		GridData gdLblMessage2 = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblMessage2.horizontalIndent = 1;
		lbltraderColor.setLayoutData(gdLblMessage2);
		lbltraderColor.setText("Trader");
		lbltraderColor.setForeground(TRADER_COLOR);
		
		lblMessage = new Label(container, SWT.NONE);
		GridData gdLblMessage = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblMessage.horizontalIndent = 1;
		lblMessage.setLayoutData(gdLblMessage);
		lblMessage.setText("");
		Color red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		lblMessage.setForeground(red);
		
		
		Label lblMsg = new Label(container, SWT.NONE);
		GridData gdLblMessage5 = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gdLblMessage5.horizontalIndent = 1;
		lblMsg.setLayoutData(gdLblMessage5);
		lblMsg.setText("Message :");
		
		messageTableView = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		messageTable = messageTableView.getTable();
		GridData gd_message = new GridData(SWT.FILL, SWT.CENTER, true,false, 3, 1);
		gd_message.horizontalIndent = 1;	
		gd_message.heightHint=70;
		messageTable.setLayoutData(gd_message);
		
		sendGroupManageeRequestEvent();		
		return super.createDialogArea(parent);
	}
	
	private void pushMessage(final String msg){
		log.info("push msg:{}",msg);
		messageStack.push(msg);
	}
	
	private void refreshMessage(){
		container.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				while(!messageStack.isEmpty()){
					messageTableView.add( messageStack.pop());
				}			
			}
		});
	}
	
	private void sendGroupManageeRequestEvent(){
		GroupManageeRequestEvent event = new GroupManageeRequestEvent(ID, Business.getInstance().getFirstServer(), accountId);
		sendRemoteEvent(event);
	}
	protected void doAssign() {
		IStructuredSelection sel = (IStructuredSelection)nonManageeListView.getSelection();
		if( null == sel )
			return ;
		
		
		manageeListView.add(sel.getFirstElement());
		nonManageeListView.remove(sel.getFirstElement());
		setRoleColor(manageeListView);
	}

	protected void doDeAssign() {
		IStructuredSelection sel = (IStructuredSelection)manageeListView.getSelection();
		if( null == sel )
			return ;
		nonManageeListView.add(sel.getFirstElement());
		manageeListView.remove(sel.getFirstElement());
		setRoleColor(nonManageeListView);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonArea =  new Composite(parent, SWT.NONE);		
		GridLayout layoutButtons = new GridLayout(2, true);
		layoutButtons.marginRight = 30;
		layoutButtons.marginLeft = 30;
		buttonArea.setLayout(layoutButtons);
		buttonArea.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		btnOk = new Button(buttonArea, SWT.PUSH);
		btnOk.setText("Confirm");
		btnOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendManageeSet();
			}
		});
		
		btnCancel = new Button(buttonArea, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GroupManagementDialog.this.close();
			}
		});

		return buttonArea;
	}
	
	private java.util.List<String> getList(Table table){
		java.util.List <String>datas = new ArrayList<String>();
		TableItem items[]= table.getItems();
		for(TableItem item : items){
			datas.add(item.getText(0));
		}
		return datas;
	}
	
	
	protected void sendManageeSet() {
		
		java.util.List<GroupManagement> groupList = new ArrayList<GroupManagement>();
		
		java.util.List <String>manageeList = getList(manageeTable);
		for( int i = 0 ; i<manageeList.size() ; i++){		
			String managee = manageeList.get(i);
			GroupManagement gm = new GroupManagement(accountId,managee);
			groupList.add(gm);
		}
		
		java.util.List<UserGroup> originManageeList = userGroup.getNoneRecursiveManageeList();
		java.util.List<GroupManagement> addList = new ArrayList<GroupManagement>();
		java.util.List<GroupManagement> delList = new ArrayList<GroupManagement>();
		
		for(GroupManagement changed : groupList ){
			String managee = changed.getManaged();
			boolean isExist = false;;
			for(UserGroup ug : originManageeList){
				if(ug.getUser().equals(managee)){
					isExist = true;
					break;
				}
			}
			if(!isExist)
				addList.add(changed);
		}
		
		for( UserGroup ug : originManageeList){
			String managee = ug.getUser();
			boolean isExist = false;
			for(GroupManagement changed : groupList){
				if(changed.getManaged().equals(managee)){
					isExist = true;
					break;
				}
			}
			if(!isExist)
				delList.add(new GroupManagement(userGroup.getUser(),ug.getUser()));
		}
			
//		for(GroupManagement gm :groupList){
//			log.info("group manager:{}, managee:{}",gm.getManager(),gm.getManaged());
//		}
//		
//		for(GroupManagement gm :addList){
//			log.info("add manager:{}, managee:{}",gm.getManager(),gm.getManaged());
//		}
//		
//		for(GroupManagement gm :delList){
//			log.info("del manager:{}, managee:{}",gm.getManager(),gm.getManaged());
//		}
		
		if(!addList.isEmpty()){
			CreateGroupManagementEvent addEvent = new CreateGroupManagementEvent(ID, Business.getInstance().getFirstServer(),addList);
			sendRemoteEvent(addEvent);
		}
		
		if(!delList.isEmpty()){
			DeleteGroupManagementEvent delEvent = new DeleteGroupManagementEvent(ID, Business.getInstance().getFirstServer(),delList);
			sendRemoteEvent(delEvent);
		}
	}

	@Override
	public int open() {
		subEvent(GroupManageeReplyEvent.class);
		subEvent(CreateGroupManagementReplyEvent.class);
		subEvent(DeleteGroupManagementReplyEvent.class);
		return super.open();
	}
	
	@Override
	public boolean close() {
		unSubEvent(GroupManageeReplyEvent.class);
		unSubEvent(CreateGroupManagementReplyEvent.class);
		unSubEvent(DeleteGroupManagementReplyEvent.class);
		return super.close();
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if( event instanceof GroupManageeReplyEvent){
			
			GroupManageeReplyEvent reply = (GroupManageeReplyEvent) event;
			if(reply.isOk()){
				this.userGroup = reply.getUserGroup();
				showManageeList();
			}else{
				final String msg = getReplyErrorMessage(reply.getMesssage());
				parent.getDisplay().asyncExec(new Runnable(){

					@Override
					public void run() {
						btnOk.setEnabled(false);
						lblMessage.setText(msg);	
					}
					
				});
				this.userGroup = reply.getUserGroup();
				showManageeList();
			}
		}else if(event instanceof CreateGroupManagementReplyEvent){
			
			CreateGroupManagementReplyEvent reply = (CreateGroupManagementReplyEvent) event;
			showConfirmResultOnLabel(reply.getResult(),true);
			sendGroupManageeRequestEvent();
		}else if(event instanceof DeleteGroupManagementReplyEvent){
			
			DeleteGroupManagementReplyEvent reply = (DeleteGroupManagementReplyEvent) event;
			showConfirmResultOnLabel(reply.getResult(),false);
			sendGroupManageeRequestEvent();
		}
	}
	
	private String getReplyErrorMessage(String msg){
		 MessageBean bean = MessageLookup.getMsgBeanFromEventMessage(msg);
		 return bean.getLocalMsg();
	}
	
	private void showConfirmResultOnLabel(Map <GroupManagement,String> resultMap, boolean isAdd){
		Set <Entry<GroupManagement,String>> entrys =  resultMap.entrySet();
		for(Entry <GroupManagement,String>entry:entrys){
			GroupManagement gm = entry.getKey();
			String msg = entry.getValue();
			if(isAdd)
				pushMessage("Add Managee:"+gm.getManaged()+"  Result:"+msg);
			else
				pushMessage("Delete Managee:"+gm.getManaged()+"  Result:"+msg);
		}
		refreshMessage();
	}
	
	private void clearViewList(){
		parent.getDisplay().syncExec(new Runnable(){

			@Override
			public void run() {			
				
				nonManageeListView.getTable().removeAll();
				manageeListView.getTable().removeAll();
				manageeList.clear();
				nonManageeList.clear();
			}
			
		});		
	}
	
	private void showManageeList() {
		
		try { 
			displayLock.lock();
			clearViewList();
			final ArrayList <String>tempList = new ArrayList<String>();
			java.util.List <UserGroup> manageeGroupList = userGroup.getNoneRecursiveManageeList();
			for(UserGroup ug : manageeGroupList){
				manageeList.add(ug.getUser());
			}

			for(User user : users){
				if(!manageeList.contains(user.getId())
						&& !accountId.equals(user.getId())
						&& null != user.getRole()
						&& !UserRole.Admin.equals(user.getRole())){
					tempList.add(user.getId());
				}
			}
		
//			log.info("manageeList:{}",manageeList.size());
//			log.info("tempList:{}",tempList.size());
			parent.getDisplay().syncExec(new Runnable(){

				@Override
				public void run() {			
					nonManageeListView.add(tempList.toArray(new String[tempList.size()]));
					manageeListView.add(manageeList.toArray(new String[manageeList.size()]));
					setRoleColor(nonManageeListView);
					setRoleColor(manageeListView);
				}			
			});		
		}finally{
			displayLock.unlock();
		}

	}
	
	private void setRoleColor(TableViewer view) {
		Table table = view.getTable();
		
		TableItem items[] = table.getItems();
		for(TableItem item:items){
			String id = item.getText(0);
			User tempUser = null;
			
			for(User user:users){
				if(user.getId().equals(id)){
					tempUser = user;
					break;
				}
			}

			if(null != tempUser){
				if(UserRole.Admin.equals(tempUser.getRole())){
					item.setForeground(0,ADMIN_COLOR);
				}else if(UserRole.RiskManager.equals(tempUser.getRole())){
					item.setForeground(0,RISK_MANAGER_COLOR);
				}else if(UserRole.Trader.equals(tempUser.getRole())){
					item.setForeground(0,TRADER_COLOR);
				}
			}
		}
	}

	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz,ID, this);		
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
}
