package com.cyanspring.id.Library.Util;

public class IdSymbolUtil {
	
	public final static int Premium_FX = 687;
	public final static int PreciousMetal = 691;
	public final static int Energy = 970;
	
	
	public static String toIdSymbol(String symbol,  int exch) {
		
		switch (exch) {
		case IdSymbolUtil.Premium_FX: {
			return String.format("X:S%s", symbol);
		}
		case IdSymbolUtil.PreciousMetal: {
			return String.format("C:P%sOZSP", symbol);
		}	
		default:
			return symbol;
		}
		
	}
	
	public static String toSymbol(String idSymbol,  int exch) {
		
		switch (exch) {
		case IdSymbolUtil.Premium_FX :{		
			String symbol = idSymbol.replace("X:S", "");
			return symbol;
		}
		case IdSymbolUtil.PreciousMetal: {
		String symbol = idSymbol.replace("C:P", "");
		if (symbol.length() > 6) {
			symbol = symbol.substring(0,  6);					
		}
			return symbol;
		}
		default:
			return idSymbol;
		}
		
	}
}
