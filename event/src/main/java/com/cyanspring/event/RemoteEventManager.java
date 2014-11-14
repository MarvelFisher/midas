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
package com.cyanspring.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.transport.IObjectTransportService;

public class RemoteEventManager extends AsyncEventManager implements IRemoteEventManager {
	private static final Logger log = LoggerFactory
			.getLogger(RemoteEventManager.class);
	IObjectTransportService transport;
	boolean embedBroker;
	String channel;
	String inbox;
	
	class RemoteListener implements IObjectListener {
		public void onMessage(Object obj) {
			if (obj instanceof RemoteAsyncEvent) {
				RemoteAsyncEvent event = (RemoteAsyncEvent)obj;
				if (inbox.equals(event.getSender())) // not interested in the event sent by self
					return;	
				RemoteEventManager.super.sendEvent(event);
			}
			
		}
	}
	
	protected RemoteEventManager() {
		
	}

	public RemoteEventManager(IObjectTransportService transport) {
		this.transport = transport;
	}
	
	@Override
	public void init(String channel, String inbox)throws Exception  {
		if(null == transport)
			throw new Exception("Transport isn't instantiated");
		this.channel = channel;
		this.inbox = inbox;
		
		if (embedBroker)
			transport.startBroker();
		transport.startService();

		transport.createReceiver(inbox, new RemoteListener());
	}
	
	@Override
	public void addEventInbox(String queue) throws Exception {
		transport.createReceiver(queue, new RemoteListener());
	}
	
	@Override
	public void addEventChannel(String channel) throws Exception {
		transport.createSubscriber(channel, new RemoteListener());
	}
	
	@Override
	public void publishRemoteEvent(String channel, RemoteAsyncEvent event) throws Exception {
		if(event.getSender() == null)
			event.setSender(inbox);
		transport.publishMessage(channel, event);
	}
	
	@Override
	public void uninit() {
		try {
			close();
		} catch (Exception e) {
		}
	}
	
	@Override
	public void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	@Override
	public void close() throws Exception {
		log.debug("Closing transport...");
		clearAllSubscriptions();
		transport.closeService();
		if (embedBroker)
			transport.closeBroker();
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
		if(event.getSender() == null)
			event.setSender(inbox);
		if(event.getReceiver() == null)
			transport.publishMessage(channel, event);
		else
			transport.sendMessage(event.getReceiver(), event);
	}
	
	@Override
	public void sendGlobalEvent(RemoteAsyncEvent event) throws Exception {
		super.sendEvent(event);
		sendRemoteEvent(event);
	}

	public boolean isEmbedBroker() {
		return embedBroker;
	}

	public void setEmbedBroker(boolean embedBroker) {
		this.embedBroker = embedBroker;
	}
	
}
