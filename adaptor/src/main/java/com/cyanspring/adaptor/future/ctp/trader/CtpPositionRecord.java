package com.cyanspring.adaptor.future.ctp.trader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.client.CtpPosition;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary;
import com.cyanspring.common.util.PriceUtils;

public class CtpPositionRecord {
	private static final Logger log = LoggerFactory
			.getLogger(CtpPositionRecord.class);
	private Map<String, CtpPosition> positions = new ConcurrentHashMap<String, CtpPosition>();

	private String getKey(String symbol, boolean isBuy) {
		return symbol + (isBuy?"-B":"-S");
	}
	
	void addPosition(String symbol, boolean isBuy, double qty) {
		CtpPosition position = positions.get(getKey(symbol, isBuy));
		if(null == position) {
			positions.put(getKey(symbol, isBuy), new CtpPosition(symbol, isBuy, qty, 0));
		} else {
			position.setTdQty(position.getTdQty()+qty);
		}
	}
	
	void deductPosition(String symbol, boolean isBuy, boolean isYesterday, double qty) {
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
	
	void onTradeUpdate(String symbol, boolean isBuy, byte flag, double qty) {
		if(flag == TraderLibrary.THOST_FTDC_OF_Open) {
			addPosition(symbol, isBuy, qty);
		} else if (flag == TraderLibrary.THOST_FTDC_OF_CloseYesterday) {
			deductPosition(symbol, !isBuy, true, qty);
		} else if (flag == TraderLibrary.THOST_FTDC_OF_CloseToday) {
			deductPosition(symbol, !isBuy, false, qty);
		} else {
			log.error("Unhandled flag: " + symbol + ", " + isBuy + ", " + flag + ", " + qty);
			addPosition(symbol, isBuy, qty);
		}
		log.debug("onTradeUpdate: " + positions);
	}
	
	byte getPositionFlag(String symbol, boolean isBuy, double qty) {
		CtpPosition position = positions.get(getKey(symbol, !isBuy));
		if(null == position) {
			return TraderLibrary.THOST_FTDC_OF_Open;
		} else if(PriceUtils.GreaterThan(position.getYdQty(), qty)) {
			return TraderLibrary.THOST_FTDC_OF_CloseYesterday;
		} else if(PriceUtils.GreaterThan(position.getTdQty(), qty)) {
			return TraderLibrary.THOST_FTDC_OF_CloseToday;
		} else {
			return TraderLibrary.THOST_FTDC_OF_Open;
		}
	}
	
	void inject(CtpPosition position) {
		positions.put(getKey(position.getSymbol(), position.isBuy()), position);
	}
	
	void clear() {
		positions.clear();
	}
	
	@Override
	public String toString() {
		return positions.toString();
	}
}
