package com.cyanspring.common.alert;

import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.util.IdGenerator;
public class TradeAlert implements Comparable<TradeAlert>{
		private String id;
		private String userId;
		private String symbol;
		private double price;
		private long quantity ;
		private OrderReason orderReason;
		private String dateTime ;
		private String content ;
		
		public TradeAlert(String userId, String symbol,OrderReason orderReason, long quantity, double price, String dateTime, String content) {
			super();
			this.id = "A" + IdGenerator.getInstance().getNextID();
			this.userId = userId; //david
			this.symbol = symbol;  //USDJPY
			this.orderReason = orderReason ;
			this.quantity = quantity; //-1000000
			this.price = price; // 118.153
			this.dateTime = dateTime; // yyyy-mm-dd hh:mm:ss
			this.content = content ;
		}
		
		@Override
		public int compareTo(TradeAlert other) {
			int iReturn = this.userId.compareTo(other.userId);
			if (iReturn == 0)
			{
				iReturn = this.id.compareTo(other.id);
			}
			return iReturn;
		}
		
		public String getId() {
			return id;
		}
		
		public void setId(String strId){
			this.id = strId ;
		}

		public String getUserId() {
			return userId;
		}
		
		public String getSymbol() {
			return symbol;
		}
		
		public OrderReason getOrderReason()
		{
			return orderReason ;
		}
		public long getQuantity()
		{
			return quantity ;
		}

		public double getPrice() {
			return price;
		}
		
		public String getTime()
		{
			return dateTime ;
		}
		
		public String getContent()
		{
			return content ;
		}
		
		public void setContent(String content)
		{
			this.content = content ;
		}
}
