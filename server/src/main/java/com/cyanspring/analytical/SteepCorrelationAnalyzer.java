package com.cyanspring.analytical;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketdata.Quote;

public class SteepCorrelationAnalyzer implements IQuoteAnalyzer {
	private static final Logger log = LoggerFactory
			.getLogger(SteepCorrelationAnalyzer.class);
	
	private long shortTerm;
	private long longTerm;
	private Map<String, SteepCorrelationRecord> records = new HashMap<String, SteepCorrelationRecord>();
	
	@Override
	public void init() {
	}

	@Override
	public void uninit() {
	}
	
	@Override
	public void analyze(Quote quote) {
		SteepCorrelationRecord record = records.get(quote.getSymbol());
		if(!records.containsKey(quote.getSymbol())) {
			record = new SteepCorrelationRecord(quote.getSymbol(), shortTerm, longTerm);
			records.put(quote.getSymbol(), record);
		}
		
		record.process(quote);
		
	}

	public void setShortTerm(long shortTerm) {
		this.shortTerm = shortTerm;
	}

	public void setLongTerm(long longTerm) {
		this.longTerm = longTerm;
	}

}
