package com.cyanspring.common.refdata;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataBitUtil;
import com.cyanspring.common.staticdata.strategy.AbstractRefDataStrategy;

import mockit.Mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INFO/spring/HotRefDataTest.xml")
public class HotRefDataTest {
	
	public Map<String, Quote> qMap;
	public AbstractRefDataStrategy strategy;
	public RefData simRefData;
	
	@Autowired
	private MarketSessionUtil marketSessionUtil;
	
	@Before
	public void setUp() throws Exception {
		marketSessionUtil.init();
		Quote q = new Quote(null, null, null) {
			public double i;			
			@Mock
			public double getTotalVolume() {
				return i++;
			} 
		};
		
		qMap = new HashMap<>();
		qMap.put("*", q);
		
		strategy = new AbstractRefDataStrategy(){
			
		};
		
		simRefData = new RefData();
		simRefData.setCategory("*");
		simRefData.setContractPolicy("Default");
		simRefData.setSymbol("*");
		simRefData.setRefSymbol("*");
		simRefData.setENDisplayName("*");
		simRefData.setTWDisplayName("*");
		simRefData.setCNDisplayName("*");
		simRefData.setSettlementDate("2015-11-11");
	}
	
	@Test
	public void test() throws Exception {
		strategy.init(Calendar.getInstance(), qMap);
		List<RefData> refDataList = strategy.updateRefData(simRefData);
		RefData hot = refDataList.get(refDataList.size()-1);
		assertTrue(RefDataBitUtil.isHot(hot.getInstrumentType()));
	}
}
