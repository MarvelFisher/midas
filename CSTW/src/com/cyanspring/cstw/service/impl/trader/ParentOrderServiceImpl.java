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

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/11/17
 *
 */
public class ParentOrderServiceImpl implements IParentOrderService {

	private static final Logger log = LoggerFactory
			.getLogger(ParentOrderServiceImpl.class);

	private double delta = 0.000001;

	@Override
	public void quickEnterOrder(ParentOrderModel model, CustomOrderType type) {
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.USER.value(), Business.getInstance().getUser());
		fields.put(OrderField.ACCOUNT.value(), Business.getInstance()
				.getAccount());
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
		EnterParentOrderEvent event = new EnterParentOrderEvent(Business
				.getInstance().getInbox(), Business.getInstance()
				.getFirstServer(), fields, model.getReceiverId(), false);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	public void cancelOrder(String currentSymbol) {
		cancelOrder(currentSymbol, null);
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
				if (null == orderPrice)
					continue;

				isPriceEqual = PriceUtils.Equal(orderPrice, price, delta);
			}
			if (!status.isCompleted() && symbol.equals(currentSymbol)
					&& isPriceEqual) {
				if (null != symbol && id != null) {
					log.info("ParentOrderService cancelOrder:{},{}", symbol, id);
				}
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

}
