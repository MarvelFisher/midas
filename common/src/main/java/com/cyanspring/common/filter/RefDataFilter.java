package com.cyanspring.common.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataTplLoader;
import com.cyanspring.common.staticdata.fu.IType;

/**
 * 
 * @author alvinxie
 * 
 */
public class RefDataFilter implements IDataObjectFilter {
	
	@Autowired
	RefDataTplLoader refDataTplLoader;

	private static final Logger log = LoggerFactory.getLogger(RefDataFilter.class);

	private IType[] types;

	public IType[] getTypes() {
		return types;
	}

	public void setTypes(IType[] types) {
		this.types = types;
	}

	/**
	 * Change the value of property "types" of bean "refDataFilter" to filter.
	 * 
	 * TheDataObject with matching "iType" and non-duplicate Symbol will be 
	 * added into a new DataObject list then return. 
	 * 
	 * Reference path:
	 * /cyanspring-server/conf/fc/fc.xml
	 * 
	 * Key: iType + Symbol
	 * 
	 * {category}.{exchange} = {refSymbol} -> 活躍 (若同類別存在同年月, 活躍優先保存)
	 * 
	 * @param lstDataObj
	 *            The DataObject list to be filtered
	 * @return The filtered DataObject list
	 */
	@Override
	public List<? extends DataObject> filter(List<? extends DataObject> lstDataObj) throws Exception {
		ConcurrentHashMap<String, RefData> mapRefData = new ConcurrentHashMap<String, RefData>();

		ArrayList<String> lstITypes = new ArrayList<String>();
		for (IType iType : getTypes()) {
			lstITypes.add(iType.getValue());
		}

		for (DataObject obj : lstDataObj) {
			RefData refData = (RefData) obj;
			String type = refData.getIType();
			if (type == null || type.isEmpty()) {
				log.error("IType cannot be null or empty.");
				throw new Exception("IType cannot be null or empty.");
			}
			
			if (lstITypes.contains(type)) {
				String symbol = refData.getSymbol();
				if (symbol != null && !symbol.isEmpty()) {
					symbol = symbol.toLowerCase();
				} else {
					log.error("Symbol cannot be null or empty.");
					throw new Exception("Symbol cannot be null or empty.");
				}
				
				String category = refData.getCategory();
				String exchange = refData.getExchange();
				String refSymbol = refData.getRefSymbol();
				
				if (category == null || category.isEmpty()
						|| exchange == null || exchange.isEmpty()
						|| refSymbol == null || refSymbol.isEmpty()) {
					log.error("Category, Exchange, RefSymbol "
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
		}

		List<RefData> lstRefData = new ArrayList<RefData>(mapRefData.values());
		
		lstRefData = excludeNonExistingProducts(lstRefData);
		
		return lstRefData;
	}
	
	private List<RefData> excludeNonExistingProducts(List<RefData> lstRefData) throws Exception {
		if (lstRefData != null && lstRefData.size() > 0) {
			List<RefData> lstRefDataTpl = refDataTplLoader.getRefDataList();
			
			// equals() and hashCode() of RefData have been "override" based on "Symbol"
			if (lstRefDataTpl != null && lstRefDataTpl.size() > 0) {
				lstRefData.retainAll(lstRefDataTpl);				
			}
		} else {
			log.error("The given RefData list cannot be null or empty");
			throw new Exception("The given RefData list cannot be null or empty");
		}
		
		return lstRefData;
	}

}
