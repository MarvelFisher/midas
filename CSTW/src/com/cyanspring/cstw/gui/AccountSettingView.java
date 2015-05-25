package com.cyanspring.cstw.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountSettingType;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.type.KeyValue;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.gui.common.PropertyTableViewer;

public class AccountSettingView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(PropertyView.class);
	public static final String ID = "com.cyanspring.cstw.gui.AccountSettingViewer";
	private PropertyTableViewer viewer;
	private Action editAction;
	private boolean editMode;
	private ImageRegistry imageRegistry;
	private String objectId;
	@SuppressWarnings("rawtypes")
	private Class clazz;
	private List<String> editableFields;
	private Composite composite= null;
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		composite=parent;
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
		editAction.setToolTipText("Edit account settings");
		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.EDIT_ICON.toString());
		editAction.setImageDescriptor(imageDesc);
		
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(editAction);

		// subscribe to business event
		Business.getInstance().getEventManager().subscribe(AccountSettingSnapshotReplyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(AccountSelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ChangeAccountSettingReplyEvent.class, this);
		
	}
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void confirmChange() {
		List<KeyValue> changedFields = viewer.workoutChangedFields();
		HashMap<String, Object> oldFields = viewer.getSavedInput();
		String id = (String)oldFields.get(AccountSettingType.ID.value());
		StringBuilder sb = new StringBuilder("Please confirm the changes: \n");
		for (KeyValue pair : changedFields) {
			String strOld = "[old]";
			String strNew = "[new]";
			try {
				strOld = BeanHolder.getInstance().getDataConverter().toString(pair.key, oldFields.get(pair.key));
				strNew = BeanHolder.getInstance().getDataConverter().toString(pair.key, pair.value);
				if(isNumberType(strOld) && !isNumberType(strNew)){
					MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ERROR);
					messageBox.setText("Error Type");
					messageBox.setMessage(pair.key+" type error : "+strOld+" -> "+strNew);
					messageBox.open();
					return;
				}
			} catch (DataConvertException e) {
				log.error(e.getMessage(), e);
			}
			sb.append(pair.key + ": [" + strOld + " -> " + strNew +"]\n");
		}
		try {
			if (changedFields.size() > 0 && 
					MessageDialog.openConfirm(viewer.getControl().getShell(), "Are you sure?", sb.toString())) {
				AccountSetting changes = new AccountSetting(id);
				for(KeyValue pair : changedFields){
					Object oldValue = oldFields.get(pair.key);
						Object newValue = BeanHolder.getInstance().getDataConverter().fromString(oldValue==null?String.class:oldValue.getClass(), pair.key, pair.value.toString());
						changes.put(pair.key, newValue);
				}
				changes.put(AccountSettingType.ID.value(), id);
				sendRemoteEvent(new ChangeAccountSettingRequestEvent(ID, Business.getInstance().getFirstServer(), changes));
				sendRemoteEvent(new AccountSettingSnapshotRequestEvent(ID, Business.getInstance().getFirstServer(), id, null));
			}
		} catch (DataConvertException e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	private boolean isNumberType(String strOld) {
		try{
			Double.parseDouble(strOld);
		}catch(Exception e){
			return false;
		}
		return true;
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
			viewer.turnOnEditMode();
		} else {
			this.editMode = false;
			editAction.setText("Edit");
			editAction.setToolTipText("Edit order parameters");
			ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.EDIT_ICON.toString());
			editAction.setImageDescriptor(imageDesc);
			viewer.turnOffEditMode();
		}
	}

	public AccountSettingView() {
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
		if(event instanceof AccountSelectionEvent) {
			AccountSelectionEvent evt = (AccountSelectionEvent)event;
			AccountSettingSnapshotRequestEvent request = new AccountSettingSnapshotRequestEvent(ID, Business.getInstance().getFirstServer(), evt.getAccount(), null);
			sendRemoteEvent(request);
		} else if(event instanceof AccountSettingSnapshotReplyEvent) {
			displayObject(((AccountSettingSnapshotReplyEvent) event).getAccountSetting().getFields());
		} else if(event instanceof ChangeAccountSettingReplyEvent) {
			ChangeAccountSettingReplyEvent evt = (ChangeAccountSettingReplyEvent)event;
			log.info("Account setting change: " + evt.isOk() + ", " + evt.getMessage());
		} else {
			log.error("Unhandled event: " + event);
		}
	}
	
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
