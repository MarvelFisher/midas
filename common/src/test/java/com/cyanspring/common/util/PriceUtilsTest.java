package com.cyanspring.common.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PriceUtilsTest {

	@Test
	public void testInt() {
		assertTrue(!PriceUtils.Equal(1, 2, 0.1));
		assertTrue(PriceUtils.Equal(3, 3, 0.1));
		assertTrue(PriceUtils.Equal(6, 6, 0.01));
		assertTrue(PriceUtils.Equal(9, 9, 0.001));
	}

	@Test
	public void testDoubleThree() {
		assertTrue(!PriceUtils.Equal(1.0, 1.01, 0.001));
		assertTrue(!PriceUtils.Equal(1.01, 1.0, 0.001));
	}
	
	@Test
	public void testDoubleFive() {
		assertTrue(PriceUtils.Equal(8556.0,  8556.0000002, 0.0001));
	}

}
