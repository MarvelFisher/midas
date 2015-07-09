package com.cyanspring.common.staticdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.Default;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.fu.IRefDataStrategy;
import com.cyanspring.common.staticdata.fu.MappingData;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataFactory extends RefDataService{
    
    List<RefData> refDataList;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private String market = Default.getMarket();
	private XStream xstream = new XStream(new DomDriver("UTF-8"));
	private Map<String,IRefDataStrategy> strategyMap = new HashMap<>();
    private Map<String, List<MappingData>> strategyMapping;
	private MarketSessionUtil marketSessionUtil;
    private String strategyPack = "com.cyanspring.common.staticdata.fu";

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		log.info("initialising with " + refDataFile);
		File file = new File(refDataFile);
		if (file.exists()) {
            refDataList = (List<RefData>)xstream.fromXML(file);
		} else {
			throw new Exception("Missing refdata file: " + refDataFile);
		}
	}

    @Override
    public boolean update(String tradeDate) throws Exception {
        if (refDataList == null){
            log.warn(this.getClass().getSimpleName() + "is not initial or initialising.");
            return false;
        }
        log.info("Updating refData....");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(tradeDate));
        IRefDataStrategy strategy;
        for(RefData refData: refDataList) {
            if(!strategyMap.containsKey(refData.getStrategy())){
                try {
                    Class<IRefDataStrategy> tempClz = (Class<IRefDataStrategy>)Class.forName( strategyPack + "." + refData.getStrategy() + "Strategy");
                    Constructor<IRefDataStrategy> ctor = tempClz.getConstructor();
                    strategy = ctor.newInstance();
                    List<MappingData> list = null;
                    if (strategyMapping != null)
                        list = strategyMapping.get(refData.getStrategy());

                    strategy.setRequireData(marketSessionUtil, list);
                } catch (Exception e) {
                    log.error("Can't find strategy: {}", refData.getStrategy());
                    strategy = new IRefDataStrategy() {
                        @Override
                        public void init(Calendar cal) {

                        }

                        @Override
                        public void updateRefData(RefData refData) {

                        }

                        @Override
                        public void setRequireData(Object... objects) {

                        }
                    };
                }
                strategyMap.put(refData.getStrategy(), strategy);
                updateMarginRate(refData);
            }else{
                strategy = strategyMap.get(refData.getStrategy());
            }
            strategy.init(cal);
            strategy.updateRefData(refData);
        }
        saveRefDataToFile(refDataFile, refDataList);
        return true;
    }

    @Override
	public void uninit() {
        log.info("uninitialising");
        strategyMap.clear();
	}
	
	@Override
	public RefData getRefData(String symbol) {
        for (RefData refData : refDataList){
            if (refData.getSymbol().equals(symbol))
                return refData;
        }
        return null;
	}

	@Override
	public List<RefData> getRefDataList() {
		return refDataList;
	}

	private void saveRefDataToFile(String path, List<RefData> list){
		File file = new File(path);
		try {
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
			xstream.toXML(list, writer);
			os.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}	
	}
	
	public void setMarketSessionUtil(MarketSessionUtil marketSessionUtil) {
		this.marketSessionUtil = marketSessionUtil;
	}

    public void setStrategyPack(String strategyPack) {
        this.strategyPack = strategyPack;
    }

    public void setStrategyMapping(Map<String, List<MappingData>> strategyMapping) {
        this.strategyMapping = strategyMapping;
    }
}
