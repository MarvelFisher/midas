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
    Map<String, Double> commissionByMarket = new HashMap<String, Double>();
    Map<String, Double> commissionByExchange = new HashMap<String, Double>();

    public Map<String, Double> getCommissionByMarket() {
		return commissionByMarket;
	}

	public void setCommissionByMarket(Map<String, Double> commissionByMarket) {
		this.commissionByMarket = commissionByMarket;
	}

	public Map<String, Double> getCommissionByExchange() {
		return commissionByExchange;
	}

	public void setCommissionByExchange(Map<String, Double> commissionByExchange) {
		this.commissionByExchange = commissionByExchange;
	}

	protected void updateCommission(RefData refData) {
		if (refData != null) {
			// If LOT_COMMISSION_FEE has value, need not proceed
			if (!nullOrZero(refData.getLotCommissionFee())) {
				return;
			}

			double commission = refData.getCommissionFee();
			if (PriceUtils.isZero(commission)) {
	    		String market = refData.getMarket();
	    		if (commissionByMarket != null
	    				&& commissionByMarket.size() > 0
	    				&& market != null) {
	    			commission = commissionByMarket.get(market);
		            if (!nullOrZero(commission)) {
						refData.setCommissionFee(commission);
						return;
					}
				}

	            String exchange = refData.getExchange();
	            if (commissionByExchange != null
	            		&& commissionByExchange.size() > 0
	            		&& exchange != null) {
	            	commission = commissionByExchange.get(exchange);
		            if (!nullOrZero(commission)) {
		            	refData.setCommissionFee(commission);
		            	return;
					}
				}

	            refData.setCommissionFee(Default.getCommission());
	    	}
		}
	}

	protected void updateMarginRate(RefData refData) {
		if (refData != null) {
	        if (PriceUtils.isZero(refData.getMarginRate())) {
	            String market = refData.getMarket();
	            if (marginRateByMarket != null
	            		&& marginRateByMarket.size() > 0
	            		&& market != null) {
		            double marketMarginRate = marginRateByMarket.get(market);
		            if (!nullOrZero(marketMarginRate)) {
		            	refData.setMarginRate(marketMarginRate);
		            	return;
		            }
	            }

	            String exchange = refData.getExchange();
	            if (marginRateByExchange != null
	            		&& marginRateByExchange.size() > 0
	            		&& exchange != null) {
		            double exchangeMarginRate = marginRateByExchange.get(exchange);
		            if (!nullOrZero(exchangeMarginRate)) {
		            	refData.setMarginRate(exchangeMarginRate);
		            	return;
		            }
	            }
	        }

	        refData.setMarginRate(1 / Default.getMarginTimes());
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

    private boolean nullOrZero(Double val) {
		return val == null || PriceUtils.isZero(val);
	}

}
