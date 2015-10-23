package com.cyanspring.common.global;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class OpenPositionSummaryEvent extends RemoteAsyncEvent {
	private double buyPercent;
	private double sellPercent;
	private double buyAvgPrice;
	private double sellAvgPrice;
	private double buyVolume;
	private double sellVolume;
	
	public OpenPositionSummaryEvent(String key, String receiver, double buyPercent,
			double sellPercent, double buyAvgPrice, double sellAvgPrice,
			double buyVolume, double sellVolume) {
		super(key, receiver);
		this.buyPercent = buyPercent;
		this.sellPercent = sellPercent;
		this.buyAvgPrice = buyAvgPrice;
		this.sellAvgPrice = sellAvgPrice;
		this.buyVolume = buyVolume;
		this.sellVolume = sellVolume;
	}

	public double getBuyPercent() {
		return buyPercent;
	}

	public double getSellPercent() {
		return sellPercent;
	}

	public double getBuyAvgPrice() {
		return buyAvgPrice;
	}

	public double getSellAvgPrice() {
		return sellAvgPrice;
	}

	public double getBuyVolume() {
		return buyVolume;
	}

	public double getSellVolume() {
		return sellVolume;
	}
}
