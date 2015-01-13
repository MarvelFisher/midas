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
package com.cyanspring.common.event.marketsession;

import java.util.Date;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketsession.MarketSessionType;

public class MarketSessionEvent extends RemoteAsyncEvent {
	private MarketSessionType session;
	private String market;
	private Date start;
	private Date end;
	private String tradeDate;

	public MarketSessionEvent(String key, String receiver,
			MarketSessionType session, Date start, Date end, String tradeDate, String market) {
		super(key, receiver);
		this.session = session;
		this.start = start;
		this.end = end;
		this.tradeDate = tradeDate;
		this.market = market;
	}

	public MarketSessionType getSession() {
		return session;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}
	
	public String getTradeDate(){
		return tradeDate;
	}
}
