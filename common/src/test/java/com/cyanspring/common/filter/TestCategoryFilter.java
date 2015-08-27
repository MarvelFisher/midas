package com.cyanspring.common.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
@ContextConfiguration(locations = { "classpath:META-INFO/spring/RefDataFilterTest.xml" })
public class TestCategoryFilter {

	@Autowired
	@Qualifier("categoryFilter")
	IRefDataFilter iDataFilter;
	
	RefData refData1;
	RefData refData2;
	RefData refData3;
	List<RefData> lstRefData;
	
//	@BeforeClass
//	public static void beforeClass() {
//		new MockUp<FuturesRefDataFilter>() {
//			@Mock
//			private boolean isValidContractDate(RefData refData) {
//				return true;
//			}
//		};
//	}
	
	@Before
	public void before() {
		lstRefData = new ArrayList<RefData>();
	}
	
	@Test
	public void testRefDataFilter() throws Exception {
		refData1 = new RefData();
		refData1.setIType(IType.FUTURES_CX.getValue());
		refData1.setSymbol("IF1502");
		refData1.setCategory("AG");
		refData1.setExchange("SHF");
		refData1.setRefSymbol("AG12.SHF");

		// This record doesn't exist in FcRefDataTemplate thus will be excluded.
		refData2 = new RefData();
		refData2.setIType(IType.FUTURES.getValue());
		refData2.setSymbol("ag1511.SHF");
		refData2.setCategory("BG");
		refData2.setExchange("SHF");
		refData2.setRefSymbol("AG11.SHF");

		// AG 活躍
		refData3 = new RefData();
		refData3.setIType(IType.FUTURES_CX.getValue());
		refData3.setSymbol("IF1502");
		refData3.setCategory("AG");
		refData3.setExchange("SHF");
		refData3.setRefSymbol("AG.SHF");

		lstRefData.add(refData1);
		lstRefData.add(refData2);
		lstRefData.add(refData3);
		assertEquals(3, lstRefData.size());

		List<RefData> lstFilteredRefData = (List<RefData>) iDataFilter.filter(lstRefData);
		assertEquals(2, lstFilteredRefData.size());
	}
	
	@Test
	public void testRefDataFilterErrorHandling() {
		refData1 = new RefData();
		lstRefData.add(refData1);
		
		try {
			iDataFilter.filter(lstRefData);
			fail("DataObjectException was not thrown expectedly while Category is null");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		refData1.setCategory("AG");
		try {
			iDataFilter.filter(lstRefData);
		} catch (Exception e) {
			fail("DataObjectException was thrown unexpectedly while Category is not null");
		}
	}
	
}
