package com.cyanspring.adaptor.avro;

import java.util.List;

import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.stream.IStreamAdaptor;

public class AvroAdaptor implements IStreamAdaptor<IDownStreamConnection>{

	private List<IDownStreamConnection> cons;
	
	@Override
	public void init() throws Exception {
		for (IDownStreamConnection con : cons)
			con.init();
	}

	@Override
	public void uninit() {
		for (IDownStreamConnection con : cons)
			con.uninit();
	}

	@Override
	public List<IDownStreamConnection> getConnections() {
		return cons;
	}
	
	public void setConnections(List<IDownStreamConnection> cons) {
		this.cons = cons;
	}
}
