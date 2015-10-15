package com.cyanspring.common.cstw.tick;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.AbstractTickTable;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.staticdata.RefData;

public class Ticker {
	private static final Logger log = LoggerFactory
			.getLogger(Ticker.class);
	private AbstractTickTable tickTable;
	private RefData refData;
	private DecimalFormat format = new DecimalFormat("#.#####");
	private int decimalPoint =0;
	private boolean fxRule = false;
	
	public Ticker(AbstractTickTable tickTable,RefData refData) {
		this.tickTable = tickTable;
		this.refData = refData;
		decimalPoint = refData.getDeciamlPoint();
//		log.info("decimalPoint:{}",decimalPoint);
		if(decimalPoint < 0){//is fx rule
			fxRule = true;
		}else{
			String prefix = "#";
			if(decimalPoint != 0)
				prefix +=".";
			
//			log.info("pad:{}",pad("#",decimalPoint));
			format = new DecimalFormat(prefix+pad("#",decimalPoint));
		}
	}
	
	public String formatPrice(double price){
		if(fxRule){
			int total = 7 ;
			String value = Double.toString(price);
			int point = value.indexOf(".");						
			format = new DecimalFormat(pad("#",point)+"."+pad("#",(total-point-1)));
			return format.format(price);
			
		}else{
//			log.info("price:{}",price);
			return format.format(price);
		}
	}
	
	
	private String pad(String symbol, int decimalPoint) {
		StringBuffer sb = new StringBuffer("");	
		for(int i = 0; i < decimalPoint; i++ ){
			sb.append(symbol);
		}
		
		return sb.toString();
	}

	public String tickUp(double price, boolean roundUp) {
		return formatPrice(tickTable.tickUp(price, roundUp));
	}

	public String tickDown(double price, boolean roundUp) {
		return  formatPrice(tickTable.tickDown(price, roundUp));
	}
	
	public String tickDown(double price, int ticks, boolean roundUp) {
		return formatPrice(tickTable.tickDown(price, ticks, roundUp));
	}
	
	public String tickUp(double price, int ticks, boolean roundUp) {
		return formatPrice(tickTable.tickUp(price, ticks, roundUp));
	}
	
	public ITickTable getTickTable() {
		return this.tickTable;
	}
}
