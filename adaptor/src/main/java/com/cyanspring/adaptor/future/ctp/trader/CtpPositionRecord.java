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
	private Map<String, HoldPosition> holds = new HashMap<String, HoldPosition>();
	private final int margin;
	private final double maxOpenPosition;
	private boolean injecting;

	public CtpPositionRecord(int margin, double maxOpenPosition) {
		this.margin = margin;
		this.maxOpenPosition = maxOpenPosition;
	}
	
	private class HoldPosition {
		private double ydQty;
		private double tdQty;
		
		public HoldPosition(double ydQty, double tdQty) {
			super();
			this.ydQty = ydQty;
			this.tdQty = tdQty;
		}
		
		public double getYdQty() {
			return ydQty;
		}
		public void setYdQty(double ydQty) {
			this.ydQty = ydQty;
		}
		public double getTdQty() {
			return tdQty;
		}
		public void setTdQty(double tdQty) {
			this.tdQty = tdQty;
		}
		@Override
		public String toString() {
			return "" + ydQty + "," + tdQty;
		}
	}

	private String getKey(String symbol, boolean isBuy) {
		return symbol + (isBuy?"-B":"-S");
	}
	
	synchronized void onTradeUpdate(String symbol, boolean isBuy, byte flag, double qty) {
		CtpPosition position = positions.get(getKey(symbol, !isBuy));
		if(null == position)
			return;
		
		HoldPosition hold = holds.get(getKey(symbol, !isBuy));
		if(null == hold)
			return;
		
		if(TraderLibrary.THOST_FTDC_OF_CloseYesterday == flag) {
			position.setYdQty(position.getYdQty() - qty);
			hold.setYdQty(hold.getYdQty() - qty);
		} else if (TraderLibrary.THOST_FTDC_OF_CloseToday == flag ||
				TraderLibrary.THOST_FTDC_OF_Close == flag) {
			position.setTdQty(position.getTdQty() - qty);
			hold.setTdQty(hold.getTdQty() - qty);
		}
		log.debug("onTradeUpdate: " + this.toString());
	}
	
	synchronized byte holdQuantity(String symbol, boolean isBuy, double qty) {
		log.debug("Before holdQuantity: " + this.toString());
		byte result = TraderLibrary.THOST_FTDC_OF_Open;
		CtpPosition position = positions.get(getKey(symbol, !isBuy));
		if(null == position)
			return result;

		log.debug("hold position: " + position);		
		HoldPosition hold = holds.get(getKey(symbol, !isBuy));
		if(null == hold) {
			hold = new HoldPosition(0, 0);
			holds.put(getKey(symbol, !isBuy), hold);
		}
		
		if(PriceUtils.EqualGreaterThan(position.getYdQty() - hold.getYdQty() - qty - margin, 0)) {
			hold.setYdQty(hold.getYdQty() + qty);
			result = TraderLibrary.THOST_FTDC_OF_CloseYesterday;
		} else if(PriceUtils.EqualGreaterThan(position.getTdQty() - hold.getTdQty() - qty - margin, 0)) {
			hold.setTdQty(hold.getTdQty() + qty);
			result = TraderLibrary.THOST_FTDC_OF_CloseToday;
		}
		log.debug("After holdQuantity: " + this.toString());
		return result;
	}

	synchronized void releaseQuantity(String symbol, boolean isBuy, byte flag, double qty) {
		HoldPosition hold = holds.get(getKey(symbol, !isBuy));
		if(null == hold) {
			log.warn("hold position is null: " + symbol + ", " + isBuy + ", " + flag + ", " + qty);
			return;
		}
		
		if(flag == TraderLibrary.THOST_FTDC_OF_CloseYesterday) {
			if(PriceUtils.LessThan(hold.getYdQty(), qty))
				log.error("release position: " + symbol + ", " + isBuy + ", " + flag + ", " + qty);
			else
				hold.setYdQty(hold.getYdQty() - qty);
		} else if(flag == TraderLibrary.THOST_FTDC_OF_CloseToday ||
				flag == TraderLibrary.THOST_FTDC_OF_Close ) {
			if(PriceUtils.LessThan(hold.getTdQty(), qty))
				log.error("release position: " + symbol + ", " + isBuy + ", " + flag + ", " + qty);
			else
				hold.setTdQty(hold.getTdQty() - qty);
		}
		log.debug("release Quantity: " + this.toString());
	}
		
	synchronized void inject(CtpPosition position, boolean last) {
		if(!injecting) {
			positions.clear();
			injecting = true;
		}
		
		if(last)
			injecting = false;
		
		CtpPosition existing = positions.get(getKey(position.getSymbol(), position.isBuy()));
		if(null == existing) {
			positions.put(getKey(position.getSymbol(), position.isBuy()), position);
		} else {
			existing.add(position);
		}
		log.debug("After inject: " + toString());
	}
	
	synchronized boolean checkMaxOpenPosition(String symbol) {
		double totalOpenPosition = 0;
		CtpPosition record = positions.get(getKey(symbol, true));
		if(null != record)
			totalOpenPosition += record.getTotalOpenPosition();
		
		record = positions.get(getKey(symbol, false));
		if(null != record)
			totalOpenPosition += record.getTotalOpenPosition();
		
		log.debug("TotalOpenPosition: " + symbol + ", " + totalOpenPosition);
		if(PriceUtils.EqualGreaterThan(totalOpenPosition, maxOpenPosition))
			return false;
		
		return true;
	}
	
	synchronized void clear() {
		positions.clear();
	}
	
	@Override
	public String toString() {
		return positions.toString() + " ::: " + holds.toString();
	}
}
