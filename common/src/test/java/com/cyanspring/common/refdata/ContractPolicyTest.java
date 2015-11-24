package com.cyanspring.common.refdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mock;

import org.junit.Before;
import org.junit.Test;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.policy.AUContractPolicy;
import com.cyanspring.common.staticdata.policy.BUContractPolicy;
import com.cyanspring.common.staticdata.policy.DefaultContractPolicy;
import com.cyanspring.common.staticdata.policy.IndexContractPolicy;
import com.cyanspring.common.staticdata.policy.OddsContractPolicy;
import com.cyanspring.common.staticdata.policy.TXContractPolicy;

public class ContractPolicyTest {

	RefData data;
	Calendar cal;

	@Before
	public void Before() {
		data = new RefData();
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2016);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 1);
	}

	@Test
	public void testAUContractPolicy() {
		AUContractPolicy policy = new AUContractPolicy() {
			@Mock
			public Calendar getFirstContractMonth(RefData refData) {
				return cal;
			}
		};

		List<String> lstExpectedContractMonth = new ArrayList<String>() {{
			add("201601");
			add("201602");
			add("201603");
			add("201604");
			add("201606");
			add("201608");
			add("201610");
			add("201612");
		}};

		List<String> lstContractMonth = null;
		try {
			lstContractMonth = policy.getContractMonths(data);
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(lstContractMonth);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

	@Test
	public void testBUContractPolicy() {
		BUContractPolicy policy = new BUContractPolicy() {
			@Mock
			public Calendar getFirstContractMonth(RefData refData) {
				return cal;
			}
		};

		List<String> lstExpectedContractMonth = new ArrayList<String>() {{
			add("201601");
			add("201602");
			add("201603");
			add("201604");
			add("201605");
			add("201606");
			add("201609");
			add("201612");
			add("201703");
			add("201706");
			add("201709");
			add("201712");
		}};

		List<String> lstContractMonth = null;
		try {
			lstContractMonth = policy.getContractMonths(data);
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(lstContractMonth);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

	@Test
	public void testDefaultContractPolicy() {
		DefaultContractPolicy policy = new DefaultContractPolicy() {
			@Mock
			public Calendar getFirstContractMonth(RefData refData) {
				return cal;
			}

			@Mock
			public List<Integer> getContractMonths() {
				List<Integer> contractMonths = new ArrayList<>();
				for (int i = 0; i < 12; i++) {
					Calendar c = (Calendar) cal.clone();
					c.add(Calendar.MONTH, i);
					contractMonths.add(c.get(Calendar.MONTH));
				}
				return contractMonths;
			}
		};

		Calendar cal = null;
		try {
			cal = policy.getFirstContractMonth(data);
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		List<String> lstExpectedContractMonth = new ArrayList<>();

		for (int i = 0; i < 12; i++) {
			Calendar c = (Calendar) cal.clone();
			c.add(Calendar.MONTH, i);
			lstExpectedContractMonth.add(policy.ymSdf.format(c.getTime()));
		}

		List<Integer> contractMonths = policy.getContractMonths();
		policy.setContractMonths(contractMonths);

		List<String> lstContractMonth = null;
		try {
			lstContractMonth = policy.getContractMonths(data);
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(lstContractMonth);
		assertTrue(lstContractMonth.size() == 12);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

	@Test
	public void testIndexContractPolicy() {
		IndexContractPolicy policy = new IndexContractPolicy() {
			@Mock
			public Calendar getFirstContractMonth(RefData refData) {
				return cal;
			}
		};

		List<String> lstExpectedContractMonth = new ArrayList<String>() {{
			add("201601");
			add("201602");
			add("201603");
			add("201606");
		}};

		List<String> lstContractMonth = null;
		try {
			lstContractMonth = policy.getContractMonths(data);
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(lstContractMonth);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

	@Test
	public void testOddsContractPolicy() {
		OddsContractPolicy policy = new OddsContractPolicy() {
			@Mock
			public Calendar getFirstContractMonth(RefData refData) {
				return cal;
			}
		};

		List<String> lstExpectedContractMonth = new ArrayList<String>() {{
			add("201601");
			add("201603");
			add("201605");
			add("201607");
			add("201609");
			add("201611");
		}};

		List<String> lstContractMonth = null;
		try {
			lstContractMonth = policy.getContractMonths(data);
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(lstContractMonth);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

	@Test
	public void testTXContractPolicy() {
		TXContractPolicy policy = new TXContractPolicy() {
			@Mock
			public Calendar getFirstContractMonth(RefData refData) {
				return cal;
			}
		};

		List<String> lstExpectedContractMonth = new ArrayList<String>() {{
			add("201601");
			add("201602");
			add("201603");
			add("201606");
			add("201609");
		}};

		List<String> lstContractMonth = null;
		try {
			lstContractMonth = policy.getContractMonths(data);
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(lstContractMonth);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

}
