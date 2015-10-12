package com.cyanspring.cstw.trader.gui.composite.speeddepth.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.model.SpeedDepthModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/09
 *
 */
public final class SpeedDepthService {

	private static final Logger log = LoggerFactory
			.getLogger(SpeedDepthService.class);

	private List<SpeedDepthModel> currentList;

	private double tick;

	private double middleValue;

	public void setTick(double tick) {
		this.tick = tick;
	}

	public List<SpeedDepthModel> getSpeedDepthList(Quote quote, boolean isLock) {
		List<SpeedDepthModel> list = new ArrayList<SpeedDepthModel>();

		if (quote.getAsks() != null) {
			int askSize = quote.getAsks().size();
			for (int i = askSize - 1; i >= 0; i--) {
				if (i < 10) {
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
			for (int i = 0; i < quote.getBids().size() && i < 10; i++) {
				SpeedDepthModel model = new SpeedDepthModel();
				model.setSymbol(quote.getSymbol());
				QtyPrice qp = quote.getBids().get(i);
				if (i == 0) {
					middleValue = qp.price;
				}
				model.setPrice(qp.price);
				if (i < 5) {
					model.setVol(qp.quantity);
				}
				model.setType(SpeedDepthModel.BID);
				list.add(model);
			}
		}

		if (isLock && currentList != null) {
			combineListByPrice(list);
		} else {
			currentList = list;
			refreshByTick(quote.getSymbol());
		}

		refreshByCurrentOrder();

		return currentList;
	}

	private void combineListByPrice(List<SpeedDepthModel> list) {
		for (SpeedDepthModel currentModel : currentList) {
			currentModel.setVol(0);
		}
		for (SpeedDepthModel model : list) {
			for (SpeedDepthModel currentModel : currentList) {
				if (PriceUtils.Equal(currentModel.getPrice(), model.getPrice())) {
					currentModel.setType(model.getType());
					currentModel.setVol(model.getVol());
				}
			}
		}
	}

	private void refreshByTick(String symbol) {
		List<SpeedDepthModel> list = new ArrayList<SpeedDepthModel>();
		for (int i = 10; i > -10; i--) {
			SpeedDepthModel model = new SpeedDepthModel();
			model.setSymbol(symbol);
			model.setPrice(middleValue + i * tick);
			for (SpeedDepthModel currentModel : currentList) {
				if (PriceUtils.Equal(currentModel.getPrice(), model.getPrice())) {
					model.setType(currentModel.getType());
					model.setVol(currentModel.getVol());
				}
			}
			list.add(model);
		}
		currentList = list;
	}

	private void refreshByCurrentOrder() {
		for (SpeedDepthModel currentModel : currentList) {
			currentModel.setAskQty(0);
			currentModel.setBidQty(0);
		}
		List<Map<String, Object>> orders = Business.getInstance()
				.getOrderManager().getParentOrders();
		for (Map<String, Object> map : orders) {
			OrdStatus status = (OrdStatus) map.get("Status");
			if (!status.isCompleted()) {
				for (SpeedDepthModel currentModel : currentList) {
					String symbol = (String) map.get("Symbol");
					double price = (Double) map.get("Price");
					if (currentModel.getSymbol().equals(symbol)
							&& PriceUtils.Equal(price, currentModel.getPrice())) {
						OrderSide side = (OrderSide) map.get("Side");
						double cumQty = (Double) map.get("Qty");
						if (side.isBuy()) {
							currentModel.setAskQty(currentModel.getAskQty()
									+ cumQty);
						} else if (side.isSell()) {
							currentModel.setBidQty(currentModel.getBidQty()
									+ cumQty);
						}
					}
				}
			}
		}
	}

	public void quickEnterOrder(SpeedDepthModel model, String side,
			String quantity) {
		HashMap<String, Object> fields = new HashMap<String, Object>();

		fields.put(OrderField.SYMBOL.value(), model.getSymbol());
		fields.put(OrderField.SIDE.value(), side);
		fields.put(OrderField.TYPE.value(), "Limit");
		fields.put(OrderField.QUANTITY.value(), quantity);
		fields.put(OrderField.PRICE.value(), model.getPrice());
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		fields.put(OrderField.USER.value(), Business.getInstance().getUser());
		fields.put(OrderField.ACCOUNT.value(), Business.getInstance()
				.getAccount());

		EnterParentOrderEvent event = new EnterParentOrderEvent(Business
				.getInstance().getInbox(), Business.getInstance()
				.getFirstServer(), fields, null, false);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void cancelOrder(String currentSymbol, Double price) {
		List<Map<String, Object>> orders = Business.getInstance()
				.getOrderManager().getParentOrders();
		for (Map<String, Object> map : orders) {
			String symbol = (String) map.get(OrderField.SYMBOL.value());
			String id = (String) map.get(OrderField.ID.value());
			OrdStatus status = (OrdStatus) map
					.get(OrderField.ORDSTATUS.value());
			boolean isPriceEqual;
			if (price == null) {
				isPriceEqual = true;
			} else {
				Double orderPrice = (Double) map.get(OrderField.PRICE.value());
				isPriceEqual = PriceUtils.Equal(orderPrice, price);
			}
			if (!status.isCompleted() && symbol.equals(currentSymbol)
					&& isPriceEqual) {
				CancelParentOrderEvent event = new CancelParentOrderEvent(id,
						Business.getInstance().getFirstServer(), id, false,
						null, true);
				try {
					Business.getInstance().getEventManager()
							.sendRemoteEvent(event);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	public void cancelOrder(String currentSymbol) {
		cancelOrder(currentSymbol, null);
	}

}
