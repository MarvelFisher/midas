package com.cyanspring.common.staticdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.marketsession.TradeDateManager;
import com.cyanspring.common.staticdata.fu.AbstractRefDataStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataFactory extends RefDataService {
	
    protected static final Logger log = LoggerFactory.getLogger(RefDataFactory.class);
    List<RefData> refDataList;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private XStream xstream = new XStream(new DomDriver("UTF-8"));
    private Map<String, AbstractRefDataStrategy> strategyMap = new HashMap<>();
    private MarketSessionUtil marketSessionUtil;
    private String strategyPack = "com.cyanspring.common.staticdata.fu";
    private String refDataTemplatePath;
    private List<RefData> refDataTemplateList;
    private Map<String,RefData> refDataTemplateMap = new HashMap<String,RefData>();
    
    @SuppressWarnings("unchecked")
    @Override
    public void init() throws Exception {
    	
        log.info("initialising with " + refDataTemplatePath);     
        if(StringUtils.hasText(refDataTemplatePath)){
        	
            log.info("read refdata template:{}",refDataTemplatePath);
            File templateFile = new File(refDataTemplatePath);
            if (templateFile.exists()) {
            	refDataTemplateList = (List<RefData>) xstream.fromXML(templateFile);
            	if(null != refDataTemplateList && !refDataTemplateList.isEmpty()){
            		buildTemplateMap(refDataTemplateList);
            	}
            } else {
                throw new Exception("Missing refdata template: " + refDataTemplatePath);
            }
        }
        
        //init category
        if(null != refDataList && !refDataList.isEmpty()){
        	for(RefData refData : refDataList){	        		
        		refData.setCategory(getCategory(refData));
        	}
        }
        
    }

    private void buildTemplateMap(List<RefData> refDataTemplateList) {
		for(RefData ref : refDataTemplateList){
			String spotName = ref.getCategory();
			if(refDataTemplateMap.containsKey(spotName)){
				log.info("duplicate refData template :{}",spotName);
				continue;
			}else{
				log.info("build template category:{},strategy:{}",spotName,ref.getStrategy());
				refDataTemplateMap.put(spotName, ref);
			}
		}
	}

	@Override
    public boolean updateAll(String tradeDate) throws Exception {
        if (refDataList == null) {
            log.warn(this.getClass().getSimpleName() + "is not initial or initialising.");
            return false;
        }
        log.info("Updating refData....");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(tradeDate));
        for (RefData refData : refDataList) {
            updateRefData(cal, refData);
        }
    
        return true;
    }

    private RefData searchRefDataTemplate(RefData refData){

    	String spotName = getCategory(refData);
    	RefData templateRefData = null;
    	if(refDataTemplateMap.containsKey(spotName) && null != refDataTemplateMap.get(spotName)){
    		templateRefData = refDataTemplateMap.get(spotName);
    		return (RefData)templateRefData.clone();
    	}
    	
    	log.info("can't find template:{}",spotName);
    	return  null;
    }
    
	protected String getCategory(RefData refData){
		return RefDataUtil.getCategory(refData);
	}
    
	private void updateRefData(Calendar cal, RefData refData) {
		
		AbstractRefDataStrategy strategy;
        RefData template = searchRefDataTemplate(refData);
        if( null == template){
        	return;
        }else{
        	refData.setStrategy(template.getStrategy());
        }
		log.info("update refData:{}, strategy:{}",refData.getRefSymbol(),refData.getStrategy());

		if (!strategyMap.containsKey(refData.getStrategy())) {
		    try {
		        Class<AbstractRefDataStrategy> tempClz = (Class<AbstractRefDataStrategy>) Class.forName(strategyPack + "." + refData.getStrategy() + "Strategy");
		        Constructor<AbstractRefDataStrategy> ctor = tempClz.getConstructor();
		        strategy = ctor.newInstance();
		        strategy.setRequireData(marketSessionUtil);
		    } catch (Exception e) {
		    	log.info(e.getMessage(),e);
		        log.error("Can't find strategy: {}", refData.getStrategy());
		        strategy = new AbstractRefDataStrategy() {
		            @Override
		            public void init(Calendar cal,RefData template) {

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
		} else {
		    strategy = strategyMap.get(refData.getStrategy());
		}
		strategy.init(cal,template);
		strategy.updateRefData(refData);
		log.info("settlement date:{}, index type:{}",refData.getSettlementDate(),refData.getIndexSessionType());
//		log.info("XML:"+xstream.toXML(refData));
	}

    @Override
    public void uninit() {
        log.info("uninitialising");
        strategyMap.clear();
    }

    @Override
    public RefData getRefData(String symbol) {
        for (RefData refData : refDataList) {
            if (refData.getSymbol().equals(symbol))
                return refData;
        }
        return null;
    }

    @Override
    public List<RefData> getRefDataList() {
        return refDataList;
    }

    @Override
    public void injectRefDataList(List<RefData> refDataList) {
    	if(this.refDataList == null)
    		this.refDataList = new ArrayList<>();
    	
        this.refDataList.addAll(refDataList);
    }

    @Override
    public void clearRefData() {
        refDataList.clear();
    }

    public void setMarketSessionUtil(MarketSessionUtil marketSessionUtil) {
        this.marketSessionUtil = marketSessionUtil;
    }

    public void setStrategyPack(String strategyPack) {
        this.strategyPack = strategyPack;
    }

	@Override
	public RefData update(RefData refData, String tradeDate) throws Exception {
		Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(tradeDate));
        updateRefData(cal, refData);
        if (refDataList.contains(refData))
        	refDataList.remove(refData);
        
        refDataList.add(refData);
		return refData;
	}

	public String getRefDataTemplatePath() {
		return refDataTemplatePath;
	}

	public void setRefDataTemplatePath(String refDataTemplatePath) {
		this.refDataTemplatePath = refDataTemplatePath;
	}

	@Override
	public List<RefData> update(String index, String tradeDate) throws Exception {
		List<RefData> ret = new ArrayList<>();
		for (RefData refData : refDataList) {
			if (index.equals(refData.getCategory())) {
				refData = update(refData, tradeDate);
				ret.add(refData);
			}
		}
		
		return ret;
	}

	public Map<String, RefData> getRefDataTemplateMap() {
		return refDataTemplateMap;
	}

	public void setRefDataTemplateMap(Map<String, RefData> refDataTemplateMap) {
		this.refDataTemplateMap = refDataTemplateMap;
	}

	@Override
	public boolean remove(RefData refData) {
		if (refDataList.contains(refData)) {
			refDataList.remove(refData);
			return true;
		}
		return false;
	}

}
