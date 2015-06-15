package com.cyanspring.cstw.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.statistic.AccountStatisticReplyEvent;
import com.cyanspring.common.event.statistic.AccountStatisticRequestEvent;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.common.PropertyTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;

public class AccountStatisticView extends ViewPart implements
		IAsyncEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(AccountStatisticView.class);
	public static final String ID = "com.cyanspring.cstw.gui.AccountStatisticViewer";
	private PropertyTableViewer viewer;
	private Action refreshAction;
	private boolean editMode;
	private ImageRegistry imageRegistry;
	private String objectId;
	@SuppressWarnings("rawtypes")
	private Class clazz;
	private List<String> editableFields;
	private Composite composite= null;

	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long maxRefreshInterval = 10000;
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof AsyncTimerEvent) {
			sendAccountStatisticRequest();
		}else if(event instanceof AccountStatisticReplyEvent){
			log.info("get reply");		
			displayObject(((AccountStatisticReplyEvent) event).getAccount());
		}else{
			log.error("Unhandle Event:{}",event.getClass().getSimpleName());
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		subEvent(AccountStatisticReplyEvent.class);
		composite = parent;
		imageRegistry = Activator.getDefault().getImageRegistry();
		viewer = new PropertyTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, BeanHolder.getInstance().getDataConverter());
		viewer.init();
		createRefreshAction(parent);
		
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
	public void setFocus() {
		
	}
	
	private void createRefreshAction(final Composite parent){
		
		refreshAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {

				if(!refreshAction.isChecked()) {
					cancelScheduleJob(timerEvent);
				} else { 
					scheduleJob(timerEvent,maxRefreshInterval);
				}
			}
		};
		
		refreshAction.setChecked(false);		
		refreshAction.setText("AutoRefresh");
		refreshAction.setToolTipText("AutoRefresh");	
		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.REFRESH_ICON.toString());	
		refreshAction.setImageDescriptor(imageDesc);
		
		getViewSite().getActionBars().getToolBarManager().add(refreshAction);
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz, this);		
	}
	private void sendAccountStatisticRequest(){
		log.info("sendAccountStatisticRequest");
		AccountStatisticRequestEvent evt = new AccountStatisticRequestEvent(ID, Business.getInstance().getFirstServer());
		sendRemoteEvent(evt);
	}
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void scheduleJob(AsyncTimerEvent timerEvent,
			long maxRefreshInterval) {
		Business.getInstance().getScheduleManager().scheduleRepeatTimerEvent(maxRefreshInterval,
				AccountStatisticView.this, timerEvent);
	}

	private void cancelScheduleJob(AsyncTimerEvent timerEvent) {
		Business.getInstance().getScheduleManager().cancelTimerEvent(timerEvent);		
	}
}
