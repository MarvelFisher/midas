/**
 * 
 */
package com.cyanspring.cstw.model.riskmgr;

/**
 * @author Yu-Junfeng
 * @create 18 Aug 2015
 */
public final class RCUserStatisticsModel {

	// 交易員
	private String trader;

	// 已實現盈利
	private Double realizedProfit;

	// 總交易額
	private Double turnover;

	// 是否停機
	private boolean stop;

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}

	public Double getRealizedProfit() {
		return realizedProfit;
	}

	public void setRealizedProfit(double realizedProfit) {
		this.realizedProfit = realizedProfit;
	}

	public Double getTurnover() {
		return turnover;
	}

	public void setTurnover(double turnover) {
		this.turnover = turnover;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public static class Builder {
		private String trader;
		private Double realizedProfit;
		private Double turnover;
		private boolean stop;

		public Builder trader(String val) {
			trader = val;
			return this;
		}

		public Builder realizedProfit(double val) {
			realizedProfit = val;
			return this;
		}

		public Builder turnover(double val) {
			turnover = val;
			return this;
		}

		public Builder stop(boolean val) {
			stop = val;
			return this;
		}

		public RCUserStatisticsModel build() {
			return new RCUserStatisticsModel(this);
		}
	}

	private RCUserStatisticsModel(Builder builder) {
		trader = builder.trader;
		realizedProfit = builder.realizedProfit;
		turnover = builder.turnover;
		stop = builder.stop;
	}

}
