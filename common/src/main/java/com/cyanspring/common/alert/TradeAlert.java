package com.cyanspring.common.alert;

import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.util.IdGenerator;
public class TradeAlert implements Comparable<TradeAlert>{
		private String id;
		private String userId;
		private String symbol;
		private double price;
        private String Commdity;
        private double quantity ;
		private OrderReason orderReason;
		private String dateTime ;
		private String content ;
		
		public TradeAlert(String userId, String symbol,OrderReason orderReason, double quantity, double price, String dateTime, String content) {
			super();
			this.id = "A" + IdGenerator.getInstance().getNextID();
			this.setUserId(userId); //david
			this.setSymbol(symbol);  //USDJPY
			this.setOrderReason(orderReason) ;
			this.setQuantity(quantity); //-1000000
			this.setPrice(price); // 118.153
			this.setDateTime(dateTime); // yyyy-mm-dd hh:mm:ss
			this.content = content ;
		}

        public TradeAlert(String userId, String symbol,OrderReason orderReason, double quantity, double price, String dateTime, String content, String commdity) {
            super();
            this.id = "A" + IdGenerator.getInstance().getNextID();
            this.setUserId(userId); //david
            this.setSymbol(symbol);  //USDJPY
            this.setOrderReason(orderReason) ;
            this.setQuantity(quantity); //-1000000
            this.setPrice(price); // 118.153
            this.setDateTime(dateTime); // yyyy-mm-dd hh:mm:ss
            this.setCommdity(commdity);
            this.content = content ;
        }

		public TradeAlert()
		{
			super();
		}
		
		@Override
		public int compareTo(TradeAlert other) {
			int iReturn = this.getId().compareTo(other.getId());
			if (iReturn == 0)
			{
				iReturn = this.getUserId().compareTo(other.getUserId());
			}
			return iReturn;
		}
		
		@Override
		public boolean equals(Object obj){		
			return this.getId().equals(((TradeAlert)obj).getId());
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

		public void setUserId(String userId) {
			this.userId = userId;
		}
		
		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		
		public OrderReason getOrderReason()
		{
			return orderReason ;
		}
		
		public void setOrderReason(OrderReason orderReason) {
			this.orderReason = orderReason;
		}
		
		public double getQuantity()
		{
			return quantity ;
		}
		
		public void setQuantity(double quantity) {
			this.quantity = quantity;
		}
		
		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}		
		
		public void setDateTime(String dateTime) {
			this.dateTime = dateTime;
		}
		
		public String getContent()
		{
			return content ;
		}
		
		public void setContent(String content)
		{
			this.content = content ;
		}

		public String getDateTime() {
			return dateTime;
		}

        public String getCommdity() {
            return Commdity;
        }

        public void setCommdity(String commdity) {
            Commdity = commdity;
        }

}
