package com.cyanspring.avro;

import java.text.SimpleDateFormat;

import org.junit.Test;

import com.cyanspring.avro.generate.trade.bean.NewOrderRequest;
import com.cyanspring.avro.wrap.WrapObjectType;
import com.cyanspring.avro.wrap.WrapOrderSide;
import com.cyanspring.avro.wrap.WrapOrderType;
import com.cyanspring.avro.wrap.WrapTimeInForce;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.type.TimeInForce;

public class AvroSerializationTest {

	@Test
	public void test() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");

		AvroSerialization avroSerialization = new AvroSerialization();

		String exchangeAccount = "99995555";
		
		String orderID = "A0001";
		String clOrderId = "X0001";
		
		ChildOrder childOrder = getChildOrder();

		NewOrderRequest newOrderRequest = NewOrderRequest
				.newBuilder()
				.setOrderId(orderID)
				.setExchangeAccount(exchangeAccount)
				.setObjectType(WrapObjectType.NewOrderRequest.getCode())
				.setSymbol(childOrder.getSymbol())
				.setPrice(childOrder.getPrice())
				.setQuantity(childOrder.getQuantity())
				.setOrderSide(
						WrapOrderSide.valueOf(childOrder.getSide()).getCode())
				.setOrderType(WrapOrderType.valueOf(OrderType.Market).getCode())
				.setTimeInForce(WrapTimeInForce.valueOf(TimeInForce.DAY).getCode())
				.setAccount(childOrder.getAccount())
				.setTxId(childOrder.getId())
				.setCreated(dateFormat.format(childOrder.getCreated()))
				.setClOrderId(clOrderId)
				.build();
		
		System.out.println("Serialize NewOrderRequest : " + newOrderRequest.toString());
		
		try {
			byte[] bytes = (byte[]) avroSerialization.serialize(new AvroSerializableObject(newOrderRequest, WrapObjectType.NewOrderRequest));
			AvroSerializableObject deSerializeObject = (AvroSerializableObject) avroSerialization.deSerialize(bytes);
			WrapObjectType objectType = deSerializeObject.getObjectType();
			if(objectType.equals(WrapObjectType.NewOrderRequest)){
				NewOrderRequest deSerializeRecord = (NewOrderRequest) deSerializeObject.getRecord();
				System.out.println("DeSerializeRecord : " + deSerializeRecord.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

	}

	private ChildOrder getChildOrder() {

		// Set ParentOrder Symbol
		String symbol = "000002";

		// Set ParentOrder OrderSide
		OrderSide side = OrderSide.Buy;

		// Set ParentOrder Quantity
		Double quantity = 500d;

		// Set ParentOrder Price
		Double price = 15d;

		// Set ParentOrder OrderType
		OrderType orderType = OrderType.Limit;

		// init ParentOrder
		ParentOrder parentOrder = new ParentOrder(symbol, side, quantity,
				price, orderType);

		// Set ChildOrder exchangeOrderType
		ExchangeOrderType exchangeOrderType = ExchangeOrderType.MARKET;

		ChildOrder order = parentOrder.createChild(parentOrder.getQuantity(),
				parentOrder.getPrice(), exchangeOrderType);
		
		order.setAccount("acc1");
		return order;
	}

}
