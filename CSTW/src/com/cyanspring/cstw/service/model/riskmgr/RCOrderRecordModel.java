/**
 * 
 */
package com.cyanspring.cstw.service.model.riskmgr;

/**
 * @author Yu-Junfeng
 * @create 24 Aug 2015
 */
public class RCOrderRecordModel {
	
	/**
	 * 是否终结
	 */
	private boolean isPending;
	
	/**
	 * 是否成交
	 */
	private boolean isComplete;
	
	/**
	 * 交易账号
	 */
	private String subAccount;
	
	/**
	 * 委托编号
	 */
	private String orderId;

	/**
	 * 证券代码
	 */
	private String symbol;

	/**
	 * 证券名称
	 */
	private String symbolName;

	/**
	 * 交易类型
	 */
	private String side;
	
	/**
	 * 价格
	 */
	private Double price;
	
	/**
	 *  委托数量
	 */
	private Double volume;

	/**
	 * 委托状态
	 */
	private String orderStatus;
	
	/**
	 * 已成交数量
	 */
	private Double cumQty;

	/**
	 * 时间
	 */
	private String createTime;

	/**
	 * 交易员
	 */
	private String trader;
	
	/**
	 * 交易费用
	 */
	private Double executionFee;
	
	public boolean isPending() {
		return isPending;
	}
	
	public void setPending(boolean isPending) {
		this.isPending = isPending;
	}
	
	public boolean isComplete() {
		return isComplete;
	}
	
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public String getSubAccount() {
		return subAccount;
	}

	public void setSubAccount(String subAccount) {
		this.subAccount = subAccount;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbolName() {
		return symbolName;
	}

	public void setSymbolName(String symbolName) {
		this.symbolName = symbolName;
	}

	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Double getCumQty() {
		return cumQty;
	}

	public void setCumQty(double cumQty) {
		this.cumQty = cumQty;
	}
	
	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}
	
	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}
	
	public Double getExecutionFee() {
		return executionFee;
	}
	
	public void setExecutionFee(double fee) {
		this.executionFee = fee;
	}

	public static class Builder {
		private boolean pending;
		private boolean complete;
		private String subAccount;
		private String orderId;
		private String symbol;
		private String symbolName;
		private String side;
		private Double price;
		private Double cumQty;
		private Double volume;
		private String orderStatus;
		private String createTime;
		private String trader;
		private Double executionFee;
		
		public Builder pending(boolean val) {
			pending = val;		return this;
		}
		
		public Builder complete(boolean val) {
			complete = val;		return this;
		}
		
		public Builder subAccount(String val) {
			subAccount = val;		return this;
		}
		
		public Builder orderId(String val) {
			orderId = val;		return this;
		}
		
		public Builder symbol(String val) {
			symbol = val;		return this;
		}
		
		public Builder symbolName(String val) {
			symbolName = val;		return this;
		}
		
		public Builder side(String val) {
			side = val;		return this;
		}
		
		public Builder price(double val) {
			price = val;		return this;
		}
		
		public Builder cumQty(double val) {
			cumQty = val;		return this;
		}
		
		public Builder volume(double val) {
			volume = val;		return this;
		}
		
		public Builder orderStatus(String val) {
			orderStatus = val;		return this;
		}
		
		public Builder createTime(String val) {
			createTime = val;		return this;
		}
		
		public Builder trader(String val) {
			trader = val;		return this;
		}
		
		public Builder executionFee(double val) {
			executionFee = val;		 return this;
		}
		
		public RCOrderRecordModel build() {
			return new RCOrderRecordModel(this);
		}
	}
	
	private RCOrderRecordModel(Builder builder) {
		isPending		 = builder.pending;
		isComplete		 = builder.complete;
		subAccount		 = builder.subAccount;
		orderId			 = builder.orderId;
		symbol			 = builder.symbol;
		symbolName		 = builder.symbolName;
		side			 = builder.side;
		price			 = builder.price;
		cumQty			 = builder.cumQty;
		volume			 = builder.volume;
		orderStatus		 = builder.orderStatus;
		createTime		 = builder.createTime;
		trader			 = builder.trader;
		executionFee	 = builder.executionFee;
	}

}
