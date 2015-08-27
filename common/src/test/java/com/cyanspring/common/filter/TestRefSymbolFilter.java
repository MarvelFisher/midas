package com.cyanspring.common.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INFO/spring/RefSymbolFilterTest.xml" })
public class TestRefSymbolFilter {
	
	@Autowired
	@Qualifier("refSymbolFilter")
	IRefDataFilter iDataFilter;
	
	RefData refData1;
	RefData refData2;
	List<RefData> lstRefData;

	boolean flagAG = false;
	boolean flagAG11 = false;
	
	@Before
	public void before() {
		lstRefData = new ArrayList<RefData>();
	}
	
	@Test
	public void testRefDataFilter() throws Exception {
		// Will be replaced by 活躍 one later
		refData1 = new RefData();
		refData1.setIType(IType.FUTURES_CX.getValue());
		refData1.setSymbol("IF1502");
		refData1.setCategory("AG");
		refData1.setExchange("SHF");
		refData1.setRefSymbol("AG12.SHF");

		// AG 活躍
		refData2 = new RefData();
		refData2.setIType(IType.FUTURES_CX.getValue());
		refData2.setSymbol("IF1502");
		refData2.setCategory("AG");
		refData2.setExchange("SHF");
		refData2.setRefSymbol("AG.SHF");

		lstRefData.add(refData1);
		lstRefData.add(refData2);
		assertEquals(2, lstRefData.size());
		
		List<RefData> lstFilteredRefData = (List<RefData>) iDataFilter.filter(lstRefData);
		assertEquals(1, lstFilteredRefData.size());
		
		for (RefData refData : lstFilteredRefData) {
			if (refData.getRefSymbol().equals("AG.SHF")) {
				flagAG = true;
			} else if (refData.getRefSymbol().equals("AG11.SHF")) {
				flagAG11 = true;
			}
		}
		
		assertTrue(flagAG);
		assertFalse(flagAG11); // since symbol doesn't exist in FcRefDataTemplate and were excluded.
	}
}
