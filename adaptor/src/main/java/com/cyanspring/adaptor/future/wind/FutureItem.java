package com.cyanspring.adaptor.future.wind;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.wind.td.tdf.TDF_CODE;
import cn.com.wind.td.tdf.TDF_FUTURE_DATA;

import com.cyanspring.adaptor.future.wind.test.FutureFeed;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.StringUtil;

public class FutureItem implements AutoCloseable {
	
	private static final Logger log = LoggerFactory
			.getLogger(FutureItem.class);

	static ConcurrentHashMap<String, FutureItem> futureItemBySymbolMap = new ConcurrentHashMap<String, FutureItem>();
	/**
	 * member
	 */
	String symbolId;
	int tDate = 0;
	long totalVolume = 0;
	long volume = 0;

	double settlePrice = 0;
	long openInterest = 0;
	double highLimit = 0;
	double lowLimit = 0;

	public static FutureItem getItem(String symbolId, String windCode,
			boolean enableCreateNew) {

		synchronized (futureItemBySymbolMap) {
			if (futureItemBySymbolMap.containsKey(symbolId) == true) {
				return futureItemBySymbolMap.get(symbolId);
			}

			// else
			if (enableCreateNew) {
				FutureItem item = new FutureItem(symbolId);
				if (WindFutureDataAdaptor.instance.gateway)
					item.setMarket(windCode.split("\\.")[1]);
				futureItemBySymbolMap.put(symbolId, item);
				return item;
			}
			return null;
		}
	}

	public static List<SymbolInfo> getSymbolInfoList() {
		List<FutureItem> list = new ArrayList<FutureItem>();
		synchronized (futureItemBySymbolMap) {
			list.addAll(futureItemBySymbolMap.values());
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
		synchronized (futureItemBySymbolMap) {
			list.addAll(futureItemBySymbolMap.values());
			futureItemBySymbolMap.clear();
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

	public static boolean makeBidAskList(long[] bids, long[] bidsizes,
			long[] asks, long[] asksizes, List<QtyPrice> bidList,
			List<QtyPrice> askList) {

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

		// WindFutureDataAdaptor.info("%s, %s, %s, %s, %s", code.getMarket(),
		// code.getCode(), code.getWindCode(),
		// WindFutureDataAdaptor.convertGBString(code.getCNName()),
		// code.getENName());

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

		makeBidAskList(data.getBidPrice(), data.getBidVol(),
				data.getAskPrice(), data.getAskVol(), bids, asks);

		Quote quote = new Quote(symbolId, bids, asks);

		// tick time
		String timeStamp = String.format("%d-%d", data.getTradingDay(),
				data.getTime());
		Date tickTime;
		try {
			if(data.getTime() < WindFutureDataAdaptor.AM10){
				tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
			}else{
				tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
			}
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

		// check stale
		String strategy = WindFutureDataAdaptor.marketRuleBySymbolMap.get(symbolId);
		MarketSessionData marketSessionData = null;
		try {
			marketSessionData = WindFutureDataAdaptor.instance
					.getMarketSessionUtil().getCurrentMarketSessionType(strategy,
							DateUtil.now());
		} catch (Exception e) {
			LogUtil.logException(log, e);
			return;
		}

		if (WindFutureDataAdaptor.instance.isMarketDataLog())
			WindFutureDataAdaptor.debug("Wind Strategy=" + strategy
					+ ",Symbol=" + symbolId + ",MarketSessionType="
					+ marketSessionData.getSessionType() + ",Time=" + DateUtil.now());

		if (marketSessionData.getSessionType() == MarketSessionType.PREOPEN
				|| marketSessionData.getSessionType() == MarketSessionType.CLOSE) {
			quote.setStale(true);
		}

		if (marketSessionData.getSessionType() == MarketSessionType.OPEN) {
			quote.setStale(false);
		}

		boolean change = false;
		
		double settlePrice = (double) data.getSettlePrice() /10000;
		if(item.settlePrice != settlePrice){
			item.settlePrice = settlePrice;
			change = true;			
		}

		long openInterest = data.getOpenInterest();
		if (item.openInterest != openInterest) {
			item.openInterest = openInterest;
			change = true;
		}

		double highLimit = (double) data.getHighLimited() / 10000;
		if (item.highLimit != highLimit) {
			item.highLimit = highLimit;
			change = true;
		}

		double lowLimit = (double) data.getLowLimited() / 10000;
		if (item.lowLimit != lowLimit) {
			item.lowLimit = lowLimit;
			change = true;
		}

		//volume
		long totalVolume = data.getVolume();

		if (totalVolume - item.totalVolume > 0) {
			item.volume = totalVolume - item.volume;
			item.totalVolume = totalVolume;
		}
		quote.setTotalVolume(totalVolume);
		quote.setLastVol(item.volume);

		// process Extend field
		DataObject quoteExt = null;
		if(change){
			quoteExt = new DataObject();
			quoteExt.put(QuoteExtDataField.SYMBOL.value(), symbolId);
			quoteExt.put(QuoteExtDataField.ID.value(), quote.getId());
			quoteExt.put(QuoteExtDataField.TIMESTAMP.value(), tickTime);
			quoteExt.put(QuoteExtDataField.CEIL.value(), highLimit);
			quoteExt.put(QuoteExtDataField.FLOOR.value(), lowLimit);
			quoteExt.put(QuoteExtDataField.SETTLEPRICE.value(), settlePrice);
			quoteExt.put(QuoteExtDataField.OPENINTEREST.value(), openInterest);
		}

		
		// fire quote event
		String s = quote.toString();
		WindFutureDataAdaptor.instance.saveLastQuote(quote, quoteExt);
		WindFutureDataAdaptor.instance.sendQuote(quote, quoteExt);

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
		// if(WindFutureDataAdaptor.instance.isMarketDataLog()){
		// TimeSpan ts = TimeSpan.getTimeSpan(now, timeLast);
		// if (ts.getTotalSeconds() >= 20) {
		// WindFutureDataAdaptor.info(s);
		// timeLast = now;
		// }
		// }
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

	public static void main(String[] args) {
		String timeStamp = String.format("%d-%d", 20150318, 90000000);
		Date tickTime;
		try {
			tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
			System.out.println(tickTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
