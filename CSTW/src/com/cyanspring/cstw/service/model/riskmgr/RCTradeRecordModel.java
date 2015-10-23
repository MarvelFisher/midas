/**
 * 
 */
package com.cyanspring.cstw.service.model.riskmgr;

/**
 * @author Yu-Junfeng
 * @create 10 Aug 2015
 */
public class RCTradeRecordModel {
	
	// 交易账号
	private String account;
	
	// 记录号
	private String record;
	
	// 证券代码
	private String symbol;
	
	// 证券名称
	private String symbolName;
	
	// 交易类型
	private String type;
	
	// 成交股数
	private Double volume;
	
	// 成交价格
	private Double price;
	
	// 成交金额
	private Double totalPrice;
	
	// 交易时间
	private String tradeTime;
	
	// 交易费用
	private Double executionRate;
	
	// 交易员
	private String trader;
		
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}

	public Double getExecutionRate() {
		return executionRate;
	}

	public void setExecutionRate(double executionRate) {
		this.executionRate = executionRate;
	}

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}
	
	public static class Builder {
		private String account;
		private String record;
		private String symbol;
		private String symbolName;
		private String type;
		private Double volume;
		private Double price;
		private Double totalPrice;
		private String tradeTime;
		private Double executionRate;
		private String trader;
		
		public Builder() {
		}
		
		public Builder account(String val) {
			account = val;	return this;
		}
		
		public Builder record(String val) {
			record = val;	return this;
		}
		
		public Builder symbol(String val) {
			symbol = val;	return this;
		}
		
		public Builder symbolName(String val) {
			symbolName = val; 	return this;
		}
		
		public Builder type(String val) {
			type = val;		return this;
		}
		
		public Builder volume(double val) {
			volume = val;	return this;
		}
		
		public Builder price(double val) {
			price = val; 	return this;
		}
		
		public Builder totalPrice(double val) {
			totalPrice = val; 	return this;
		}
		
		public Builder tradeTime(String val) {
			tradeTime = val; 	return this;
		}
		
		public Builder executionRate(double val) {
			executionRate = val;	return this;
		}
		
		public Builder trader(String val) {
			trader = val; 	return this;
		}
		
		public RCTradeRecordModel build() {
			return new RCTradeRecordModel(this);
		}
	}
	
	private RCTradeRecordModel(Builder builder) {
		account			 = builder.account;
		record			 = builder.record;
		symbol			 = builder.symbol;
		symbolName 		 = builder.symbolName;
		type			 = builder.type;
		volume			 = builder.volume;
		price			 = builder.price;
		totalPrice		 = builder.totalPrice;
		tradeTime 		 = builder.tradeTime;
		executionRate	 = builder.executionRate;
		trader			 = builder.trader;
	}


}
