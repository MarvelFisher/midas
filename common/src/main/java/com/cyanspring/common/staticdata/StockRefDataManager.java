package com.cyanspring.common.staticdata;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.fu.AbstractRefDataStrategy;
import com.cyanspring.common.staticdata.fu.IType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class StockRefDataManager extends RefDataService {

    protected static final Logger log = LoggerFactory.getLogger(StockRefDataManager.class);
    private List<RefData> refDataList = new CopyOnWriteArrayList<>();
    private XStream xstream = new XStream(new DomDriver("UTF-8"));

    //Futures
    private Map<String, AbstractRefDataStrategy> strategyMap = new HashMap<>();
    private String strategyPack = "com.cyanspring.common.staticdata.fu";
    private String refDataTemplatePath;
    private List<RefData> refDataTemplateList;
    private Map<String,RefData> refDataTemplateMap = new HashMap<String,RefData>();

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws Exception {
		log.info("initialising with " + refDataTemplatePath);
		if (StringUtils.hasText(refDataTemplatePath)) {

			log.info("read refdata template:{}", refDataTemplatePath);
			File templateFile = new File(refDataTemplatePath);
			if (templateFile.exists()) {
				refDataTemplateList = (List<RefData>) xstream.fromXML(templateFile);
				if (null != refDataTemplateList && !refDataTemplateList.isEmpty()) {
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
        cal.setTime(getSettlementDateFormat().parse(tradeDate));
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
		String iType = refData.getIType();
		if(IType.isFuture(iType)){
			updateFutureRefData(cal,refData);
		}else if(IType.isStock(iType) || IType.isIndex(iType)){
			updateStockRefData(cal,refData);
		}else{
			log.info("none support type:{} , {}",iType,refData.getRefSymbol());
		}
	}

	private void updateStockRefData(Calendar cal, RefData refData) {
		updateMarginRate(refData);
		updateCommission(refData);
	}

	@Override
    public void uninit() {
        log.info("uninitialising");
        strategyMap.clear();
    }

    @Override
    public RefData getRefData(String symbol) {
    	Iterator <RefData>refDataIte = refDataList.iterator();
    	while(refDataIte.hasNext()){
    		RefData tempRefData = refDataIte.next();
        	if(!StringUtils.hasText(tempRefData.getSymbol())){
        		continue;
        	}
            if (tempRefData.getSymbol().equals(symbol)) {
				return tempRefData;
			}
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

    public void setStrategyPack(String strategyPack) {
        this.strategyPack = strategyPack;
    }

	@Override
	public RefData add(RefData refData, String tradeDate) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getSettlementDateFormat().parse(tradeDate));
		updateRefData(cal, refData);
		remove(refData);
		refDataList.add(refData);
		return refData;
	}

	public String getRefDataTemplatePath() {
		return refDataTemplatePath;
	}

	public void setRefDataTemplatePath(String refDataTemplatePath) {
		this.refDataTemplatePath = refDataTemplatePath;
	}

	private void updateFutureRefData(Calendar cal, RefData refData){

		initCategory(refData);
		AbstractRefDataStrategy strategy;
		RefData template = searchRefDataTemplate(refData);
		if (null == template) {
			return;
		} else {
			refData.setStrategy(template.getStrategy());
		}
		log.info("update refData:{}, strategy:{}", refData.getRefSymbol(), refData.getStrategy());

		if (!strategyMap.containsKey(refData.getStrategy())) {
			try {
				Class<AbstractRefDataStrategy> tempClz = (Class<AbstractRefDataStrategy>) Class
						.forName(strategyPack + "." + refData.getStrategy() + "Strategy");
				Constructor<AbstractRefDataStrategy> ctor = tempClz.getConstructor();
				strategy = ctor.newInstance();
			} catch (Exception e) {
				log.info(e.getMessage(), e);
				log.error("Can't find strategy: {}", refData.getStrategy());
				strategy = new AbstractRefDataStrategy() {
					@Override
					public void init(Calendar cal, RefData template) {

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
			updateCommission(refData);
		} else {
			strategy = strategyMap.get(refData.getStrategy());
		}
		strategy.init(cal, template);
		strategy.updateRefData(refData);
		refData.setDayTradable(template.getDayTradable());
		log.info("settlement date:{}, index type:{}", refData.getSettlementDate(), refData.getIndexSessionType());
//		log.info("XML:"+xstream.toXML(refData));
	}

	private void initCategory(RefData refData) {

		if(StringUtils.hasText(refData.getCategory())) {
			return;
		}

		String commodity = refData.getCommodity();
		if (!StringUtils.hasText(commodity) || (StringUtils.hasText(commodity)
				&& (commodity.equals(RefDataCommodity.FUTUREINDEX.getValue())))) {
			refData.setCategory(getCategory(refData));
		}
	}

	@Override
	public List<RefData> update(String index, String tradeDate) throws Exception {
		List<RefData> ret = new ArrayList<>();

		for (RefData refData : refDataList) {
			if (index.equals(refData.getCategory())) {
				ret.add(refData);
			}
		}

		for(RefData refData : ret){
			add(refData, tradeDate);
		}

		return ret;
	}

	@Override
	public boolean remove(RefData refData) {

		boolean remove = false;
		if(null == refDataList || refDataList.isEmpty()) {
			return remove;
		}

		List<RefData> delList = new ArrayList<RefData>();

		for (RefData ref : refDataList) {
			if (ref.getRefSymbol().equals(refData.getRefSymbol())) {
				delList.add(ref);
			}
		}

		if (!delList.isEmpty()) {
			remove = true;
		}

		for (RefData ref : delList) {
			refDataList.remove(ref);
		}

		return remove;
	}

	public Map<String, RefData> getRefDataTemplateMap() {
		return refDataTemplateMap;
	}

	public void setRefDataTemplateMap(Map<String, RefData> refDataTemplateMap) {
		this.refDataTemplateMap = refDataTemplateMap;
	}

	private SimpleDateFormat getSettlementDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd") ;
	}
}
