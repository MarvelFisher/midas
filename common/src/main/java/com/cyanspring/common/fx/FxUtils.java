package com.cyanspring.common.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.util.PriceUtils;

public class FxUtils {
	private static final Logger log = LoggerFactory
			.getLogger(FxUtils.class);
	
	static public String getFromCurrency(String symbol) {
		return symbol.substring(0, 3);
	}
	
	static public String getToCurrency(String symbol) {
		return symbol.substring(3);
	}
	
	static public double convertPositionToCurrency(IRefDataManager refDataManager, 
			IFxConverter fxConverter,
			String baseCurrency, 
			String symbol, double qty, double price) {
		if(null == refDataManager) {
			log.warn("RefDataManager is null");
			return qty;
		}
			
		RefData refData = refDataManager.getRefData(symbol);
		if(null != refData &&
			null != refData.getExchange() && 
			refData.getExchange().equals("FX")) {
			String currency = refData.getCurrency();
			if(null != currency && 
					!currency.equals(baseCurrency)) {
				try {
					return fxConverter.getFxQty(currency, baseCurrency, qty);
				} catch (FxException e) {
					log.error(e.getMessage(), e);
				}
			} 
			return qty;
		} else if(null != refData) {
			double pricePerUnit = refData.getPricePerUnit();
			if(!PriceUtils.isZero(pricePerUnit)) {
				return qty * pricePerUnit * price;
			}
			return qty * price;
		} else {
			return qty * price;
		}
	}

	static public double convertPnLToCurrency(IRefDataManager refDataManager, 
			IFxConverter fxConverter, String baseCurrency, String symbol, double qty) {
		if(null == refDataManager) {
			log.warn("RefDataManager is null");
			return qty;
		}
			
		RefData refData = refDataManager.getRefData(symbol);
		if(null != refData &&
				null != refData.getExchange() && 
				refData.getExchange().equals("FX")) {
			String fxCurrency = refData.getFxCurrency();
			if(null != fxCurrency && 
					!fxCurrency.equals(baseCurrency)) {
				try {
					return fxConverter.getFxQty(fxCurrency, baseCurrency, qty);
				} catch (FxException e) {
					log.error(e.getMessage(), e);
				}
			} 
			return qty;
		} else {
			return qty;
		}
	}
	
	static public double calculatePnL(IRefDataManager refDataManager, String symbol, double qty, double price) {
		if(null == refDataManager) {
			log.warn("RefDataManager is null");
			return qty * price;
		}
			
		RefData refData = refDataManager.getRefData(symbol);
		if(null != refData) {
			double pricePerUnit = refData.getPricePerUnit();
			if(!PriceUtils.isZero(pricePerUnit)) {
				return qty * pricePerUnit * price;
			}
			return qty * price;
		} else {
			return qty * price;
		}
		
	}
}
