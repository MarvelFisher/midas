package com.cyanspring.common.staticdata.fu;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataFactory;
import com.cyanspring.common.staticdata.fu.AbstractRefDataStrategy.Locale;
import com.cyanspring.common.staticdata.fu.AbstractRefDataStrategy.Type;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class AbstractRefDataStrategyTest extends AbstractRefDataStrategy{
	static{
		org.apache.log4j.BasicConfigurator.configure();
	}
    protected static final Logger log = LoggerFactory.getLogger(AbstractRefDataStrategyTest.class);
	public RefDataFactory fac = new RefDataFactory();
	private Map<String,RefData> refDataTemplateMap = new HashMap<String,RefData>();
	private RefData templateRef = null;
	private RefData refData = new RefData();
	public AbstractRefDataStrategyTest() throws Exception {
		String path = AbstractRefDataStrategyTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()
				+AbstractRefDataStrategyTest.class.getPackage().getName().replace(".", "/")+"/";
		fac.setRefDataTemplatePath(path+"FcRefDataTemplate.xml");
		fac.init();
		refDataTemplateMap = fac.getRefDataTemplateMap();
		templateRef = refDataTemplateMap.get("CU");
		
		//CodeTableMF.xml
		refData.setIType("122");
		refData.setCNDisplayName("沪银连四-ag1512");
		refData.setRefSymbol("AG04.SHF");
		refData.setExchange("SHF");
		refData.setCode("ag04");
		refData.setENDisplayName("AG.SHF");
	}
	@Test
	public void test() {
		init(Calendar.getInstance(), templateRef);	
		String combineCnName = refData.getCNDisplayName();
		String combineTwName = refData.getTWDisplayName();
		if(!StringUtils.hasText(combineTwName))
			combineTwName = combineCnName;
		
		String refSymbol = refData.getRefSymbol();
		
		assertEquals("ag1512.SHF", getSymbol(refData));
		assertEquals("SHF", refData.getExchange());
		assertEquals("AG", getCategory(refSymbol));
		assertEquals("沪铜1512", getCNName(combineCnName));
		assertEquals("滬銅1512", getTWName(combineTwName));
		assertEquals("AG1512", getEnName(refData));
		assertEquals("沪铜", getSpotName(combineCnName, Locale.CN));
		assertEquals("滬銅", getSpotName(combineTwName, Locale.TW));
		assertEquals("沪铜", getSpotName(combineCnName, Locale.CN));
		assertEquals("沪铜2015年2月合约",getCNDetailName(combineCnName));
		assertEquals("滬銅2015年2月合约",getTWDetailName(combineTwName));
		assertEquals("沪铜2015年2月合约",getCNDetailName(combineCnName));
	}

	
	
	
}
