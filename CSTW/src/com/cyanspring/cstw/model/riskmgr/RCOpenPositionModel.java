package com.cyanspring.cstw.model.riskmgr;


/**
 * RISK CONTROL PositionModel
 * 
 * @author Yu-Junfeng
 * @create 30 Jul 2015
 */
public class RCOpenPositionModel {

	// id
	private String id;

	// 交易账号
	private String subAccount;

	// 证券代码
	private String instrumentCode;

	// 证券名称
	private String instrumentName;

	// 仓位方向
	private RCPositionDirection positionDirection;

	// 当前仓位股数
	private double instrumentQuality;

	// 当前浮盈
	private double urPnl;
	
	// 实现盈利
	private double pnl;

	// 平均价格
	private double averagePrice;

	// 交易员
	private String trader;

	// OPEN / CLOSE
	private RCPositionType type;
	
	// define OPEN / CLOSE type for RCOpenPositionModel
	public enum RCPositionType {
		Open("Open"),
		Close("Close");
		
		private String val;
		RCPositionType(String val) {
			this.val = val;
		}
		@Override
		public String toString() {
			return val;
		}
	}
	
	//define LONG / SHORT type
	public enum RCPositionDirection {
		Long("LONG"),
		Short("SHORT");
		
		private String val;
		RCPositionDirection(String val) {
			this.val = val;
		}
		@Override
		public String toString() {
			return val;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubAccount() {
		return subAccount;
	}

	public void setSubAccount(String subAccount) {
		this.subAccount = subAccount;
	}

	public String getInstrumentCode() {
		return instrumentCode;
	}

	public void setInstrumentCode(String instrumentCode) {
		this.instrumentCode = instrumentCode;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public RCPositionDirection getPositionDirection() {
		return positionDirection;
	}

	public void setPositionDirection(RCPositionDirection positionDirection) {
		this.positionDirection = positionDirection;
	}

	public double getInstrumentQuality() {
		return instrumentQuality;
	}

	public void setInstrumentQuality(double instrumentQuality) {
		this.instrumentQuality = instrumentQuality;
	}

	public double getUrPnl() {
		return urPnl;
	}

	public void setUrPnl(double urPnl) {
		this.urPnl = urPnl;
	}
	
	public double getPnl() {
		return pnl;
	}
	
	public void setPnl(double pnl) {
		this.pnl = pnl;
	}

	public double getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(double averagePrice) {
		this.averagePrice = averagePrice;
	}

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}

	public RCPositionType getType() {
		return type;
	}

	public void setType(RCPositionType type) {
		this.type = type;
	}

	public static class Builder {
		private String id;
		private String subAccount;
		private String instrumentCode;
		private String instrumentName;
		private RCPositionDirection positionDirection;
		private double instrumentQuality;
		private double urPnl;
		private double pnl;
		private double averagePrice;
		private String trader;
		private RCPositionType type;

		public Builder() {

		}

		public Builder id(String val) {
			id = val;
			return this;
		}

		public Builder subAccount(String val) {
			subAccount = val;
			return this;
		}

		public Builder instrumentCode(String val) {
			instrumentCode = val;
			return this;
		}

		public Builder instrumentName(String val) {
			instrumentName = val;
			return this;
		}

		public Builder direction(RCPositionDirection val) {
			positionDirection = val;
			return this;
		}

		public Builder quality(double val) {
			instrumentQuality = val;
			return this;
		}

		public Builder urPnl(double val) {
			urPnl = val;
			return this;
		}
		
		public Builder pnl(double val) {
			pnl = val;
			return this;
		}

		public Builder avgPrice(double val) {
			averagePrice = val;
			return this;
		}

		public Builder trader(String val) {
			trader = val;
			return this;
		}
		
		public Builder type(RCPositionType val) {
			type = val;
			return this;
		}

		public RCOpenPositionModel build() {
			return new RCOpenPositionModel(this);
		}
	}

	private RCOpenPositionModel(Builder builder) {
		id = builder.id;
		subAccount = builder.subAccount;
		instrumentCode = builder.instrumentCode;
		instrumentName = builder.instrumentName;
		positionDirection = builder.positionDirection;
		instrumentQuality = builder.instrumentQuality;
		urPnl = builder.urPnl;
		pnl = builder.pnl;
		averagePrice = builder.averagePrice;
		trader = builder.trader;
		type = builder.type;
	}

}
