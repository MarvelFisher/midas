package com.cyanspring.adaptor.future.wind;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import cn.com.wind.td.tdf.TDF_CODE;
import cn.com.wind.td.tdf.TDF_FUTURE_DATA;

import com.cyanspring.adaptor.future.wind.test.FutureFeed;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.StringUtil;
import com.cyanspring.id.Library.Util.TimeSpan;

public class FutureItem implements AutoCloseable{

	static Hashtable<String, FutureItem> symbolTable = new Hashtable<String, FutureItem>();
	/**
	 * member
	 */
	String symbolId;
	int tDate = 0;
	long totalVolume = 0;
	long volume = 0;
	
	double preSettle = 0;
	long preOpenInterest = 0;
	double highLimit = 0;
	double lowLimit = 0;
	
	
	public static FutureItem getItem(String symbolId, String windCode, boolean enableCreateNew) {

		synchronized (symbolTable) {
			if (symbolTable.containsKey(symbolId) == true) {
				return symbolTable.get(symbolId);
			}

			// else
			if (enableCreateNew) {
				FutureItem item = new FutureItem(symbolId);
				if(WindFutureDataAdaptor.instance.gateway) item.setMarket(windCode.split("\\.")[1]);
				symbolTable.put(symbolId, item);
				return item;
			}
			return null;
		}
	}
	
	public static List<SymbolInfo> getSymbolInfoList() {
		List<FutureItem> list = new ArrayList<FutureItem>();
		synchronized (symbolTable) {	
			list.addAll(symbolTable.values());
		}
		
		List<SymbolInfo> outList = new ArrayList<SymbolInfo>();
		for (FutureItem item : list) {
			SymbolInfo info = item.getSymbolInfo();								
			outList.add(info);
		}
		return outList;
	}
	
	public static void clearSymbols() {
		List<FutureItem> list = new ArrayList<FutureItem>();
		synchronized (symbolTable) {
			list.addAll(symbolTable.values());
			symbolTable.clear();
		}
		for (FutureItem item : list) {
			try {
				item.close();
			} catch (Exception e) {
				WindFutureDataAdaptor.exception(e);
			}
		}
		list.clear();	
	}

	public static boolean makeBidAskList(long[] bids, long[] bidsizes, long[] asks, long[] asksizes,
			List<QtyPrice> bidList, List<QtyPrice> askList) {

		for (int i = 0; i < bids.length; i++) {
			double price = (double) bids[i] / 10000;
			long size = bidsizes[i];
			QtyPrice bidask = new QtyPrice(size, price);
			bidList.add(bidask);
		}

		for (int i = 0; i < asks.length; i++) {
			double price = (double) asks[i] / 10000;
			long size = asksizes[i];
			QtyPrice bidask = new QtyPrice(size, price);
			askList.add(bidask);
		}

		return true;
	}

	public static void setBidAsk(Quote quote, QtyPrice bid, QtyPrice ask) {
		if (bid != null) {
			quote.setBid(bid.getPrice());
			quote.setBidVol(bid.getQuantity());
		}
		if (ask != null) {
			quote.setAsk(ask.getPrice());
			quote.setAskVol(ask.getQuantity());
		}
	}

	String market;
	String cnName;
	String enName;
	public String getEnName() {
		return enName;
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getCnName() {
		return cnName;
	}

	public void setCnName(String cnName) {
		this.cnName = cnName;
	}

	
	public static SymbolInfo processCODE(TDF_CODE code) {
		String symbolId = code.getCode();
		String windCode = code.getWindCode();
		FutureItem item = FutureItem.getItem(symbolId, windCode, true);
			
		item.setMarket(code.getMarket());
		String cnName = WindFutureDataAdaptor.convertGBString(code.getCNName());
		item.setCnName(cnName);
		String enName = code.getENName();
		if (enName.isEmpty()) {
			enName = symbolId;			
		}
		item.setEnName(enName);
		
		
		//WindFutureDataAdaptor.info("%s, %s, %s, %s, %s", code.getMarket(),
		//		code.getCode(), code.getWindCode(),
		//		WindFutureDataAdaptor.convertGBString(code.getCNName()), code.getENName());
		
		return item.getSymbolInfo();
		
		
	}
	
	static Date timeLast = DateUtil.now(); 
	static int lastShow = 0;
	public static void processFutureData(TDF_FUTURE_DATA data) {
		String symbolId = data.getWindCode();
		String windCode = data.getWindCode();

		FutureItem item = getItem(symbolId, windCode, true);

		data.getStatus();

		List<QtyPrice> bids = new ArrayList<QtyPrice>();
		List<QtyPrice> asks = new ArrayList<QtyPrice>();

		makeBidAskList(data.getBidPrice(), data.getBidVol(), data.getAskPrice(), data.getAskVol(), bids, asks);

		Quote quote = new Quote(symbolId, bids, asks);

		// tick time
		String timeStamp = String.format("%d-%d", data.getActionDay(), data.getTime());
		Date tickTime;
		try {
			tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
		} catch (ParseException e) {
			tickTime = DateUtil.now();
		}
		quote.setTimeStamp(tickTime);

		// bid/ask
		QtyPrice bid = bids.size() > 0 ? bids.get(0) : null;
		QtyPrice ask = asks.size() > 0 ? asks.get(0) : null;

		setBidAsk(quote, bid, ask);

		// update price
		quote.setOpen((double) data.getOpen() / 10000);
		quote.setHigh((double) data.getHigh() / 10000);
		quote.setLow((double) data.getLow() / 10000);
		quote.setLast((double) data.getMatch() / 10000);
		quote.setClose((double) data.getPreClose() / 10000);
		
		//check stale
		String strategy = WindFutureDataAdaptor.strategyht.get(symbolId);
		String msState = WindFutureDataAdaptor.instance.marketSessionUtil.getCurrentMarketSessionType(strategy, DateUtil.now()).name();
		
		if(WindFutureDataAdaptor.instance.isMarketDataLog())
		WindFutureDataAdaptor.debug("Wind Strategy=" + strategy + ",SesssionState=" + msState);
		
		if("CLOSE".equals(msState) || "PREOPEN".equals(msState)){
			quote.setStale(true);
		}
		
		
		// if (diff ) send info event
		// ==================
		//quote.setPresettlePrice((double) data.getSettlePrice() / 10000); //.getPreSettlePrice() / 10000);
		//quote.setOpenInterest(data.getOpenInterest());
		
		
		//QuoteExt quoteExt = new QuoteExt(item.symbolId, item.market);
		boolean change = false;
		double preSettle = (double)data.getPreSettlePrice() / 10000 ;
		if (item.preSettle != preSettle) {
			item.preSettle = preSettle;
			change = true;
		}
		
		long preOpenInterest = data.getPreOpenInterest();
		if (item.preOpenInterest != preOpenInterest) {
			item.preOpenInterest = preOpenInterest;
			change = true;
		}
		
		double highLimit = (double)data.getHighLimited() / 10000 ;
		if (item.highLimit != highLimit) {
			item.highLimit = highLimit;
			change = true;
		}
		
		double lowLimit = (double)data.getLowLimited() / 10000 ;
		if (item.lowLimit != lowLimit) {
			item.lowLimit = lowLimit;
			change = true;
		}
		
		if (change) {
			// fire QuoteExt
		}
		
		
		// settle price
		// pre settle price
		// high limit
		// low limit
		
		// update volume
		long totalVolume = data.getVolume();
		
		if (item.totalVolume == 0) {
			item.totalVolume = totalVolume;			
			return;
		}
			
		if (totalVolume - item.totalVolume > 0) {
			item.volume = totalVolume - item.volume;
			item.totalVolume = totalVolume;

			quote.setTotalVolume(totalVolume);
			quote.setLastVol(item.volume);
		}

		// fire quote event
		String s = quote.toString();
		WindFutureDataAdaptor.instance.sendQuote(quote);
		
		Date now = DateUtil.now();
		int timestamp = DateUtil.dateTime2Time(now);
		// show quote
		FutureFeed future = FutureFeed.instance;
		if (future.isSelectAll || future.isWatchSymbol(symbolId)) {
			if (timestamp != lastShow) {
				FutureFeed.instance.showQuote(quote);				
			}
		}
		
		// log quote as alive frame
		if(WindFutureDataAdaptor.instance.isMarketDataLog()){ 
			TimeSpan ts = TimeSpan.getTimeSpan(now, timeLast);
			if (ts.getTotalSeconds() >= 20) {			
				WindFutureDataAdaptor.info(s);
				timeLast = now;
			}		
		}
	}

	public String windCode() {
		return String.format(symbolId);
	}
	
	public SymbolInfo getSymbolInfo() {
		SymbolInfo info = new SymbolInfo(getMarket(), symbolId);
		info.setWindCode(windCode());
		info.setCnName(getCnName());
		info.setEnName(getEnName());
		return info;
	}
	
	public FutureItem(String symbolId) {
		this.symbolId = symbolId;
	}

	public void loadData(String file) {
		String[] arr = StringUtil.split(file, ',');

		if (arr.length < 2)
			return;

		tDate = Integer.parseInt(arr[0]);
		totalVolume = Long.parseLong(arr[1]);
	}

	public String writeData() {
		FixStringBuilder sb = new FixStringBuilder(',');
		sb.append(tDate);
		sb.append(totalVolume);
		return sb.toString();
	}

	@Override
	public void close() throws Exception {
		FinalizeHelper.suppressFinalize(this);
	}
}
