package com.cyanspring.common.refdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataTplLoader;
import com.cyanspring.common.staticdata.policy.DefaultContractPolicy;
import com.cyanspring.common.staticdata.strategy.AbstractRefDataStrategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INFO/spring/RefDataTplLoaderTest.xml" })
public class GenRefDataListTest {

	class RefDataStrategy extends AbstractRefDataStrategy {
	}

	@Autowired
	RefDataTplLoader refDataTplLoader;

	@Autowired
	RefDataTplLoader refDataTplLoaderFT;

	List<RefData> lstRefData;

	RefData data;
	Calendar cal;

	@Before
	public void before() {
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2016);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 1);

		new MockUp<DefaultContractPolicy>() {
			@Mock
			public Calendar getFirstContractMonth(RefData refData) {
				return cal;
			}
		};

		try {
			lstRefData = refDataTplLoader.getRefDataList();
		} catch (Exception e) {
			fail("Failed loading RefData template");
		}
	}

	@Test
	public void testAUContractPolicy() {
		List<RefData> lstAUTemplate = new ArrayList<>();
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null && d.getContractPolicy().equals("AU")) {
				RefData data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				lstAUTemplate.add(data);
			}
		}

		assertTrue(lstAUTemplate.size() > 0);

		List<String> lstExpectedAUContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1602");
			add("1603");
			add("1604");
			add("1606");
			add("1608");
			add("1610");
			add("1612");
		}};

		List<String> lstExpectedAUContractMonthCZC = new ArrayList<String>() {{
			add("601");
			add("602");
			add("603");
			add("604");
			add("606");
			add("608");
			add("610");
			add("612");
		}};

		RefDataStrategy strategy = new RefDataStrategy();

		for (RefData data : lstAUTemplate) {
			List<RefData> lstAURefData = strategy.updateRefData(data);
			assertNotNull(lstAURefData);
			int size = lstAURefData.size();
			assertTrue(size == 8);

			NumberFormat formatter = new DecimalFormat("##00");
			for (int i = 0; i < size; i++) {
				RefData d = lstAURefData.get(i);
				String yymm = lstExpectedAUContractMonth.get(i);
				String ymm = lstExpectedAUContractMonthCZC.get(i);
				String seq = formatter.format(i);
				if (d.getExchange().equals("CZC")) {
					assertTrue(d.getSymbol().contains(ymm));
				} else {
					assertTrue(d.getSymbol().contains(yymm));
				}
				assertTrue(d.getRefSymbol().contains(seq));
				assertTrue(d.getENDisplayName().contains(yymm));
				assertTrue(d.getCNDisplayName().contains(yymm));
				assertTrue(d.getTWDisplayName().contains(yymm));
			}
		}
	}

	@Test
	public void testBUContractPolicy() {
		List<RefData> lstBUTemplate = new ArrayList<>();
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null && d.getContractPolicy().equals("BU")) {
				RefData data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				lstBUTemplate.add(data);
			}
		}

		assertTrue(lstBUTemplate.size() > 0);

		List<String> lstExpectedBUContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1602");
			add("1603");
			add("1604");
			add("1605");
			add("1606");
			add("1609");
			add("1612");
			add("1703");
			add("1706");
			add("1709");
			add("1712");
		}};

		List<String> lstExpectedBUContractMonthCZC = new ArrayList<String>() {{
			add("601");
			add("602");
			add("603");
			add("604");
			add("605");
			add("606");
			add("609");
			add("612");
			add("703");
			add("706");
			add("709");
			add("712");
		}};

		RefDataStrategy strategy = new RefDataStrategy();

		for (RefData data : lstBUTemplate) {
			List<RefData> lstBURefData = strategy.updateRefData(data);
			assertNotNull(lstBURefData);
			int size = lstBURefData.size();
			assertTrue(size == 12);

			NumberFormat formatter = new DecimalFormat("##00");
			for (int i = 0; i < size; i++) {
				RefData d = lstBURefData.get(i);
				String yymm = lstExpectedBUContractMonth.get(i);
				String ymm = lstExpectedBUContractMonthCZC.get(i);
				String seq = formatter.format(i);
				if (d.getExchange().equals("CZC")) {
					assertTrue(d.getSymbol().contains(ymm));
				} else {
					assertTrue(d.getSymbol().contains(yymm));
				}
				assertTrue(d.getRefSymbol().contains(seq));
				assertTrue(d.getENDisplayName().contains(yymm));
				assertTrue(d.getCNDisplayName().contains(yymm));
				assertTrue(d.getTWDisplayName().contains(yymm));
			}
		}
	}

	@Test
	public void testIndexContractPolicy() {
		List<RefData> lstIndexTemplate = new ArrayList<>();
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null && d.getContractPolicy().equals("Index")) {
				RefData data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				lstIndexTemplate.add(data);
			}
		}

		assertTrue(lstIndexTemplate.size() > 0);

		List<String> lstExpectedIndexContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1602");
			add("1603");
			add("1606");
		}};

		List<String> lstExpectedIndexContractMonthCZC = new ArrayList<String>() {{
			add("601");
			add("602");
			add("603");
			add("606");
		}};

		RefDataStrategy strategy = new RefDataStrategy();

		for (RefData data : lstIndexTemplate) {
			List<RefData> lstIndexRefData = strategy.updateRefData(data);
			assertNotNull(lstIndexRefData);
			int size = lstIndexRefData.size();
			assertTrue(size == 4);

			NumberFormat formatter = new DecimalFormat("##00");
			for (int i = 0; i < size; i++) {
				RefData d = lstIndexRefData.get(i);
				String yymm = lstExpectedIndexContractMonth.get(i);
				String ymm = lstExpectedIndexContractMonthCZC.get(i);
				String seq = formatter.format(i);
				if (d.getExchange().equals("CZC")) {
					assertTrue(d.getSymbol().contains(ymm));
				} else {
					assertTrue(d.getSymbol().contains(yymm));
				}
				assertTrue(d.getRefSymbol().contains(seq));
				assertTrue(d.getENDisplayName().contains(yymm));
				assertTrue(d.getCNDisplayName().contains(yymm));
				assertTrue(d.getTWDisplayName().contains(yymm));
			}
		}
	}

	@Test
	public void testOddsContractPolicy() {
		List<RefData> lstOddsTemplate = new ArrayList<>();
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null && d.getContractPolicy().equals("Odds")) {
				RefData data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				lstOddsTemplate.add(data);
			}
		}

		assertTrue(lstOddsTemplate.size() > 0);

		List<String> lstExpectedOddsContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1603");
			add("1605");
			add("1607");
			add("1609");
			add("1611");
		}};

		List<String> lstExpectedOddsContractMonthCZC = new ArrayList<String>() {{
			add("601");
			add("603");
			add("605");
			add("607");
			add("609");
			add("611");
		}};

		RefDataStrategy strategy = new RefDataStrategy();

		for (RefData data : lstOddsTemplate) {
			List<RefData> lstOddsRefData = strategy.updateRefData(data);
			assertNotNull(lstOddsRefData);
			int size = lstOddsRefData.size();
			assertTrue(size == 6);

			NumberFormat formatter = new DecimalFormat("##00");
			for (int i = 0; i < size; i++) {
				RefData d = lstOddsRefData.get(i);
				String yymm = lstExpectedOddsContractMonth.get(i);
				String ymm = lstExpectedOddsContractMonthCZC.get(i);
				String seq = formatter.format(i);
				if (d.getExchange().equals("CZC")) {
					assertTrue(d.getSymbol().contains(ymm));
				} else {
					assertTrue(d.getSymbol().contains(yymm));
				}
				assertTrue(d.getRefSymbol().contains(seq));
				assertTrue(d.getENDisplayName().contains(yymm));
				assertTrue(d.getCNDisplayName().contains(yymm));
				assertTrue(d.getTWDisplayName().contains(yymm));
			}
		}
	}

	@Test
	public void testTXContractPolicy() {
		try {
			lstRefData = refDataTplLoaderFT.getRefDataList();
		} catch (Exception e) {
			fail("Failed loading RefData template");
		}

		List<RefData> lstTXTemplate = new ArrayList<>();
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null && d.getContractPolicy().equals("TX")) {
				RefData data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				lstTXTemplate.add(data);
			}
		}

		assertTrue(lstTXTemplate.size() > 0);

		List<String> lstExpectedTXContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1602");
			add("1603");
			add("1606");
			add("1609");
		}};

		List<String> lstExpectedTXSeq = new ArrayList<String>() {{
			add("A6");
			add("B6");
			add("C6");
			add("F6");
			add("I6");
		}};

		RefDataStrategy strategy = new RefDataStrategy();

		for (RefData data : lstTXTemplate) {
			List<RefData> lstTXRefData = strategy.updateRefData(data);
			assertNotNull(lstTXRefData);
			int size = lstTXRefData.size();
			assertTrue(size == 5);

			NumberFormat formatter = new DecimalFormat("##00");
			for (int i = 0; i < size; i++) {
				RefData d = lstTXRefData.get(i);
				String yymm = lstExpectedTXContractMonth.get(i);
				String seq = formatter.format(i);
				assertTrue(d.getSymbol().contains(lstExpectedTXSeq.get(i)));
				assertTrue(d.getRefSymbol().contains(seq));
				assertTrue(d.getENDisplayName().contains(yymm));
				assertTrue(d.getCNDisplayName().contains(yymm));
				assertTrue(d.getTWDisplayName().contains(yymm));
				assertTrue(d.getCode().contains(yymm));
				assertTrue(d.getSubscribeSymbol().contains(yymm));
			}
		}
	}

	@Test
	public void testDefaultContractPolicy() {
		List<String> lstExCategory = new ArrayList<String>() {{
			add("JD");
			add("M");
			add("Y");
			add("RM");
			add("RS");
			add("RU");
			add("FU");
		}};

		List<RefData> lstDefaultTemplate = new ArrayList<>();
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null
					&& d.getContractPolicy().equals("Default")
					&& !lstExCategory.contains(d.getCategory())) {
				RefData data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				lstDefaultTemplate.add(data);
			}
		}

		assertTrue(lstDefaultTemplate.size() > 0);

		List<String> lstExpectedDefaultContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1602");
			add("1603");
			add("1604");
			add("1605");
			add("1606");
			add("1607");
			add("1608");
			add("1609");
			add("1610");
			add("1611");
			add("1612");
		}};

		List<String> lstExpectedDefaultContractMonthCZC = new ArrayList<String>() {{
			add("601");
			add("602");
			add("603");
			add("604");
			add("605");
			add("606");
			add("607");
			add("608");
			add("609");
			add("610");
			add("611");
			add("612");
		}};

		RefDataStrategy strategy = new RefDataStrategy();

		for (RefData data : lstDefaultTemplate) {
			List<RefData> lstDefaultRefData = strategy.updateRefData(data);
			assertNotNull(lstDefaultRefData);
			int size = lstDefaultRefData.size();
			assertTrue(size == 12);

			NumberFormat formatter = new DecimalFormat("##00");
			for (int i = 0; i < size; i++) {
				RefData d = lstDefaultRefData.get(i);
				String yymm = lstExpectedDefaultContractMonth.get(i);
				String ymm = lstExpectedDefaultContractMonthCZC.get(i);
				String seq = formatter.format(i);
				if (d.getExchange().equals("CZC")) {
					assertTrue(d.getSymbol().contains(ymm));
				} else {
					assertTrue(d.getSymbol().contains(yymm));
				}
				assertTrue(d.getRefSymbol().contains(seq));
				assertTrue(d.getENDisplayName().contains(yymm));
				assertTrue(d.getCNDisplayName().contains(yymm));
				assertTrue(d.getTWDisplayName().contains(yymm));
			}
		}
	}

	@Test
	public void testDefaultContractPolicyJD() {
		RefData data = null;
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null
					&& d.getContractPolicy().equals("Default")
					&& d.getCategory().equals("JD")) {
				data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				break;
			}
		}

		assertNotNull(data);

		List<String> lstExpectedDefaultContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1602");
			add("1603");
			add("1604");
			add("1605");
			add("1606");
			add("1609");
			add("1610");
			add("1611");
			add("1612");
		}};

		RefDataStrategy strategy = new RefDataStrategy();
		List<RefData> lstDefaultRefData = strategy.updateRefData(data);
		assertNotNull(lstDefaultRefData);
		int size = lstDefaultRefData.size();
		assertTrue(size == 10);

		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < size; i++) {
			RefData d = lstDefaultRefData.get(i);
			String yymm = lstExpectedDefaultContractMonth.get(i);
			String seq = formatter.format(i);
			assertTrue(d.getSymbol().contains(yymm));
			assertTrue(d.getRefSymbol().contains(seq));
			assertTrue(d.getENDisplayName().contains(yymm));
			assertTrue(d.getCNDisplayName().contains(yymm));
			assertTrue(d.getTWDisplayName().contains(yymm));
		}
	}

	@Test
	public void testDefaultContractPolicyMY() {
		RefData data = null;
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null
					&& d.getContractPolicy().equals("Default")
					&& (d.getCategory().equals("M") || d.getCategory().equals("Y"))) {
				data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				break;
			}
		}

		assertNotNull(data);

		List<String> lstExpectedDefaultContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1603");
			add("1605");
			add("1607");
			add("1608");
			add("1609");
			add("1611");
			add("1612");
		}};

		RefDataStrategy strategy = new RefDataStrategy();
		List<RefData> lstDefaultRefData = strategy.updateRefData(data);
		assertNotNull(lstDefaultRefData);
		int size = lstDefaultRefData.size();
		assertTrue(size == 8);

		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < size; i++) {
			RefData d = lstDefaultRefData.get(i);
			String yymm = lstExpectedDefaultContractMonth.get(i);
			String seq = formatter.format(i);
			assertTrue(d.getSymbol().contains(yymm));
			assertTrue(d.getRefSymbol().contains(seq));
			assertTrue(d.getENDisplayName().contains(yymm));
			assertTrue(d.getCNDisplayName().contains(yymm));
			assertTrue(d.getTWDisplayName().contains(yymm));
		}
	}

	@Test
	public void testDefaultContractPolicyRM() {
		RefData data = null;
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null
					&& d.getContractPolicy().equals("Default")
					&& d.getCategory().equals("RM")) {
				data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				break;
			}
		}

		assertNotNull(data);

		List<String> lstExpectedDefaultContractMonth = new ArrayList<String>() {{
			add("601");
			add("603");
			add("605");
			add("607");
			add("608");
			add("609");
			add("611");
		}};

		RefDataStrategy strategy = new RefDataStrategy();
		List<RefData> lstDefaultRefData = strategy.updateRefData(data);
		assertNotNull(lstDefaultRefData);
		int size = lstDefaultRefData.size();
		assertTrue(size == 7);

		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < size; i++) {
			RefData d = lstDefaultRefData.get(i);
			String yymm = lstExpectedDefaultContractMonth.get(i);
			String seq = formatter.format(i);
			assertTrue(d.getSymbol().contains(yymm));
			assertTrue(d.getRefSymbol().contains(seq));
			assertTrue(d.getENDisplayName().contains(yymm));
			assertTrue(d.getCNDisplayName().contains(yymm));
			assertTrue(d.getTWDisplayName().contains(yymm));
		}
	}

	@Test
	public void testDefaultContractPolicyRS() {
		RefData data = null;
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null
					&& d.getContractPolicy().equals("Default")
					&& d.getCategory().equals("RS")) {
				data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				break;
			}
		}

		assertNotNull(data);

		List<String> lstExpectedDefaultContractMonth = new ArrayList<String>() {{
			add("607");
			add("608");
			add("609");
			add("611");
		}};

		RefDataStrategy strategy = new RefDataStrategy();
		List<RefData> lstDefaultRefData = strategy.updateRefData(data);
		assertNotNull(lstDefaultRefData);
		int size = lstDefaultRefData.size();
		assertTrue(size == 4);

		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < size; i++) {
			RefData d = lstDefaultRefData.get(i);
			String yymm = lstExpectedDefaultContractMonth.get(i);
			String seq = formatter.format(i);
			assertTrue(d.getSymbol().contains(yymm));
			assertTrue(d.getRefSymbol().contains(seq));
			assertTrue(d.getENDisplayName().contains(yymm));
			assertTrue(d.getCNDisplayName().contains(yymm));
			assertTrue(d.getTWDisplayName().contains(yymm));
		}
	}

	@Test
	public void testDefaultContractPolicyRU() {
		RefData data = null;
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null
					&& d.getContractPolicy().equals("Default")
					&& d.getCategory().equals("RU")) {
				data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				break;
			}
		}

		assertNotNull(data);

		List<String> lstExpectedDefaultContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1603");
			add("1604");
			add("1605");
			add("1606");
			add("1607");
			add("1608");
			add("1609");
			add("1610");
			add("1611");
		}};

		RefDataStrategy strategy = new RefDataStrategy();
		List<RefData> lstDefaultRefData = strategy.updateRefData(data);
		assertNotNull(lstDefaultRefData);
		int size = lstDefaultRefData.size();
		assertTrue(size == 10);

		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < size; i++) {
			RefData d = lstDefaultRefData.get(i);
			String yymm = lstExpectedDefaultContractMonth.get(i);
			String seq = formatter.format(i);
			assertTrue(d.getSymbol().contains(yymm));
			assertTrue(d.getRefSymbol().contains(seq));
			assertTrue(d.getENDisplayName().contains(yymm));
			assertTrue(d.getCNDisplayName().contains(yymm));
			assertTrue(d.getTWDisplayName().contains(yymm));
		}
	}

	@Test
	public void testDefaultContractPolicyFU() {
		RefData data = null;
		for (RefData d : lstRefData) {
			if (d.getContractPolicy() != null
					&& d.getContractPolicy().equals("Default")
					&& d.getCategory().equals("FU")) {
				data = (RefData) d.clone();
				data.setSettlementDate("2016-01-31");
				break;
			}
		}

		assertNotNull(data);

		List<String> lstExpectedDefaultContractMonth = new ArrayList<String>() {{
			add("1601");
			add("1603");
			add("1604");
			add("1605");
			add("1606");
			add("1607");
			add("1608");
			add("1609");
			add("1610");
			add("1611");
			add("1612");
		}};

		RefDataStrategy strategy = new RefDataStrategy();
		List<RefData> lstDefaultRefData = strategy.updateRefData(data);
		assertNotNull(lstDefaultRefData);
		int size = lstDefaultRefData.size();
		assertTrue(size == 11);

		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < size; i++) {
			RefData d = lstDefaultRefData.get(i);
			String yymm = lstExpectedDefaultContractMonth.get(i);
			String seq = formatter.format(i);
			assertTrue(d.getSymbol().contains(yymm));
			assertTrue(d.getRefSymbol().contains(seq));
			assertTrue(d.getENDisplayName().contains(yymm));
			assertTrue(d.getCNDisplayName().contains(yymm));
			assertTrue(d.getTWDisplayName().contains(yymm));
		}
	}

}
