package com.cyanspring.cstw.ui.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.signal.AmendSignalEvent;
import com.cyanspring.common.event.signal.CancelSignalEvent;
import com.cyanspring.common.event.signal.SignalEvent;
import com.cyanspring.common.event.signal.SignalSubEvent;
import com.cyanspring.common.util.ArrayMap;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;
import com.cyanspring.cstw.event.SignalSelectionEvent;
import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class SignalView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(SignalView.class);
	public static final String ID = "com.cyanspring.cstw.gui.SignalView";

	private DynamicTableViewer viewer;
	private boolean setColumns;
	private AsyncTimerEvent timerEvent;
	private Date lastRefreshTime = Clock.getInstance().now();
	private final long maxRefreshInterval = 300;
	private ArrayMap<String, Map<String, Object>> signals = new ArrayMap<String, Map<String, Object>>();

	private ImageRegistry imageRegistry;
	private Composite entryPanel;
	private Label lbSymbol;
	private Text edSymbol;
	private Button btOk;
	private Button btCancel;
	private Action addSymbolAction;
	private Action deleteSymbolAction;
	private Action multiAmendAction;
	private AmendDialog amendDialog;

	private void addSymbol(String symbol) {
		try {
			Map<String, Object> signal = signals.get(symbol);
			if (null != signal)
				return;
			signal = new HashMap<String, Object>();
			signal.put(OrderField.ID.value(), symbol);
			signals.put(symbol, signal);
			String server = Business.getInstance().getFirstServer();
			Business.getInstance().getEventManager()
					.sendRemoteEvent(new SignalSubEvent(ID, server, symbol));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void createAddSymbolAction(final Composite parent) {
		addSymbolAction = new Action() {
			public void run() {
				if (entryPanel.isVisible()) {
					showEntryPanel(false);
				} else {
					showEntryPanel(true);
					edSymbol.setFocus();
				}
				parent.layout();
			}
		};
		addSymbolAction.setText("Add Symbol");
		addSymbolAction.setToolTipText("Add a symbol");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.PLUS_ICON.toString());
		addSymbolAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(addSymbolAction);
	}

	private void createDeleteSymbolAction(final Composite parent) {
		deleteSymbolAction = new Action() {
			public void run() {
				TableItem items[] = viewer.getTable().getSelection();
				for (TableItem item : items) {
					Object obj = item.getData();
					if (obj instanceof HashMap) {
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) obj;
						String symbol = (String) map.get(OrderField.ID.value());
						String server = (String) map.get(OrderField.SERVER_ID
								.value());
						try {
							Business.getInstance()
									.getEventManager()
									.sendRemoteEvent(
											new CancelSignalEvent(symbol,
													server));
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						signals.remove(symbol);
					}
				}
				viewer.refresh();
			}
		};
		deleteSymbolAction.setText("Delete Symbol");
		deleteSymbolAction.setToolTipText("Delete a symbol");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.FALSE_ICON.toString());
		deleteSymbolAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(deleteSymbolAction);
	}

	private void showEntryPanel(boolean show) {
		entryPanel.setVisible(show);
		GridData data = (GridData) entryPanel.getLayoutData();
		data.exclude = !show;
		entryPanel.getParent().layout();
	}

	private void createEntryPanel(final Composite parent) {
		entryPanel = new Composite(parent, SWT.NONE);
		GridData filterGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		entryPanel.setLayout(layout);

		lbSymbol = new Label(entryPanel, SWT.NONE);
		lbSymbol.setText("Symbol: ");
		lbSymbol.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		edSymbol = new Text(entryPanel, SWT.BORDER | SWT.SEARCH);
		edSymbol.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		edSymbol.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					addSymbol(edSymbol.getText());
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

		});

		btOk = new Button(entryPanel, SWT.FLAT);
		btOk.setText("OK");
		btOk.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		btOk.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				addSymbol(edSymbol.getText());
			}

		});

		btCancel = new Button(entryPanel, SWT.FLAT);
		btCancel.setText("Cancel");
		btCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		btCancel.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				showEntryPanel(false);
			}

		});

		entryPanel.setLayoutData(filterGridData);
		entryPanel.layout();

		filterGridData.exclude = true;
		entryPanel.setVisible(false);
	}

	@Override
	public void createPartControl(Composite parent) {
		log.info("Creating signal view");
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();
		// create parent layout
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		createEntryPanel(parent);
		createAddSymbolAction(parent);
		createDeleteSymbolAction(parent);
		createMultiAmendAction(parent);

		// create table
		String strFile = "SignalView.xml";
		viewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL, strFile);
		viewer.init();

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		viewer.setInput(signals.toArray());

		final Table table = viewer.getTable();
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = table.getItem(table.getSelectionIndex());
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) obj;
					List<String> editableFields = new ArrayList<String>(map
							.keySet());
					Business.getInstance()
							.getEventManager()
							.sendEvent(
									new SignalSelectionEvent(map,
											editableFields));
				}
			}
		});

		Business.getInstance().getEventManager()
				.subscribe(OrderCacheReadyEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(SignalEvent.class, this);
	}
	
	@Override
	public void dispose() {
		Business.getInstance().getEventManager()
				.unsubscribe(OrderCacheReadyEvent.class, this);
		Business.getInstance().getEventManager()
				.unsubscribe(SignalEvent.class, this);
		super.dispose();
	}

	private void createMultiAmendAction(final Composite parent) {
		multiAmendAction = new Action() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void run() {
				amendDialog = new AmendDialog(parent.getShell());
				amendDialog.open();
				if (amendDialog.getReturnCode() == org.eclipse.jface.window.Window.OK) {
					String maTag = amendDialog.getField();
					String maValue = amendDialog.getValue();
					if (null == maTag || maTag.equals(""))
						return;
					Table table = SignalView.this.viewer.getTable();

					TableItem items[] = table.getSelection();
					for (TableItem item : items) {
						Object obj = item.getData();
						if (obj instanceof HashMap) {
							HashMap<String, Object> map = (HashMap<String, Object>) obj;
							String id = (String) map.get(OrderField.ID.value());
							String server = (String) map
									.get(OrderField.SERVER_ID.value());
							Map<String, Object> changes = new HashMap<String, Object>();
							changes.put(OrderField.ID.value(), id);
							changes.put(maTag, maValue);
							Map<String, Class> types = new HashMap<String, Class>();
							for (Entry<String, Object> entry : changes
									.entrySet()) {
								Object object = map.get(entry.getKey());
								if (null == object)
									types.put(entry.getKey(), String.class);
								else
									types.put(entry.getKey(),
											(Class<Object>) object.getClass());
							}
							AmendSignalEvent event = new AmendSignalEvent(id,
									server, changes, types);
							try {
								Business.getInstance().getEventManager()
										.sendRemoteEvent(event);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
						}
					}
				}
			}
		};
		multiAmendAction.setText("Multi amend a field");
		multiAmendAction.setToolTipText("Multi amend a field");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.EDIT_ICON.toString());
		multiAmendAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(multiAmendAction);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void show() {
		lastRefreshTime = Clock.getInstance().now();
		if (!setColumns) {
			List<Map<String, Object>> inputs = signals.toArray();

			if (inputs.size() == 0)
				return;

			log.debug("Setting signal columns");
			Map<String, Object> record = signals.get(0);
			ArrayList<ColumnProperty> columnProperties = new ArrayList<ColumnProperty>();
			for (String tag : record.keySet()) {
				columnProperties.add(new ColumnProperty(tag, 100));
			}
			viewer.setSmartColumnProperties("Signal", columnProperties);
			viewer.setInput(inputs);
			setColumns = true;
		}
		viewer.refresh();
	}

	private void asyncShow() {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				show();
			}
		});
	}

	private void smartShow() {
		if (TimeUtil.getTimePass(lastRefreshTime) > maxRefreshInterval) {
			asyncShow();
		} else if (timerEvent == null) {
			timerEvent = new AsyncTimerEvent();
			Business.getInstance().getScheduleManager()
					.scheduleTimerEvent(maxRefreshInterval, this, timerEvent);
		}
	}

	public void processSignalEvent(SignalEvent event) {
		String symbol = event.getSignal().get(String.class,
				OrderField.ID.value());
		signals.put(symbol, event.getSignal().getFields());
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof OrderCacheReadyEvent) {
			String server = Business.getInstance().getFirstServer();
			if (null == server)
				return;
			try {
				Business.getInstance().getEventManager()
						.sendRemoteEvent(new SignalSubEvent(ID, server, null));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else if (event instanceof SignalEvent) {
			processSignalEvent((SignalEvent) event);
			smartShow();
		} else if (event instanceof AsyncTimerEvent) {
			timerEvent = null;
			asyncShow();
		} else {
			log.warn("Unhandled event: " + event);
		}

	}

}
