package com.cyanspring.cstw.ui.trader.composite.speeddepth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.cstw.tick.Ticker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.ui.trader.composite.speeddepth.model.SpeedDepthModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/09
 *
 */
public final class SpeedDepthService {

	private List<SpeedDepthModel> currentList;

	private List<SpeedDepthModel> existedList;

	private ITickTable tickTable;

	private Ticker ticker;

	private double middlePrice;

	private double lastPrice;

	private double delta = 0.000001;

	private int rowLength = 10;

	public List<SpeedDepthModel> getSpeedDepthList(Quote quote, boolean isLock) {
		ticker = Business.getBusinessService().getTicker(quote.getSymbol());
		if (ticker == null) {
			return null;
		} else {
			tickTable = ticker.getTickTable();
		}

		lastPrice = quote.getLast();

		List<SpeedDepthModel> list = new ArrayList<SpeedDepthModel>();
		if (quote.getAsks() != null) {
			int askSize = quote.getAsks().size();
			for (int i = askSize - 1; i >= 0; i--) {
				if (i < rowLength) {
					SpeedDepthModel model = new SpeedDepthModel();
					QtyPrice qp = quote.getAsks().get(i);
					model.setSymbol(quote.getSymbol());
					model.setPrice(qp.price);
					if (i < 5) {
						model.setVol(qp.quantity);
					}
					model.setType(SpeedDepthModel.ASK);
					list.add(model);
				}
			}
		}

		if (quote.getBids() != null) {
			for (int i = 0; i < quote.getBids().size() && i < rowLength; i++) {
				SpeedDepthModel model = new SpeedDepthModel();
				model.setSymbol(quote.getSymbol());
				QtyPrice qp = quote.getBids().get(i);
				if (i == 0) {
					middlePrice = qp.price;
				}
				model.setPrice(qp.price);
				if (i < 5) {
					model.setVol(qp.quantity);
				}
				model.setType(SpeedDepthModel.BID);
				list.add(model);
			}
		}

		currentList = list;

		refreshByTick(quote.getSymbol());

		if (isLock) {
			if (existedList == null) {
				existedList = currentList;
			} else if (existedList != null) {
				combineExistedListByPrice(currentList);
			}
		} else {
			existedList = null;
		}

		refreshCurrentListByExistedOrder();

		return currentList;
	}

	private void refreshByTick(String symbol) {
		List<SpeedDepthModel> newList = new ArrayList<SpeedDepthModel>();
		// add 10 UP Price
		double currentPrice = 0;
		for (int i = 0; i < rowLength; i++) {
			SpeedDepthModel model = new SpeedDepthModel();
			model.setSymbol(symbol);
			if (i == 0) {
				currentPrice = middlePrice;
			} else {
				currentPrice = currentPrice + tickTable.getTick(currentPrice);
			}
			model.setPrice(currentPrice);
			model.setFormatPrice(ticker.formatPrice(currentPrice));
			combineValueByCurrentList(model);
			newList.add(0, model);
		}
		// add 10 DOWN Price
		currentPrice = middlePrice;
		for (int i = 0; i < rowLength; i++) {
			SpeedDepthModel model = new SpeedDepthModel();
			model.setSymbol(symbol);
			currentPrice = currentPrice - tickTable.getTick(currentPrice);
			model.setPrice(currentPrice);
			model.setFormatPrice(ticker.formatPrice(currentPrice));
			combineValueByCurrentList(model);
			newList.add(model);
		}
		currentList = newList;
	}

	private void combineValueByCurrentList(SpeedDepthModel model) {
		for (SpeedDepthModel currentModel : currentList) {
			if (PriceUtils.Equal(currentModel.getPrice(), model.getPrice(),
					delta)) {
				model.setType(currentModel.getType());
				model.setVol(currentModel.getVol());
			}
		}
	}

	private void combineExistedListByPrice(List<SpeedDepthModel> list) {
		// clear data
		for (SpeedDepthModel existedModel : existedList) {
			existedModel.setVol(0);
			existedModel.setLastPrice(false);
		}
		for (SpeedDepthModel model : list) {
			for (SpeedDepthModel currentModel : existedList) {
				if (PriceUtils.Equal(currentModel.getPrice(), model.getPrice(),
						delta)) {
					currentModel.setType(model.getType());
					currentModel.setVol(model.getVol());
				}
			}
		}
		currentList = existedList;
	}

	// Always run in last time
	private void refreshCurrentListByExistedOrder() {
		int i = 0;
		for (SpeedDepthModel currentModel : currentList) {
			if (PriceUtils.Equal(lastPrice, currentModel.getPrice(), delta)) {
				currentModel.setLastPrice(true);
			}
			currentModel.setAskQty(0);
			currentModel.setBidQty(0);
			currentModel.setStopAskQty(0);
			currentModel.setStopBidQty(0);
			currentModel.setIndex(i++);
		}
		List<Map<String, Object>> orders = Business.getInstance()
				.getOrderManager().getParentOrders();
		for (Map<String, Object> map : orders) {
			OrdStatus status = (OrdStatus) map.get("Status");
			if (!status.isCompleted()) {
				for (SpeedDepthModel currentModel : currentList) {
					if (map.get("Price") != null) {
						caculateQtyByPrice(currentModel, map);
					} else if (map.get("Stop Loss") != null) {
						caculateQtyByStopLoss(currentModel, map);
					}
				}
			}
		}
	}

	private void caculateQtyByPrice(SpeedDepthModel model,
			Map<String, Object> map) {
		String symbol = (String) map.get("Symbol");
		double price = (Double) map.get("Price");
		if (model.getSymbol().equals(symbol)
				&& PriceUtils.Equal(price, model.getPrice(), delta)) {
			OrderSide side = (OrderSide) map.get("Side");
			double cumQty = (Double) map.get("Qty");
			if (side.isBuy()) {
				model.setAskQty(model.getAskQty() + cumQty);
			} else if (side.isSell()) {
				model.setBidQty(model.getBidQty() + cumQty);
			}
		}
	}

	private void caculateQtyByStopLoss(SpeedDepthModel model,
			Map<String, Object> map) {
		String symbol = (String) map.get("Symbol");
		double price = (Double) map.get("Stop Loss");
		if (model.getSymbol().equals(symbol)
				&& PriceUtils.Equal(price, model.getPrice(), delta)) {
			OrderSide side = (OrderSide) map.get("Side");
			double cumQty = (Double) map.get("Qty");
			if (side.isBuy()) {
				model.setStopAskQty(model.getStopAskQty() + cumQty);
			} else if (side.isSell()) {
				model.setStopBidQty(model.getStopBidQty() + cumQty);
			}
		}
	}

	public void setRowLength(int rowLength) {
		this.rowLength = rowLength / 2;
	}

}
