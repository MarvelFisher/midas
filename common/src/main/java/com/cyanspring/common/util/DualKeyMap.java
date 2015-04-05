package com.cyanspring.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this map allows search by two keys, one is unique, one is not
// assumption: object's K1 and K2 will never be changed
public class DualKeyMap<K1, K2, V> {
	private Map<K1, V> map1 = new HashMap<K1, V>();
	private Map<K2, Map<K1, V>> map2 = new HashMap<K2, Map<K1, V>>(); 
	
	public synchronized V get(K1 k) {
		return map1.get(k);
	}
	
	public synchronized Map<K1, V> getMap(K2 k) {
		Map<K1, V> map = map2.get(k);
		return null == map? new HashMap<K1, V>() : map;
	}
	
	public synchronized Map<K1, V> removeMap(K2 k) {
		Map<K1, V> map = map2.remove(k);
		if(null != map) {
			for(K1 k1: map.keySet()) {
				map1.remove(k1);
			}
		}
		return map;
	}
	
	public synchronized V put(K1 k1, K2 k2, V v) {
		Map<K1, V> map = map2.get(k2);
		if (null == map) {
			map = new HashMap<K1, V>();
			map2.put(k2, map);
		}
		map.put(k1, v);
		return map1.put(k1, v);
	}
	
	public synchronized V remove(K1 k1, K2 k2) {
		Map<K1, V> map = map2.get(k2);
		map.remove(k1);
		return map1.remove(k1);
	}
	
	public synchronized void clear() {
		map1.clear();
		map2.clear();
	}
	
	public synchronized boolean containsKey(K1 k) {
		return map1.containsKey(k);
	}
	
	public synchronized Collection<V> values() {
		List<V> result = new ArrayList<V>();
		for(V v: map1.values())
			result.add(v);
		return result;
	}
}
