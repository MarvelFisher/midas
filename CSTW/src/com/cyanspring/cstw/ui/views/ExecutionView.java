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
package com.cyanspring.cstw.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.order.AllExecutionSnapshotReplyEvent;
import com.cyanspring.common.event.order.AllExecutionSnapshotRequestEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;
import com.cyanspring.cstw.gui.filter.ParentOrderFilter;
import com.cyanspring.cstw.localevent.AccountSelectionLocalEvent;

public class ExecutionView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(ExecutionView.class);

	public static final String ID = "com.cyanspring.cstw.gui.ExecutionView";
	private DynamicTableViewer viewer;
	private boolean columnSet;
	private Action pinAction;
	private boolean pinned;
	private String TOOLBAR_ID_PIN = "TOOLBAR_PIN";
	private ParentOrderFilter accountFilter;
	private ImageRegistry imageRegistry;
	private String currentAccount;
	private List<Execution> executionList = new ArrayList<Execution>();

	public ExecutionView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		imageRegistry = Activator.getDefault().getImageRegistry();

		// create parent layout
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		// create table
		String strFile = "ExecutionTable.xml";
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

		// create pin order action
		createPinAction(parent);

		// subscribe to business event
		Business.getInstance().getEventManager()
				.subscribe(ChildOrderUpdateEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(AllExecutionSnapshotReplyEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(AccountSelectionLocalEvent.class, this);

		UserGroup ug = Business.getInstance().getUserGroup();
		List<String> id = Business.getInstance().getAccountGroup();
		if (ug.isAdmin()) {
			id = null;
		} else if (!ug.getRole().isManagerLevel()) {
			id = new ArrayList<String>();
			id.add(Business.getInstance().getAccount());
		}

		AllExecutionSnapshotRequestEvent event = new AllExecutionSnapshotRequestEvent(
				ID, Business.getInstance().getFirstServer(), id);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}

		if (!StringUtils.hasText(currentAccount)) {
			currentAccount = Business.getInstance().getAccount();
		}
	}

	private void createPinAction(Composite parent) {
		pinAction = new StyledAction("",
				org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				pinned = pinned ? false : true;
				if (!pinned) {
					viewer.removeFilter(accountFilter);
				} else {
					accountFilter.setMatch("Account", Business.getInstance()
							.getAccount());
					viewer.addFilter(accountFilter);
				}
			}
		};

		pinAction.setId(TOOLBAR_ID_PIN);
		pinAction.setChecked(true);
		pinned = true;
		accountFilter = new ParentOrderFilter();
		accountFilter.setMatch("Account", Business.getInstance().getAccount());
		viewer.addFilter(accountFilter);

		pinAction.setText("Pin Account");
		pinAction.setToolTipText("Pin account");

		ImageDescriptor imageDesc = imageRegistry
				.getDescriptor(ImageID.PIN_ICON.toString());
		pinAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(pinAction);
	}

	@Override
	public void setFocus() {
	}

	private List<Map<String, Object>> executionListToMap(List<Execution> list) {

		if (null == list)
			return null;

		List<Map<String, Object>> tempMapList = new ArrayList<Map<String, Object>>();
		for (Execution exe : list) {
			tempMapList.add(exe.getFields());
		}

		return tempMapList;
	}

	@Override
	public void dispose() {
		super.dispose();
		Business.getInstance().getEventManager()
				.unsubscribe(ChildOrderUpdateEvent.class, this);
		Business.getInstance().getEventManager()
				.unsubscribe(AllExecutionSnapshotReplyEvent.class, this);
		Business.getInstance().getEventManager()
				.unsubscribe(AccountSelectionLocalEvent.class, this);
	}

	private void show(final List<Map<String, Object>> data) {
		if (data == null)
			return;

		if (viewer.getControl().getDisplay().isDisposed())
			return;

		viewer.getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (!columnSet && data.size() > 0) {
					Map<String, Object> map = data.get(0);
					ArrayList<ColumnProperty> columnProperties = new ArrayList<ColumnProperty>();
					Set<String> titles = map.keySet();
					for (String title : titles)
						columnProperties.add(new ColumnProperty(title, 100));

					viewer.setSmartColumnProperties(
							viewer.getTableLayoutFile(), columnProperties);
					columnSet = true;
					viewer.setInput(data);
				}
				if (viewer.getInput() == null || viewer.getInput() != data)
					viewer.setInput(data);
				viewer.refresh();
			}

		});

	}

	synchronized private void updateExecution(Execution execution) {
		if (null == execution)
			return;

		executionList.add(execution);
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				show(executionListToMap(executionList));
			}
		});
	}

	@Override
	public void onEvent(final AsyncEvent e) {
		if (e instanceof ChildOrderUpdateEvent) {
			log.info("get ChildOrderUpdateEvent");
			ChildOrderUpdateEvent event = (ChildOrderUpdateEvent) e;
			Execution execution = event.getExecution();
			if (event.getExecution() == null)
				return;
			else {
				log.info("execution is null");
			}
			updateExecution(execution);
		} else if (e instanceof AllExecutionSnapshotReplyEvent) {
			AllExecutionSnapshotReplyEvent event = (AllExecutionSnapshotReplyEvent) e;
			if (!event.isOk()) {
				log.warn("process AllExecutionSnapshotReplyEvent fail:{}",
						event.getMessage());
				return;
			}
			List<Execution> list = event.getExecutionList();
			executionList = list;
			log.info("get AllExecution list:{}", list.size());
			show(executionListToMap(list));
		} else if (e instanceof AccountSelectionLocalEvent) {
			currentAccount = ((AccountSelectionLocalEvent) e).getAccount();
			if (pinned) {
				accountFilter.setMatch("Account", currentAccount);
				show(executionListToMap(executionList));
			}
		} else {
			log.error("Unhandled event: " + e.getClass());
		}
	}

}
