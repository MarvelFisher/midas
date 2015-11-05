package com.cyanspring.cstw.business;

import java.util.List;

import com.cyanspring.common.cstw.kdb.SignalType;
import com.cyanspring.common.cstw.tick.Ticker;
import com.cyanspring.common.staticdata.RefData;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.05
 *
 */
public interface IBusinessService {

	boolean hasAuth(String view, String action);

	boolean hasViewAuth(String view);

	RefData getRefData(String symbol);

	SignalType getSignal(String symbol, double scale);

	Ticker getTicker(String symbol);

	List<String> getSymbolList();

}
