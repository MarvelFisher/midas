package com.cyanspring.common.account;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Execution;

import java.util.Calendar;
import java.util.Date;

public class OpenPosition extends Position implements Cloneable {

	public static class AvailablePosition extends OpenPosition {

		private double availableQty;

		@Override
		public double getAvailableQty() {
			return this.availableQty;
		}

		public void setAvailableQty(double availableQty) {
			this.availableQty = availableQty;
		}

		public AvailablePosition(String user, String account, String symbol, double qty, double price, double margin) {
			super(user, account, symbol, qty, price, margin);
		}
	}

	private static final Logger log = LoggerFactory
			.getLogger(OpenPosition.class);
	private double price;
	private double margin;
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}

	public double getAvailableQty()
	{
		if (Default.getSettlementDays() == 0)
		{
			return getQty();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(TimeUtil.getOnlyDate(getCreated()));
		cal.add(Calendar.DATE, Default.getSettlementDays());
		Date settlementDate = cal.getTime();

		if (Clock.getInstance().now().compareTo(settlementDate) >= 0)
		{
			return getQty();
		}

		return 0;
	}

	protected OpenPosition() {
		super();
	}
	
	// this one is used by overall position
	public OpenPosition(String user, String account, String symbol, double qty, double price, double margin) {
		super(account + "-" + symbol, user, account, symbol, qty);
		this.price = price;
		this.margin = margin;
	}
	
	// this one is used by detail position
	public OpenPosition(Execution execution, double margin) {
		super(execution.getId(), execution.getUser(), execution.getAccount(), execution.getSymbol(),
				execution.getSide().isBuy()?execution.getQuantity():-execution.getQuantity());
		this.price = execution.getPrice();
		this.margin = margin;
	}

	public OpenPosition split(double qty) { // qty + for buy, - for sell
		OpenPosition pos = this.clone();
		pos.setMargin(this.getMargin() * qty/this.getQty());
		pos.setQty(qty);
		this.setQty(this.getQty()-qty);
		this.setMargin(this.getMargin() - pos.getMargin());
		return pos;
	}
	
	 
	@Override
	protected String formatString() {
		 return super.formatString() + ", " + this.price + ", " + this.getAvailableQty();
	}

	@Override
	public OpenPosition clone() {
		try {
			return (OpenPosition)super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
}
