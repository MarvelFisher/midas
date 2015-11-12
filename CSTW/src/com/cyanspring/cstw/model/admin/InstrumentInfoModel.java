package com.cyanspring.cstw.model.admin;

import com.cyanspring.cstw.model.BasicModel;

/**
 * @author Junfeng
 * @create 12 Nov 2015
 */
public class InstrumentInfoModel extends BasicModel {
	
	private String symbolId;

	private String symbolName;

	private double qty;

	public String getSymbolId() {
		return symbolId;
	}

	public void setSymbolId(String symbolId) {
		this.symbolId = symbolId;
	}

	public double getStockQuanity() {
		return qty;
	}

	public void setStockQuanity(double qty) {
		this.qty = qty;
	}

	public String getSymbolName() {
		return symbolName;
	}

	public void setSymbolName(String symbolName) {
		this.symbolName = symbolName;
	}
	
	public static class Builder{
		private String symbolId;
		private String symbolName;
		private double qty;
		
		public Builder symbolId(String val) {
			symbolId = val;		return this;
		}
		
		public Builder symbolName(String val) {
			symbolName = val;		return this;
		}
		
		public Builder qty(double val) {
			qty = val;		return this;
		}
		
		public InstrumentInfoModel build() {
			return new InstrumentInfoModel(this);
		}
	}
	
	private InstrumentInfoModel(Builder builder) {
		symbolId = builder.symbolId;
		symbolName = builder.symbolName;
		qty = builder.qty;
	}
}
