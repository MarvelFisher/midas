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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.Clock;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.alert.ClearSingleAlertEvent;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderReplyEvent;
import com.cyanspring.common.event.strategy.PauseStrategyEvent;
import com.cyanspring.common.event.strategy.StartStrategyEvent;
import com.cyanspring.common.event.strategy.StopStrategyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.event.GuiSingleOrderStrategyUpdateEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;
import com.cyanspring.cstw.event.SingleOrderStrategySelectionEvent;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;
import com.cyanspring.cstw.gui.filter.ParentOrderFilter;

public class SingleOrderStrategyView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(SingleOrderStrategyView.class);
	public static final String ID = "com.cyanspring.cstw.gui.ParentOrderView";
	private DynamicTableViewer viewer;
	// filter controls
	private Composite filterComposite;
	private Label filterLabel;
	private Combo filterField;
	private Text filterText;
	private Button filterButton;
	private Action filterAction;
	private Action orderPadAction;
	private ParentOrderFilter viewFilter;
	private ParentOrderFilter accountFilter;
	private ImageRegistry imageRegistry;
	private Action enterOrderAction;
	private Action cancelOrderAction;
	private Action pauseOrderAction;
	private Action stopOrderAction;
	private Action startOrderAction;
	private Action saveOrderAction;
	private Action pinAction;
	private Action countOrderAction;
	private OrderDialog orderDialog;
	private AmendDialog amendDialog;
	private Menu menu;
	private boolean setColumns;
	private AsyncTimerEvent timerEvent;
	private Date lastRefreshTime = Clock.getInstance().now();
	private final long maxRefreshInterval = 300;
	private boolean pinned = true;
	
	// QuickOrderPad
	private String currentOrderPadId;
	private Composite panelComposite;
	private Text txtSymbol;
	private Text txtPrice;
	private Text txtQuantity;
	private Combo cbOrderSide;
	private Combo cbOrderType;
	private Combo cbServer;
	private Button btEnter;
	private Button btAmend;
	private Button btCancel;
	private Label lbStatus;
	GridData gdStatus;
	// end QuickOrderPad

	
	private String maTag;
	private String maValue;

	
	private enum StrategyAction { Pause, Stop, Start, ClearAlert, MultiAmend, Create, Cancel, ForceCancel, Save };
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		log.info("Creating parent order view");
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();
		
		// create layout for parent
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		
		// create pause order action
		createPauseOrderAction(parent);
		
		// create stop order action
		createStopOrderAction(parent);

		// create start order action
		createStartOrderAction(parent);

		// create enter order actions
		createEnterOrderAction(parent);
		
		// create enter order actions
		createCancelOrderAction(parent);
		
		// create filter controls
		createFilterControls(parent);
		
		// create save order action
		createSaveOrderAction(parent);
		
		// create table
	    String strFile = Business.getInstance().getConfigPath() + "ParentOrderTable.xml";
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
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					String strategyName = (String)map.get(OrderField.STRATEGY.value());
					populateOrderPad(map);
					Business.getInstance().getEventManager().
						sendEvent(new SingleOrderStrategySelectionEvent(map, Business.getInstance().getSingleOrderAmendableFields(strategyName)));
				}
			}
		});

		// create pin order action
		createPinAction(parent);
		
		createCountOrderAction(parent);
		
		createBodyMenu(parent);
		createQuickOrderPad(parent);

		// business logic goes here
		Business.getInstance().getEventManager().subscribe(OrderCacheReadyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(GuiSingleOrderStrategyUpdateEvent.class, this);
		Business.getInstance().getEventManager().subscribe(EnterParentOrderReplyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(AmendParentOrderReplyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(CancelParentOrderReplyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(AccountSelectionEvent.class, this);
		showOrders();
	}
	
	private void createQuickOrderPad(final Composite parent) {

		panelComposite = new Composite(parent, SWT.NONE);
		GridLayout panelLayout = new GridLayout(12, false);
		panelComposite.setLayout(panelLayout);
		panelLayout.marginHeight = 1;
		panelLayout.marginWidth = 5;

		Label lb1 = new Label(panelComposite, SWT.NONE);
		lb1.setText("Symbol: ");
		lb1.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		GridData gridData;
		txtSymbol = new Text(panelComposite, SWT.BORDER);
		txtSymbol.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		Label lb2 = new Label(panelComposite, SWT.NONE);
		lb2.setText("Price: ");
		lb2.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		txtPrice = new Text(panelComposite, SWT.BORDER);
		gridData = new GridData(SWT.RIGHT, SWT.FILL, false, true);
		gridData.widthHint = 100;
		txtPrice.setLayoutData(gridData);

		Label lb3 = new Label(panelComposite, SWT.NONE);
		lb3.setText("Quantity: ");
		lb3.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		txtQuantity = new Text(panelComposite, SWT.BORDER);
		gridData = new GridData(SWT.RIGHT, SWT.FILL, false, true);
		gridData.widthHint = 100;
		txtQuantity.setLayoutData(gridData);

		cbOrderSide = new Combo(panelComposite, SWT.BORDER | SWT.SEARCH);
		cbOrderSide.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		String[] sidItems = {OrderSide.Buy.toString(), OrderSide.Sell.toString()};
		cbOrderSide.setItems(sidItems);
		cbOrderSide.select(0);
		
		cbOrderType = new Combo(panelComposite, SWT.BORDER | SWT.SEARCH);
		cbOrderType.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		String[] typeItems = {OrderType.Limit.toString(), OrderType.Market.toString()};
		cbOrderType.setItems(typeItems);
		cbOrderType.select(0);
		
		cbServer = new Combo(panelComposite, SWT.BORDER | SWT.SEARCH);
		cbServer.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		btEnter = new Button(panelComposite, SWT.FLAT);
		btEnter.setText(" Enter ");
		btEnter.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		
		btAmend = new Button(panelComposite, SWT.FLAT);
		btAmend.setText("Amend");
		btAmend.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		btCancel = new Button(panelComposite, SWT.FLAT);
		btCancel.setText("Cancel");
		btCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		btEnter.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				quickEnterOrder();
			}
		});

		btAmend.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				quickAmendOrder();
			}
		});

		btCancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				quickCancelOrder();
			}
		});

		lbStatus = new Label(panelComposite, SWT.NONE);
		gdStatus = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 12, 1);
		gdStatus.horizontalIndent = 1;
		lbStatus.setLayoutData(gdStatus);
		lbStatus.setText("");
		Color red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		lbStatus.setForeground(red);
		showQuickOrderStatus(false);
		
		GridData panelGridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		panelComposite.setLayoutData(panelGridData);
		panelComposite.layout();

		panelGridData.exclude = true;
		panelComposite.setVisible(false);
		
		// create local toolbars
		orderPadAction = new Action() {
			public void run() {
				if (panelComposite.isVisible()) {
					showOrderPad(false);
				} else {
					showOrderPad(true);
					populateOrderPadServers();
				}
				parent.layout();
			}
		};

		orderPadAction.setText("order pad");
		orderPadAction.setToolTipText("show or hide orderpad");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.EDIT_ICON.toString());
		orderPadAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(orderPadAction);
	}
	
	private void showQuickOrderStatus(boolean show) {
		gdStatus.exclude = !show;
		lbStatus.setVisible(show);
		panelComposite.getParent().layout();
	}
	
	private void populateOrderPad(HashMap<String, Object> map) {
		currentOrderPadId = (String)map.get(OrderField.ID.value());
		txtSymbol.setText((String)map.get(OrderField.SYMBOL.value()));
		Double price = (Double)map.get(OrderField.PRICE.value());
		if(null != price)
			txtPrice.setText(price.toString());
		txtQuantity.setText(((Double)map.get(OrderField.QUANTITY.value())).toString());
		cbOrderType.setText(((OrderType)map.get(OrderField.TYPE.value())).toString());
		cbOrderSide.setText(((OrderSide)map.get(OrderField.SIDE.value())).toString());
		cbServer.setText((String)map.get(OrderField.SERVER_ID.value()));
	}

	private void populateOrderPadServers() {
		ArrayList<String> servers = Business.getInstance().getOrderManager().getServers();
		cbServer.removeAll();
		for(String server: servers) {
			cbServer.add(server);
		}
		cbServer.select(0);
	}
	
	private void showOrderPad(boolean show) {
		panelComposite.setVisible(show);
		GridData data = (GridData) panelComposite.getLayoutData();
		data.exclude = !show;
	}
	
	private void quickEnterOrder() {
		HashMap<String, Object> fields = new HashMap<String, Object>();

		fields.put(OrderField.SYMBOL.value(), txtSymbol.getText());
		fields.put(OrderField.SIDE.value(), cbOrderSide.getText());
		fields.put(OrderField.TYPE.value(), cbOrderType.getText());
		fields.put(OrderField.QUANTITY.value(), txtQuantity.getText());
		fields.put(OrderField.PRICE.value(), txtPrice.getText());
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		fields.put(OrderField.USER.value(), Business.getInstance().getUser());
		fields.put(OrderField.ACCOUNT.value(), Business.getInstance().getAccount());
			
		EnterParentOrderEvent event = 
			new EnterParentOrderEvent(Business.getInstance().getInbox(), cbServer.getText(), fields, null, false);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	private void quickAmendOrder() {
		
		Table table = this.viewer.getTable();
		int index = table.getSelectionIndex();
		String id = null, server = null;
		if(index>= 0) {
			TableItem item = table.getItem(table.getSelectionIndex());
			Object obj = item.getData();
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>)obj;
			id = (String)map.get(OrderField.ID.value());
			server = (String)map.get(OrderField.SERVER_ID.value());
		} else if ( null !=  currentOrderPadId) {
			id = currentOrderPadId;
			server = cbServer.getText();
		} else {
			return;
		}
		
		HashMap<String, Object> changes = new HashMap<String, Object>();
		changes.put(OrderField.ID.value(), id);
		changes.put(OrderField.PRICE.value(), txtPrice.getText());
		changes.put(OrderField.QUANTITY.value(), txtQuantity.getText());
		AmendParentOrderEvent	event = new AmendParentOrderEvent(id, server, id, changes, null);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	private void quickCancelOrder() {
		cancelOrders(false);
	}

	private void createBodyMenu(final Composite parent) {
		final Table table = viewer.getTable();
		menu = new Menu(table.getShell(), SWT.POP_UP);

		MenuItem item;
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Pause");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Pause);
			}
		});

		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Stop");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Stop);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Start");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Start);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Clear alert");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.ClearAlert);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Multi Amend");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				amendDialog = new AmendDialog(parent.getShell());
				amendDialog.open();
				if(amendDialog.getReturnCode() == org.eclipse.jface.window.Window.OK ) {
					maTag = amendDialog.getField();
					maValue = amendDialog.getValue();
					if(null == maTag || maTag.equals(""))
						return;
					strategyAction(StrategyAction.MultiAmend);
				}
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Create");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Create);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Cancel");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Cancel);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Force cancel");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.ForceCancel);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Save");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Save);
			}
		});
		
		viewer.setBodyMenu(menu);
	}
	
	private void createEnterOrderAction(final Composite parent) {
		orderDialog = new OrderDialog(parent.getShell());
		// create local toolbars
		enterOrderAction = new Action() {
			public void run() {
				//orderDialog.open();
				orderDialog.open();
			}
		};
		enterOrderAction.setText("Enter Order");
		enterOrderAction.setToolTipText("Create an order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PLUS_ICON.toString());
		enterOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(enterOrderAction);
	}
	
	private void createCancelOrderAction(Composite parent) {
		// create local toolbars
		cancelOrderAction = new Action() {
			public void run() {
				cancelOrders(false);
			}
		};
		cancelOrderAction.setText("Cancel Order");
		cancelOrderAction.setToolTipText("Cancel the order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.FALSE_ICON.toString());
		cancelOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(cancelOrderAction);
	}
	
	private void createSaveOrderAction(Composite parent) {
		// create local toolbars
		saveOrderAction = new Action() {
			public void run() {
				saveOrders();
			}
		};
		
		saveOrderAction.setText("Save order as xml");
		saveOrderAction.setToolTipText("Save order as xml");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.SAVE_ICON.toString());
		saveOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(saveOrderAction);
	}

	private void saveOrders() {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		Table table = SingleOrderStrategyView.this.viewer.getTable();

		TableItem items[] = table.getSelection();
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(TableItem item: items) {
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					list.add(map);
				}
			}
			if(list.size() == 0) {
				MessageDialog.openError(shell, "No strategy is selected", 
				"Please select the strategies you want to save");
				return;
			}
			
			FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			dialog.setFilterExtensions(new String[] {"*.xml"});

			String selectedFileName = dialog.open();
			if (selectedFileName == null){
				return;
			}
			
			File selectedFile = new File(selectedFileName); 
			selectedFile.createNewFile();
			FileOutputStream os = new FileOutputStream(selectedFile);

			if(list.size() == 1) {
				EnterParentOrderEvent event = new EnterParentOrderEvent(null, null, list.get(0), "", false);
				Business.getInstance().getXstream().toXML(event, os);
			} else {  // more than one
				List<EnterParentOrderEvent> events = new ArrayList<EnterParentOrderEvent>();
				for(Map<String, Object> map: list) {
					events.add(new EnterParentOrderEvent(null, null, map, "", false) );
				}
				Business.getInstance().getXstream().toXML(events, os);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	protected void cancelOrders(boolean force) {
		Table table = SingleOrderStrategyView.this.viewer.getTable();

		TableItem items[] = table.getSelection();
		try {
			for(TableItem item: items) {
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					String id = (String)map.get(OrderField.ID.value());
					String server = (String)map.get(OrderField.SERVER_ID.value());
						Business.getInstance().getEventManager().sendRemoteEvent(
								new CancelParentOrderEvent(id, server, id, force, null));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	private void strategyAction(StrategyAction action) {
		Table table = SingleOrderStrategyView.this.viewer.getTable();

		TableItem items[] = table.getSelection();
		for(TableItem item: items) {
			Object obj = item.getData();
			if (obj instanceof HashMap) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>)obj;
				String id = (String)map.get(OrderField.ID.value());
				String server = (String)map.get(OrderField.SERVER_ID.value());
				try {
					switch(action) {
					case Pause:
						Business.getInstance().getEventManager().
						sendRemoteEvent(new PauseStrategyEvent(id, server));
						break;
					case Stop:
						Business.getInstance().getEventManager().
						sendRemoteEvent(new StopStrategyEvent(id, server));
						break;
					case Start:
						Business.getInstance().getEventManager().
						sendRemoteEvent(new StartStrategyEvent(id, server));
						break;
					case ClearAlert:
						Business.getInstance().getEventManager().
						sendRemoteEvent(new ClearSingleAlertEvent(id, server));
						break;
					case MultiAmend:
						Map<String, Object> changes = new HashMap<String, Object>();
						changes.put(maTag, maValue);
						AmendParentOrderEvent event = new AmendParentOrderEvent(id, server, id, changes, IdGenerator.getInstance().getNextID());
						Business.getInstance().getEventManager().
							sendRemoteEvent(event);
						break;
					case Create:
						orderDialog.open();
						break;
					case Cancel:
						cancelOrders(false);
						break;
					case ForceCancel:
						cancelOrders(true);
						break;
					case Save:
						saveOrders();
						break;
					default:
						log.error("StrategyAction not supported: " + action);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}
	}
	
	private void createPinAction(final Composite parent) {
		// create local toolbars
		pinAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				pinned = pinned?false:true;
				if(!pinned) {
					viewer.removeFilter(accountFilter);
				} else { 
					accountFilter.setMatch("Account", Business.getInstance().getAccount());
					viewer.addFilter(accountFilter);
				}
				smartShowOrders();
			}
		};
		
		pinAction.setChecked(true);
		pinned = true;
		accountFilter = new ParentOrderFilter();
		accountFilter.setMatch("Account", Business.getInstance().getAccount());
		viewer.addFilter(accountFilter);
		
		pinAction.setText("Pin Account");
		pinAction.setToolTipText("Pin account");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PIN_ICON.toString());
		pinAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(pinAction);
	}

	private void createPauseOrderAction(final Composite parent) {
		// create local toolbars
		pauseOrderAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Pause);
			}
		};
		pauseOrderAction.setText("Pause Order");
		pauseOrderAction.setToolTipText("Pause order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PAUSE_ICON.toString());
		pauseOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(pauseOrderAction);
	}

	private void createStopOrderAction(final Composite parent) {
		// create local toolbars
		stopOrderAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Stop);
			}
		};
		stopOrderAction.setText("Stop Order");
		stopOrderAction.setToolTipText("Stop order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.STOP_ICON.toString());
		stopOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(stopOrderAction);
	}
	
	private void createStartOrderAction(final Composite parent) {
		// create local toolbars
		startOrderAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Start);
			}
		};
		startOrderAction.setText("Start Order");
		startOrderAction.setToolTipText("Start order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.START_ICON.toString());
		startOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(startOrderAction);
	}
	
	private void createCountOrderAction(final Composite parent) {
		// create local toolbars
		countOrderAction = new Action() {
			public void run() {
				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_INFORMATION);
				messageBox.setText("Info");
				messageBox.setMessage("Number of orders: " + Business.getInstance().getOrderManager().getParentOrders().size());
				messageBox.open();
			}
		};
		countOrderAction.setText("Check number of orders");
		countOrderAction.setToolTipText("Check number of orders");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.TRUE_ICON.toString());
		countOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(countOrderAction);
	}
	
	private void createFilterControls(final Composite parent) {
		filterComposite = new Composite(parent, SWT.NONE);
		GridData filterGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		filterComposite.setLayout(layout);

		filterLabel = new Label(filterComposite, SWT.NONE);
		filterLabel.setText("Filter: ");
		filterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		
		filterField = new Combo(filterComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		filterField.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		filterText = new Text(filterComposite, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		filterButton = new Button(filterComposite, SWT.FLAT);
		filterButton.setText("Apply Filter");
		filterButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		filterButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (viewFilter == null) {
					viewFilter = new ParentOrderFilter();
					viewFilter.setMatch(filterField.getText(), filterText.getText());
					filterButton.setText("Remove Filter");
					viewer.addFilter(viewFilter);
					filterButton.pack();
					filterComposite.layout();
				} else {
					filterButton.setText("Apply Filter");
					viewer.removeFilter(viewFilter);
					viewFilter = null;
					filterButton.pack();
					filterComposite.layout();
				}
				viewer.refresh();
			}
			
		});

		filterComposite.setLayoutData(filterGridData);
		filterComposite.layout();

		filterGridData.exclude = true;
		filterComposite.setVisible(false);
		
		// create local toolbars
		filterAction = new Action() {
			public void run() {
				if (filterComposite.isVisible()) {
					showFilter(false);
				} else {
					showFilter(true);
				}
				parent.layout();
			}
		};
		filterAction.setText("Filter");
		filterAction.setToolTipText("show or hide filter");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.FILTER_ICON.toString());
		filterAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(filterAction);
		
//		IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
//		manager.setMessage("Information for the status line");

	}
	
	private void showFilter(boolean show) {
		if (viewFilter != null) {
			filterField.setText(viewFilter.getColumn());
			filterText.setText(viewFilter.getPattern());
		}
			
		filterComposite.setVisible(show);
		GridData data = (GridData) filterComposite.getLayoutData();
		data.exclude = !show;
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void showOrders() {
		lastRefreshTime = Clock.getInstance().now();
		if(!setColumns) {
			List<Map<String, Object>> orders = Business.getInstance().getOrderManager().getParentOrders();
			
			if (orders.size() == 0)
				return;
			
			ArrayList<ColumnProperty> columnProperties = new ArrayList<ColumnProperty>();
			List<String> displayFields = Business.getInstance().getParentOrderDisplayFields();
			
			//add fields exists in both list
			for(String field: displayFields) {
//				if(titles.contains(field))
					columnProperties.add(new ColumnProperty(field, 100));
			}
			
			viewer.setSmartColumnProperties("Parent Order", columnProperties);
			filterField.removeAll();
			for(ColumnProperty prop: viewer.getDynamicColumnProperties()) {
				filterField.add(prop.getTitle());
			}
			viewer.setInput(orders);
			setColumns = true;
		}
		viewer.refresh();
	}
	
	private void asyncShowOrders() {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				showOrders();
			}
		});
	}

	private void smartShowOrders() {
		if(TimeUtil.getTimePass(lastRefreshTime) > maxRefreshInterval) {
			asyncShowOrders();
		} else if(timerEvent == null) {
			timerEvent = new AsyncTimerEvent();
			Business.getInstance().getScheduleManager().scheduleTimerEvent(maxRefreshInterval, this, timerEvent);
		}
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof OrderCacheReadyEvent) {
			log.debug("Recieved event: " + event);
			smartShowOrders();
		} else if (event instanceof GuiSingleOrderStrategyUpdateEvent) {
			log.debug("Recieved event: " + event);
			smartShowOrders();
		} else if (event instanceof AsyncTimerEvent) {
			timerEvent = null;
			asyncShowOrders();
		} else if (event instanceof AccountSelectionEvent) {
			if(pinned) {
				accountFilter.setMatch("Account", ((AccountSelectionEvent) event).getAccount());
				smartShowOrders();
			}
		} else if (event instanceof ParentOrderReplyEvent) {
			final ParentOrderReplyEvent evt = (ParentOrderReplyEvent)event;
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if(evt.isOk()) {
						lbStatus.setText("");
						showQuickOrderStatus(false);
					} else {
						lbStatus.setText(evt.getMessage());
						showQuickOrderStatus(true);
					}
						
				}
			});
		} else {
			log.warn("Unhandled event: " + event);
		}
				
	}
}
