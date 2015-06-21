package com.cyanspring.adaptor.future.ctp.trader;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.client.CtpPosition;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary;
import com.cyanspring.common.util.PriceUtils;

public class CtpPositionRecord {
	private static final Logger log = LoggerFactory
			.getLogger(CtpPositionRecord.class);
	private Map<String, CtpPosition> positions = new HashMap<String, CtpPosition>();

	private String getKey(String symbol, boolean isBuy) {
		return symbol + (isBuy?"-B":"-S");
	}
	
	private void addPosition(String symbol, boolean isBuy, boolean isYesterday, double qty) {
		CtpPosition position = positions.get(getKey(symbol, isBuy));
		if(null == position) {
			positions.put(getKey(symbol, isBuy), new CtpPosition(symbol, isBuy, qty, 0));
		} else if(isYesterday){
			position.setYdQty(position.getYdQty()+qty);
		} else {
			position.setTdQty(position.getTdQty()+qty);
		}
	}
	
	private void deductPosition(String symbol, boolean isBuy, boolean isYesterday, double qty) {
		CtpPosition position = positions.get(getKey(symbol, isBuy));
		if(position == null) {
			log.error("Trade indicates to close a non-existing position: " + symbol + ", " + isBuy + ", " + isYesterday + ", " + qty);
			return;
		}
		
		double positionQty = isYesterday?position.getYdQty():position.getTdQty();
		if(PriceUtils.GreaterThan(qty, positionQty)) {
			log.error("Trade qty is bigger than existing position: " + symbol + ", " + isBuy + ", " + isYesterday + ", " + qty + " : " + positionQty);
			return;
		}
		
		if(isYesterday) {
			position.setYdQty(position.getYdQty() - qty);
		} else {
			position.setTdQty(position.getTdQty() - qty);
		}
	}
	
	synchronized void onTradeUpdate(String symbol, boolean isBuy, byte flag, double qty) {
		if(flag == TraderLibrary.THOST_FTDC_OF_Open) {
			addPosition(symbol, isBuy, false, qty);
		} 
		log.debug("onTradeUpdate: " + flag + ", " + positions);
	}
	
	synchronized byte holdQuantity(String symbol, boolean isBuy, double qty) {
		CtpPosition position = positions.get(getKey(symbol, !isBuy));
		byte result = TraderLibrary.THOST_FTDC_OF_Open;
		if(null == position) {
			result = TraderLibrary.THOST_FTDC_OF_Open;
		} else if(PriceUtils.EqualGreaterThan(position.getYdQty(), qty)) {
			deductPosition(symbol, !isBuy, true, qty);
			result = TraderLibrary.THOST_FTDC_OF_CloseYesterday;
		} else if(PriceUtils.EqualGreaterThan(position.getTdQty(), qty)) {
			deductPosition(symbol, !isBuy, false, qty);
			result = TraderLibrary.THOST_FTDC_OF_CloseToday;
		}
		log.debug("holdQuantity: " + positions);
		return result;
	}

	synchronized void releaseQuantity(String symbol, boolean isBuy, byte flag, double qty) {
		CtpPosition position = positions.get(getKey(symbol, !isBuy));
		if(null == position) {
			log.error("releaseQuantity can't find record " + symbol + ", " + isBuy + ", " + flag + ", " + qty);
		} else if(flag == TraderLibrary.THOST_FTDC_OF_CloseYesterday) {
			addPosition(symbol, !isBuy, true, qty);
		} else if(flag == TraderLibrary.THOST_FTDC_OF_CloseToday) {
			addPosition(symbol, !isBuy, false, qty);
		}
		log.debug("releaseQuantity: " + positions);
	}
		
	synchronized void inject(CtpPosition position) {
		positions.put(getKey(position.getSymbol(), position.isBuy()), position);
	}
	
	synchronized void clear() {
		positions.clear();
	}
	
	@Override
	public String toString() {
		return positions.toString();
	}
}
