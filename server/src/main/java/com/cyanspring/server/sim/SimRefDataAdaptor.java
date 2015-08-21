package com.cyanspring.server.sim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.RefData;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SimRefDataAdaptor implements IRefDataAdaptor {

	private String filePath = "./conf/sim/codetable_f2.xml";
	private XStream xstream;
	private Map<String, CodeTableData> map;
	private List<IRefDataListener> listeners;

	@Override
	public boolean getStatus() {
		return true;
	}

	@Override
	public void flush() {

	}

	@Override
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		xstream = new XStream(new DomDriver("UTF-8"));
		File file = new File(filePath);
		map = (Map<String, CodeTableData>) xstream.fromXML(file);
		
		listeners = new ArrayList<>();
	}

	@Override
	public void uninit() {
		xstream = null;
		listeners = null;
		map = null;
	}

	@Override
	public void subscribeRefData(IRefDataListener listener) throws Exception {
		if (!listeners.contains(listener))
			listeners.add(listener);
		List<RefData> refDataList = new ArrayList<>();
		for (Entry<String, CodeTableData> e : map.entrySet()) {
			CodeTableData data = e.getValue();
			RefData refData = new RefData();
			refData.setRefSymbol(data.getWindCode());
			refData.setCNDisplayName(data.getCnName());
			refData.setExchange(data.getSecurityExchange());
			refData.setCode(data.getWindCode());
			refData.setIType(String.valueOf(data.getSecurityType()));
			refDataList.add(refData);
		}
		listener.onRefData(refDataList);
	}

	@Override
	public void unsubscribeRefData(IRefDataListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
