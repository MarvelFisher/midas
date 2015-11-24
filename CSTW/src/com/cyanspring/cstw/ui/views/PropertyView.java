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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.signal.AmendSignalEvent;
import com.cyanspring.common.event.signal.SignalEvent;
import com.cyanspring.common.event.strategy.AmendMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.StrategyLogEvent;
import com.cyanspring.common.type.KeyValue;
import com.cyanspring.common.type.LogType;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.gui.common.PropertyTableViewer;
import com.cyanspring.cstw.localevent.InstrumentSelectionLocalEvent;
import com.cyanspring.cstw.localevent.MultiInstrumentStrategySelectionLocalEvent;
import com.cyanspring.cstw.localevent.ObjectSelectionLocalEvent;
import com.cyanspring.cstw.localevent.SignalSelectionLocalEvent;
import com.cyanspring.cstw.localevent.SingleInstrumentStrategySelectionLocalEvent;
import com.cyanspring.cstw.localevent.SingleOrderStrategySelectionLocalEvent;

public class PropertyView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(PropertyView.class);
	public static final String ID = "com.cyanspring.cstw.gui.PropertyView";
	private PropertyTableViewer viewer;
	private Action editAction;
	private boolean editMode;
	private ImageRegistry imageRegistry;
	private String objectId;
	@SuppressWarnings("rawtypes")
	private Class clazz;
	private List<String> editableFields;
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		imageRegistry = Activator.getDefault().getImageRegistry();

		viewer = new PropertyTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, BeanHolder.getInstance().getDataConverter());
		viewer.init();
		
		// create local toolbars
		editAction = new Action() {
			public void run() {
				if(editMode) {
					viewer.applyEditorValue();
					confirmChange();
				}
				setEditMode(editMode?false:true);
			}
		};
		editAction.setText("Edit");
		editAction.setToolTipText("Edit strategy parameters");
		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.EDIT_ICON.toString());
		editAction.setImageDescriptor(imageDesc);
		
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(editAction);

		// subscribe to business event
		Business.getInstance().getEventManager().subscribe(SingleOrderStrategySelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().subscribe(SingleInstrumentStrategySelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().subscribe(AmendParentOrderReplyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ParentOrderUpdateEvent.class, this);
		Business.getInstance().getEventManager().subscribe(MultiInstrumentStrategyUpdateEvent.class, this);
		Business.getInstance().getEventManager().subscribe(MultiInstrumentStrategySelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().subscribe(InstrumentSelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().subscribe(SignalSelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().subscribe(SignalEvent.class, this);
	}
	
	@Override
	public void dispose() {
		Business.getInstance().getEventManager().unsubscribe(SingleOrderStrategySelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(SingleInstrumentStrategySelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(AmendParentOrderReplyEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(ParentOrderUpdateEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(MultiInstrumentStrategyUpdateEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(MultiInstrumentStrategySelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(InstrumentSelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(SignalSelectionLocalEvent.class, this);
		Business.getInstance().getEventManager().unsubscribe(SignalEvent.class, this);
		super.dispose();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void confirmChange() {
		List<KeyValue> changedFields = viewer.workoutChangedFields();
		HashMap<String, Object> oldFields = viewer.getSavedInput();
		String id = (String)oldFields.get(OrderField.ID.value());
		String server = (String)oldFields.get(OrderField.SERVER_ID.value());
		StringBuilder sb = new StringBuilder("Please confirm the changes: \n");
		for (KeyValue pair : changedFields) {
			String strOld = "[old]";
			String strNew = "[new]";
			try {
				strOld = BeanHolder.getInstance().getDataConverter().toString(pair.key, oldFields.get(pair.key));
				strNew = BeanHolder.getInstance().getDataConverter().toString(pair.key, pair.value);
			} catch (DataConvertException e) {
				log.error(e.getMessage(), e);
			}
			sb.append(pair.key + ": [" + strOld + " -> " + strNew +"]\n");
		}
		if (changedFields.size() > 0 && 
				MessageDialog.openConfirm(viewer.getControl().getShell(), "Are you sure?", sb.toString())) {
			HashMap<String, Object> changes = new HashMap<String, Object>();
			for(KeyValue pair : changedFields){
				changes.put(pair.key, pair.value);
			}
			changes.put(OrderField.ID.value(), id);
			RemoteAsyncEvent event;
			if(clazz.equals(SingleOrderStrategySelectionLocalEvent.class)) {
				event = new AmendParentOrderEvent(id, server, id, changes, null);
			} else if (clazz.equals(MultiInstrumentStrategySelectionLocalEvent.class)) {
				event = new AmendMultiInstrumentStrategyEvent(id, server, changes, null, null);
			} else if (clazz.equals(InstrumentSelectionLocalEvent.class)) {
				String strategyId = (String)oldFields.get(OrderField.STRATEGY_ID.value());
				changes.put(OrderField.ID.value(), id);
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				list.add(changes);
				event = new AmendMultiInstrumentStrategyEvent(strategyId, server, null, list, null);
			} else if (clazz.equals(SignalSelectionLocalEvent.class)) {
				Map<String, Class> types = new HashMap<String, Class>();
				for(Entry<String, Object> entry: changes.entrySet()) {
					Object obj = oldFields.get(entry.getKey());
					if(null == obj)
						types.put(entry.getKey(), String.class);
					else
						types.put(entry.getKey(), (Class<Object>)obj.getClass());
				}
				event = new AmendSignalEvent(id, server, changes, types);
			} else {
				log.error("Unknown event type: " + clazz);
				return;
			}
			try {
				Business.getInstance().getEventManager().sendRemoteEvent(event);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
	}
	
	private void setEditMode(boolean editMode) {
		if (viewer.getInput() == null)
			return;
		if(editMode) {
			this.editMode = true;
			editAction.setText("Done");
			editAction.setToolTipText("Save order parameters");
			ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.NONE_EDIT_ICON.toString());
			editAction.setImageDescriptor(imageDesc);
			viewer.turnOnEditMode(editableFields);
		} else {
			this.editMode = false;
			editAction.setText("Edit");
			editAction.setToolTipText("Edit order parameters");
			ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.EDIT_ICON.toString());
			editAction.setImageDescriptor(imageDesc);
			viewer.turnOffEditMode();
		}
	}

	public PropertyView() {
	}

	@Override
	public void setFocus() {

	}

	private void displayObject(final Map<String, Object> object) {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.setInput(object);
				viewer.refresh();
			}
		});
	}
	
	@Override
	public void onEvent(final AsyncEvent event) {
		if (event instanceof ObjectSelectionLocalEvent) {
			if (editMode)
				return;
			ObjectSelectionLocalEvent selectionEvent = (ObjectSelectionLocalEvent)event;
			objectId = (String)selectionEvent.getData().get(OrderField.ID.value());
			clazz = event.getClass();
			editableFields = selectionEvent.getEditableFields();
			displayObject(selectionEvent.getData());
		} else if (event instanceof ParentOrderUpdateEvent) {
			if (editMode)
				return;
			
			ParentOrderUpdateEvent update = (ParentOrderUpdateEvent)event;
			ParentOrder parentOrder = update.getOrder();
			if(!parentOrder.getId().equals(objectId))
				return;
			Map<String, Object> map = parentOrder.getFields();
//			map.put(OrderField.SERVER.value(), update.getSender());
			displayObject(map);
		} else if (event instanceof MultiInstrumentStrategyUpdateEvent) {
			if (editMode)
				return;
			if(null == clazz)
				return;
			MultiInstrumentStrategyUpdateEvent update = (MultiInstrumentStrategyUpdateEvent)event;
			if (clazz.equals(MultiInstrumentStrategySelectionLocalEvent.class)) {
				if(!update.getStrategyData().getId().equals(objectId))
					return;
				Map<String, Object> map = update.getStrategyData().getFields();
//				map.put(OrderField.SERVER.value(), update.getSender());
				displayObject(map);
			} else if (clazz.equals(InstrumentSelectionLocalEvent.class)) {
				Instrument instr = update.getStrategyData().getInstrumentData().get(objectId);
				if(null == instr)
					return;

				Map<String, Object> map = instr.getFields();
//				map.put(OrderField.SERVER.value(), update.getSender());
				displayObject(map);
			}
		} else if (event instanceof SignalEvent) {
			if (editMode)
				return;
			if(null == clazz)
				return;
			if(!clazz.equals(event.getClass()))
				return;
			SignalEvent signalEvent = (SignalEvent)event;
			String symbol = signalEvent.getSignal().get(String.class, OrderField.ID.value());
			if(symbol.equals(objectId))
				displayObject(signalEvent.getSignal().getFields());
		} else if (event instanceof AmendParentOrderReplyEvent) {
			AmendParentOrderReplyEvent reply = (AmendParentOrderReplyEvent)event;
			if(!reply.isOk())
				Business.getInstance().getEventManager().sendEvent(
					new StrategyLogEvent(reply.getKey(), null, LogType.Warn, 
							"Amend order failed: " + reply.getMessage(),
							Business.getInstance().getAccount()));
		} else {
			log.error("Unhandled event: " + event);
		}
	}
	



}
