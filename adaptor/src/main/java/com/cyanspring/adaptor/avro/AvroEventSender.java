package com.cyanspring.adaptor.avro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.avro.AvroSerializableObject;
import com.cyanspring.avro.generate.trade.bean.AmendOrderRequest;
import com.cyanspring.avro.generate.trade.bean.CancelOrderRequest;
import com.cyanspring.avro.generate.trade.bean.NewOrderRequest;
import com.cyanspring.avro.wrap.WrapObjectType;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.transport.IObjectTransportService;
import com.cyanspring.common.transport.ISerialization;

public class AvroEventSender implements IDownStreamEventSender {
	private static final Logger log = LoggerFactory.getLogger(AvroEventSender.class);
	private IObjectTransportService transportService;
	private IObjectListener listener;
	private ISerialization serializator;
	private String channel;
	@Autowired
	private SystemInfo systemInfo;
	
	@Override
	public void init() throws Exception {
		if (listener == null)
			throw new Exception("Listener not set");
		if (transportService == null)
			throw new Exception("TransportService not set");
		if (serializator == null)
			throw new Exception("Serializator not set");
		channel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + systemInfo.getDownStream();
		transportService.createSubscriber(channel, this);
	}

	@Override
	public void uninit() throws Exception {
		transportService.removeSubscriber(channel, this);
		listener = null;
		serializator = null;
		transportService = null;
	}

	@Override
	public void sendRemoteEvent(Object o) {
		byte[] bytes;
		try {
			if (o instanceof NewOrderRequest)
				bytes = (byte[]) serializator.serialize(new AvroSerializableObject((NewOrderRequest)o, WrapObjectType.NewOrderRequest));
			else if (o instanceof AmendOrderRequest)
				bytes = (byte[]) serializator.serialize(new AvroSerializableObject((AmendOrderRequest)o, WrapObjectType.AmendOrderRequest));				
			else if (o instanceof CancelOrderRequest)
				bytes = (byte[]) serializator.serialize(new AvroSerializableObject((CancelOrderRequest)o, WrapObjectType.CancelOrderRequest));
			else
				throw new Exception("Unhandle remote event");
			transportService.sendMessage(channel, bytes);
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
			AvroSerializableObject deObj = (AvroSerializableObject) serializator.deSerialize(obj);			
			listener.onMessage(deObj);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
