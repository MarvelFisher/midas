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
package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MarketSessionTime {
		
	public List<SessionData> lst = new ArrayList<>();
//	public MarketSessionTimeFormat timeFormat;
	private String timeFormat;
	
	
	public static class SessionData{
		public MarketSessionType session;
		public String weekDay;
		public String date;
		public String start;
		public String end;		
		public SessionData(MarketSessionType session, String start, String end){
			this.session = session;
			this.start = start;
			this.end = end;
		}
		
		public SessionData(){
			
		}
	}
	
//	public MarketSessionTime(MarketSessionType session, Date start, Date end) {
//		super();
//		this.session = session;
//		this.start = start;
//		this.end = end;
//	}
	
	public MarketSessionTime(List<SessionData> data) throws ParseException {
		timeFormat = "HH:mm:ss";
		lst = data;
	}
	
	public MarketSessionTime(MarketSessionType session, List<String> data){
		timeFormat = "yyyy-MM-dd";
		for(String date : data){
			SessionData sessionData = new SessionData();
			sessionData.session = session;
			if(date.length() == 1){				
				sessionData.weekDay = date;
			}else{
				sessionData.date = date;
			}
			lst.add(sessionData);
		}
	}

	
	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}
	
}
