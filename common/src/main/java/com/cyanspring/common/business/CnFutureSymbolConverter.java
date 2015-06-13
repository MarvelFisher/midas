package com.cyanspring.common.business;

public class CnFutureSymbolConverter implements ISymbolConverter {

	@Override
	public String convert(String symbol) {
		int pos = symbol.indexOf(".CF");
		if(pos <= -1)
			return symbol;
		
		return symbol.substring(0, pos);
	}
}
