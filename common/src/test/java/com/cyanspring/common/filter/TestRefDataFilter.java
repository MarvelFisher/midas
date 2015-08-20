package com.cyanspring.common.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INFO/spring/RefDataFilterTest.xml" })
public class TestRefDataFilter {

	@Autowired
	IDataObjectFilter iDataFilter;
	
	RefData refData1;
	RefData refData2;
	RefData refData3;
	List<RefData> lstRefData;
	
	@Before
	public void executeBeforeEachTest() {
		lstRefData = new ArrayList<RefData>();
	}
	
	@Test
	public void testRefDataFilter() throws Exception {
		refData1 = new RefData();
		refData1.setIType(IType.FUTURES_CX.getValue());
		refData1.setSymbol("ag1512.SHF");
		refData1.setCategory("AG");
		refData1.setExchange("SHF");
		refData1.setRefSymbol("AG12.SHF");

		refData2 = new RefData();
		refData2.setIType(IType.FUTURES.getValue());
		refData2.setSymbol("ag1511.SHF");
		refData2.setCategory("AG");
		refData2.setExchange("SHF");
		refData2.setRefSymbol("AG11.SHF");

		// AG 活躍
		refData3 = new RefData();
		refData3.setIType(IType.FUTURES_CX.getValue());
		refData3.setSymbol("ag1512.SHF");
		refData3.setCategory("AG");
		refData3.setExchange("SHF");
		refData3.setRefSymbol("AG.SHF");

		lstRefData.add(refData1);
		lstRefData.add(refData2);
		lstRefData.add(refData3);
		assertEquals(3, lstRefData.size());

		@SuppressWarnings("unchecked")
		List<RefData> lstFilteredRefData = (List<RefData>) iDataFilter.filter(lstRefData);
		assertEquals(2, lstFilteredRefData.size());
		
		boolean flagAG = false;
		boolean flagAG11 = false;
		boolean flagAG12 = false;
		
		for (RefData refData : lstFilteredRefData) {
			if (refData.getRefSymbol().equals("AG.SHF")) {
				flagAG = true;
			} else if (refData.getRefSymbol().equals("AG11.SHF")) {
				flagAG11 = true;
			} else if (refData.getRefSymbol().equals("AG12.SHF")) {
				flagAG12 = true;
			}
		}
		
		assertTrue(flagAG);
		assertTrue(flagAG11);
		assertFalse(flagAG12); // since it should have been replaced by the 活躍 one
	}
	
	@Test
	public void testDuplicateItemHandling() throws Exception {
		refData1 = new RefData();
		refData1.setIType(IType.FUTURES_CX.getValue());
		refData1.setSymbol("ag1512.SHF");
		refData1.setCategory("AG");
		refData1.setExchange("SHF");
		refData1.setRefSymbol("AG12.SHF");

		refData2 = new RefData();
		refData2.setIType(IType.FUTURES_CX.getValue());
		refData2.setSymbol("ag1512.SHF");
		refData2.setCategory("AG");
		refData2.setExchange("SHF");
		refData2.setRefSymbol("AG12.SHF");
		
		lstRefData.add(refData1);
		lstRefData.add(refData2);
		
		@SuppressWarnings("unchecked")
		List<RefData> lstFilteredRefData = (List<RefData>) iDataFilter.filter(lstRefData);
		
		assertEquals(1, lstFilteredRefData.size());
	}
	
	@Test
	public void testRefDataFilterErrorHandling() {
		refData1 = new RefData();
		lstRefData.add(refData1);
		
		try {
			iDataFilter.filter(lstRefData);
			fail("DataObjectException was not thrown expectedly while IType is null");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		refData1.setIType(IType.FUTURES_CX.getValue());
		try {
			iDataFilter.filter(lstRefData);
			fail("DataObjectException was not thrown expectedly while Symbol is null");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		refData1.setSymbol("ag1512.SHF");
		try {
			iDataFilter.filter(lstRefData);
			fail("DataObjectException was not thrown expectedly while Category is null");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		refData1.setCategory("AG");
		try {
			iDataFilter.filter(lstRefData);
			fail("DataObjectException was not thrown expectedly while Exchange is null");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		refData1.setExchange("SHF");
		try {
			iDataFilter.filter(lstRefData);
			fail("DataObjectException was not thrown expectedly while RefSymbol is null");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		refData1.setRefSymbol("AG12.SHF");
		try {
			iDataFilter.filter(lstRefData);
		} catch (Exception e) {
			// TODO: handle exception
			fail("DataObjectException was thrown unexpectedly");
		}
	}

}
