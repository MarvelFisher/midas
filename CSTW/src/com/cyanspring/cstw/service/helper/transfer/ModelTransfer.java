package com.cyanspring.cstw.service.helper.transfer;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;

/**
 * 
 * @author NingXiaofeng
 *
 */
public final class ModelTransfer {

	/**
	 * 风控管理 - 交易记录
	 * 
	 * @param order
	 * @param refDataManager
	 * @return
	 */
	public static RCTradeRecordModel getRCTradeRecordModel(ParentOrder order) {
		RCTradeRecordModel model = new RCTradeRecordModel.Builder()
				.record(order.getId()).symbol(order.getSymbol())
				.type(order.getSide().isBuy() ? "Buy" : "Sell")
				.volume(order.getCumQty()).price(order.getAvgPx())
				.totalPrice(order.getCumQty() * order.getAvgPx())
				.tradeTime(order.getModified().toString()).executionRate(0.0)
				.trader(order.getUser()).build();
		return model;
	}

	/**
	 * 风控管理 - 当前所有仓位记录转换 - 未平仓
	 * 
	 * @param position
	 * @param refDataManager
	 * @return
	 */
	public static RCOpenPositionModel parseCurrentPositionRecord(
			OverallPosition position) {
		RCOpenPositionModel positionModel = new RCOpenPositionModel.Builder()
				.id(position.getId())
				.subAccount(position.getExchangeSubAccount())
				.instrumentCode(position.getSymbol())
				// .quality(Math.abs(position.getQty()))
				.urPnl(position.getUrPnL()).pnl(position.getPnL())
				.trader(position.getUser()).build();
		/*
		 * if (PriceUtils.isZero(position.getQty())) {
		 * positionModel.setType(RCPositionType.Close); } else {
		 * positionModel.setType(RCPositionType.Open); }
		 * positionModel.setAveragePrice(position.getPrice()); if
		 * (PriceUtils.GreaterThan(position.getQty(), 0)) {
		 * positionModel.setPositionDirection(RCPositionDirection.Long); } else
		 * { positionModel.setPositionDirection(RCPositionDirection.Short); }
		 */
		return positionModel;
	}

}
