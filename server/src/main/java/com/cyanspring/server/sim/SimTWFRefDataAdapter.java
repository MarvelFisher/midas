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
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataCommodity;
import com.cyanspring.common.staticdata.fu.IType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SimTWFRefDataAdapter extends SimRefDataAdaptor {

	private static final Logger log = LoggerFactory
			.getLogger(SimTWFRefDataAdapter.class);

	@Override
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		filePath = "./conf/sim/codetable_ltft.xml";
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

		timer = new Timer("SimTWFRefDataAdapter");
		RefDataUpdateTask updateTask = new RefDataUpdateTask(listeners,
				refDataList, this);
		timer.schedule(updateTask, 5000);
	}

}
