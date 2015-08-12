package com.cyanspring.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.transport.IClientSocketListener;
import com.cyanspring.common.transport.IClientSocketService;

public class ClientSocketEventManager extends AsyncEventManager implements
		IRemoteEventManager {
	private static final Logger log = LoggerFactory
			.getLogger(ClientSocketEventManager.class);
	
	private IClientSocketService socketService;
	
	private IClientSocketListener listener = new IClientSocketListener() {
		public void onMessage(Object obj) {
			if (obj instanceof RemoteAsyncEvent) {
				RemoteAsyncEvent event = (RemoteAsyncEvent)obj;
				ClientSocketEventManager.super.sendEvent(event);
			}
			
		}

		@Override
		public void onConnected(boolean connected) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public ClientSocketEventManager() {
		
	}

	public ClientSocketEventManager(IClientSocketService socketService) {
		this.socketService = socketService;
	}
	
	@Override
	public void init(String channel, String inbox)throws Exception  {
		socketService.addListener(listener);
		socketService.init();
	}
	
	@Override
	public void addEventInbox(String queue) throws Exception {
	}
	
	@Override
	public void addEventChannel(String channel) throws Exception {
	}
	
	@Override
	public void publishRemoteEvent(String channel, RemoteAsyncEvent event) throws Exception {
		// to be implemented
	}
	
	@Override
	public void uninit() {
		try {
			close();
		} catch (Exception e) {
		}
	}
	
//	@Override
//	public void finalize() throws Throwable {
//		close();
//		super.finalize();
//	}
	
	@Override
	public void close() throws Exception {
		log.debug("Closing transport...");
		clearAllSubscriptions();
		socketService.uninit();
	}
	
	// depends on whether receiver is set to send local event or remote event
	@Override
	public void sendLocalOrRemoteEvent(RemoteAsyncEvent event) throws Exception {
		if(event.getReceiver() == null)
			sendEvent(event);
		else
			sendRemoteEvent(event);
	}
	
	// depends on whether receiver is set to send broadcast event or PtoP event
	@Override
	public void sendRemoteEvent(RemoteAsyncEvent event) throws Exception {
		if(!socketService.sendMessage(event))
			throw new Exception("Channel not yet ready to send");
	}
	
	@Override
	public void sendGlobalEvent(RemoteAsyncEvent event) throws Exception {
		super.sendEvent(event);
		sendRemoteEvent(event);
	}

	public boolean isEmbedBroker() {
		return false;
	}

	public void setEmbedBroker(boolean embedBroker) {
	}

	public IClientSocketService getSocketService() {
		return socketService;
	}

	public void setSocketService(IClientSocketService socketService) {
		this.socketService = socketService;
	}

}
