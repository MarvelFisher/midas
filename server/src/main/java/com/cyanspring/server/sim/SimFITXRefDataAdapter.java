package com.cyanspring.server.sim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.CodeTableData;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataCommodity;
import com.cyanspring.common.staticdata.fu.IType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SimFITXRefDataAdapter implements IRefDataAdaptor {

	private static final Logger log = LoggerFactory
			.getLogger(SimFITXRefDataAdapter.class);

	private String filePath = "./conf/sim/codetable_ft.xml";
	private XStream xstream;
	private Map<String, CodeTableData> map;
	private List<IRefDataListener> listeners;
	private Boolean status = false;
	private Timer timer;

	@Override
	public boolean getStatus() {
		return status;
	}

	@Override
	public void flush() {

	}

	@Override
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		log.info("Initializing");
		xstream = new XStream(new DomDriver("UTF-8"));
		listeners = new ArrayList<>();
		File file = new File(filePath);
		map = (Map<String, CodeTableData>) xstream.fromXML(file);

		log.info("Setting raw refData...");
		List<RefData> refDataList = new ArrayList<>();
		for (Entry<String, CodeTableData> e : map.entrySet()) {
			CodeTableData codeTableData = e.getValue();
			if (codeTableData.getSecurityType() == 1) {
				continue;
			}
			RefData refData = new RefData();
			String extractYYMMStr = codeTableData.getWindCode().replaceAll("\\D+", "").substring(2);
            refData.setSymbol(codeTableData.getShowID());
            refData.setDesc(codeTableData.getGroup());
            refData.setCurrency(codeTableData.getCurrency());
            refData.setENDisplayName(codeTableData.getProduct() + extractYYMMStr);
            refData.setTWDisplayName(codeTableData.getProductName() + extractYYMMStr);
            refData.setCNDisplayName(codeTableData.getProductName() + extractYYMMStr);
            refData.setRefSymbol(refData.getSymbol() + "." + codeTableData.getSecurityExchange());
            refData.setCategory(codeTableData.getProduct());
            refData.setSpotTWName(codeTableData.getProductName());
            refData.setCode(codeTableData.getWindCode());
            refData.setExchange(codeTableData.getSecurityExchange());
            refData.setIType(IType.FUTURES_FT.getValue());
            refData.setCommodity(RefDataCommodity.FUTURES.getValue());
			refDataList.add(refData);
		}

		timer = new Timer("SimFITXRefDataAdapter");
		RefDataUpdateTask updateTask = new RefDataUpdateTask(listeners,
				refDataList, this);
		timer.schedule(updateTask, 5000);
	}

	@Override
	public void uninit() {
		xstream = null;
		listeners = null;
		map = null;
	}

	@Override
	public void subscribeRefData(IRefDataListener listener) throws Exception {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void unsubscribeRefData(IRefDataListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void setStatus(boolean status) {
		this.status = status;
	}

}
