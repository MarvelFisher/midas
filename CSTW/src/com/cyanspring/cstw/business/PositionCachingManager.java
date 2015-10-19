/**
 * 
 */
package com.cyanspring.cstw.business;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;

/**
 * @author junfeng
 *
 */
public class PositionCachingManager implements IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(PositionCachingManager.class);
	
	IRemoteEventManager eventManager;
	
	
	public PositionCachingManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof OrderCacheReadyEvent){
			
		}

	}
	
	public void init() {
		Business.getInstance().getEventManager()
				.subscribe(AccountSelectionEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(AccountSnapshotReplyEvent.class, this);
		if (Business.getInstance().getOrderManager().isReady()) {
			sendRequestEvent();
		} else {
			Business.getInstance().getEventManager()
					.subscribe(OrderCacheReadyEvent.class, this);
		}		
		
	}
	
	private void sendRequestEvent() {
		if (UserRole.RiskManager == Business.getInstance().getUserGroup().getRole()){
			Set<UserGroup> users = Business.getInstance().getUserGroup().getManageeSet();
			for(UserGroup user : users){
				sendSubscriptionRequest(user.getUser());
			}
		}
		else if (UserRole.BackEndRiskManager == Business.getInstance().getUserGroup().getRole()) {
			
		}
		else if (UserRole.Trader == Business.getInstance().getUserGroup().getRole()) {
			Account account = Business.getInstance().getLoginAccount();
			if (null != account) {
				sendSubscriptionRequest(account.getId());
			}
		}
		
	}

	private void sendSubscriptionRequest(String userId){
		
	}
	
	public void unInit() {
		
	}

}
