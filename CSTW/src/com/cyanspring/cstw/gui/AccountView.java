package com.cyanspring.cstw.gui;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllAccountSnapshotRequestEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.util.ArrayMap;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;
import com.cyanspring.cstw.event.SelectUserAccountEvent;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class AccountView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(AccountView.class);
	public static final String ID = "com.cyanspring.cstw.gui.AccountViewer";
	private DynamicTableViewer viewer;
	private ArrayMap<String, Account> accounts = new ArrayMap<String, Account>();
	
	private boolean columnCreated;
	private Menu menu;
	private CreateUserDialog createUserDialog;
	private Action createUserAction;
	private Action createCountAccountAction;
	private ImageRegistry imageRegistry;
	
	@Override
	public void createPartControl(Composite parent) {
		log.info("Creating account view");
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();
		
		// create parent layout
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		String strFile = Business.getInstance().getConfigPath() + "AccountTable.xml";
		viewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
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
					Account account = (Account)obj;
					Business.getInstance().getEventManager().
						sendEvent(new SelectUserAccountEvent(account.getUserId(), account.getId()));
					Business.getInstance().getEventManager().
						sendEvent(new AccountSelectionEvent(account.getId()));
				}
			}
		});

		createUserAccountAction(parent);
		createCountAccountAction(parent);
		//createMenu(parent);
		
		if(Business.getInstance().getOrderManager().isReady())
			sendSubscriptionRequest();
		else
			Business.getInstance().getEventManager().subscribe(OrderCacheReadyEvent.class, this);
	}

	private void createUserAccountAction(final Composite parent) {
		createUserDialog = new CreateUserDialog(parent.getShell());
		// create local toolbars
		createUserAction = new Action() {
			public void run() {
				//orderDialog.open();
				createUserDialog.open();
			}
		};
		createUserAction.setText("Creat a user & account");
		createUserAction.setToolTipText("Create a user & account");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PLUS_ICON.toString());
		createUserAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createUserAction);
	}
	
	private void createCountAccountAction(final Composite parent) {
		// create local toolbars
		createCountAccountAction = new Action() {
			public void run() {
				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_INFORMATION);
				messageBox.setText("Info");
				messageBox.setMessage("Number of accounts: " + accounts.size());
				messageBox.open();
			}
		};
		createCountAccountAction.setText("Check number of accounts");
		createCountAccountAction.setToolTipText("Check number of accounts");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.TRUE_ICON.toString());
		createCountAccountAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createCountAccountAction);
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
					for(TableItem item: items) {
						Object obj = item.getData();
						if (obj instanceof Account) {
							Account account = (Account)obj;
							Business.getInstance().getEventManager().
								sendEvent(new SelectUserAccountEvent(account.getUserId(), account.getId()));
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
		viewer.setBodyMenu(menu);
	}

	private void sendSubscriptionRequest() {
		if(Business.getInstance().isLoginRequired())
			return;
		Business.getInstance().getEventManager().subscribe(AccountUpdateEvent.class, 
				this);
		Business.getInstance().getEventManager().subscribe(AccountDynamicUpdateEvent.class, 
				this);
		Business.getInstance().getEventManager().subscribe(AllAccountSnapshotReplyEvent.class, ID,
				this);
		
		AllAccountSnapshotRequestEvent request = new AllAccountSnapshotRequestEvent(ID, Business.getInstance().getFirstServer());
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void createOpenPositionColumns(List<Account> list) {
		if(!columnCreated) {
			Object obj = list.get(0);
			List<ColumnProperty> properties = viewer.setObjectColumnProperties(obj);		
			viewer.setSmartColumnProperties(obj.getClass().getName(), properties);
			viewer.setInput(list);
			columnCreated = true;
		}
	}

	private void showOpenPositions() {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(viewer) {
					if(viewer.isViewClosing())
						return;
					if(accounts.toArray().size() > 0)
						createOpenPositionColumns(accounts.toArray());
					viewer.refresh();
				}
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	private void processAccountUpdate(Account account) {
		accounts.put(account.getId(), account);
		showOpenPositions();
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof OrderCacheReadyEvent) {
			sendSubscriptionRequest();
		} else if (event instanceof AllAccountSnapshotReplyEvent) {
			AllAccountSnapshotReplyEvent evt = (AllAccountSnapshotReplyEvent)event;
			for(Account account: evt.getAccounts()) {
				accounts.put(account.getId(), account);
			}
			log.info("Loaded accounts: " + evt.getAccounts().size());
			showOpenPositions();
		} else if (event instanceof AccountUpdateEvent) {
			processAccountUpdate(((AccountUpdateEvent)event).getAccount());
		} else if (event instanceof AccountDynamicUpdateEvent) {
			processAccountUpdate(((AccountDynamicUpdateEvent)event).getAccount());
		}
	}

}
