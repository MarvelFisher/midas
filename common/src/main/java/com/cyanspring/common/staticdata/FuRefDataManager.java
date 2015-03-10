package com.cyanspring.common.staticdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
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
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.fu.RefDataStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class FuRefDataManager implements IPlugin, IRefDataManager{
	private static final Logger log = LoggerFactory
			.getLogger(FuRefDataManager.class);
	String refDataFile;	
	Map<String, RefData> refSymbolMap;
	Map<String, RefData> symbolMap;
	private String market = Default.getMarket();
	private XStream xstream = new XStream(new DomDriver());
	private Map<String,RefDataStrategy> strategyMap = new HashMap<>();
	private MarketSessionUtil marketSessionUtil;

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		log.info("initialising with " + refDataFile);
		refSymbolMap = new HashMap<String, RefData>();
		symbolMap = new HashMap<String, RefData>();
		File file = new File(refDataFile);
		List<RefData> list;
		if (file.exists()) {
			list = (List<RefData>)xstream.fromXML(file);
		} else {
			throw new Exception("Missing refdata file: " + refDataFile);
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(Clock.getInstance().now());
		RefDataStrategy strategy;
		for(RefData refData: list) {
			if(!strategyMap.containsKey(refData.getStrategy())){
				Class<RefDataStrategy> tempClz = (Class<RefDataStrategy>)Class.forName("com.cyanspring.common.staticdata.fu." + refData.getStrategy() + "Strategy");
				Constructor<RefDataStrategy> ctor = tempClz.getConstructor(MarketSessionUtil.class);
				strategy = ctor.newInstance(marketSessionUtil);
				strategyMap.put(refData.getStrategy(), strategy);
			}else{
				strategy = strategyMap.get(refData.getStrategy());
			}
			strategy.init(cal);
			strategy.setExchangeRefData(refData);
			refSymbolMap.put(refData.getRefSymbol(), refData); //key = Ref Symbol
			symbolMap.put(refData.getSymbol(), refData); //key = Symbol
		}		
		saveRefDataToFile(refDataFile, new ArrayList<RefData>(refSymbolMap.values()));
	}	

	@Override
	public void uninit() {
		log.info("uninitialising");
		refSymbolMap.clear();
	}
	
	@Override
	public RefData getRefData(String symbol) {
		return symbolMap.get(symbol);
	}

	@Override
	public String getRefDataFile() {
		return refDataFile;
	}

	@Override
	public List<RefData> getRefDataList() {
		return new ArrayList<RefData>(refSymbolMap.values());
	}

	@Override
	public String getMarket() {
		return market;
	}

	@Override
	public void setRefDataFile(String refDataFile) {
		this.refDataFile = refDataFile;
	}
	
	private void saveRefDataToFile(String path, List<RefData> list){
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
	
	public void setMarketSessionUtil(MarketSessionUtil marketSessionUtil) {
		this.marketSessionUtil = marketSessionUtil;
	}
	
	@Override
	public RefData getRefDataBySymbol(String symbol){
		return symbolMap.get(symbol);
	}

	@Override
	public RefData getRefDataByRefSymbol(String refSymbol) {
		return refSymbolMap.get(refSymbol);
	}
}