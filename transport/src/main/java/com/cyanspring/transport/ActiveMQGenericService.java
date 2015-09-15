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
package com.cyanspring.transport;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.transport.IObjectSender;
import com.cyanspring.common.transport.IObjectTransportService;
import com.cyanspring.common.transport.ISerialization;
import com.cyanspring.transport.tools.XStreamSerialization;

/**
 * 
 * @author phoenix
 */
public class ActiveMQGenericService extends ActiveMQService implements
		IObjectTransportService {

	static Logger log = LoggerFactory.getLogger(ActiveMQGenericService.class);

	// default ISerialization instance
	private ISerialization defaultSerialization = new XStreamSerialization();

	// ISerialization Mapping,
	// Key = topic, Value = ISerialization instance
	private HashMap<String, ISerialization> serializationMap = new HashMap<String, ISerialization>();

	private HashMap<String, ArrayList<IObjectListener>> objSubscribers = new HashMap<String, ArrayList<IObjectListener>>();
	private HashMap<IObjectListener, MessageConsumer> objConsumers = new HashMap<IObjectListener, MessageConsumer>();

	public void setDefaultSerialization(ISerialization defaultSerialization) {
		this.defaultSerialization = defaultSerialization;
	}

	public void setSerializationMap(
			HashMap<String, ISerialization> serializationMap) {
		this.serializationMap = serializationMap;
	}

	class ObjectListenerAdaptor implements MessageListener {

		private IObjectListener listener;
		private ISerialization serialization;

		public ObjectListenerAdaptor(IObjectListener listener,
				ISerialization serialization) {
			this.listener = listener;
			this.serialization = serialization;
		}

		@Override
		public void onMessage(Message message) {
			try {
				if (message instanceof TextMessage) {
					String str = ((TextMessage) message).getText();
					log.debug("ActiveMQGenericService Received TextMessage: "
							+ str);
					Object obj = serialization.deSerialize(str);
					listener.onMessage(obj);
				} else if (message instanceof BytesMessage) {
					BytesMessage bms = (BytesMessage) message;
					int length = (int) bms.getBodyLength();
					if (length == 0) {
						log.warn("ActiveMQGenericService BytesMessage length is 0, message=["
								+ message.toString() + "]");
						return;
					}
					byte[] bytes = new byte[length];
					bms.readBytes(bytes);
					Object obj = serialization.deSerialize(bytes);
					if (obj == null) {
						return;
					}
					listener.onMessage(obj);
					// log.debug("ActiveMQGenericService Received BytesMessage: "
					// + new String(bytes));
				}
				// TODO 处理其他Message类型
				else {
					log.error("ActiveMQGenericService Message Type is Undefined : "
							+ message.toString());
				}
			} catch (JMSException e) {
				log.error("ActiveMQGenericService onMessage JMSException is : "
						+ e.getMessage(), e);
			} catch (Exception e) {
				log.error("ActiveMQGenericService onMessage Exception is : "
						+ e.getMessage(), e);
			}
		}
	}

	class ObjectSender implements IObjectSender {

		private MessageProducer producer;
		private ISerialization serialization;

		public ObjectSender(MessageProducer producer,
				ISerialization serialization) {
			this.producer = producer;
			this.serialization = serialization;
		}

		@Override
		public void sendMessage(Object obj) throws Exception {
			if (obj == null) {
				log.warn("object is null.");
				return;
			}
			Object sobj = serialization.serialize(obj);
			if (sobj instanceof byte[]) {
				byte[] bytes = (byte[]) sobj;
				if (bytes == null || bytes.length == 0) {
					log.warn("bytes is null or array is empty !");
					return;
				}
				BytesMessage message = session.createBytesMessage();
				message.writeBytes(bytes);
				producer.send(message);
				if (log.isDebugEnabled()) {
					log.debug("Sending BytesMessage length is : "
							+ bytes.length);
				}
			} else if (sobj instanceof String) {
				String str = (String) sobj;
				TextMessage txt = session.createTextMessage(str);
				producer.send(txt);
				if (log.isDebugEnabled()) {
					log.debug("Sending TextMessage body is : " + str);
				}
			} else {
				throw new IllegalArgumentException("obj is undefined type!");
			}
		}
	}

	private ISerialization getSerializationInstance(String topic) {
		if (topic == null) {
			return defaultSerialization;
		}
		if (serializationMap.containsKey(topic)) {
			return serializationMap.get(topic);
		} else {
			return defaultSerialization;
		}
	}

	@Override
	public void createReceiver(String subject, IObjectListener listener)
			throws Exception {
		// only one listener per subject allowed for point to point connection
		MessageConsumer consumer = receivers.get(subject);
		if (null == consumer) {
			Destination dest = session.createQueue(subject);
			consumer = session.createConsumer(dest);
			receivers.put(subject, consumer);
		}
		if (null == listener) {
			consumer.setMessageListener(null);
		} else {
			ISerialization serialization = getSerializationInstance(subject);
			consumer.setMessageListener(new ObjectListenerAdaptor(listener,
					serialization));
		}
	}

	@Override
	public void createSubscriber(String subject, IObjectListener listener)
			throws Exception {
		// many listeners per subject allowed for publish/subscribe
		ArrayList<IObjectListener> listeners = objSubscribers.get(subject);
		if (null == listeners) {
			listeners = new ArrayList<IObjectListener>();
			objSubscribers.put(subject, listeners);
		}
		if (!listeners.contains(listener)) {
			Destination dest = session.createTopic(subject);
			MessageConsumer consumer = session.createConsumer(dest);
			ISerialization serialization = getSerializationInstance(subject);
			consumer.setMessageListener(new ObjectListenerAdaptor(listener,
					serialization));
			listeners.add(listener);
			objConsumers.put(listener, consumer);
		}
	}

	@Override
	public void removeSubscriber(String subject, IObjectListener listener)
			throws Exception {
		ArrayList<IObjectListener> listeners = objSubscribers.get(subject);
		if (null == listeners) {
			return;
		}
		if (listeners.contains(listener)) {
			MessageConsumer consumer = objConsumers.get(listener);
			if (null == consumer) {
				return;
			}
			consumer.setMessageListener(null);
			objConsumers.remove(listener);
			listeners.remove(listener);
		}
	}

	@Override
	public void sendMessage(String subject, Object obj) throws Exception {
		createObjectSender(subject).sendMessage(obj);
	}

	@Override
	public void publishMessage(String subject, Object obj) throws Exception {
		createObjectPublisher(subject).sendMessage(obj);
	}

	@Override
	public IObjectSender createObjectSender(String subject) throws Exception {
		MessageProducer producer = senders.get(subject);
		if (null == producer) {
			Destination dest = session.createQueue(subject);
			producer = session.createProducer(dest);
			producer.setDeliveryMode(persistent);
			senders.put(subject, producer);
		}
		ISerialization serialization = getSerializationInstance(subject);
		return new ObjectSender(producer, serialization);
	}

	@Override
	public IObjectSender createObjectPublisher(String subject) throws Exception {
		MessageProducer producer = publishers.get(subject);
		if (null == producer) {
			Destination dest = session.createTopic(subject);
			producer = session.createProducer(dest);
			producer.setDeliveryMode(persistent);
			publishers.put(subject, producer);
		}
		ISerialization serialization = getSerializationInstance(subject);
		return new ObjectSender(producer, serialization);
	}

}
