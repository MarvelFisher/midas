package refdata;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataFactory;
import com.cyanspring.common.staticdata.strategy.AbstractRefDataStrategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INFO/spring/SettlementDayTest.xml")
public class SettlementDayTest {
	
	@Autowired
	private HashMap<String, String> settlementDayMap;
	
	@Autowired
	private MarketSessionUtil marketSessionUtil;
	
	private String strategyPack = "com.cyanspring.common.staticdata.strategy.";
	private String checkDate = "2015-11-01";
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
	@Before
	public void setUp() throws Exception {
		marketSessionUtil.init();
		
		if (settlementDayMap == null)
			throw new Exception("settlementDayMap not set");
		for (Entry<String, String> e : settlementDayMap.entrySet()) {
			if (!StringUtils.hasLength(e.getKey()) || !StringUtils.hasLength(e.getValue())) {
				throw new Exception("Map data can't not be null or empty");
			}
		}
	}

	@Test
	public void test() throws Exception {
		for (Entry<String, String> e : settlementDayMap.entrySet()) {
			Class<?> clz = Class.forName(strategyPack + e.getKey());
			Constructor<?> ctor = clz.getConstructor();
			AbstractRefDataStrategy strategy = (AbstractRefDataStrategy) ctor.newInstance();
			Date date = sdf.parse(checkDate);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			strategy.init(cal, new HashMap<String, Quote>());
			RefData simRefData = new RefData();
			simRefData.setCategory("*");
			simRefData.setContractPolicy("Default");
			simRefData.setSymbol("*");
			simRefData.setRefSymbol("*");
			simRefData.setENDisplayName("*");
			simRefData.setTWDisplayName("*");
			simRefData.setCNDisplayName("*");
			List<RefData> refDataList = strategy.updateRefData(simRefData); 
			RefData refData = refDataList.get(0);			
			assertTrue(refData.getSettlementDate().equals(e.getValue()));
		}
	}

}
