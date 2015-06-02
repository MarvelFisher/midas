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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.fu.IRefDataStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataFactory implements IPlugin, IRefDataManager{
	private static final Logger log = LoggerFactory
			.getLogger(RefDataFactory.class);
	String refDataFile;	
    List<RefData> refDataList;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private String market = Default.getMarket();
	private XStream xstream = new XStream(new DomDriver("UTF-8"));
	private Map<String,IRefDataStrategy> strategyMap = new HashMap<>();
	private MarketSessionUtil marketSessionUtil;

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
                    Class<IRefDataStrategy> tempClz = (Class<IRefDataStrategy>)Class.forName("com.cyanspring.common.staticdata.fu." + refData.getStrategy() + "Strategy");
                    Constructor<IRefDataStrategy> ctor = tempClz.getConstructor(MarketSessionUtil.class);
                    strategy = ctor.newInstance(marketSessionUtil);
                } catch (RuntimeException e) {
                    log.warn("Can't find strategy: {}", refData.getStrategy());
                    strategy = new IRefDataStrategy() {
                        @Override
                        public void init(Calendar cal) {

                        }

                        @Override
                        public boolean update(Calendar tradeDate) {
                            return false;
                        }

                        @Override
                        public void setExchangeRefData(RefData refData) {

                        }
                    };
                }
                strategyMap.put(refData.getStrategy(), strategy);
            }else{
                strategy = strategyMap.get(refData.getStrategy());
            }
            strategy.init(cal);
            strategy.setExchangeRefData(refData);
        }
        saveRefDataToFile(refDataFile, refDataList);
        return true;
    }

    @Override
	public void uninit() {
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
	public String getRefDataFile() {
		return refDataFile;
	}

	@Override
	public List<RefData> getRefDataList() {
		return refDataList;
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
}
