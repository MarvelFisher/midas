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

public class ForexTickTable extends AbstractTickTable{
	//private final static double minPrice = 0.01;
	private final static double maxPrice = 1000000000.0;
	private final int scale = 10000000;
	private final double delta = 0.0000001;
	private double tickTable[][] = { 
		{0,		0.1,		0.0000001},
		{0.1,			10,			0.00005},
		{10,			20,			0.0001},
		{20,			50,			0.0005},
		{50,		Double.MAX_VALUE,		0.005}
	};

	
	private double getTick(double price) {
		
		for (double[] arr : tickTable) {
			if (arr[0] < price && price <= arr[1]) {
				return arr[2];
			}
		}
		
		int length = tickTable.length;
		if (price <= tickTable[0][1])
			return tickTable[0][2];
		if (price >= tickTable[length - 1][0])
			return tickTable[length - 1][2];		
		
		return  delta;		
		 
	}
	
	public double getSpread(double priceBid, double priceAsk) {
		
		return (priceAsk - priceBid) / getTick(priceBid);
		//return 0;
	}

	@Override
	protected double getMaxPrice() {
		return maxPrice;
	}

	@Override
	protected int getScale() {
		return scale;
	}

	@Override
	protected double getDelta() {
		return delta;
	}

	@Override
	public double[][] getTickTable() {
		return tickTable;
	}

	public void setTickTable(double[][] tickTable) {
		this.tickTable = tickTable;
	}
}
