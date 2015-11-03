package com.cyanspring.marketdata.adaptor;

import com.cyanspring.marketdata.data.*;
import com.cyanspring.marketdata.item.FutureItem;
import com.cyanspring.marketdata.item.IndexItem;
import com.cyanspring.marketdata.item.StockItem;
import com.cyanspring.marketdata.item.TransationItem;
import com.cyanspring.marketdata.type.FDTFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class QuoteMgr {
	private static final Logger log = LoggerFactory.getLogger(QuoteMgr.class);
	private WindAdaptor windAdaptor;
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
	private Thread controlReqThread = null;
	private ConcurrentHashMap<String, FutureData> futureDataBySymbolMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, StockData> stockDataBySymbolMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, IndexData> indexDataBySymbolMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, TransationData> transationDataBySymbolMap = new ConcurrentHashMap<>();
	private WindDataParser windDataParser = new WindDataParser();
	private List<Object> qList = new ArrayList<>();

	QuoteMgr(WindAdaptor windGateWayAdapter) {
		this.windAdaptor = windGateWayAdapter;
	}

	public void init() {
		if (controlReqThread == null) {
			// ControlReqThread control queue task, if queue size > 0 , poll and
			// exec process method.
			controlReqThread = new Thread(new Runnable() {
				@Override
				public void run() {
					int cnt;
					int max = 50;
					while (true) {
						qList.clear();
						qList.add(queue.poll());
						cnt = queue.drainTo(qList) + 1;
						for (Object obj : qList) {
							processGateWayMessage((HashMap<Integer, Object>) obj);
						}
						if (cnt > max) {
							max = cnt;
							log.info("windAdaptor queue reach new max: " + max);
						}
					}
				}
			});
			controlReqThread.setName("QuoteMgr-" + windAdaptor.getId());
			controlReqThread.start();
		}
	}

	public void uninit() {
		futureDataBySymbolMap.clear();
		stockDataBySymbolMap.clear();
		indexDataBySymbolMap.clear();
		transationDataBySymbolMap.clear();
		FutureItem.futureItemBySymbolMap.clear();
		StockItem.stockItemBySymbolMap.clear();
		IndexItem.indexItemBySymbolMap.clear();
		TransationItem.transationItemBySymbolMap.clear();
		if (controlReqThread != null) {
			controlReqThread.interrupt();
			controlReqThread = null;
		}
	}

	public void addRequest(Object reqObj) {
		if (controlReqThread != null) {
			queue.offer(reqObj);
		}
	}

	public boolean checkSymbol(String symbol) {
		boolean futureExist = FutureItem.futureItemBySymbolMap.containsKey(symbol);
		boolean stockExist = StockItem.stockItemBySymbolMap.containsKey(symbol);
		boolean indexExist = IndexItem.indexItemBySymbolMap.containsKey(symbol);
		return futureExist || stockExist || indexExist;
	}

	public void processGateWayMessage(HashMap<Integer, Object> inputMessageHashMap) {
		if (inputMessageHashMap == null || inputMessageHashMap.size() == 0) return;
		int datatype = (int) inputMessageHashMap.get(FDTFields.PacketType);
		try {
			switch (datatype) {
			case FDTFields.WindMarketData:
				StockData stockData = null;
				stockData = windDataParser.convertToStockData(inputMessageHashMap, stockDataBySymbolMap);
				StockItem.processMarketData(stockData, windAdaptor);
				break;
			case FDTFields.WindIndexData:
				IndexData indexData = null;
				indexData = windDataParser.convertToIndexData(inputMessageHashMap, indexDataBySymbolMap);
				IndexItem.processIndexData(indexData, windAdaptor);
				break;
			case FDTFields.WindFutureData:
				FutureData futureData = null;
				futureData = windDataParser.convertToFutureData(inputMessageHashMap, futureDataBySymbolMap);
				FutureItem.processFutureData(futureData, windAdaptor);
				break;
			case FDTFields.WindTransaction:
				TransationData transationData = null;
				transationData = windDataParser.convertToTransationData(inputMessageHashMap, transationDataBySymbolMap);
				TransationItem.processTransationData(transationData, windAdaptor);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return;
		}
	}
}
