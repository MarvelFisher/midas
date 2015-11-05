package com.cyanspring.cstw.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.cstw.position.IPositionChangeListener;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotRequestEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotRequestEvent.PositionType;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.ExecutionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.event.statistic.AccountNumberReplyEvent;
import com.cyanspring.common.event.statistic.AccountNumberRequestEvent;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.gui.bean.PositionStatisticBean;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;
import com.cyanspring.cstw.session.CSTWSession;

public class ClosedPositionView extends ViewPart implements IAsyncEventListener {

	private static final Logger log = LoggerFactory
			.getLogger(ClosedPositionView.class);
	public static final String ID = "com.cyanspring.cstw.gui.ClosedPositionViewer";
	private DynamicTableViewer closedPositionViewer;

	private int limitAccount = 2000;
	private Action aggregateAction;
	private Action refreshAction;
	private ImageRegistry imageRegistry;
	private ReentrantLock lock = new ReentrantLock();
	private Map<String, PositionStatisticBean> symbolCpMap = new HashMap<String, PositionStatisticBean>();
	private ConcurrentHashMap<String, List<ClosedPosition>> accountCpMap = new ConcurrentHashMap<String, List<ClosedPosition>>();
	private AsyncTimerEvent refreshEvent = new AsyncTimerEvent();
	private long maxRefreshInterval = 1000;
	private boolean isAggregateColumnCreated = false;
	private boolean isAllPositionColumnCreate = false;

	enum Title {
		CLASS, MARGIN
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof AsyncTimerEvent) {

			displayClosedPosition();
		} else if (event instanceof AllPositionSnapshotReplyEvent) {

			AllPositionSnapshotReplyEvent allPosition = (AllPositionSnapshotReplyEvent) event;
			List<ClosedPosition> cps = allPosition.getClosedPositionList();
			if (null != cps) {
				for (ClosedPosition cp : cps) {
					updatePosition(cp);
				}
			}
			displayClosedPosition();
		} else if (event instanceof ClosedPositionUpdateEvent) {

			ClosedPosition position = ((ClosedPositionUpdateEvent) event)
					.getPosition();
			log.info("update closed position:{},{}", position.getAccount(),
					position.getSymbol());
			updatePosition(position);
		} else if (event instanceof AccountNumberReplyEvent) {

			AccountNumberReplyEvent accountNum = (AccountNumberReplyEvent) event;
			if (accountNum.isOk()) {
				log.info("Account Number:{}", accountNum.getAccountNumber());
				if (limitAccount > accountNum.getAccountNumber()) {
					sendAllPositionRequestEvent();
				} else {
					log.error("Total account :" + accountNum.getAccountNumber()
							+ " exceed account limit:" + limitAccount);
					showDialog("Total account :"
							+ accountNum.getAccountNumber()
							+ " exceed account limit:" + limitAccount);
					refreshAction.setEnabled(false);
				}
			} else {
				log.error(accountNum.getErrorMessage());
				showDialog(accountNum.getErrorMessage());
				refreshAction.setEnabled(false);
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {

		subEvent(AllPositionSnapshotReplyEvent.class);
		subEvent(AccountNumberReplyEvent.class);
		subEvent(ClosedPositionUpdateEvent.class, null);

		imageRegistry = Activator.getDefault().getImageRegistry();
		final Composite mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayout(new FillLayout());

		createClosedPositionViewer(mainComposite);
		createAggregateAction(parent);
		createRefreshAction(parent);

		AccountNumberRequestEvent accountNumberEvent = new AccountNumberRequestEvent(
				ID, Business.getInstance().getFirstServer());
		sendRemoteEvent(accountNumberEvent);
		try {
			scheduleJob(refreshEvent, maxRefreshInterval);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void updatePosition(ClosedPosition position) {

		String account = position.getAccount();
		List<ClosedPosition> tempList = new ArrayList<ClosedPosition>();
		if (accountCpMap.containsKey(account)) {
			List<ClosedPosition> oldList = accountCpMap.get(account);
			oldList.add(position);
			accountCpMap.put(account, oldList);
		} else {
			tempList = new ArrayList<ClosedPosition>();
			tempList.add(position);
			accountCpMap.put(account, tempList);
		}
	}

	private void collectAllPosition(List<ClosedPosition> ops) {

		log.info("Collect All position:{}", ops.size());
		for (ClosedPosition op : ops) {
			List<ClosedPosition> opList = null;
			String account = op.getAccount();
			if (accountCpMap.containsKey(account)) {
				opList = accountCpMap.get(account);
				boolean isSymbolExist = false;
				for (ClosedPosition oldOp : opList) {
					if (oldOp.getSymbol().equals(op.getSymbol())) {
						isSymbolExist = true;
						oldOp.addQty(op.getQty());
						oldOp.setPnL(op.getPnL() + oldOp.getPnL());
						oldOp.setAcPnL(op.getAcPnL() + oldOp.getAcPnL());
					}
				}
				if (!isSymbolExist)
					opList.add(op);
			} else {
				opList = new ArrayList<ClosedPosition>();
				opList.add(op);
			}
			accountCpMap.put(account, opList);
		}
	}

	private void caculate(ClosedPosition op) {
		PositionStatisticBean bean = null;
		String symbol = op.getSymbol();
		if (symbolCpMap.containsKey(symbol)) {
			bean = symbolCpMap.get(op.getSymbol());
		} else {
			bean = new PositionStatisticBean();
			bean.setSymbol(op.getSymbol());
		}

		bean.setQty(op.getQty() + bean.getQty());
		bean.setPnL(op.getPnL() + bean.getPnL());
		bean.setAcPnL(op.getAcPnL() + bean.getAcPnL());

		if (PriceUtils.isZero(bean.getQty()))
			return;

		if (null != bean)
			symbolCpMap.put(symbol, bean);
	}

	private void sendAllPositionRequestEvent() {
		accountCpMap.clear();
		symbolCpMap.clear();
		log.info("send AllPositionSnapshotRequestEvent");
		AllPositionSnapshotRequestEvent request = new AllPositionSnapshotRequestEvent(
				ID, Business.getInstance().getFirstServer(),
				PositionType.ClosedPosition);
		sendRemoteEvent(request);
	}

	public void displayClosedPosition() {

		try {
			lock.lock();
			if (!aggregateAction.isChecked()) {
				showAllClosedPosition();
			} else {
				showAggregateOpenPosition();
			}
		} finally {
			lock.unlock();
		}
	}

	private void showAggregateOpenPosition() {

		symbolCpMap = new HashMap<String, PositionStatisticBean>();
		final List<ClosedPosition> cpList = getAllClosedPositionList();

		for (ClosedPosition cp : cpList) {
			caculate(cp);
		}

		final List<PositionStatisticBean> psbList = new ArrayList<PositionStatisticBean>();
		psbList.addAll(symbolCpMap.values());

		closedPositionViewer.getControl().getDisplay()
				.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (closedPositionViewer) {

							if (closedPositionViewer.isViewClosing())
								return;

							if (null == psbList || psbList.isEmpty()) {
								closedPositionViewer.setInput(psbList);
								closedPositionViewer.refresh();
								return;
							}

							if (!isAggregateColumnCreated) {
								List<ColumnProperty> properties = closedPositionViewer
										.setObjectColumnProperties(psbList
												.get(0));
								properties = filterColumn(properties);

								if (properties.size() < closedPositionViewer
										.getComparator().getColumn())
									closedPositionViewer.getComparator()
											.setColumn(0);

								closedPositionViewer.setSmartColumnProperties(
										psbList.get(0).getClass().getName(),
										properties);
								isAggregateColumnCreated = true;
							}

							closedPositionViewer.setInput(psbList);
							closedPositionViewer.refresh();
						}
					}
				});

	}

	private void showAllClosedPosition() {

		final List<ClosedPosition> psbList = getAllClosedPositionList();
		closedPositionViewer.getControl().getDisplay()
				.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (closedPositionViewer) {

							if (closedPositionViewer.isViewClosing())
								return;

							if (null == psbList || psbList.isEmpty()) {
								closedPositionViewer.setInput(psbList);
								closedPositionViewer.refresh();
								return;
							}
							if (!isAllPositionColumnCreate) {
								List<ColumnProperty> properties = closedPositionViewer
										.setObjectColumnProperties(psbList
												.get(0));
								closedPositionViewer.setSmartColumnProperties(
										psbList.get(0).getClass().getName(),
										properties);
								isAllPositionColumnCreate = true;
							}

							closedPositionViewer.setInput(psbList);
							closedPositionViewer.refresh();
						}
					}
				});
	}

	private List<ColumnProperty> filterColumn(List<ColumnProperty> properties) {

		List<ColumnProperty> propList = new ArrayList<ColumnProperty>();
		for (ColumnProperty cp : properties) {
			if (!cp.getTitle().toUpperCase().equals(Title.CLASS.name())
					&& !cp.getTitle().toUpperCase().equals(Title.MARGIN.name())) {
				propList.add(cp);
			}
		}
		return propList;
	}

	private List<ClosedPosition> getAllClosedPositionList() {

		final List<ClosedPosition> psbList = new ArrayList<ClosedPosition>();
		Iterator<List<ClosedPosition>> opIterator = accountCpMap.values()
				.iterator();
		while (opIterator.hasNext()) {
			List<ClosedPosition> ops = opIterator.next();
			for (ClosedPosition op : ops) {
				if (!PriceUtils.isZero(op.getQty()))
					psbList.add(op);
			}
		}
		return psbList;
	}

	private void createRefreshAction(final Composite parent) {

		refreshAction = new StyledAction("",
				org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				if (refreshAction.isChecked()) {
					scheduleJob(refreshEvent, maxRefreshInterval);
				} else {
					cancelScheduleJob(refreshEvent);
				}
			}
		};

		refreshAction.setChecked(true);
		refreshAction.setText("Auto Refresh");
		refreshAction.setToolTipText("Auto Refresh");
		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.REFRESH_ICON.toString());
		refreshAction.setImageDescriptor(imageDesc);
		getViewSite().getActionBars().getToolBarManager().add(refreshAction);
	}

	private void createAggregateAction(final Composite parent) {

		aggregateAction = new StyledAction("",
				org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {

				isAggregateColumnCreated = false;
				isAllPositionColumnCreate = false;

				if (!aggregateAction.isChecked()) {
					showAllClosedPosition();
				} else {
					showAggregateOpenPosition();
				}
			}
		};

		aggregateAction.setChecked(false);
		aggregateAction.setText("Aggregate");
		aggregateAction.setToolTipText("Aggregate");
		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.FILTER_ICON.toString());
		aggregateAction.setImageDescriptor(imageDesc);
		// getViewSite().getActionBars().getToolBarManager().add(aggregateAction);
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		Business.getInstance().getScheduleManager()
				.cancelTimerEvent(refreshEvent);
		unSubEvent(AllPositionSnapshotReplyEvent.class);
		unSubEvent(AccountNumberReplyEvent.class);
		unSubEvent(ClosedPositionUpdateEvent.class, null);
		super.dispose();
	}

	private void createClosedPositionViewer(Composite parent) {
		String strFile = CSTWSession.getInstance().getConfigPath()
				+ "ClosedPositionViewTable.xml";
		closedPositionViewer = new DynamicTableViewer(parent, SWT.MULTI
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, Business
				.getInstance().getXstream(), strFile, BeanHolder.getInstance()
				.getDataConverter());
		closedPositionViewer.init();
	}

	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void subEvent(Class<? extends AsyncEvent> clazz) {
		Business.getInstance().getEventManager().subscribe(clazz, ID, this);
	}

	private void unSubEvent(Class<? extends AsyncEvent> clazz) {
		Business.getInstance().getEventManager().unsubscribe(clazz, ID, this);
	}

	private void unSubEvent(Class<? extends AsyncEvent> clazz, String ID) {
		Business.getInstance().getEventManager().unsubscribe(clazz, ID, this);
	}

	private void subEvent(Class<? extends AsyncEvent> clazz, String ID) {
		Business.getInstance().getEventManager().subscribe(clazz, ID, this);
	}

	private void scheduleJob(AsyncTimerEvent timerEvent, long maxRefreshInterval) {

		Business.getInstance()
				.getScheduleManager()
				.scheduleRepeatTimerEvent(maxRefreshInterval,
						ClosedPositionView.this, timerEvent);
	}

	private void cancelScheduleJob(AsyncTimerEvent timerEvent) {
		Business.getInstance().getScheduleManager()
				.cancelTimerEvent(timerEvent);
	}

	public void showDialog(final String msg) {
		if (null == closedPositionViewer)
			return;

		if (closedPositionViewer.getControl().getDisplay().isDisposed())
			return;

		closedPositionViewer.getControl().getDisplay()
				.asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(closedPositionViewer
								.getControl().getShell(), "Closed Position",
								msg);
					}
				});
	}

}
