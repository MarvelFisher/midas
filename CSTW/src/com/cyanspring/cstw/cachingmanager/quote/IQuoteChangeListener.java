package com.cyanspring.cstw.cachingmanager.quote;

import java.util.Set;

import com.cyanspring.common.marketdata.Quote;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/16
 *
 */
public interface IQuoteChangeListener {

	Set<String> getQuoteSymbolSet();

	void refreshByQuote(Quote quote);

}
