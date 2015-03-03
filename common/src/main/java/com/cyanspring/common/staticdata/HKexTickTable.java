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

public class HKexTickTable extends AbstractTickTable{
	private final static double tickTable[][] = { 
		{0.01,		0.25,		0.001},
		{0.25,		0.50,		0.005},
		{0.50,		10.00,		0.010},
		{10.00,		20.00,		0.020},
		{20.00,		100.00,		0.050},
		{100.00,	200.00,		0.100},
		{200.00,	500.00,		0.200},
		{500.00,	1000.00,	0.500},
		{1000.00,	2000.00,	1.000},
		{2000.00,	5000.00,	2.000},
		{5000.00,	9995.00,	5.000}
	};

	//private final static double minPrice = 0.01;
	private final static double maxPrice = 9995;
	private final int scale = 1000;
	private final double delta = 0.000001;

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

}
