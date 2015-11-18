package com.cyanspring.common.refdata;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mock;

import org.junit.Before;
import org.junit.Test;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.policy.IndexContractPolicy;

public class IndexContractPolicyTest {

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

		List<String> lstContractMonth = policy.getContractMonths(data);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

}
