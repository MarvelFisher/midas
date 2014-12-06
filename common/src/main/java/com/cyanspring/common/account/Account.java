package com.cyanspring.common.account;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;


public class Account extends BaseAccount implements Cloneable {
	
	protected Map<String, OpenPosition> mapLastSymbolUpdate = null;
	
	protected Account() {
		super();
	}
	
	public Account(String id, String userId) {
		super(id, userId);
	}

	@Override
	public synchronized Account clone() throws CloneNotSupportedException {
		return (Account)super.clone();
	}
	
	public boolean isNeedNotifyOpenPositionUpdate(OpenPosition pos) {
		
		OpenPosition lastPos = null;
		
		if(mapLastSymbolUpdate == null)
			mapLastSymbolUpdate = new HashMap<String, OpenPosition>();
		else
			lastPos = mapLastSymbolUpdate.get(pos.getSymbol());
		
		if((pos.getAcPnL() != lastPos.getAcPnL()) || (pos.getPnL() != lastPos.getPnL())
				|| (pos.getPrice() != lastPos.getPrice()) || (pos.getQty() != lastPos.getPrice()))
		{
			mapLastSymbolUpdate.put(pos.getSymbol(), pos);
			return true;
		}
		
		return false;
	}
}
