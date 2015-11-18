package com.cyanspring.common.staticdata.policy;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mock;

import org.junit.Before;
import org.junit.Test;

import com.cyanspring.common.staticdata.RefData;

public class TXContractPolicyTest {

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
	public void test() {
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

		List<String> lstContractMonth = policy.getContractMonths(data);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

}
