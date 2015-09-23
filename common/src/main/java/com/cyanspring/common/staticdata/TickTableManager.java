/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.staticdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class TickTableManager {
	
    @Autowired
    IRefDataManager refDataManager;
    
	private Map<String, AbstractTickTable> tickTables;
	
	public Map <AbstractTickTable,List<RefData>> buildTickTableSymbolMap(String symbol){
		List<RefData> list = null;
		if(StringUtils.hasText(symbol)){
			list = new ArrayList<RefData>();
			RefData refData =refDataManager.getRefData(symbol);
			if( null != refData){
				list.add(refData);
			}
		}else{
			 list = refDataManager.getRefDataList();
		}
		
		if( null == list || list.isEmpty())
			return null;
		
		Map <AbstractTickTable,List<RefData>> map = new HashMap<AbstractTickTable,List<RefData>>();
		for(RefData refData : list){
			AbstractTickTable table = getTickTable(refData);
			List <RefData> tempList = null;
			RefData tempData = new RefData();
			tempData.setSymbol(refData.getSymbol());
			tempData.setDecimalPoint(refData.getDeciamlPoint());
			if(map.containsKey(table)){
				tempList = map.get(table);
				if( null != tempList){
					tempList.add(tempData);
					map.put(table, tempList);
				}
			}else{
				tempList = new ArrayList<RefData>();
				tempList.add(tempData);
				map.put(table, tempList);
			}
		}
		return map;
	}

	public AbstractTickTable getTickTable(RefData refData) {
		if( null == refData)
			return tickTables.get("DEFAULT");
		
		String tickTableID = refData.getTickTable();
		String exchange = refData.getExchange();
		AbstractTickTable tickTable;
		if(null != tickTableID && (tickTable = tickTables.get(tickTableID)) != null) {
			 return tickTable;
		}
		if(null != exchange && (tickTable = tickTables.get(exchange)) != null) {
			 return tickTable;
		}
			
		return tickTables.get("DEFAULT");
	}

	public Map<String, AbstractTickTable> getTickTables() {
		return tickTables;
	}

	public void setTickTables(Map<String, AbstractTickTable> tickTables) {
		this.tickTables = tickTables;
	}
	

}
