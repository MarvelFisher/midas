package com.cyanspring.cstw.service.helper.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.pool.ExchangeAccount;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel.RCPositionDirection;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel.RCPositionType;
import com.cyanspring.cstw.model.riskmgr.RCOrderRecordModel;
import com.cyanspring.cstw.model.riskmgr.RCOrderRecordModel.Builder;
import com.cyanspring.cstw.model.riskmgr.RCTradeRecordModel;
import com.cyanspring.cstw.model.riskmgr.RCUserStatisticsModel;

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
				.quality(Math.abs(position.getQty()))
				.urPnl(position.getUrPnL()).pnl(position.getRealizedPnL())
				.trader(position.getAccount()).build();

		if (PriceUtils.isZero(position.getQty())) {
			positionModel.setType(RCPositionType.Close);
		} else {
			positionModel.setType(RCPositionType.Open);
		}
		positionModel.setAveragePrice(position.getAvgPrice());
		if (PriceUtils.GreaterThan(position.getQty(), 0)) {
			positionModel.setPositionDirection(RCPositionDirection.Long);
		} else {
			positionModel.setPositionDirection(RCPositionDirection.Short);
		}

		return positionModel;
	}

	/**
	 * 风控管理 - 股票统计信息转换(包含已平仓和未平仓)
	 * 
	 * @param position
	 * @param refDataManager
	 * @return
	 */
	public static RCInstrumentModel parseStockStatisticModel(
			OverallPosition position) {
		if (PriceUtils.isZero(position.getTotalQty())) {
			return null;
		}
		RCInstrumentModel model = new RCInstrumentModel.Builder()
				.account(position.getExchangeSubAccount())
				.symbol(position.getSymbol())
				.realizedProfit(position.getRealizedPnL())
				.trades(position.getExecCount()).volume(position.getTotalQty())
				.turnover(position.getTurnover())
				.commission(position.getCommission())
				.trader(position.getAccount()).build();
		return model;
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
		double totalQty = 0.0;
		for (OverallPosition position : positionMap.values()) {
			if (trader == null) {
				trader = position.getAccount();
			}
			pnl += position.getRealizedPnL();
			totalTurnOver += position.getTurnover();
			totalQty += position.getTotalQty();
		}
		if (PriceUtils.isZero(totalQty)) {
			return null;
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
			builder.side(parentOrder.getSide().name());
			builder.orderStatus(parentOrder.getOrdStatus().name());
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

	/**
	 * 风控管理 - 股票汇总信息 - 包含已平仓和未平仓
	 * 
	 * @param positionList
	 * @param refDataManager
	 * @return
	 */
	public static List<RCInstrumentModel> parseStockSummaryRecordList(
			List<OverallPosition> positionList) {
		List<RCInstrumentModel> stockRecordList = new ArrayList<RCInstrumentModel>();
		Map<String, Map<String, List<OverallPosition>>> subAccountSybmolRecordMap = new HashMap<String, Map<String, List<OverallPosition>>>();
		Map<String, List<OverallPosition>> symbolRecordMap = null;
		String subAccount = null;
		String symbol = null;
		for (OverallPosition position : positionList) {
			if (PriceUtils.isZero(position.getTotalQty())) {
				continue;
			}
			subAccount = position.getExchangeSubAccount();
			symbol = position.getSymbol();
			if (subAccountSybmolRecordMap.containsKey(subAccount)) {
				symbolRecordMap = subAccountSybmolRecordMap.get(subAccount);
			} else {
				symbolRecordMap = new HashMap<String, List<OverallPosition>>();
				subAccountSybmolRecordMap.put(subAccount, symbolRecordMap);
			}
			if (!symbolRecordMap.containsKey(symbol)) {
				symbolRecordMap.put(symbol, new ArrayList<OverallPosition>());
			}
			symbolRecordMap.get(symbol).add(position);
		}

		RCInstrumentModel stockRecord = null;
		for (Entry<String, Map<String, List<OverallPosition>>> subAccountEntry : subAccountSybmolRecordMap
				.entrySet()) {
			for (Entry<String, List<OverallPosition>> symbolEntry : subAccountEntry
					.getValue().entrySet()) {
				double pnl = 0.0;
				double tradeCount = 0.0;
				double volume = 0.0;
				double totalTurnOver = 0.0;
				double commission = 0.0;
				symbol = symbolEntry.getKey();
				for (OverallPosition position : symbolEntry.getValue()) {
					pnl = pnl + position.getRealizedPnL();
					tradeCount = tradeCount + position.getExecCount();
					volume = volume + position.getTotalQty();
					totalTurnOver = totalTurnOver + position.getTurnover();
					commission = commission + position.getCommission();
				}
				stockRecord = new RCInstrumentModel.Builder()
						.account(subAccountEntry.getKey()).symbol(symbol)
						.realizedProfit(pnl).trades(tradeCount).volume(volume)
						.turnover(totalTurnOver).commission(commission).build();
				stockRecordList.add(stockRecord);
			}
		}
		return stockRecordList;
	}

	public static ExchangeAccountModel parseExchangeAccountModel(
			ExchangeAccount exchangeAccount) {
		ExchangeAccountModel model = new ExchangeAccountModel.Builder()
				.id(exchangeAccount.getId()).name(exchangeAccount.getName())
				.build();
		return model;
	}

}
