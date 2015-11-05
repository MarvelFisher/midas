package com.cyanspring.cstw.ui.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.cstw.kdb.SignalType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.kdb.VolatilityListReplyEvent;
import com.cyanspring.common.event.kdb.VolatilityListRequestEvent;
import com.cyanspring.common.event.kdb.VolatilityUpdateEvent;
import com.cyanspring.common.kdb.Volatility;
import com.cyanspring.common.util.ArrayMap;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class KDBInfoView extends ViewPart implements IAsyncEventListener {
	enum Column {
		Symbol, Time, Scale
	}

	private static final Logger log = LoggerFactory
			.getLogger(KDBInfoView.class);
	public static final String ID = "com.cyanspring.cstw.kdb.gui.KDBInfoViewer";
	private Composite parentComposite;
	private ImageRegistry imageRegistry;
	private ArrayMap<String, Volatility> realTimeMap = new ArrayMap<String, Volatility>();
	private ArrayMap<String, List<Volatility>> historyMap = new ArrayMap<String, List<Volatility>>();
	private ReentrantLock lock = new ReentrantLock();
	private boolean needUpdate = false;
	private boolean realTimeColumnCreated = false;
	private AsyncTimerEvent refreshEvent = new AsyncTimerEvent();
	private long minRefreshInterval = 300;
	private DynamicTableViewer realTimeViewer;
	private DynamicTableViewer pastTimeViewer;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	private Image redIcon = null;
	private Image yellowIcon = null;
	private Image greenIcon = null;

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof VolatilityListReplyEvent) {
			log.info("receive VolatilityListReplyEvent");
			VolatilityListReplyEvent e = (VolatilityListReplyEvent) event;
			updateVolatilityMap(e.getVolatilities());
			needUpdate = true;
		} else if (event instanceof VolatilityUpdateEvent) {
			log.info("receive VolatilityUpdateEvent");
			VolatilityUpdateEvent e = (VolatilityUpdateEvent) event;
			updateVolatilityMap(e.getVolatilities());
			needUpdate = true;
		} else if (event instanceof AsyncTimerEvent) {
			if (needUpdate)
				refreshRealTimeTable();
		}
	}

	private void refreshRealTimeTable() {
		needUpdate = false;
		realTimeViewer.getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				synchronized (realTimeViewer) {
					if (realTimeViewer.isViewClosing())
						return;

					if (null != realTimeViewer.getTable()
							&& null != realTimeViewer.getTable().getMenu()
							&& realTimeViewer.getTable().getMenu().isVisible()) {
						return;
					}

					if (!realTimeColumnCreated) {

						ArrayList<Volatility> list = realTimeMap.toArray();
						if (list.isEmpty())
							return;

						Object obj = list.get(0);
						List<ColumnProperty> properties = realTimeViewer
								.setObjectColumnProperties(obj);
						realTimeViewer.setSmartColumnProperties(obj.getClass()
								.getName(), properties);

						realTimeColumnCreated = true;
						realTimeViewer.setInput(list);
					}

					realTimeViewer.refresh();

				}
			}
		});

		setTableItemFormat();
	}

	private void setTableItemFormat() {

		realTimeViewer.getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				synchronized (realTimeViewer) {
					int timeColumn = getColumnPosition(
							realTimeViewer.getTable(), Column.Time);
					int scaleColumn = getColumnPosition(
							realTimeViewer.getTable(), Column.Scale);
					TableItem items[] = realTimeViewer.getTable().getItems();
					for (TableItem item : items) {
						Volatility bean = (Volatility) item.getData();
						String time = timeFormat.format(bean.getTime());
						double scale = bean.getScale();
						item.setText(timeColumn, time);
						SignalType type = Business.getBusinessService()
								.getSignal(bean.getSymbol(), scale);
						if (SignalType.RED == type) {
							item.setImage(scaleColumn, redIcon);
						} else if (SignalType.YELLOW == type) {
							item.setImage(scaleColumn, yellowIcon);
						} else if (SignalType.GREEN == type) {
							item.setImage(scaleColumn, greenIcon);
						}
					}
				}
			}
		});
	}

	private int getColumnPosition(Table table, Column col) {
		TableColumn columns[] = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			if (col.name().equals(columns[i].getText()))
				return i;
		}
		return -1;
	}

	@Override
	public void createPartControl(Composite parent) {
		parentComposite = parent;
		imageRegistry = Activator.getDefault().getImageRegistry();
		redIcon = imageRegistry.getDescriptor(ImageID.REDLIGHT_ICON.toString())
				.createImage();
		yellowIcon = imageRegistry.getDescriptor(
				ImageID.YELLOWLIGHT_ICON.toString()).createImage();
		greenIcon = imageRegistry.getDescriptor(
				ImageID.GREENLIGHT_ICON.toString()).createImage();
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		createRealTimeViewer(sashForm);
		createPastTimeViewer(sashForm);

		subEvent(VolatilityListReplyEvent.class);
		subEvent(VolatilityUpdateEvent.class);
		VolatilityListRequestEvent request = new VolatilityListRequestEvent(
				IdGenerator.getInstance().getNextID(), Business.getInstance()
						.getFirstServer());
		sendRemoteEvent(request);
		scheduleJob(refreshEvent, minRefreshInterval);
	}

	private void createRealTimeViewer(Composite parent) {
		String strFile = Business.getInstance().getConfigPath()
				+ "VolatilityTable.xml";
		realTimeViewer = new DynamicTableViewer(parent, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, Business
				.getInstance().getXstream(), strFile, BeanHolder.getInstance()
				.getDataConverter());

		realTimeViewer.init();
		realTimeViewer.getTable().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = realTimeViewer.getTable();
				TableItem items[] = table.getSelection();
				for (TableItem item : items) {
					Object obj = item.getData();
					if (obj instanceof Volatility) {
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void createPastTimeViewer(Composite parent) {
		String strFile = Business.getInstance().getConfigPath()
				+ "VolatilityTable.xml";
		pastTimeViewer = new DynamicTableViewer(parent, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, Business
				.getInstance().getXstream(), strFile, BeanHolder.getInstance()
				.getDataConverter());
		pastTimeViewer.init();
		pastTimeViewer.getTable().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = pastTimeViewer.getTable();
				TableItem items[] = table.getSelection();
				for (TableItem item : items) {
					Object obj = item.getData();
					if (obj instanceof Volatility) {
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void updateVolatilityMap(List<Volatility> volatilities) {
		try {
			lock.lock();
			for (Volatility vol : volatilities) {
				String symbol = vol.getSymbol();
				if (!StringUtils.hasText(symbol)) {
					continue;
				}
				if (historyMap.toMap().containsKey(symbol)) {
					List<Volatility> list = historyMap.get(symbol);
					list.add(vol);
					historyMap.put(symbol, list);
				} else {
					List<Volatility> list = new ArrayList<Volatility>();
					list.add(vol);
					historyMap.put(symbol, list);
				}
				realTimeMap.put(symbol, vol);
			}
		} finally {
			if (lock.isLocked())
				lock.unlock();
		}
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		super.dispose();
		cancelScheduleJob(refreshEvent);
		unSubEvent(VolatilityListReplyEvent.class);
		unSubEvent(VolatilityUpdateEvent.class);
	}

	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void scheduleJob(AsyncTimerEvent timerEvent, long maxRefreshInterval) {
		Business.getInstance()
				.getScheduleManager()
				.scheduleRepeatTimerEvent(maxRefreshInterval, KDBInfoView.this,
						timerEvent);
	}

	private void cancelScheduleJob(AsyncTimerEvent timerEvent) {
		Business.getInstance().getScheduleManager()
				.cancelTimerEvent(timerEvent);
	}

	private void showMessageBox(final String msg, Composite parent) {
		parent.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(parentComposite
						.getShell(), SWT.ICON_INFORMATION);
				messageBox.setText("Info");
				messageBox.setMessage(msg);
				messageBox.open();
			}

		});
	}

	private void subEvent(Class<? extends AsyncEvent> clazz) {
		Business.getInstance().getEventManager().subscribe(clazz, this);
	}

	private void unSubEvent(Class<? extends AsyncEvent> clazz) {
		Business.getInstance().getEventManager().subscribe(clazz, this);
	}

	class VolatilitySorter implements Comparator<Volatility> {

		@Override
		public int compare(Volatility vol1, Volatility vol2) {
			if (vol1.getTime().getTime() > vol2.getTime().getTime()) {
				return 0;
			}

			return 1;
		}

	}
}
