package com.cyanspring.common.staticdata;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INFO/spring/RefDataFactoryTest.xml" })
public class TestRefDataFactory {

	protected static final Logger log = LoggerFactory.getLogger(TestRefDataFactory.class);
	List<RefData> refDataList = new CopyOnWriteArrayList<>();

	@Autowired
	RefDataFactory factory;

	@Test
	public void validateRefDataTemplateMap() throws Exception {
		// Only allow category INDEX to have multiple records in single template
		factory.init();
		Map<String, Map<String, List<RefData>>> refDataTemplateMap = factory.getTemplateMap();
		
		for (Entry<String, Map<String, List<RefData>>> eMap : refDataTemplateMap.entrySet()) {
			Map<String, List<RefData>> lstRefDataTemplateMap = eMap.getValue();
			Set<String> keys = lstRefDataTemplateMap.keySet();
			for (String key : keys) {
				List<RefData> lstRefData = lstRefDataTemplateMap.get(key);
				if (!key.equals("INDEX") && lstRefData.size() > 1) {
					fail("Categoy " + key + " is not allowed to have multiple entries in template_FC");
				}
			}
		}
	}

}
