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
package com.cyanspring.common.downstream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.server.event.DownStreamReadyEvent;
import com.cyanspring.common.stream.IStreamAdaptor;
import com.cyanspring.common.type.ExecType;

public class DownStreamManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(DownStreamManager.class);
	@Autowired 
	private IAsyncEventManager eventManager;
	
	private List<IStreamAdaptor<IDownStreamConnection>> adaptors;
	private Map<String, IDownStreamSender> senders = Collections.synchronizedMap(new HashMap<String, IDownStreamSender>());
	// these are special connection that will not be in the load balancing pool
	private List<IStreamAdaptor<IDownStreamConnection>> specialAdaptors;
	private Map<String, IDownStreamSender> specialSenders = Collections.synchronizedMap(new HashMap<String, IDownStreamSender>());
	
	
	public DownStreamManager(List<IStreamAdaptor<IDownStreamConnection>> adaptors, 
			List<IStreamAdaptor<IDownStreamConnection>> specialAdaptors) {
		this.adaptors = adaptors;
		this.specialAdaptors = specialAdaptors;
	}
	
	@Override
	public void init() throws DownStreamException {
		log.info("initialising");
		for(IStreamAdaptor<IDownStreamConnection> adaptor: adaptors) {
			try {
				adaptor.init();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new DownStreamException(e.getMessage());
			}
			for(IDownStreamConnection connection: adaptor.getConnections()) {
				if(senders.containsKey(connection.getId()))
					throw new DownStreamException("This connection id already exists: " + connection.getId(),ErrorMessage.DOWN_STREAM_CONN_ID_EXIST);
				IDownStreamSender sender = connection.setListener(new DownStreamListener(connection));
				senders.put(connection.getId(), sender);
			}
		}
		for(IStreamAdaptor<IDownStreamConnection> adaptor: specialAdaptors) {
			try {
				adaptor.init();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new DownStreamException(e.getMessage(),ErrorMessage.DOWN_STREAM_EXCEPTION);
			}
			for(IDownStreamConnection connection: adaptor.getConnections()) {
				if(senders.containsKey(connection.getId()))
					throw new DownStreamException("This connection id already exists: " + connection.getId(),ErrorMessage.DOWN_STREAM_CONN_ID_EXIST);
				IDownStreamSender sender = connection.setListener(new DownStreamListener(connection));
				specialSenders.put(connection.getId(), sender);
			}
		}
		if(allReady()) {
			eventManager.sendEvent(new DownStreamReadyEvent(null, true));
		}

	}

	@Override
	public void uninit() {
		log.info("uninitialising");
		for(IStreamAdaptor<IDownStreamConnection> adaptor: adaptors) {
			for(IDownStreamConnection connection: adaptor.getConnections()) {
				try {
					connection.setListener(null);
				} catch (DownStreamException e) {
					log.error(e.getMessage(), e);
				}
			}
			adaptor.uninit();
		}
		for (IStreamAdaptor<IDownStreamConnection> adaptor : specialAdaptors) {
			for (IDownStreamConnection connection : adaptor.getConnections()) {
				try {
					connection.setListener(null);
				} catch (DownStreamException e) {
					log.error(e.getMessage(), e);
				}
			}
			adaptor.uninit();
		}
		eventManager.sendEvent(new DownStreamReadyEvent(null, false));
	}

	private boolean allReady() {
		for(IStreamAdaptor<IDownStreamConnection> adaptor: adaptors) {
			for(IDownStreamConnection connection: adaptor.getConnections()) {
				if(!connection.getState()) {
					log.debug("Connection is still down: " + connection.getId());
					return false;
				}
			}
		}
		return true;
	}
	
	class DownStreamListener implements IDownStreamListener {
		IDownStreamConnection connection;

		public DownStreamListener(IDownStreamConnection connection) {
			super();
			this.connection = connection;
		}

		@Override
		public void onState(boolean on) {
			if (!on) {
				log.warn("Down Stream connection is down: " + connection.getId(),ErrorMessage.DOWN_STREAM_CONN_DOWN);
			} else {
				if(allReady()) {
					eventManager.sendEvent(new DownStreamReadyEvent(null, true));
				}
			}
		}

		@Override
		public void onOrder(ExecType execType, ChildOrder order,
				Execution execution, String message) {
			if(order != null) {
				order = order.clone();
				order.touch();
			}
			UpdateChildOrderEvent event = 
				new UpdateChildOrderEvent(order.getStrategyId(), execType, 
						order, execution==null?null:execution.clone(), message);
			eventManager.sendEvent(event);
		}

		@Override
		public void onError(String orderId, String message) {
			log.error(orderId + ": " + message);
		}
		
	}
	// this method will pull one connection from the load balance pool
	public IDownStreamSender getSender() throws DownStreamException {
		ArrayList<IDownStreamSender> list = new ArrayList<IDownStreamSender>();
		if(senders.size() == 0)
			throw new DownStreamException("There are no DownStreamSender available",ErrorMessage.DOWN_STREAM_SENDER_NOT_AVAILABLE);
		
		for(Entry<String, IDownStreamSender> entry: senders.entrySet()) {
			list.add(entry.getValue());
		}
		if(list.size() == 0)
			throw new DownStreamException("There are no DownStreamSender available yet",ErrorMessage.DOWN_STREAM_SENDER_NOT_AVAILABLE);
		int i = new Random().nextInt(list.size());
		return list.get(i);
	}
	
	public IDownStreamSender getSender(String dest) throws DownStreamException {
		if(null == dest)
			return this.getSender();
		
		IDownStreamSender sender = senders.get(dest);
		if(null == sender)
			sender = specialSenders.get(dest);
		
		if(null == sender)
			throw new DownStreamException("Can't find this sender: " + dest,ErrorMessage.DOWN_STREAM_SENDER_NOT_AVAILABLE);
		return sender;
	}
	
}
