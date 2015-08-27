package com.cyanspring.common.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.RefData;

public class RefSymbolFilter implements IRefDataFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefSymbolFilter.class);

	/**
	 * This filter is aimed to only keep the 活躍 one RefData 
	 * if there are same {IType}+{Symbol} combination in lstRefData
	 * 
	 * Key: IType + Symbol
	 * {category}.{exchange} = {refSymbol} means 活躍 (若同類別存在同年月, 活躍優先保存)
	 * 
	 * @param lstRefData
	 *            The RefData list to be filtered
	 * @return The filtered RefData list
	 */
	@Override
	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		ArrayList<RefData> fLstRefData;
		if (lstRefData != null) {
			ConcurrentHashMap<String, RefData> mapRefData = new ConcurrentHashMap<String, RefData>();
			for (RefData refData : lstRefData) {
				String type = refData.getIType();
				if (type == null || type.isEmpty()) {
					LOGGER.error("IType cannot be null or empty.");
					throw new Exception("IType cannot be null or empty.");
				}
				
				String symbol = refData.getSymbol();
				if (symbol != null && !symbol.isEmpty()) {
					symbol = symbol.toLowerCase();
				} else {
					LOGGER.error("Symbol cannot be null or empty.");
					throw new Exception("Symbol cannot be null or empty.");
				}
				
				String category = refData.getCategory();
				String exchange = refData.getExchange();
				String refSymbol = refData.getRefSymbol();
				
				if (category == null || category.isEmpty()
						|| exchange == null || exchange.isEmpty()
						|| refSymbol == null || refSymbol.isEmpty()) {
					LOGGER.error("Category, Exchange, RefSymbol "
							+ "cannot be null or empty.");
					throw new Exception("Category, Exchange, RefSymbol "
							+ "cannot be null or empty.");
				}
				
				String key = type + symbol;
				// If DataObject has duplicate IType+Symbol, exclude the later one unless it's 活躍
				if (mapRefData.containsKey(key)) { 
					if (refSymbol.equals(category + "." + exchange)) {
						// Means current one is 活躍, remove existing one
						mapRefData.remove(key);
					} else {
						continue; // Even Map has unique keys...
					}
				}
				
				mapRefData.put(key, refData);
			}
			
			fLstRefData = new ArrayList<RefData>(mapRefData.values());
		} else {
			LOGGER.error("The given RefData list cannot be null");
			throw new Exception("The given RefData list cannot be null");
		}
		
		return fLstRefData;
	}

}
