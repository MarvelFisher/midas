package com.cyanspring.common.staticdata.policy;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mock;
import org.junit.Before;
import org.junit.Test;

import com.cyanspring.common.staticdata.RefData;

public class DefaultContractPolicyTest {

	RefData data;
	Calendar cal;

	@Before
	public void Before() {
		data = new RefData();
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
	}

	@Test
	public void test() {
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

		Calendar cal = policy.getFirstContractMonth(data);
		List<String> lstExpectedContractMonth = new ArrayList<>();

		for (int i = 0; i < 12; i++) {
			Calendar c = (Calendar) cal.clone();
			c.add(Calendar.MONTH, i);
			lstExpectedContractMonth.add(policy.ymSdf.format(c.getTime()));
		}

		List<Integer> contractMonths = policy.getContractMonths();
		policy.setContractMonths(contractMonths);

		List<String> lstContractMonth = policy.getContractMonths(data);
		assertTrue(lstContractMonth.size() == 12);
		lstExpectedContractMonth.removeAll(lstContractMonth);
		assertTrue(lstExpectedContractMonth.size() == 0);
	}

}
