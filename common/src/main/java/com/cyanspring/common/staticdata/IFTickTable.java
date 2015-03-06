package com.cyanspring.common.staticdata;

public class IFTickTable extends AbstractTickTable {
	private final static double maxPrice = 1000000000.0;
	private final int scale = 5;
	private final double delta = 0.0000001;
	private final static double tickTable[][] = {
		{0,		Double.MAX_VALUE,		0.2}
	};
	
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
