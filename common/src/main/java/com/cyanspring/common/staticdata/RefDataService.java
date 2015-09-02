package com.cyanspring.common.staticdata;

import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.util.PriceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public abstract class RefDataService implements IPlugin, IRefDataManager  {
    protected static final Logger log = LoggerFactory
            .getLogger(RefDataService.class);
    String market = Default.getMarket();
    String refDataFile;
    Map<String, Double> marginRateByMarket = new HashMap<String, Double>();
    Map<String, Double> marginRateByExchange = new HashMap<String, Double>();

    protected void updateMarginRate(RefData refData){
        if (PriceUtils.isZero(refData.getMarginRate())){
            String market = refData.getMarket();
            Double marginRate = marginRateByMarket.get(market);
            if (marginRate != null && !PriceUtils.isZero(marginRate)) {
            	refData.setMarginRate(marginRate);
            	return;
            }
            
            String exchange = refData.getExchange();
            marginRate = marginRateByExchange.get(exchange);
            if (marginRate != null && !PriceUtils.isZero(marginRate)) {
            	refData.setMarginRate(marginRate);
            } else {
                refData.setMarginRate(1/ Default.getMarginTimes());
            }
        }
    }

    public Map<String, Double> getMarginRateByMarket() {
        return marginRateByMarket;
    }

    public void setMarginRateByMarket(Map<String, Double> marginRateByMarket) {
        this.marginRateByMarket = marginRateByMarket;
    }

    public Map<String, Double> getMarginRateByExchange() {
        return marginRateByExchange;
    }

    public void setMarginRateByExchange(Map<String, Double> marginRateByExchange) {
        this.marginRateByExchange = marginRateByExchange;
    }

    public String getRefDataFile() {
        return refDataFile;
    }

    public void setRefDataFile(String refDataFile) {
        this.refDataFile = refDataFile;
    }

    public String getMarket() {
        return market;
    }
}
