/**
 * 
 */
package com.cyanspring.cstw.service.model.riskmgr;

/**
 * @author Yu-Junfeng
 * @create 17 Aug 2015
 */
public class RCInstrumentModel {
	
	// 交易账号
	private String account;
	
	// 证券代码
	private String symbol;
	
	// 证券名称
	private String symbolName;
	
	// 已实现盈利
	private Double realizedProfit;
	
	// 交易笔数
	private Double trades;
	
	// 总交易股数
	private Double volume;
	
	// 总交易额
	private Double turnover;
	
	// 总交易费用
	private Double commission;
	
	// 交易员
	private String trader;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
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

	public Double getRealizedProfit() {
		return realizedProfit;
	}

	public void setRealizedProfit(double realizedProfit) {
		this.realizedProfit = realizedProfit;
	}

	public Double getTrades() {
		return trades;
	}

	public void setTrades(double trades) {
		this.trades = trades;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public Double getTurnover() {
		return turnover;
	}

	public void setTurnover(double turnover) {
		this.turnover = turnover;
	}

	public Double getCommission() {
		return commission;
	}

	public void setCommission(double commission) {
		this.commission = commission;
	}

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}
	
	public static class Builder {
		private String account;
		private String symbol;
		private String symbolName;
		private Double realizedProfit;
		private Double trades;
		private Double volume;
		private Double turnover;
		private Double commission;
		private String trader;
		
		public Builder() {
		}
		
		public Builder account(String val) {
			account = val;	return this;
		}
		
		public Builder symbol(String val) {
			symbol = val;	return this;
		}
		
		public Builder symbolName(String val) {
			symbolName = val;	return this;
		}
		
		public Builder realizedProfit(double val) {
			realizedProfit = val;	return this;
		}
		
		public Builder trades(double val) {
			trades = val; 	return this;
		}
		
		public Builder volume(double val) {
			volume = val;	return this;
		}
		
		public Builder turnover(double val) {
			turnover = val;		return this;
		}
		
		public Builder commission(double val) {
			commission = val;	return this;
		}
		
		public Builder trader(String val) {
			trader = val;	return this;
		}
		
		public RCInstrumentModel build() {
			return new RCInstrumentModel(this);
		}
		
	}
	
	private RCInstrumentModel(Builder builder) {
		account			= builder.account;
		symbol			= builder.symbol;
		symbolName		= builder.symbolName;
		realizedProfit	= builder.realizedProfit;
		trades			= builder.trades;
		volume			= builder.volume;
		turnover		= builder.turnover;
		commission		= builder.commission;
		trader			= builder.trader;
	}

}
