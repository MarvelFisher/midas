package com.cyanspring.cstw.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.cstw.localevent.AccountSelectionLocalEvent;
import com.cyanspring.cstw.session.GuiSession;

public class TraderInfoListener implements IAsyncEventListener{
	private static final Logger log = LoggerFactory
			.getLogger(TraderInfoListener.class);
	public static final String ID = "com.cyanspring.cstw.business.TraderInfoListener"; //$NON-NLS-1$

	private AsyncTimerEvent refreshEvent = new AsyncTimerEvent();
	private long minRefreshInterval = 3000;
	private String account = null; 
	private AccountSetting accountSetting = null;

	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof AccountSettingSnapshotReplyEvent) {
			AccountSettingSnapshotReplyEvent e = (AccountSettingSnapshotReplyEvent) event;
			if(null != e.getAccountSetting()){
				accountSetting = e.getAccountSetting();
				GuiSession.getInstance().setAccountSetting(accountSetting);
			}
		} else if (event instanceof AccountSelectionLocalEvent) {

			AccountSelectionLocalEvent evt = (AccountSelectionLocalEvent) event;
			this.account = evt.getAccount();
		} else if(event instanceof AsyncEvent) {
			sendAccountSettingRequestEvent();
		}
	}

	private void sendAccountSettingRequestEvent(){
		if(null != account){
			AccountSettingSnapshotRequestEvent settingRequestEvent = new AccountSettingSnapshotRequestEvent(ID, Business.getInstance().getFirstServer(), account, null);
			sendRemoteEvent(settingRequestEvent);
		}
	}
	
	public void init(Account account){
		subEvent(AccountSettingSnapshotReplyEvent.class);
		subEvent(AccountSelectionLocalEvent.class);
		scheduleJob(refreshEvent,minRefreshInterval);
		if(null != account)
			this.account = account.getId();
	}
	
	public void unInit(){
		unSubEvent(AccountSettingSnapshotReplyEvent.class);
		unSubEvent(AccountSelectionLocalEvent.class);
		cancelScheduleJob(refreshEvent);
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz,this);
	}
	
	private void unSubEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().unsubscribe(clazz,this);		
	}
	
	private void scheduleJob(AsyncTimerEvent timerEvent, long maxRefreshInterval) {
		Business.getInstance()
				.getScheduleManager()
				.scheduleRepeatTimerEvent(maxRefreshInterval, TraderInfoListener.this,
						timerEvent);
	}

	private void cancelScheduleJob(AsyncTimerEvent timerEvent) {
		Business.getInstance().getScheduleManager()
				.cancelTimerEvent(timerEvent);
	}
	
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
