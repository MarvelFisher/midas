package com.cyanspring.cstw.ui.trader.composite.speeddepth.model;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthModel {

	public static final int ASK = 0;

	public static final int BID = 1;

	private double vol = 0;

	private double price = 0;

	private String formatPrice;

	private int type;

	private double askQty;

	private double bidQty;

	private String symbol;

	private boolean isLastPrice;

	private int index;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getVol() {
		return vol;
	}

	public void setVol(double vol) {
		this.vol = vol;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getAskQty() {
		return askQty;
	}

	public void setAskQty(double askQty) {
		this.askQty = askQty;
	}

	public double getBidQty() {
		return bidQty;
	}

	public void setBidQty(double bidQty) {
		this.bidQty = bidQty;
	}

	public boolean isLastPrice() {
		return isLastPrice;
	}

	public void setLastPrice(boolean isLastPrice) {
		this.isLastPrice = isLastPrice;
	}

	public String getFormatPrice() {
		return formatPrice;
	}

	public void setFormatPrice(String formatPrice) {
		this.formatPrice = formatPrice;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
