package com.fdt.lts.client.error;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.common.type.OrderType;

public class OrderChecker {
	public static boolean checkNewOrder(Order order){
		if(!checkWriteSpaceAndNull(order.getSymbol()))
			return false;
		if(order.getSide() == null)
			return false;
		if(order.getType() == null)
			return false;
		if(!checkDouble(order.getPrice()) && order.getType().equals(OrderType.Limit))
			return false;
		if(!checkDouble(new Double(order.getQuantity())))
			return false;
		return true;
	}
	
	public static boolean checkAmendOrder(Order order){
		if(!checkWriteSpaceAndNull(order.getId()))
			return false;
		if(!checkDouble(order.getPrice()))
			return false;
		if(!checkDouble(new Double(order.getQuantity())))
			return false;
		return true;
	}
	
	public static boolean checkCancelOrder(Order order){
		if(!checkWriteSpaceAndNull(order.getId()))
			return false;
		return true;
	}
	
	public static boolean checkStopOrder(Order order){
		if(!checkWriteSpaceAndNull(order.getSymbol()))
			return false;
		if(order.getSide() == null)
			return false;
		if(order.getType() == null)
			return false;
		if(!checkDouble(order.getStopLossPrice()))
			return false;
		if(!checkDouble(new Double(order.getQuantity())))
			return false;
		return true;
	}
	
	public static boolean checkWriteSpaceAndNull(String str){
		if(str == null || str.trim() == "")
			return false;
		return true;
	}
	
	public static boolean checkDouble(double price){
		if(price > 0)
			return true;
		return false;
	}
	
}

