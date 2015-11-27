package com.cyanspring.cstw.service.impl.trader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.CustomOrderType;
import com.cyanspring.cstw.model.trader.ParentOrderModel;
import com.cyanspring.cstw.service.iservice.trader.IParentOrderService;
import com.cyanspring.cstw.session.CSTWSession;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/11/17
 *
 */
public final class ParentOrderServiceImpl implements IParentOrderService {

	private static final Logger log = LoggerFactory
			.getLogger(ParentOrderServiceImpl.class);

	private double delta = 0.000001;

	@Override
	public void quickEnterOrder(ParentOrderModel model, CustomOrderType type) {
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.USER.value(), CSTWSession.getInstance()
				.getUserId());
		fields.put(OrderField.ACCOUNT.value(), CSTWSession.getInstance()
				.getAccountId());
		fields.put(OrderField.SYMBOL.value(), model.getSymbol());
		fields.put(OrderField.SIDE.value(), model.getSide());
		fields.put(OrderField.QUANTITY.value(), model.getQuantity());
		if (type == CustomOrderType.Stop) {
			fields.put(OrderField.STRATEGY.value(), "STOP");
			fields.put(OrderField.STOP_LOSS_PRICE.value(), model.getPrice());
			fields.put(OrderField.TYPE.value(), CustomOrderType.Market.name());
		} else {
			fields.put(OrderField.STRATEGY.value(), "SDMA");
			fields.put(OrderField.PRICE.value(), model.getPrice());
			fields.put(OrderField.TYPE.value(), type.name());
		}
		EnterParentOrderEvent event = new EnterParentOrderEvent(CSTWSession
				.getInstance().getInbox(), Business.getBusinessService()
				.getFirstServer(), fields, model.getReceiverId(), false);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	public void cancelAllOrder(String currentSymbol) {
		List<Map<String, Object>> orders = Business.getInstance()
				.getOrderManager().getParentOrders();
		for (Map<String, Object> map : orders) {
			String symbol = (String) map.get(OrderField.SYMBOL.value());
			String id = (String) map.get(OrderField.ID.value());
			OrdStatus status = (OrdStatus) map
					.get(OrderField.ORDSTATUS.value());
			if (!status.isCompleted() && symbol.equals(currentSymbol)) {
				CancelParentOrderEvent event = new CancelParentOrderEvent(id,
						Business.getBusinessService().getFirstServer(), id, false,
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

	public void cancelOrder(String currentSymbol, Double cancelPrice,
			CustomOrderType type) {
		List<Map<String, Object>> orders = Business.getInstance()
				.getOrderManager().getParentOrders();
		for (Map<String, Object> map : orders) {
			String symbol = (String) map.get(OrderField.SYMBOL.value());
			String id = (String) map.get(OrderField.ID.value());
			OrdStatus status = (OrdStatus) map
					.get(OrderField.ORDSTATUS.value());
			boolean isPriceEqual;
			Double orderPrice;
			if (type == CustomOrderType.Stop) {
				orderPrice = (Double) map.get(OrderField.STOP_LOSS_PRICE
						.value());
			} else {
				orderPrice = (Double) map.get(OrderField.PRICE.value());
			}
			if (orderPrice == null) {
				isPriceEqual = false;
			} else if (cancelPrice == null) {
				isPriceEqual = true;
			} else {
				isPriceEqual = PriceUtils.Equal(orderPrice, cancelPrice, delta);
			}
			if (!status.isCompleted() && symbol.equals(currentSymbol)
					&& isPriceEqual) {
				CancelParentOrderEvent event = new CancelParentOrderEvent(id,
						Business.getBusinessService().getFirstServer(), id, false,
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

}
