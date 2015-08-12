package com.cyanspring.apievent.obj;

/**
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class OpenPosition extends Position {
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

	@Override
	protected String formatString() {
		 return super.formatString() + ", " + this.price + ", ";
	}
    
}
