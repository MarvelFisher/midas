package com.cyanspring.common.refdata;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mock;

import org.junit.Before;
import org.junit.Test;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.policy.OddsContractPolicy;

public class OddsContractPolicyTest {

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

		List<String> lstContractMonth = policy.getContractMonths(data);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

}
