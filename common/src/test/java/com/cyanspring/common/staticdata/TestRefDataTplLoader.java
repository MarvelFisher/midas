package com.cyanspring.common.staticdata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INFO/spring/RefDataTplLoaderTest.xml" })
public class TestRefDataTplLoader {
	
	@Autowired
	RefDataTplLoader refDataTplLoader;

	@Test
	public void testGetRefDataList() throws Exception {
		List<RefData> lstRefData = refDataTplLoader.getRefDataList();
		assertTrue(lstRefData != null);
		assertTrue(lstRefData.size() > 0);
	}
	
}
