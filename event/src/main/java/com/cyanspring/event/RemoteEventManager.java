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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.IAsyncEventBridge;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.transport.IObjectTransportService;

public class RemoteEventManager extends AsyncEventManager implements IRemoteEventManager {
	private static final Logger log = LoggerFactory
			.getLogger(RemoteEventManager.class);
	private IObjectTransportService transport;
	private boolean embedBroker;
	private String channel;
	private String inbox;
	private ConcurrentHashMap<String, IAsyncEventBridge> bridgeMap = new ConcurrentHashMap<String, IAsyncEventBridge>();
	private List<IAsyncEventBridge> bridges;

	class RemoteListener implements IObjectListener {
		public void onMessage(Object obj) {
			if (obj instanceof RemoteAsyncEvent) {
				RemoteAsyncEvent event = (RemoteAsyncEvent)obj;
				if (inbox.equals(event.getSender())) {
					return;
				}
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
		if(null != bridges) {
			for(IAsyncEventBridge bridge: bridges) {
				IAsyncEventBridge existing = bridgeMap.put(bridge.getBridgeId(), bridge);
				if(null != existing) {
					throw new Exception("Duplicate IAsyncEventBridge id: " + bridge.getBridgeId());
				}
			}
		}

		if(null == transport) {
			throw new Exception("Transport isn't instantiated");
		}
		this.channel = channel;
		this.inbox = inbox;

		if (embedBroker) {
			transport.startBroker();
		}
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
	public void uninit() {
		boolean interrupted = false;

		try {
			close();
		} catch (InterruptedException ie) {
			log.error(ie.getMessage(), ie);
			interrupted = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (interrupted) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
			}
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
		if (embedBroker) {
			transport.closeBroker();
		}
	}

	private void sendToAllBridges(RemoteAsyncEvent event) {
		for(IAsyncEventBridge bridge: bridgeMap.values()) {
			bridge.onBridgeEvent(event);
		}
	}

	// depends on whether receiver is set to send local event or remote event
	@Override
	public void sendLocalOrRemoteEvent(RemoteAsyncEvent event) throws Exception {
		if(event.getReceiver() == null) {
			sendEvent(event);
		} else {
			sendRemoteEvent(event);
		}
	}

	// depends on whether receiver is set to send broadcast event or PtoP event
	@Override
	public void sendRemoteEvent(RemoteAsyncEvent event) throws Exception {
		if(event.getSender() == null) {
			event.setSender(inbox);
		}

		if(event.getReceiver() == null) {
			sendToAllBridges(event);
			transport.publishMessage(channel, event);
		} else {
			IAsyncEventBridge bridge = bridgeMap.get(event.getReceiver());
			if (bridge != null) {
				bridge.onBridgeEvent(event);
			} else {
				transport.sendMessage(event.getReceiver(), event);
			}
		}
	}

	@Override
	public void publishRemoteEvent(String channel, RemoteAsyncEvent event) throws Exception {
		if(event.getSender() == null) {
			event.setSender(inbox);
		}

		sendToAllBridges(event);
		transport.publishMessage(channel, event);
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

	public List<IAsyncEventBridge> getBridges() {
		return bridges;
	}

	public void setBridges(List<IAsyncEventBridge> bridges) {
		this.bridges = bridges;
	}


}
