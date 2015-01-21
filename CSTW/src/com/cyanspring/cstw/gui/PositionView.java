package com.cyanspring.cstw.gui;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.ExecutionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class PositionView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(PositionView.class);
	public static final String ID = "com.cyanspring.cstw.gui.PositionViewer";
	private DynamicTableViewer openPositionViewer;
	private DynamicTableViewer closedPositionViewer;
	private DynamicTableViewer executionViewer;
	private Composite topComposite;
	private Menu menu;
	
	// display data
	private Account account;
	private List<OpenPosition> openPositions;
	private List<ClosedPosition> closedPositions;
	private List<Execution> executions;
	
	private DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
	private String currentAccount = Business.getInstance().getAccount();
	
	@Override
	public void createPartControl(Composite parent) {
		// create views
		final Composite mainComposite = new Composite(parent,SWT.BORDER);
    	//create top composite
		topComposite = new Composite(mainComposite,SWT.NONE);
		topComposite.setLayout(new FillLayout());
		createAccountInfoPad(topComposite);

		final Sash sash = new Sash (mainComposite, SWT.HORIZONTAL);
         //create bottom composite
		final Composite bottomComposite = new Composite(mainComposite,SWT.BORDER);
		bottomComposite.setLayout(new FillLayout());
		
		// setting up form layout
		mainComposite.setLayout (new FormLayout ());
		
		FormData topData = new FormData ();
		topData.left = new FormAttachment (0, 0);
		topData.right = new FormAttachment (100, 0);
		topData.top = new FormAttachment (0, 0);
		topData.bottom = new FormAttachment (sash, 0);
		topComposite.setLayoutData (topData);

		final int limit = 20;
		final FormData sashData = new FormData ();
		sashData.left = new FormAttachment (0, 0);
		sashData.right = new FormAttachment (100, 0);
		sashData.top = new FormAttachment (sash, 60);
		sash.setLayoutData (sashData);
		
		sash.addListener (SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				Rectangle sashRect = sash.getBounds ();
				Rectangle shellRect = mainComposite.getClientArea();
				int top = shellRect.height - sashRect.height - limit;
				e.y = Math.max (Math.min (e.y, top), limit);
				if (e.y != sashRect.y)  {
					sashData.top = new FormAttachment (0, e.y);
					mainComposite.layout ();
				}
			}
		});
		
		FormData bottomData = new FormData ();
		bottomData.left = new FormAttachment (0, 0);
		bottomData.right = new FormAttachment (100, 0);
		bottomData.top = new FormAttachment (sash, 0);
		bottomData.bottom = new FormAttachment (100, 0);
		bottomComposite.setLayoutData (bottomData);

		// create the left composite in the bottom composite
		Composite leftComposite = new Composite(bottomComposite,SWT.BORDER);
		leftComposite.setLayout(new GridLayout(1, true));
		Label lbOpenPosition = new Label(leftComposite, SWT.NONE);
		lbOpenPosition.setText("Open Positions");
		Composite openComposite = new Composite(leftComposite, SWT.NONE);
		openComposite.setLayout(new FillLayout());
		openComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createOpenPositionViewer(openComposite);

		final Sash sash1 = new Sash (bottomComposite, SWT.VERTICAL);
         //create mid composite
		Composite midComposite = new Composite(bottomComposite,SWT.BORDER);
		midComposite.setLayout(new GridLayout(1, true));
		Label lbClosedPosition = new Label(midComposite, SWT.NONE);
		lbClosedPosition.setText("Closed Positions");
		Composite closedComposite = new Composite(midComposite, SWT.NONE);
		closedComposite.setLayout(new FillLayout());
		closedComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createClosedPositionViewer(closedComposite);

		final Sash sash2 = new Sash (bottomComposite, SWT.VERTICAL);
        //create mid composite
		Composite rightComposite = new Composite(bottomComposite,SWT.BORDER);
		rightComposite.setLayout(new GridLayout(1, true));
		Label lbExecutionPosition = new Label(rightComposite, SWT.NONE);
		lbExecutionPosition.setText("Trades");
		Composite executionComposite = new Composite(rightComposite, SWT.NONE);
		executionComposite.setLayout(new FillLayout());
		executionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createExecutionViewer(executionComposite);

		// setting up form layout for bottom composite
		bottomComposite.setLayout (new FormLayout());
		
		FormData leftData = new FormData ();
		leftData.left = new FormAttachment (0, 0);
		leftData.right = new FormAttachment (sash1, 0);
		leftData.top = new FormAttachment (0, 0);
		leftData.bottom = new FormAttachment (100, 0);
		leftComposite.setLayoutData (leftData);

		final FormData sashData1 = new FormData ();
		sashData1.left = new FormAttachment (sash1, 300);
		sashData1.top = new FormAttachment (0, 0);
		sashData1.bottom = new FormAttachment (100, 0);
		sash1.setLayoutData (sashData1);
		
		sash1.addListener (SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				Rectangle sashRect = sash1.getBounds ();
				Rectangle shellRect = bottomComposite.getClientArea();
				int right = shellRect.width - sashRect.width - limit;
				e.x = Math.max (Math.min (e.x, right), limit);
				if (e.x != sashRect.x)  {
					sashData1.left = new FormAttachment (0, e.x);
					bottomComposite.layout ();
				}
			}
		});
		
		FormData midData = new FormData ();
		midData.left = new FormAttachment (sash1, 0);
		midData.right = new FormAttachment (sash2, 0);
		midData.top = new FormAttachment (0, 0);
		midData.bottom = new FormAttachment (100, 0);
		midComposite.setLayoutData (midData);

		final FormData sashData2 = new FormData ();
		sashData2.left = new FormAttachment (sash2, 600);
		sashData2.top = new FormAttachment (0, 0);
		sashData2.bottom = new FormAttachment (100, 0);
		sash2.setLayoutData (sashData2);
		
		sash2.addListener (SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				Rectangle sashRect = sash2.getBounds ();
				Rectangle shellRect = bottomComposite.getClientArea();
				int right = shellRect.width - sashRect.width - limit;
				e.x = Math.max (Math.min (e.x, right), limit);
				if (e.x != sashRect.x)  {
					sashData2.left = new FormAttachment (0, e.x);
					bottomComposite.layout ();
				}
			}
		});
		
		FormData rightData = new FormData ();
		rightData.left = new FormAttachment (sash2, 0);
		rightData.right = new FormAttachment (100, 0);
		rightData.top = new FormAttachment (0, 0);
		rightData.bottom = new FormAttachment (100, 0);
		rightComposite.setLayoutData (rightData);

		createMenu(leftComposite);
		
		// finally lay them out
		mainComposite.layout();
		
		if(!Business.getInstance().isLoginRequired())
			Business.getInstance().getEventManager().subscribe(AccountSelectionEvent.class, this);

		Business.getInstance().getEventManager().subscribe(AccountSnapshotReplyEvent.class, ID, this);

		if(Business.getInstance().getOrderManager().isReady())
			sendSubscriptionRequest(Business.getInstance().getAccount());
		else
			Business.getInstance().getEventManager().subscribe(OrderCacheReadyEvent.class, this);
	}
	
	// account fields
	private Label lbValue;
	private Label lbCash;
	private Label lbMargin;
	private Label lbDailyPnL;
	private Label lbPnL;
	private Label lbUrPnL;
	
	private void createAccountInfoPad(Composite parent) {
		GridLayout layout = new GridLayout(3, true);
		layout.marginRight = 30;
		layout.marginLeft = 30;
		layout.horizontalSpacing = 50;
		parent.setLayout(layout);
		
		Composite comp1 = new Composite(parent, SWT.NONE);
		comp1.setLayout(new GridLayout(2, true));
		comp1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		
		Composite comp2 = new Composite(parent, SWT.NONE);
		comp2.setLayout(new GridLayout(2, true));
		comp2.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		
		Composite comp3 = new Composite(parent, SWT.NONE);
		comp3.setLayout(new GridLayout(2, true));
		comp3.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));

		Label lb1 = new Label(comp1, SWT.LEFT);
		lb1.setText("Account value: ");
		lb1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbValue = new Label(comp1, SWT.RIGHT);
		lbValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lb2 = new Label(comp2, SWT.LEFT);
		lb2.setText("Cash value: ");
		lb2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbCash = new Label(comp2, SWT.RIGHT);
		lbCash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lb3 = new Label(comp3, SWT.LEFT);
		lb3.setText("Margin value: ");
		lb3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbMargin = new Label(comp3, SWT.RIGHT);
		lbMargin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lb4 = new Label(comp1, SWT.LEFT);
		lb4.setText("Daily P&&L: ");
		lb4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbDailyPnL = new Label(comp1, SWT.RIGHT);
		lbDailyPnL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lb5 = new Label(comp2, SWT.LEFT);
		lb5.setText("P&&L: ");
		lb5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbPnL = new Label(comp2, SWT.RIGHT);
		lbPnL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lb6 = new Label(comp3, SWT.LEFT);
		lb6.setText("Unrealized P&&L: ");
		lb6.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbUrPnL = new Label(comp3, SWT.RIGHT);
		lbUrPnL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	private void createOpenPositionViewer(Composite parent) {
	    String strFile = Business.getInstance().getConfigPath() + "OpenPositionTable.xml";
		openPositionViewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		openPositionViewer.init();
	}
	
	private void createClosedPositionViewer(Composite parent) {
	    String strFile = Business.getInstance().getConfigPath() + "ClosedPositionTable.xml";
		closedPositionViewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		closedPositionViewer.init();
	}
	
	private void createExecutionViewer(Composite parent) {
	    String strFile = Business.getInstance().getConfigPath() + "ExecutionPositionTable.xml";
		executionViewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		executionViewer.init();
	}
	
	private void createMenu(Composite parent) {
		menu = new Menu(openPositionViewer.getTable().getShell(), SWT.POP_UP);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Close position");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					Table table = openPositionViewer.getTable();

					TableItem items[] = table.getSelection();

					for(TableItem item: items) {
						Object obj = item.getData();
						if (obj instanceof OpenPosition) {
							OpenPosition position = (OpenPosition)obj;
							//TODO: we need to change when CSTW talks to multip servers
							String server = Business.getInstance().getFirstServer();
							
							ClosePositionRequestEvent request = new ClosePositionRequestEvent(position.getAccount(), 
									server, position.getAccount(), 
									position.getSymbol(), OrderReason.ManualClose, 
									IdGenerator.getInstance().getNextID());
							Business.getInstance().getEventManager().sendRemoteEvent(request);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
		openPositionViewer.setBodyMenu(menu);
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private boolean openPositionColumnsCreated = false;
	private void createOpenPositionColumns(List<OpenPosition> positions) {
		if(!openPositionColumnsCreated) {
			Object obj = positions.get(0);
			List<ColumnProperty> properties = openPositionViewer.setObjectColumnProperties(obj);		
			String[] from = {"PnL"};
			String[] to = {"Ur. P&&L"};
			openPositionViewer.setTitleMap(from, to);
			openPositionViewer.setSmartColumnProperties(obj.getClass().getName(), properties);
			openPositionViewer.setInput(positions);
			openPositionColumnsCreated = true;
		}
	}
	
	private boolean closedPositionColumnsCreated = false;
	private void createClosedPositionColumns(List<ClosedPosition> closedPositions) {
		if(!closedPositionColumnsCreated) {
			Object obj = closedPositions.get(0);
			List<ColumnProperty> properties = closedPositionViewer.setObjectColumnProperties(obj);
			String[] from = {"BuyPrice", "SellPrice", "PnL"};
			String[] to = {"Buy Px", "Sell Px", "P&&L"};
			closedPositionViewer.setTitleMap(from, to);
			closedPositionViewer.setSmartColumnProperties(obj.getClass().getName(), properties);
			closedPositionViewer.setInput(closedPositions);
			closedPositionColumnsCreated = true;
		}
	}
	
	private boolean executionColumnsCreated = false;
	private void createExecutionColumns(List<Execution> executions) {
		if(!executionColumnsCreated) {
			Object obj = executions.get(0);
			List<ColumnProperty> properties = executionViewer.setObjectColumnProperties(obj);
			executionViewer.setSmartColumnProperties(obj.getClass().getName(), properties);
			executionViewer.setInput(executions);
			executionColumnsCreated = true;
		}
	}
	
	private void showAccount() {
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(openPositionViewer) {
					if (PositionView.this.account == null ||
							openPositionViewer.isViewClosing())
						return;
					
					PositionView.this.lbValue.setText(decimalFormat.format(PositionView.this.account.getValue()));
					PositionView.this.lbCash.setText(decimalFormat.format(PositionView.this.account.getCash()));
					PositionView.this.lbMargin.setText(decimalFormat.format(PositionView.this.account.getMargin()));
					PositionView.this.lbDailyPnL.setText(decimalFormat.format(PositionView.this.account.getDailyPnL()));
					PositionView.this.lbPnL.setText(decimalFormat.format(PositionView.this.account.getPnL()));
					PositionView.this.lbUrPnL.setText(decimalFormat.format(PositionView.this.account.getUrPnL()));
					PositionView.this.topComposite.layout();
				}
			}
		});
	}
	
	private void showOpenPositions(final boolean setInput) {
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(openPositionViewer) {
					if(null == PositionView.this.openPositions || openPositionViewer.isViewClosing())
						return;
					
					if(PositionView.this.openPositions.size() > 0)
						createOpenPositionColumns(PositionView.this.openPositions);
					
					if(setInput)
						openPositionViewer.setInput(PositionView.this.openPositions);

					openPositionViewer.refresh();
				}
			}
		});
	}
	
	private void showClosedPositions(final boolean setInput) {
		closedPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(closedPositionViewer) {
					if(null == PositionView.this.closedPositions ||
							closedPositionViewer.isViewClosing())
						return;
					if(PositionView.this.closedPositions.size() > 0)
						createClosedPositionColumns(PositionView.this.closedPositions);
					
					if(setInput)
						closedPositionViewer.setInput(PositionView.this.closedPositions);
					closedPositionViewer.refresh();
				}
			}
		});	
	}
	
	private void showExecutions(final boolean setInput) {
		executionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(executionViewer) {
					if(null == PositionView.this.executions ||
					   executionViewer.isViewClosing())
						return;				
					if(PositionView.this.executions.size() > 0)
						createExecutionColumns(PositionView.this.executions);
					
					if(setInput)
						executionViewer.setInput(PositionView.this.executions);
					executionViewer.refresh();
				}
			}
		});
	}
	
	private void sendSubscriptionRequest(String account) {
		// unsubscribe for current account event
		Business.getInstance().getEventManager().unsubscribe(OpenPositionUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(OpenPositionDynamicUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(ClosedPositionUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(ExecutionUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(AccountUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(AccountDynamicUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(ClosePositionReplyEvent.class, 
				currentAccount, this);
		
		currentAccount = account;
		// subscribe for new account
		Business.getInstance().getEventManager().subscribe(OpenPositionUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().subscribe(OpenPositionDynamicUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().subscribe(ClosedPositionUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().subscribe(ExecutionUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().subscribe(AccountUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().subscribe(AccountDynamicUpdateEvent.class, 
				currentAccount, this);
		Business.getInstance().getEventManager().subscribe(ClosePositionReplyEvent.class, 
				currentAccount, this);
		
		AccountSnapshotRequestEvent request = 
				new AccountSnapshotRequestEvent(ID, Business.getInstance().getFirstServer(),
						currentAccount, null);
		
		log.debug("AccountSnapshotRequestEvent sent: " + currentAccount);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void processOpenPosition(OpenPosition position) {
		if(null == openPositions)
			return;
		
		synchronized(openPositions) {
			boolean found = false;
			for(int i=0; i<openPositions.size(); i++) {
				OpenPosition pos = openPositions.get(i);
				if(pos.getSymbol().equals(position.getSymbol())) {
					if(PriceUtils.isZero(position.getQty())) {
						openPositions.remove(i);
					} else {
						openPositions.set(i, position);
					}
					found = true;
					break;
				}
			}
			if(!found && !PriceUtils.isZero(position.getQty()))
				openPositions.add(position);
		}
		showOpenPositions(false);
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof OrderCacheReadyEvent) {
			sendSubscriptionRequest(Business.getInstance().getAccount());
		} else if(event instanceof AccountSnapshotReplyEvent) {
			AccountSnapshotReplyEvent evt = (AccountSnapshotReplyEvent)event;
			log.debug("AccountSnapshotReplyEvent received: " + evt.getAccount());
			this.account = evt.getAccount();
			this.openPositions = evt.getOpenPositions();
			this.closedPositions = evt.getClosedPositions();
			this.executions = evt.getExecutions();
			
			showAccount();
			showOpenPositions(true);
			showClosedPositions(true);
			showExecutions(true);
		} else if(event instanceof AccountUpdateEvent) {
			this.account = ((AccountUpdateEvent)event).getAccount();
			showAccount();
		} else if(event instanceof AccountDynamicUpdateEvent) {
			this.account = ((AccountDynamicUpdateEvent)event).getAccount();
			showAccount();
		} else if(event instanceof OpenPositionUpdateEvent) {
			OpenPositionUpdateEvent evt = (OpenPositionUpdateEvent)event;
			processOpenPosition(evt.getPosition());
		} else if(event instanceof OpenPositionDynamicUpdateEvent) {
			OpenPositionDynamicUpdateEvent evt = (OpenPositionDynamicUpdateEvent)event;
			processOpenPosition(evt.getPosition());
		} else if(event instanceof ClosedPositionUpdateEvent) {
			ClosedPositionUpdateEvent evt = (ClosedPositionUpdateEvent)event;
			if(null == closedPositions)
				return;
			
			synchronized(closedPositions) {
				closedPositions.add(evt.getPosition());
			}
			showClosedPositions(false);
		} else if(event instanceof ExecutionUpdateEvent) {
			ExecutionUpdateEvent evt = (ExecutionUpdateEvent)event;
			if(null == executions)
				return;
			
			synchronized(executions) {
				executions.add(evt.getExecution());
			}
			showExecutions(false);
		} else if(event instanceof ClosePositionReplyEvent) {
			ClosePositionReplyEvent evt = (ClosePositionReplyEvent)event;
			log.info("Close position reply: " + evt.getAccount() + ", " + evt.getSymbol() + ", " + evt.isOk() + ", " + evt.getMessage());
		} else if(event instanceof AccountSelectionEvent) {
			AccountSelectionEvent evt = (AccountSelectionEvent)event;
			sendSubscriptionRequest(evt.getAccount());
		}
	}

}
