package com.cyanspring.common.staticdata;

import com.cyanspring.common.util.PriceUtils;

public abstract class AbstractTickTable implements ITickTable {
	
	protected abstract double getMaxPrice();
	protected abstract int getScale();
	protected abstract double getDelta();
	public abstract double[][] getTickTable();
	
	private double roundPrice(double price) {
		return ((long)((price + getDelta()) * getScale()))/(double)getScale();
	}
	
	@Override
	public double tickUp(double price, boolean roundUp) {
		return tickUp(price, 1, roundUp);
	}

	@Override
	public double tickDown(double price, boolean roundUp) {
		return tickDown(price, 1, roundUp);
	}

	@Override
	public double getRoundedPrice(double price, boolean up) {	
		if (PriceUtils.GreaterThan(price, getMaxPrice())) {
			return getMaxPrice();
		}
		double rounded = 0;
		for(double[] band: getTickTable()) {
			//find the right range
			if(PriceUtils.EqualGreaterThan(price, band[0]) && 
					PriceUtils.LessThan(price, band[1])) {
				long lprice = (long)(price * getScale());
				long lbase = (long)(band[0] * getScale());
				long ltick = (long)(band[2] * getScale());
				long ticks = (lprice - lbase) / ltick;
				rounded = ((double)(lbase + ticks * ltick))/getScale();
				if(up && PriceUtils.GreaterThan(price, rounded))
					rounded += band[2];
				
				break;
			}
		}
		return roundPrice(rounded);
	}
	
	@Override
	public double tickUp(double price, int ticks, boolean roundUp) {
		price = getRoundedPrice(price, roundUp);
		for(double[] band: getTickTable()) {
			if(PriceUtils.EqualGreaterThan(price, band[1]))
				continue;

			long llow = (long)(price * getScale());
			long lhigh = (long)(band[1] * getScale());
			long ltick = (long)(band[2] * getScale());
			long totalTicks = (lhigh - llow) / ltick;
			if(totalTicks >= ticks) {
				return roundPrice(price + ticks * band[2]);
			} else {
				price = band[1];
				ticks -= totalTicks;
			}
		}
		return roundPrice(price);
	}

	@Override
	public double tickDown(double price, int ticks, boolean roundUp) {
		price = getRoundedPrice(price, roundUp);
		for(int i=getTickTable().length; i>0; i--) {
			double[] band = getTickTable()[i-1];
			if(PriceUtils.EqualLessThan(price, band[0]))
				continue;

			long llow = (long)(band[0] * getScale());
			long lhigh = (long)(price * getScale());
			long ltick = (long)(band[2] * getScale());
			long totalTicks = (lhigh - llow) / ltick;
			if(totalTicks >= ticks) {
				return roundPrice(price - ticks * band[2]);
			} else {
				price = band[0];
				ticks -= totalTicks;
			}
		}
		return roundPrice(price);
	}

	@Override
	public boolean validPrice(double price) {
		return PriceUtils.GreaterThan(price, 0) && PriceUtils.LessThan(price, getMaxPrice());
	}

}
