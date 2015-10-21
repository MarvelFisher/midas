package com.cyanspring.common.cstw.tick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.statistic.TickTableReplyEvent;
import com.cyanspring.common.event.statistic.TickTableRequestEvent;
import com.cyanspring.common.staticdata.AbstractTickTable;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.IdGenerator;

public class TickManager implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(TickManager.class);
	private IRemoteEventManager eventManager;
	private Map<AbstractTickTable, List<RefData>> tickMap = new HashMap<AbstractTickTable, List<RefData>>();
	private String firstServer = "";

	public TickManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof TickTableReplyEvent) {
			log.info("receive TickTableReplyEvent");
			TickTableReplyEvent evt = (TickTableReplyEvent) event;
			processTickTableReplyEvent(evt);
		}
	}

	private void processTickTableReplyEvent(TickTableReplyEvent evt) {
		if (null != evt.getMap()){
			if( null == tickMap)
				 tickMap = new HashMap<AbstractTickTable, List<RefData>>();
			
			Map<AbstractTickTable, List<RefData>> tempMap = evt.getMap();
			Iterator <AbstractTickTable>keyIte = tempMap.keySet().iterator();
			while(keyIte.hasNext()){
				AbstractTickTable table = keyIte.next();
				List<RefData> newList = tempMap.get(table);
				if(tickMap.containsKey(table)){
					List <RefData> oldList = tickMap.get(table);
					oldList.addAll(newList);
					tickMap.put(table, oldList);
				}else{
					List <RefData> oldList = new ArrayList<RefData>();
					oldList.addAll(newList);
					tickMap.put(table, oldList);
				}
			}
			
		}
			
	}

	public List<String> getSymbolList(){	
		Set <String> tempSet = new TreeSet<String>();
		if( null == tickMap )
			return null;
		
		Iterator<AbstractTickTable> table = tickMap.keySet().iterator();
		while(table.hasNext()){
			AbstractTickTable tempTable = table.next();
			List<RefData> symbolList = tickMap.get(tempTable);
			for(RefData refData : symbolList){
				tempSet.add(refData.getSymbol());
			}
		}
		
		List<String> tempList = new ArrayList<String>();
		tempList.addAll(tempSet);
		return tempList;	
	}
	
	public List<RefData> getRefDataList(){
		List<RefData> tempList = new ArrayList<RefData>();

		if( null == tickMap )
			return null;
		
		Iterator<AbstractTickTable> table = tickMap.keySet().iterator();
		while(table.hasNext()){
			AbstractTickTable tempTable = table.next();
			List<RefData> reflList = tickMap.get(tempTable);
			tempList.addAll(reflList);
		}
		
		return tempList;	
	}
	
	public Ticker getTicker(String symbol){
		Iterator<AbstractTickTable> table = tickMap.keySet().iterator();
		while(table.hasNext()){
			AbstractTickTable tempTable = table.next();
			List<RefData> symbolList = tickMap.get(tempTable);
			for(RefData data : symbolList){
				if(data.getSymbol().equals(symbol)){
					return new Ticker(tempTable,data);
				}
			}
		}
		return null;
	}
	
	public void requestTickTableInfo(String symbol) {
		TickTableRequestEvent event = new TickTableRequestEvent(IdGenerator
				.getInstance().getNextID(), firstServer, symbol);
		try {
			eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	public void init(String server) {
		subEvent(TickTableReplyEvent.class);
		firstServer = server;
		log.info("send tick table request");
		requestTickTableInfo(null);
	}

	public void unInit() {
		unSubEvent(TickTableReplyEvent.class);
	}

	private void subEvent(Class<? extends AsyncEvent> clazz) {
		eventManager.subscribe(clazz, this);
	}

	private void unSubEvent(Class<? extends AsyncEvent> clazz) {
		eventManager.unsubscribe(clazz, this);
	}
}
