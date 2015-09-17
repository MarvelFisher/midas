package com.cyanspring.cstw.gui;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
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
import com.cyanspring.common.account.UserRole;
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
import com.cyanspring.common.event.account.UserMappingEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.StrategySnapshotRequestEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;
import com.cyanspring.cstw.event.SelectUserAccountEvent;
import com.cyanspring.cstw.gui.SetPriceDialog.Mode;
import com.cyanspring.cstw.gui.command.auth.AuthMenuManager;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;

public class PositionView extends ViewPart implements IAsyncEventListener {
	public PositionView() {
	}
	private static final Logger log = LoggerFactory.getLogger(PositionView.class);
	public static final String ID = "com.cyanspring.cstw.gui.PositionViewer";
	private DynamicTableViewer openPositionViewer;
	private DynamicTableViewer closedPositionViewer;
	private DynamicTableViewer executionViewer;
	private Composite topComposite;
	private Menu menu;
	private SetPriceDialog closePriceDialog;
	private SetPriceDialog modifyPositionDialog;

	// display data
	private Account account;
	private List<OpenPosition> openPositions;
	private List<ClosedPosition> closedPositions;
	private List<Execution> executions;

	private DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
	private String currentAccount = Business.getInstance().getAccount();
	private ImageRegistry imageRegistry;

	// pop up menu action
	private Action popClosePosition;
	private Action popManualClosePosition;
	private Action popModifyPositionPrice;
	private final String MENU_ID_CLOSEPOSITION = "POPUP_CLOSE_POSITION";
	private final String MENU_ID_MANUALCLOSEPOSITION = "POPUP_MANUAL_CLOSE_POSITION";
	private final String MENU_ID_MODIFYPOSITIONPRICE = "POPUP_MODIFY_POSITION_PRICE";
	
	//tab folder
	private CTabFolder tabFolder;
	private CTabItem tbtmOpenPositions;
	private CTabItem tbtmClosedPositions;
	private CTabItem tbtmTrades;
	@Override
	public void createPartControl(Composite parent) {
		imageRegistry = Activator.getDefault().getImageRegistry();
		// create views
		final Composite mainComposite = new Composite(parent, SWT.BORDER);
		// create top composite
		topComposite = new Composite(mainComposite, SWT.NONE);
		topComposite.setLayout(new FillLayout());
		createAccountInfoPad(topComposite);

		final Sash sash = new Sash(mainComposite, SWT.HORIZONTAL);
		// create bottom composite
		final Composite bottomComposite = new Composite(mainComposite, SWT.BORDER);
		bottomComposite.setLayout(new FillLayout());

		// setting up form layout
		mainComposite.setLayout(new FormLayout());

		FormData topData = new FormData();
		topData.left = new FormAttachment(0, 0);
		topData.right = new FormAttachment(100, 0);
		topData.top = new FormAttachment(0, 0);
		topData.bottom = new FormAttachment(sash, 0);
		topComposite.setLayoutData(topData);

		final int limit = 20;
		final FormData sashData = new FormData();
		sashData.left = new FormAttachment(0, 0);
		sashData.right = new FormAttachment(100, 0);
		sashData.top = new FormAttachment(sash, 60);
		sash.setLayoutData(sashData);

		sash.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Rectangle sashRect = sash.getBounds();
				Rectangle shellRect = mainComposite.getClientArea();
				int top = shellRect.height - sashRect.height - limit;
				e.y = Math.max(Math.min(e.y, top), limit);
				if (e.y != sashRect.y) {
					sashData.top = new FormAttachment(0, e.y);
					mainComposite.layout();
				}
			}
		});

		FormData bottomData = new FormData();
		bottomData.left = new FormAttachment(0, 0);
		bottomData.right = new FormAttachment(100, 0);
		bottomData.top = new FormAttachment(sash, 0);
		bottomData.bottom = new FormAttachment(100, 0);
		bottomComposite.setLayoutData(bottomData);

		// create the left composite in the bottom composite
		tabFolder = new CTabFolder(bottomComposite, SWT.BORDER);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		tbtmOpenPositions = new CTabItem(tabFolder, SWT.NONE);
		tbtmOpenPositions.setText("Open Positions");
			
		Composite leftComposite = new Composite(tabFolder, SWT.BORDER);
		leftComposite.setLayout(new GridLayout(1, true));
		Composite openComposite = new Composite(tabFolder, SWT.NONE);
		openComposite.setLayout(new FillLayout());
		openComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createOpenPositionViewer(openComposite);		
		tbtmOpenPositions.setControl(openComposite);
		
		if(Business.getInstance().getUserGroup().isAdmin()){
			
			tbtmClosedPositions = new CTabItem(tabFolder, SWT.NONE);
			tbtmClosedPositions.setText("Closed Positions");
			
			tbtmTrades = new CTabItem(tabFolder, SWT.NONE);
			tbtmTrades.setText("Trades");
			
			Composite midComposite = new Composite(tabFolder, SWT.BORDER);
			midComposite.setLayout(new GridLayout(1, true));
			Composite closedComposite = new Composite(midComposite, SWT.NONE);
			closedComposite.setLayout(new FillLayout());
			closedComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			createClosedPositionViewer(closedComposite);
			tbtmClosedPositions.setControl(midComposite);
			
			Composite rightComposite = new Composite(tabFolder, SWT.BORDER);
			rightComposite.setLayout(new GridLayout(1, true));
			Composite executionComposite = new Composite(rightComposite, SWT.NONE);
			executionComposite.setLayout(new FillLayout());
			executionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			createExecutionViewer(executionComposite);
			tbtmTrades.setControl(rightComposite);
		}

		createMenu(leftComposite);

		// finally lay them out
		mainComposite.layout();
		Business.getInstance().getEventManager().subscribe(AccountSelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(AccountSnapshotReplyEvent.class, ID, this);
		log.info("Business.getInstance().getOrderManager().isReady():{}",Business.getInstance().getOrderManager().isReady());
		log.info("Business.getInstance().getAccount():{}",Business.getInstance().getAccount());
		if (Business.getInstance().getOrderManager().isReady())
			sendSubscriptionRequest(Business.getInstance().getAccount());
		else
			Business.getInstance().getEventManager().subscribe(OrderCacheReadyEvent.class, this);
		
		sendTraderRequestEvent();

	}

	// account fields
	private Label lbValue;
//	private Label lbCash;
//	private Label lbMargin;
	private Label lbDailyPnL;
	private Label lbPnL;
	private Label lbUrPnL;
	private Label lbCashAvailable;
	private Label lbCashDeduct;
//	private Label lbMarginHeld;

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
		lb1.setText("Account Value: ");
		lb1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbValue = new Label(comp1, SWT.RIGHT);
		lbValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label lb8 = new Label(comp2, SWT.LEFT);
		lb8.setText("Account Cash: ");
		lb8.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbCashDeduct = new Label(comp2, SWT.RIGHT);
		lbCashDeduct.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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

		Label lb7 = new Label(comp3, SWT.LEFT);
		lb7.setText("Cash Available: ");
		lb7.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lbCashAvailable = new Label(comp3, SWT.RIGHT);
		lbCashAvailable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
//		Label lb2 = new Label(comp2, SWT.LEFT);
//		lb2.setText("Cash value: ");
//		lb2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//
//		lbCash = new Label(comp2, SWT.RIGHT);
//		lbCash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//
//		Label lb3 = new Label(comp3, SWT.LEFT);
//		lb3.setText("Margin value: ");
//		lb3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//
//		lbMargin = new Label(comp3, SWT.RIGHT);
//		lbMargin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//
//		Label lb10 = new Label(comp3, SWT.LEFT);
//		lb10.setText("Margin Held");
//		lb10.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//
//		lbMarginHeld = new Label(comp3, SWT.RIGHT);
//		lbMarginHeld.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	}

	private void createOpenPositionViewer(Composite parent) {
		String strFile = Business.getInstance().getConfigPath() + "OpenPositionTable.xml";
		openPositionViewer = new DynamicTableViewer(parent,
				SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, Business.getInstance().getXstream(),
				strFile, BeanHolder.getInstance().getDataConverter());
		openPositionViewer.init();
	}

	private void createClosedPositionViewer(Composite parent) {
		String strFile = Business.getInstance().getConfigPath() + "ClosedPositionTable.xml";
		closedPositionViewer = new DynamicTableViewer(parent,
				SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, Business.getInstance().getXstream(),
				strFile, BeanHolder.getInstance().getDataConverter());
		closedPositionViewer.init();
	}

	private void createExecutionViewer(Composite parent) {
		String strFile = Business.getInstance().getConfigPath() + "ExecutionPositionTable.xml";
		executionViewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL,
				Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		executionViewer.init();

	}

	private Action createPopClosePosition() {
		popClosePosition = new StyledAction("", org.eclipse.jface.action.IAction.AS_PUSH_BUTTON) {
			public void run() {
				Table table = openPositionViewer.getTable();

				TableItem items[] = table.getSelection();

				for (TableItem item : items) {
					Object obj = item.getData();
					if (obj instanceof OpenPosition) {
						OpenPosition position = (OpenPosition) obj;
						// TODO: we need to change when CSTW talks to multip
						// servers
						String server = Business.getInstance().getFirstServer();

						ClosePositionRequestEvent request = new ClosePositionRequestEvent(position.getAccount(), server,
								position.getAccount(), position.getSymbol(), 0.0, OrderReason.ManualClose,
								IdGenerator.getInstance().getNextID());
						try {
							Business.getInstance().getEventManager().sendRemoteEvent(request);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
		};

		popClosePosition.setId(MENU_ID_CLOSEPOSITION);
		popClosePosition.setText("Close Position");
		popClosePosition.setToolTipText("Close Position");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.ORDER_CLOSE_ICON.toString());
		popClosePosition.setImageDescriptor(imageDesc);
		return popClosePosition;
	}

	private Action createPopManualClosePosition(final Composite parent) {
		popManualClosePosition = new StyledAction("", org.eclipse.jface.action.IAction.AS_PUSH_BUTTON) {
			public void run() {

				try {
					Table table = openPositionViewer.getTable();
					TableItem items[] = table.getSelection();
					for (TableItem item : items) {
						Object obj = item.getData();
						if (obj instanceof OpenPosition) {
							OpenPosition position = (OpenPosition) obj;
							closePriceDialog = new SetPriceDialog(parent.getShell(), position, Mode.CLOSE_POSITION);
							closePriceDialog.open();
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		};

		popManualClosePosition.setId(MENU_ID_MANUALCLOSEPOSITION);
		popManualClosePosition.setText("Manual Close Position");
		popManualClosePosition.setToolTipText("Manual Close Position");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.MANUAL_CLOSE_ICON.toString());
		popManualClosePosition.setImageDescriptor(imageDesc);
		return popManualClosePosition;
	}

	private Action createPopModifyOpenPositionPrice(final Composite parent) {
		popModifyPositionPrice = new StyledAction("", org.eclipse.jface.action.IAction.AS_PUSH_BUTTON) {
			public void run() {

				try {
					Table table = openPositionViewer.getTable();
					TableItem items[] = table.getSelection();
					for (TableItem item : items) {
						Object obj = item.getData();
						if (obj instanceof OpenPosition) {
							OpenPosition position = (OpenPosition) obj;
							modifyPositionDialog = new SetPriceDialog(parent.getShell(), position, Mode.CHANGE_POSITION_PRICE);
							modifyPositionDialog.open();
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		};
		
		popModifyPositionPrice.setId(MENU_ID_MODIFYPOSITIONPRICE);
		popModifyPositionPrice.setText("Modify open position price");
		popModifyPositionPrice.setToolTipText("Modify open position price");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.MANUAL_PRICE_ICON.toString());
		popModifyPositionPrice.setImageDescriptor(imageDesc);
		return popModifyPositionPrice;
	}

	private void createMenu(Composite parent) {

		AuthMenuManager menuMgr = AuthMenuManager.newInstance(this.getPartName());
		menuMgr.add(createPopClosePosition());
		menuMgr.add(createPopManualClosePosition(parent));
		menuMgr.add(createPopModifyOpenPositionPrice(parent));
		menu = menuMgr.createContextMenu(openPositionViewer.getTable());
		openPositionViewer.setBodyMenu(menu);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private boolean openPositionColumnsCreated = false;

	private void createOpenPositionColumns(List<OpenPosition> positions) {
		if (!openPositionColumnsCreated) {
			Object obj = positions.get(0);
			List<ColumnProperty> properties = openPositionViewer.setObjectColumnProperties(obj);
			String[] from = { "PnL" };
			String[] to = { "Ur. P&&L" };
			openPositionViewer.setTitleMap(from, to);
			openPositionViewer.setSmartColumnProperties(obj.getClass().getName(), properties);
			openPositionViewer.setInput(positions);
			openPositionColumnsCreated = true;
		}
	}

	private boolean closedPositionColumnsCreated = false;

	private void createClosedPositionColumns(List<ClosedPosition> closedPositions) {
		if (!closedPositionColumnsCreated) {
			Object obj = closedPositions.get(0);
			List<ColumnProperty> properties = closedPositionViewer.setObjectColumnProperties(obj);
			String[] from = { "BuyPrice", "SellPrice", "PnL" };
			String[] to = { "Buy Px", "Sell Px", "P&&L" };
			closedPositionViewer.setTitleMap(from, to);
			closedPositionViewer.setSmartColumnProperties(obj.getClass().getName(), properties);
			closedPositionViewer.setInput(closedPositions);
			closedPositionColumnsCreated = true;
		}
	}

	private boolean executionColumnsCreated = false;

	private void createExecutionColumns(List<Execution> executions) {
		if (!executionColumnsCreated) {
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
				synchronized (openPositionViewer) {
					if (PositionView.this.account == null || openPositionViewer.isViewClosing())
						return;

					PositionView.this.lbValue.setText(decimalFormat.format(PositionView.this.account.getValue()));
					PositionView.this.lbCashDeduct.setText(decimalFormat.format(PositionView.this.account.getCashDeduct()));
					PositionView.this.lbDailyPnL.setText(decimalFormat.format(PositionView.this.account.getDailyPnL()));
					PositionView.this.lbPnL.setText(decimalFormat.format(PositionView.this.account.getPnL()));
					PositionView.this.lbUrPnL.setText(decimalFormat.format(PositionView.this.account.getUrPnL()));
					PositionView.this.lbCashAvailable
							.setText(decimalFormat.format(PositionView.this.account.getCashAvailable()));
//					PositionView.this.lbCash.setText(decimalFormat.format(PositionView.this.account.getCash()));
//					PositionView.this.lbMargin.setText(decimalFormat.format(PositionView.this.account.getMargin()));
//					PositionView.this.lbMarginHeld.setText(decimalFormat.format(PositionView.this.account.getMarginHeld()));
					PositionView.this.topComposite.layout();
					tabFolder.setSelection(tbtmOpenPositions);
				}
			}
		});
	}

	private void showOpenPositions(final boolean setInput) {
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (openPositionViewer) {
					if (null == PositionView.this.openPositions || openPositionViewer.isViewClosing())
						return;

					if (PositionView.this.openPositions.size() > 0)
						createOpenPositionColumns(PositionView.this.openPositions);

					if (setInput)
						openPositionViewer.setInput(PositionView.this.openPositions);

					openPositionViewer.refresh();
				}
			}
		});
	}

	private void showClosedPositions(final boolean setInput) {
		if( null == closedPositionViewer)
			return;
		
		closedPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (closedPositionViewer) {
					if (null == PositionView.this.closedPositions || closedPositionViewer.isViewClosing())
						return;
					if (PositionView.this.closedPositions.size() > 0)
						createClosedPositionColumns(PositionView.this.closedPositions);

					if (setInput)
						closedPositionViewer.setInput(PositionView.this.closedPositions);
					closedPositionViewer.refresh();
				}
			}
		});
	}

	private void showExecutions(final boolean setInput) {
		if(null == executionViewer)
			return;
		
		executionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (executionViewer) {
					if (null == PositionView.this.executions || executionViewer.isViewClosing())
						return;
					if (PositionView.this.executions.size() > 0)
						createExecutionColumns(PositionView.this.executions);

					if (setInput)
						executionViewer.setInput(PositionView.this.executions);
					executionViewer.refresh();
				}
			}
		});
	}

	private void sendTraderRequestEvent(){
		log.info("sendTraderRequestEvent");
		if(UserRole.Trader == Business.getInstance().getUserGroup().getRole()){
			Account account = Business.getInstance().getLoginAccount();
			if( null != account){
				sendSubscriptionRequest(account.getId());
			}
		}
	}
	
	
	private void sendSubscriptionRequest(String account) {
		// unsubscribe for current account event
		Business.getInstance().getEventManager().unsubscribe(OpenPositionUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(OpenPositionDynamicUpdateEvent.class, currentAccount,
				this);
		Business.getInstance().getEventManager().unsubscribe(ClosedPositionUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(ExecutionUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(AccountUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(AccountDynamicUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().unsubscribe(ClosePositionReplyEvent.class, currentAccount, this);

		currentAccount = account;
		// subscribe for new account
		Business.getInstance().getEventManager().subscribe(OpenPositionUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().subscribe(OpenPositionDynamicUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().subscribe(ClosedPositionUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().subscribe(ExecutionUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().subscribe(AccountUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().subscribe(AccountDynamicUpdateEvent.class, currentAccount, this);
		Business.getInstance().getEventManager().subscribe(ClosePositionReplyEvent.class, currentAccount, this);

		AccountSnapshotRequestEvent request = new AccountSnapshotRequestEvent(ID,
				Business.getInstance().getFirstServer(), currentAccount, null);

		log.debug("AccountSnapshotRequestEvent sent: " + currentAccount);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void processOpenPosition(OpenPosition position) {
		if (null == openPositions)
			return;

		synchronized (openPositions) {
			boolean found = false;
			for (int i = 0; i < openPositions.size(); i++) {
				OpenPosition pos = openPositions.get(i);
				if (pos.getSymbol().equals(position.getSymbol())) {
					if (PriceUtils.isZero(position.getQty())) {
						openPositions.remove(i);
					} else {
						openPositions.set(i, position);
					}
					found = true;
					break;
				}
			}
			if (!found && !PriceUtils.isZero(position.getQty()))
				openPositions.add(position);
		}
		showOpenPositions(false);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof OrderCacheReadyEvent) {
			sendSubscriptionRequest(Business.getInstance().getAccount());
		} else if (event instanceof AccountSnapshotReplyEvent) {
			AccountSnapshotReplyEvent evt = (AccountSnapshotReplyEvent) event;
			log.debug("AccountSnapshotReplyEvent received: " + evt.getAccount());
			this.account = evt.getAccount();
			this.openPositions = evt.getOpenPositions();
			this.closedPositions = evt.getClosedPositions();
			this.executions = evt.getExecutions();

			showAccount();
			showOpenPositions(true);
			showClosedPositions(true);
			showExecutions(true);
		} else if (event instanceof AccountUpdateEvent) {
			this.account = ((AccountUpdateEvent) event).getAccount();
			showAccount();
		} else if (event instanceof AccountDynamicUpdateEvent) {
			this.account = ((AccountDynamicUpdateEvent) event).getAccount();
			showAccount();
		} else if (event instanceof OpenPositionUpdateEvent) {
			OpenPositionUpdateEvent evt = (OpenPositionUpdateEvent) event;
			processOpenPosition(evt.getPosition());
		} else if (event instanceof OpenPositionDynamicUpdateEvent) {
			OpenPositionDynamicUpdateEvent evt = (OpenPositionDynamicUpdateEvent) event;
			processOpenPosition(evt.getPosition());
		} else if (event instanceof ClosedPositionUpdateEvent) {
			ClosedPositionUpdateEvent evt = (ClosedPositionUpdateEvent) event;
			if (null == closedPositions)
				return;

			synchronized (closedPositions) {
				closedPositions.add(evt.getPosition());
			}
			showClosedPositions(false);
		} else if (event instanceof ExecutionUpdateEvent) {
			ExecutionUpdateEvent evt = (ExecutionUpdateEvent) event;
			if (null == executions)
				return;

			synchronized (executions) {
				executions.add(evt.getExecution());
			}
			showExecutions(false);
		} else if (event instanceof ClosePositionReplyEvent) {

			ClosePositionReplyEvent evt = (ClosePositionReplyEvent) event;
			log.info("Close position reply: " + evt.getAccount() + ", " + evt.getSymbol() + ", " + evt.isOk() + ", "
					+ evt.getMessage());
		} else if (event instanceof AccountSelectionEvent) {

			AccountSelectionEvent evt = (AccountSelectionEvent) event;
			sendSubscriptionRequest(evt.getAccount());
		}
	}

}
