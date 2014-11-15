package com.cyanspring.common.util;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestDualKeyMap {

	class DualKeyObject {
		private String id;
		private String account;
		private String value;
		public DualKeyObject(String id, String account, String value) {
			super();
			this.id = id;
			this.account = account;
			this.value = value;
		}
		public String getId() {
			return id;
		}
		public String getAccount() {
			return account;
		}
		public String getValue() {
			return value;
		}
	}
	
	@Test
	public void test() {
		DualKeyMap<String, String, DualKeyObject> dualKeyMap = new DualKeyMap<String, String, DualKeyObject>();
		DualKeyObject obj1 = new DualKeyObject("1", "acc1", "1");
		DualKeyObject obj2 = new DualKeyObject("2", "acc1", "2");
		DualKeyObject obj3 = new DualKeyObject("3", "acc2", "3");
		DualKeyObject obj4 = new DualKeyObject("4", "acc2", "4");
		DualKeyObject obj5 = new DualKeyObject("5", "acc3", "5");
		dualKeyMap.put(obj1.getId(), obj1.getAccount(), obj1);
		dualKeyMap.put(obj2.getId(), obj2.getAccount(), obj2);
		dualKeyMap.put(obj3.getId(), obj3.getAccount(), obj3);
		dualKeyMap.put(obj4.getId(), obj4.getAccount(), obj4);
		dualKeyMap.put(obj5.getId(), obj5.getAccount(), obj5);
		
		assertEquals("1", dualKeyMap.get("1").getValue());
		assertEquals("2", dualKeyMap.get("2").getValue());
		assertEquals("3", dualKeyMap.get("3").getValue());
		assertEquals("4", dualKeyMap.get("4").getValue());
		assertEquals("5", dualKeyMap.get("5").getValue());
		
		Map<String, DualKeyObject> map1 = dualKeyMap.getMap("acc2");
		assertEquals(null, map1.get("1"));
		assertEquals(null, map1.get("2"));
		assertEquals("3", map1.get("3").getValue());
		assertEquals("4", map1.get("4").getValue());
		assertEquals(null, map1.get("5"));
		
		Map<String, DualKeyObject> map2 = dualKeyMap.getMap("xxx");
		assertEquals(0, map2.size());
		
		DualKeyObject obj6 = new DualKeyObject("3", "acc2", "6");
		dualKeyMap.put(obj6.getId(), obj6.getAccount(), obj6);
		assertEquals("6", dualKeyMap.get("3").getValue());
	}
	
	
}
