package com.cyanspring.common.business;

public class CnFutureSymbolConverter implements ISymbolConverter {

	@Override
	public String convertDown(String symbol) {
		int pos = symbol.indexOf(".CF");
		if(pos <= -1)
			return symbol;
		
		return symbol.substring(0, pos);
	}

	@Override
	public String convertUp(String symbol) {
		return symbol + ".CF";
	}
	
}
