package com.cyanspring.adaptor.future.ctp.trader;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;

public class CtpTradeConnection implements IDownStreamConnection {
	private static final Logger log = LoggerFactory
			.getLogger(CtpTradeConnection.class);

	private String url;
	private String conLog;
	private String id;
	private boolean state; 
	private IDownStreamListener listener;
	
	public CtpTradeConnection(String id, String url, String conLog) {
		this.id = id;
		this.url = url;
		this.conLog = conLog;
	}
	
	@Override
	public void init() throws Exception {
		// TODO does login and initialization

	}
	

	@Override
	public void uninit() {
		// TODO clean up

	}

	@Override
	public String getId() {
		return id;
	}

//	private void onOrder() {
//		listener.onOrder(execType, order, execution, message);
//	}
	
	@Override
	public boolean getState() {
		// TODO Auto-generated method stub
		return false;
	}

	class DownStreamSender implements IDownStreamSender {
		@Override
		public void newOrder(ChildOrder order) throws DownStreamException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancelOrder(ChildOrder order) throws DownStreamException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean getState() {
			return CtpTradeConnection.this.getState();
		}
		
	}
	
	@Override
	public IDownStreamSender setListener(IDownStreamListener listener)
			throws DownStreamException {
		
		if(listener != null && this.listener != null)
			throw new DownStreamException("Support only one listener");
		
		this.listener = listener;
		return new DownStreamSender();
	}


}
