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

import java.util.Map;

public class TickTableManager {
	private Map<String, ITickTable> tickTables;

	public ITickTable getTickTable(RefData refData) {
		String tickTableID = refData.getTickTable();
		String exchange = refData.getExchange();
		ITickTable tickTable;
		if(null != tickTableID && (tickTable = tickTables.get(tickTableID)) != null) {
			 return tickTable;
		}
		if(null != exchange && (tickTable = tickTables.get(exchange)) != null) {
			 return tickTable;
		}
			
		return tickTables.get("DEFAULT");
	}

	public Map<String, ITickTable> getTickTables() {
		return tickTables;
	}

	public void setTickTables(Map<String, ITickTable> tickTables) {
		this.tickTables = tickTables;
	}
	

}
