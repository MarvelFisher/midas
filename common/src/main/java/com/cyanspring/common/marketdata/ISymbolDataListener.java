package com.cyanspring.common.marketdata;

import java.util.List;

public interface ISymbolDataListener {
	public void onSymbol(List<SymbolInfo> symbol);
}
