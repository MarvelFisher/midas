package com.cyanspring.cstw.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.ParentOrder;

public class GroupOrderCache {
	private static final Logger log = LoggerFactory.getLogger(GroupOrderCache.class);
	private ConcurrentHashMap <String,Map<String,ParentOrder>> orderMap = new ConcurrentHashMap<String,Map<String,ParentOrder>>();
	private ConcurrentHashMap <String,Map<String,Instrument>> instMap = new ConcurrentHashMap<String,Map<String,Instrument>>();
	private ConcurrentHashMap <String,Map<String,MultiInstrumentStrategyData>> multiInstMap = new ConcurrentHashMap<String,Map<String,MultiInstrumentStrategyData>>();

	public GroupOrderCache() {
	}
	
	synchronized public void updateOrder(ParentOrder order){
		List <ParentOrder> tmpList = new ArrayList<ParentOrder>();
		tmpList.add(order);
		updateOrder(tmpList);
	}
	
	synchronized public void updateInstrument(Instrument inst){
		List <Instrument> tmpList = new ArrayList<Instrument>();
		tmpList.add(inst);
		updateInstrument(tmpList);
	}
	
	synchronized public void updateMultiInstrumentStrategyData(MultiInstrumentStrategyData multiInst){
		List <MultiInstrumentStrategyData> tmpList = new ArrayList<MultiInstrumentStrategyData>();
		tmpList.add(multiInst);
		updateMultiInstrumentStrategyData(tmpList);
	}
	
	synchronized public void updateOrder(List<ParentOrder> orderList){
		if(null == orderList || orderList.isEmpty())
			return ;
		
		for(ParentOrder order : orderList){
			String account = order.getAccount();
			log.info("receive order:{},{}",account,order.getId());
			Map<String,ParentOrder> tempMap = null;
			if(orderMap.containsKey(account)){
				tempMap = orderMap.get(account);
				if(null == tempMap)
					tempMap = new HashMap<String,ParentOrder>();
				
				tempMap.put(order.getId(),order);
				orderMap.put(account, tempMap);
			}else{
				tempMap = new HashMap<String,ParentOrder>();
				tempMap.put(order.getId(),order);
				orderMap.put(account, tempMap);
			}
		}
	}
	
	synchronized public void updateInstrument(List<Instrument> instList){
		if(null == instList || instList.isEmpty())
			return ;
		
		for(Instrument inst : instList){
			String account = inst.getAccount();
			Map<String,Instrument> tempMap = null;
			if(instMap.containsKey(account)){
				tempMap = instMap.get(account);
				if(null == tempMap)
					tempMap = new HashMap<String,Instrument>();
				
				tempMap.put(inst.getId(),inst);
				instMap.put(account, tempMap);
			}else{
				tempMap = new HashMap<String,Instrument>();
				tempMap.put(inst.getId(),inst);
				instMap.put(account, tempMap);
			}
		}		
	}
	
	synchronized public void updateMultiInstrumentStrategyData(List<MultiInstrumentStrategyData> multiInstLists){
		if(null == multiInstLists || multiInstLists.isEmpty())
			return ;
		
		for(MultiInstrumentStrategyData multiInst : multiInstLists){
			String account = multiInst.getAccount();
			Map<String,MultiInstrumentStrategyData> tempMap = null;
			if(multiInstMap.containsKey(account)){
				tempMap = multiInstMap.get(account);
				if(null == tempMap)
					tempMap = new HashMap<String,MultiInstrumentStrategyData>();
				
				tempMap.put(multiInst.getId(),multiInst);
				multiInstMap.put(account, tempMap);
			}else{
				tempMap = new HashMap<String,MultiInstrumentStrategyData>();
				tempMap.put(multiInst.getId(),multiInst);
				multiInstMap.put(account, tempMap);
			}
		}	
	}
	
	public List <Map<String,Object>> getAllParentOrders(){
		List <Map<String,Object>> orders = new ArrayList<Map<String,Object>>();
		Iterator <Map<String,ParentOrder>>ite = orderMap.values().iterator();
		while(ite.hasNext()){
			Map <String,ParentOrder> tempMap= ite.next();
			Set <Entry<String,ParentOrder>>entryOrder = tempMap.entrySet();
			for(Entry <String,ParentOrder> order:entryOrder){
				ParentOrder tempParentOrder = order.getValue();
				orders.add(tempParentOrder.getFields());
			}
		}
		return orders;
	}
	
}
