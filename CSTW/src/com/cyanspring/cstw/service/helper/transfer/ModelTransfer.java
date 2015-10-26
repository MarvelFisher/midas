package com.cyanspring.cstw.service.helper.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel;
import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel.Builder;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;
import com.cyanspring.cstw.service.model.riskmgr.RCUserStatisticsModel;

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

	/**
	 * 风控管理 - 个人统计信息转换(包含已平仓和未平仓)
	 * 
	 * @param positionMap
	 * @return
	 */
	public static RCUserStatisticsModel parseRCIndividualStatisticsModel(
			Map<String, OverallPosition> positionMap) {
		if (positionMap == null || positionMap.isEmpty()) {
			return null;
		}
		String trader = null;
		double pnl = 0.0;
		double totalTurnOver = 0.0;
		for (OverallPosition position : positionMap.values()) {
			if (trader == null) {
				trader = position.getUser();
			}
			pnl += position.getPnL();
			totalTurnOver += position.getTurnover();
		}
		RCUserStatisticsModel model = new RCUserStatisticsModel.Builder()
				.trader(trader).realizedProfit(pnl).turnover(totalTurnOver)
				.build();
		return model;
	}

	/**
	 * 风控管理-交易记录(成交/未成交)
	 * 
	 * @param orderList
	 * @param refDataKeeper
	 * @return
	 */
	public static List<RCOrderRecordModel> parseOrderRecordList(
			List<ParentOrder> parentOrderList) {
		List<RCOrderRecordModel> orderList = new ArrayList<RCOrderRecordModel>();
		Builder builder = null;
		for (ParentOrder parentOrder : parentOrderList) {
			builder = new RCOrderRecordModel.Builder();
			builder.orderId(parentOrder.getId());
			builder.pending(!parentOrder.getOrdStatus().isCompleted());
			builder.complete(parentOrder.getOrdStatus() == OrdStatus.FILLED);
			builder.symbol(parentOrder.getSymbol());
			builder.cumQty(parentOrder.getCumQty());
			builder.price(parentOrder.getPrice());
			builder.volume(parentOrder.getQuantity());
			builder.createTime(parentOrder.getModified().toString());
			builder.trader(parentOrder.getUser());
			builder.executionFee(0);
			orderList.add(builder.build());
		}
		return orderList;
	}

}
