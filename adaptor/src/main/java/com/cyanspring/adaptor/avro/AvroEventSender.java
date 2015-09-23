package com.cyanspring.adaptor.avro;

import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.avro.AvroSerializableObject;
import com.cyanspring.avro.wrap.WrapObjectType;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.transport.IObjectSender;
import com.cyanspring.common.transport.IObjectTransportService;

public class AvroEventSender implements IDownStreamEventSender {
	private static final Logger log = LoggerFactory.getLogger(AvroEventSender.class);
	private IObjectTransportService transportService;
	private IObjectListener listener;
	private String channel;
	private String node;
	private IObjectSender publisher;
	
	@Autowired
	private SystemInfo systemInfo;
	
	public AvroEventSender(IObjectTransportService transportService) {
		this.transportService = transportService;
	}
	
	@Override
	public void init() throws Exception {
		if (listener == null)
			throw new Exception("Listener not set");
		if (transportService == null)
			throw new Exception("TransportService not set");
		if (channel == null)
			channel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + systemInfo.getDownStream() + ".channel";
		transportService.createPublisher(channel);
		publisher = transportService.createObjectPublisher(channel);
		if (node == null)
			node = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + systemInfo.getDownStream() + ".node";
		transportService.createSubscriber(node, this);
	}

	@Override
	public void uninit() throws Exception {
		transportService.removeSubscriber(channel, this);
		listener = null;
		transportService = null;
	}

	@Override
	public void sendRemoteEvent(Object o, WrapObjectType type) {
		try {
			publisher.sendMessage(new AvroSerializableObject((SpecificRecord) o, type));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
	}

	@Override
	public void setEventListener(IObjectListener listener) {
		this.listener = listener;
	}

	@Override
	public void onMessage(Object obj) {
		try {
			listener.onMessage(obj);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
}
