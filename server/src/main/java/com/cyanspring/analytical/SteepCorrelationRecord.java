package com.cyanspring.analytical;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.TimeThrottler;

public class SteepCorrelationRecord {
	private static final Logger log = LoggerFactory
			.getLogger(SteepCorrelationAnalyzer.class);
	private String symbol;
	private long shortTerm;
	private long longTerm;
	private TimeThrottler throttler;

	Record current;
	List<Record> records = new LinkedList<Record>();
	List<Record> pendings = new LinkedList<Record>();
	
	class Record {
		String symbol;
		double startBid;
		double startAsk;
		double endShortBid;
		double endShortAsk;
		double endLongBid;
		double endLongAsk;

		Date time;
		TimeThrottler throttler;
		public Record(String symbol, double startBidPrice,
				double startAskPrice,
				Date time) {
			super();
			this.symbol = symbol;
			this.startBid = startBidPrice;
			this.startAsk = startAskPrice;
			this.time = time;
		}
		
		double getShortBid() {
			return endShortBid - startBid;
		}
		
		double getShortAsk() {
			return endShortAsk - startAsk;
		}
		
		double getLongBid() {
			return endLongBid - endShortBid;
		}
		
		double getLongAsk() {
			return endLongAsk - endShortAsk;
		}
	}
	
	public SteepCorrelationRecord(String symbol, long shortTerm, long longTerm) {
		this.symbol = symbol;
		this.shortTerm = shortTerm;
		this.longTerm = longTerm;
		this.throttler = new TimeThrottler(shortTerm);
	}
	
	public void process(Quote quote) {
		if(throttler.check()) {
			current.endShortBid = quote.getBid();
			current.endShortAsk = quote.getAsk();
			current.throttler = new TimeThrottler(longTerm);
			pendings.add(current);

			current = new Record(quote.getSymbol(), 
					quote.getBid(), quote.getAsk(),
					Clock.getInstance().now());
		}

		Iterator<Record> it = pendings.iterator();
		while(it.hasNext()) {
			Record record = it.next();
			if(record.throttler.check()) {
				record.endLongBid = quote.getBid();
				record.endLongAsk = quote.getAsk();
				record.throttler = null;
				it.remove();
				records.add(record);
			}
		}
	}
	
	public void summarise() {
		double totalBid = 0.0;
		double totalAsk = 0.0;
		long bidCount = 0;
		long askCount = 0;
		for(Record record: records) {
			if(record.getShortBid() < 0) {
				totalBid += record.getShortBid();
				bidCount++;
			}
			if(record.getShortAsk() > 0) {
				totalAsk += record.getShortAsk();
				askCount++;
			}
		}
		double avgBid = totalBid/bidCount;
		double avgAsk = totalAsk/askCount;
		
		LinkedList<Record> bidList = new LinkedList<Record>(records);
		
        Collections.sort(bidList, new Comparator<Record>() {
            @Override
            public int compare(Record p1, Record p2) {
                if (PriceUtils.GreaterThan(p1.getShortBid(), p2.getShortBid()))
                    return 1;
                else if (PriceUtils.LessThan(p1.getShortBid(), p2.getShortBid()))
                    return -1;

                return 0;
            }
        });

		LinkedList<Record> askList = new LinkedList<Record>(records);
        
        Collections.sort(askList, new Comparator<Record>() {
            @Override
            public int compare(Record p1, Record p2) {
                if (PriceUtils.LessThan(p1.getShortBid(), p2.getShortBid()))
                    return 1;
                else if (PriceUtils.GreaterThan(p1.getShortBid(), p2.getShortBid()))
                    return -1;

                return 0;
            }
        });
	}

	public String getSymbol() {
		return symbol;
	}

	public long getShortTerm() {
		return shortTerm;
	}

	public long getLongTerm() {
		return longTerm;
	}
}
