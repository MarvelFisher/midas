package com.cyanspring.common.filter;

import static org.junit.Assert.assertEquals;

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
	RefDataFilter refDataFilter;
	
	RefData refData1;
	RefData refData2;
	RefData refData3;
	RefData refData4;
	List<RefData> lstRefData;
	
	@Before
	public void before() {
		lstRefData = new ArrayList<RefData>();
	}

	@Test
	public void test() throws Exception {
		// Will be replaced by 活躍 one later
		refData1 = new RefData();
		refData1.setIType(IType.FUTURES_CX.getValue());
		refData1.setSymbol("IF1502");
		refData1.setCategory("AG");
		refData1.setExchange("SHF");
		refData1.setRefSymbol("AG12.SHF");
		refData1.setSettlementDate("2017-08-21");

		refData2 = new RefData();
		refData2.setIType(IType.FUTURES_CX.getValue());
		refData2.setSymbol("ag1511.SHF");
		refData2.setCategory("AG");
		refData2.setExchange("SHF");
		refData2.setRefSymbol("AG11.SHF");
		refData2.setSettlementDate("2017-08-21");

		// AG 活躍
		refData3 = new RefData();
		refData3.setIType(IType.FUTURES_CX.getValue());
		refData3.setSymbol("IF1502");
		refData3.setCategory("AG");
		refData3.setExchange("SHF");
		refData3.setRefSymbol("AG.SHF");
		refData3.setSettlementDate("2017-08-21");
		
		// Non-existing Category
		refData4 = new RefData();
		refData4.setIType(IType.FUTURES_CX.getValue());
		refData4.setSymbol("IF1502");
		refData4.setCategory("BG");
		refData4.setExchange("SHF");
		refData4.setRefSymbol("AG.SHF");
		refData4.setSettlementDate("2017-08-21");

		lstRefData.add(refData1);
		lstRefData.add(refData2);
		lstRefData.add(refData3);
		lstRefData.add(refData4);
		assertEquals(4, lstRefData.size());
		
		lstRefData = refDataFilter.filter(lstRefData);
		
		assertEquals(2, lstRefData.size());
		assertEquals("AG.SHF", lstRefData.get(1).getRefSymbol());
	}
	
}
