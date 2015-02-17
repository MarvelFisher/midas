/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.staticdata;
import com.cyanspring.common.util.PriceUtils;

public class ForexTickTable implements ITickTable {
	//private final static double minPrice = 0.01;
	private final static double maxPrice = 1000000000.0;
	private final int scale = 100000;
	private final double delta = 0.0000001;
	private final static double tickTable[][] = { 
		{0.0000001,		0.1,		0.0000001},
		{0.1,			10,			0.00005},
		{10,			20,			0.0001},
		{20,			50,			0.0005},
		{50,		maxPrice,		0.005}
	};

	
	private double roundPrice(double price) {
		return ((int)((price + delta) * scale))/(double)scale;
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
		if (PriceUtils.GreaterThan(price, maxPrice)) {
			return maxPrice;
		}
		double rounded = 0;
		for(double[] band: tickTable) {
			//find the right range
			if(PriceUtils.EqualGreaterThan(price, band[0]) && 
					PriceUtils.LessThan(price, band[1])) {
				int lprice = (int)(price * scale);
				int lbase = (int)(band[0] * scale);
				int ltick = (int)(band[2] * scale);
				int ticks = (lprice - lbase) / ltick;
				rounded = ((double)(lbase + ticks * ltick))/scale;
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
		for(double[] band: tickTable) {
			if(PriceUtils.EqualGreaterThan(price, band[1]))
				continue;

			int llow = (int)(price * scale);
			int lhigh = (int)(band[1] * scale);
			int ltick = (int)(band[2] * scale);
			int totalTicks = (lhigh - llow) / ltick;
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
		for(int i=tickTable.length; i>0; i--) {
			double[] band = tickTable[i-1];
			if(PriceUtils.EqualLessThan(price, band[0]))
				continue;

			int llow = (int)(band[0] * scale);
			int lhigh = (int)(price * scale);
			int ltick = (int)(band[2] * scale);
			int totalTicks = (lhigh - llow) / ltick;
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
		return PriceUtils.GreaterThan(price, 0) && PriceUtils.LessThan(price, maxPrice);
	}

}
