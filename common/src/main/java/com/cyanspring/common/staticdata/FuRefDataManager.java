package com.cyanspring.common.staticdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.staticdata.fu.IFStrategy;
import com.cyanspring.common.staticdata.fu.RefDataStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class FuRefDataManager implements IPlugin, IRefDataManager{
	private static final Logger log = LoggerFactory
			.getLogger(FuRefDataManager.class);
	String refDataFile;	
	Map<String, RefData> map = new HashMap<String, RefData>();
	private String market = Default.getMarket();
	private XStream xstream = new XStream(new DomDriver());
	private Map<String,RefDataStrategy> stragetyMap = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		log.info("initialising with " + refDataFile);
		File file = new File(refDataFile);
		List<RefData> list;
		if (file.exists()) {
			list = (List<RefData>)xstream.fromXML(file);
		} else {
			throw new Exception("Missing refdata file: " + refDataFile);
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(Clock.getInstance().now());
		RefDataStrategy stragety;
		for(RefData refData: list) {
			if(!stragetyMap.containsKey(refData.getStrategy())){
				stragety = (RefDataStrategy) Class.forName("com.cyanspring.common.staticdata.fu." + refData.getStrategy() + "Strategy").newInstance();
				stragetyMap.put(refData.getStrategy(), stragety);
			}else{
				stragety = stragetyMap.get(refData.getStrategy());
			}
			stragety.init(cal);
			stragety.setExchangeRefData(refData);
			//map.put(refData.getSymbol(), refData);
			map.put(refData.getRefSymbol(), refData); //key = refsymbol
		}		
		saveRefDataToFile(refDataFile, new ArrayList<RefData>(map.values()));
	}

	@Override
	public void uninit() {
		log.info("uninitialising");
		map.clear();
	}
	
	@Override
	public RefData getRefData(String symbol) {
		return map.get(symbol);
	}

	@Override
	public String getRefDataFile() {
		return refDataFile;
	}

	@Override
	public List<RefData> getRefDataList() {
		return new ArrayList<RefData>(map.values());
	}

	@Override
	public String getMarket() {
		return market;
	}

	@Override
	public void setRefDataFile(String refDataFile) {
		this.refDataFile = refDataFile;
	}
	
	private void saveRefDataToFile(String path, List list){
		File file = new File(path);
		try {
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(list, os);
			os.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}	
	}
}