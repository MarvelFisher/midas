package com.cyanspring.cstw.gui;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.AccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.ActiveAccountReplyEvent;
import com.cyanspring.common.event.account.ActiveAccountRequestEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.order.StrategySnapshotRequestEvent;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.ArrayMap;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.event.SelectUserAccountEvent;
import com.cyanspring.cstw.event.ServerStatusEvent;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;

public class AccountView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(AccountView.class);
	public static final String ID = "com.cyanspring.cstw.gui.AccountViewer";
	private DynamicTableViewer viewer;
	private ArrayMap<String, Account> accounts = new ArrayMap<String, Account>();
	private Account currentAccount = null;
	private boolean columnCreated;
	private Menu menu;
	private CreateUserDialog createUserDialog;
	private Action createUserAction;
	private Action createCountAccountAction;
	private Action createManualRefreshAction;
	private Action createSearchIdAction;
	private Action createActiveAccountAction;
	private Composite searchBarComposite;
	private GridData gd_searchBar;
	private Text searchText;
	private Button searchButton;
	private ImageRegistry imageRegistry;
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long maxRefreshInterval = 10000;
	private boolean show;
	
	private final RGB PURPLE = new RGB(171,130,255);
	private final RGB WHITE = new RGB(255,255,255);
	private final RGB GRAY = new RGB(181,181,181);
	private final Color FROZEN_COLOR = new Color(Display.getCurrent(), PURPLE);
	private final Color TERMINATE_COLOR = new Color(Display.getCurrent(), GRAY);
	private final Color NORMAL_COLOR = new Color(Display.getCurrent(), WHITE);
	private final String COLUMN_STATE="State";
	private Composite parentComposite = null;
	@Override
	public void createPartControl(Composite parent) {
		log.info("Creating account view");
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();
		parentComposite = parent;
		// create parent layout
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		
		initSearchBar(parent);

		String strFile = Business.getInstance().getConfigPath()
				+ "AccountTable.xml";
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
				TableItem item = table.getItem(table.getSelectionIndex());
				Object obj = item.getData();
				if (obj instanceof Account) {
					Account account = (Account) obj;
					currentAccount = account;
					Business.getInstance()
							.getEventManager()
							.sendEvent(
									new SelectUserAccountEvent(account
											.getUserId(), account.getId()));
					Business.getInstance()
							.getEventManager()
							.sendEvent(
									new AccountSelectionEvent(account.getId()));

					try {
						Business.getInstance()
								.getEventManager()
								.sendRemoteEvent(
										new StrategySnapshotRequestEvent(
												account.getId(), Business
														.getInstance()
														.getFirstServer(), null));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		});
		createManualRefreshToggleAction(parent);
		createUserAccountAction(parent);
		createCountAccountAction(parent);
		createResetAccountAction(parent);
		createActiveAccountAction(parent);
		createSearchIdAction(parent);
		if (Business.getInstance().isFirstServerReady())
			sendSubscriptionRequest(Business.getInstance().getFirstServer());
		else
			Business.getInstance().getEventManager()
					.subscribe(ServerStatusEvent.class, this);

		log.info("no auto refresh account version");
//		 Business.getInstance().getScheduleManager().scheduleRepeatTimerEvent(maxRefreshInterval,
//		 this, timerEvent);

	}
	
	private void initSearchBar(Composite parent) {
		searchBarComposite = new Composite(parent, SWT.NONE);
		gd_searchBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		searchBarComposite.setLayoutData(gd_searchBar);
		searchBarComposite.setLayout(new GridLayout(2, false));

		searchText = new Text(searchBarComposite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, false, false, 1,
				1);
		gd_text.widthHint = 350;
		searchText.setLayoutData(gd_text);

		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					searchText();
				}
			}
		});

		searchButton = new Button(searchBarComposite, SWT.NONE);
		searchButton.setText("search");
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchText();
			}
		});
		searchBarComposite.setVisible(false);
		gd_searchBar.heightHint = 0;
		parent.layout();
	}
	
	private void searchText() {
		String textValue = searchText.getText();
		for (int i = 0; i < viewer.getTable().getItemCount(); i++) {
			Account account = (Account) viewer.getTable().getItem(i).getData();
			if (account.getId().toUpperCase().contains(textValue.toUpperCase())) {
				viewer.getTable().setSelection(i);
			}
		}

	}

	private void createManualRefreshToggleAction(final Composite parent) {

		createManualRefreshAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				showMessageBox("createManualRefreshAction.isChecked():"+createManualRefreshAction.isChecked(), parent);
				if(!createManualRefreshAction.isChecked()) {
					log.info("into cancel");
					//Business.getInstance().getScheduleManager().cancelTimerEvent(timerEvent);
				} else { 
					log.info("into schedule");
					Business.getInstance().getScheduleManager().scheduleRepeatTimerEvent(maxRefreshInterval,
							AccountView.this, timerEvent);
				}

			}
		};
		
		createManualRefreshAction.setChecked(false);		
		createManualRefreshAction.setText("AutoRefresh");
		createManualRefreshAction.setToolTipText("AutoRefresh");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.REFRESH_ICON.toString());
		createManualRefreshAction.setImageDescriptor(imageDesc);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createManualRefreshAction);
	}
	
	private void createUserAccountAction(final Composite parent) {
		createUserDialog = new CreateUserDialog(parent.getShell());
		// create local toolbars
		createUserAction = new Action() {
			public void run() {
				// orderDialog.open();
				createUserDialog.open();
			}
		};
		createUserAction.setText("Creat a user & account");
		createUserAction.setToolTipText("Create a user & account");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.PLUS_ICON.toString());
		createUserAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createUserAction);
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
	
	private void createActiveAccountAction(final Composite parent) {
		// create local toolbars
		createActiveAccountAction = new Action() {
			public void run() {
				if( null == currentAccount ){
					showMessageBox("You need select a account",parent);
				}else{
					if(AccountState.ACTIVE.equals(currentAccount.getState())){
						showMessageBox("this account is already in ACTIVE state",parent);
						return;
					}
					if(!StringUtils.hasText(currentAccount.getId())){
						showMessageBox("account id is empty",parent);
						return;
					}
					log.info("ActiveAccountRequestEvent currentAccount:{}",currentAccount.getId());
					ActiveAccountRequestEvent event = new ActiveAccountRequestEvent(ID, Business.getInstance().getFirstServer(), currentAccount.getId()); 
					try {
						Business.getInstance().getEventManager().sendRemoteEvent(event);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		};
		createActiveAccountAction.setText("Active Account");
		createActiveAccountAction.setToolTipText("Active Account");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.ACTIVE_ICON.toString());
		createActiveAccountAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createActiveAccountAction);
	}
	private void createCountAccountAction(final Composite parent) {
		// create local toolbars
		createCountAccountAction = new Action() {
			public void run() {
				MessageBox messageBox = new MessageBox(parent.getShell(),
						SWT.ICON_INFORMATION);
				messageBox.setText("Info");
				messageBox.setMessage("Number of accounts: " + accounts.size());
				messageBox.open();
			}
		};
		createCountAccountAction.setText("Check number of accounts");
		createCountAccountAction.setToolTipText("Check number of accounts");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.TRUE_ICON.toString());
		createCountAccountAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createCountAccountAction);
	}

	private void createResetAccountAction(final Composite parent) {
		// create local toolbars
		createCountAccountAction = new Action() {
			public void run() {
				Table table = viewer.getTable();
				TableItem items[] = table.getSelection();
				Account account = null;
				if (items.length > 0) {
					TableItem item = items[0];
					Object obj = item.getData();
					if (obj instanceof Account) {
						account = (Account) obj;
					}
				}

				if (null == account)
					return;

				boolean result = MessageDialog.openConfirm(parent.getShell(),
						"Reset account", "Are you sure to reset account: "
								+ account.getId());

				if (result) {
					try {
						Business.getInstance()
								.getEventManager()
								.sendRemoteEvent(
										new ResetAccountRequestEvent(ID,
												Business.getInstance()
														.getFirstServer(),
												account.getId(), IdGenerator
														.getInstance()
														.getNextID(), account
														.getUserId(), Default
														.getMarket(), null));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}

			}
		};
		createCountAccountAction.setText("reset account");
		createCountAccountAction.setToolTipText("Reset account");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.CANCEL_ICON.toString());
		createCountAccountAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createCountAccountAction);

	}
	
	private void createSearchIdAction(final Composite parent) {
		createSearchIdAction = new Action() {
			public void run() {
				searchBarComposite.setVisible(!searchBarComposite.isVisible());
				if (searchBarComposite.isVisible()) {
					gd_searchBar.heightHint = 40;
				} else {
					gd_searchBar.heightHint = 0;
				}
				parent.layout();
			}
		};
		createSearchIdAction.setToolTipText("Search Id from table");
		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.FILTER_ICON.toString());
		createSearchIdAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createSearchIdAction);
	}

	private void createMenu(Composite parent) {
		menu = new Menu(viewer.getTable().getShell(), SWT.POP_UP);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Set current account");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					Table table = viewer.getTable();
					TableItem items[] = table.getSelection();
					for (TableItem item : items) {
						Object obj = item.getData();
						if (obj instanceof Account) {
							Account account = (Account) obj;
							Business.getInstance()
									.getEventManager()
									.sendEvent(
											new SelectUserAccountEvent(account
													.getUserId(), account
													.getId()));
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
		viewer.setBodyMenu(menu);
	}

	private void sendSubscriptionRequest(String server) {
		if (Business.getInstance().isLoginRequired())
			return;
		Business.getInstance().getEventManager()
				.subscribe(AccountUpdateEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(AccountDynamicUpdateEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(AllAccountSnapshotReplyEvent.class, ID, this);
		Business.getInstance().getEventManager()
				.subscribe(ActiveAccountReplyEvent.class, ID, this);

		AllAccountSnapshotRequestEvent request = new AllAccountSnapshotRequestEvent(
				ID, server);
		log.debug("AllAccountSnapshotRequestEvent sent");
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void createOpenPositionColumns(List<Account> list) {
		if (!columnCreated) {
			Object obj = list.get(0);
			List<ColumnProperty> properties = viewer
					.setObjectColumnProperties(obj);
			viewer.setSmartColumnProperties(obj.getClass().getName(),
					properties);
			viewer.setInput(list);
			columnCreated = true;
		}
	}

	private void setBackgroundColorFromState(){

		viewer.getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				
				TableColumn columns[] = viewer.getTable().getColumns();
				int stateColumn = -1;
				for(int i=0 ; i < columns.length ; i++){
					if(COLUMN_STATE.equals(columns[i].getText())){
						stateColumn = i;
						break;
					}
				}
				if(stateColumn == -1){
					log.error("can't find state column");
					return;
				}
				
				for(TableItem item : viewer.getTable().getItems()){	
					String state = item.getText(stateColumn);
					String account = item.getText(0);
					log.info("account:{}, state:{}",account,state);
					if( AccountState.FROZEN.name() == state ){
						item.setBackground(FROZEN_COLOR);
					}else if( AccountState.TERMINATED.name() == state){
						item.setBackground(TERMINATE_COLOR);
					}else{
						item.setBackground(NORMAL_COLOR);
					}
				}
				viewer.refresh();
			}
		});
	}
	
	private void showAccounts() {
		if (!show)
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (viewer) {
					if (viewer.isViewClosing())
						return;
					if (accounts.toArray().size() > 0)
						createOpenPositionColumns(accounts.toArray());

					viewer.refresh();
				}
			}
		});
		show = false;
		setBackgroundColorFromState();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void processAccountUpdate(Account account) {
		log.info("update account:{}",account.getId());
		accounts.put(account.getId(), account);
		show = true;
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof ServerStatusEvent) {
			sendSubscriptionRequest(((ServerStatusEvent) event).getServer());
		} else if (event instanceof AllAccountSnapshotReplyEvent) {
			AllAccountSnapshotReplyEvent evt = (AllAccountSnapshotReplyEvent) event;
			for (Account account : evt.getAccounts()) {
				accounts.put(account.getId(), account);
			}
			log.info("Loaded accounts: " + evt.getAccounts().size());
			show = true;
			showAccounts();
		} else if (event instanceof AccountUpdateEvent) {
			log.info("get account update:{},{}",((AccountUpdateEvent) event).getAccount().getId(),((AccountUpdateEvent) event).getAccount().getState());

			processAccountUpdate(((AccountUpdateEvent) event).getAccount());
		} else if (event instanceof AccountDynamicUpdateEvent) {
			log.info("get account dynamic update:{},{}",((AccountDynamicUpdateEvent) event).getAccount().getId(),((AccountDynamicUpdateEvent) event).getAccount().getState());
			processAccountUpdate(((AccountDynamicUpdateEvent) event)
					.getAccount());
		} else if (event instanceof AsyncTimerEvent) {
			log.info("refresh event");
			show = true;
			showAccounts();
		}else if (event instanceof ActiveAccountReplyEvent){
			log.info("receive ActiveAccountReplyEvent");
			ActiveAccountReplyEvent replyEvent = (ActiveAccountReplyEvent) event;
			if(replyEvent.isOk()){
				showMessageBox(replyEvent.getAccount()+" set to ACTIVE success!", parentComposite);
				String id = replyEvent.getAccount();
				Account account = accounts.get(id);
				account.setState(AccountState.ACTIVE);
				accounts.put(id,account);
				show = true;
				showAccounts();
			}else{
				MessageBean bean = MessageLookup.getMsgBeanFromEventMessage(replyEvent.getMessage());
				showMessageBox(bean.getLocalMsg(), parentComposite);
			}
		}
	}

}
