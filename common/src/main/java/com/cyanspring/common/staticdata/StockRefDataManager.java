package com.cyanspring.common.staticdata;

import java.io.File;
import java.lang.reflect.Constructor;
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
import com.cyanspring.common.staticdata.fu.IType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class StockRefDataManager extends RefDataService {

    protected static final Logger log = LoggerFactory.getLogger(RefDataFactory.class);
    private List<RefData> refDataList;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private XStream xstream = new XStream(new DomDriver("UTF-8"));
    
    //Futures
    private Map<String, AbstractRefDataStrategy> strategyMap = new HashMap<>();
    private MarketSessionUtil marketSessionUtil;
    private String strategyPack = "com.cyanspring.common.staticdata.fu";
    private String refDataTemplatePath;
    private List<RefData> refDataTemplateList;
    private Map<String,RefData> refDataTemplateMap = new HashMap<String,RefData>();
    
	@Autowired
	TradeDateManager tradeDateManager;
    
    @SuppressWarnings("unchecked")
    @Override
    public void init() throws Exception {
        log.info("initialising with future template" + refDataTemplatePath);
        if(StringUtils.hasText(refDataTemplatePath)){
        	
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
    }

    private void buildTemplateMap(List<RefData> refDataTemplateList) {
		for(RefData ref : refDataTemplateList){
			String spotName = ref.getCategory();
			if(refDataTemplateMap.containsKey(spotName)){
				log.info("duplicate refData template :{}",spotName);
				continue;
			}else{
				log.info("spotName:{},strategy:{}",spotName,ref.getStrategy());
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

    	String spotName = getCategory(refData.getRefSymbol());
    	log.info("ready find template :{}",spotName);
    	RefData templateRefData = null;
    	if(refDataTemplateMap.containsKey(spotName) && null != refDataTemplateMap.get(spotName)){
    		templateRefData = refDataTemplateMap.get(spotName);
    		log.info("find template :{}",templateRefData.getCategory());
    		return (RefData)templateRefData.clone();
    	}
    	
    	log.info("can't find template:{}",spotName);
    	return  null;
    }
    
	protected String getCategory(String refSymbol){
		String category =  refSymbol.replaceAll(".[A-Z]+$", "").replaceAll("\\d", "");
		if(category.length() > 2 ){
			return category.substring(0, 2);
		}else{
			return category;
		}
	}
	
	private void updateRefData(Calendar cal, RefData refData) {
		String iType = refData.getIType();
		if(IType.isFuture(iType)){
			updateFutureRefData(cal,refData);
		}else if(IType.isStock(iType)){
			updateStockRefData(cal,refData);
		}else{
			log.info("none support type:{} , {}",iType,refData.getRefSymbol());
		}
	}

	private void updateStockRefData(Calendar cal, RefData refData) {
		updateMarginRate(refData);
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
        this.refDataList = refDataList;
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
        refDataList.add(refData);
		return refData;
	}

	public String getRefDataTemplatePath() {
		return refDataTemplatePath;
	}

	public void setRefDataTemplatePath(String refDataTemplatePath) {
		this.refDataTemplatePath = refDataTemplatePath;
	}

	public TradeDateManager getTradeDateManager() {
		return tradeDateManager;
	}

	public void setTradeDateManager(TradeDateManager tradeDateManager) {
		this.tradeDateManager = tradeDateManager;
	}
	
	private void updateFutureRefData(Calendar cal, RefData refData){
		AbstractRefDataStrategy strategy;
        RefData template = searchRefDataTemplate(refData);
        if( null == template){
        	return;
        }else{
        	refData.setStrategy(template.getStrategy());
        }
		log.info("refData:{}, strategy:{}",refData.getRefSymbol(),refData.getStrategy());

		if (!strategyMap.containsKey(refData.getStrategy())) {
		    try {
		        Class<AbstractRefDataStrategy> tempClz = (Class<AbstractRefDataStrategy>) Class.forName(strategyPack + "." + refData.getStrategy() + "Strategy");
		        Constructor<AbstractRefDataStrategy> ctor = tempClz.getConstructor();
		        strategy = ctor.newInstance();
		        strategy.setRequireData(marketSessionUtil,tradeDateManager);
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

	@Override
	public boolean remove(RefData refData) {
		if (refDataList.contains(refData)) {
			refDataList.remove(refData);
			return true;
		}
		return false;
	}
	
	public Map<String, RefData> getRefDataTemplateMap() {
		return refDataTemplateMap;
	}

	public void setRefDataTemplateMap(Map<String, RefData> refDataTemplateMap) {
		this.refDataTemplateMap = refDataTemplateMap;
	}
}
