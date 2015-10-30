package com.cyanspring.common.staticdata.strategy;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.IndexSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.policy.DefaultContractPolicy;

public abstract class AbstractRefDataStrategy implements IRefDataStrategy {

	enum Type {
		INDEX_FU,MERCHANDISE_FU
	}

	enum Locale{
		CN,TW
	}

	protected static final Logger log = LoggerFactory.getLogger(AbstractRefDataStrategy.class);
	private MarketSessionUtil marketSessionUtil;
	private Calendar cal;
    private final String CONTRACT_POLICY_PACKAGE = "com.cyanspring.common.staticdata.policy";
    private final String MONTH_PATTERN1 = "${YYYYMM}";
    private final String MONTH_PATTERN2 = "${YYMM}";
    private final String MONTH_PATTERN3 = "${MY}";
    private final String SEQ_PATTERN = "${SEQ}";
    private final Map<Integer, String> mapMonthAlphabet = new HashMap<Integer, String>(){{
    		put(0, "A"); // Jan
    		put(1, "B"); // Feb
    		put(2, "C"); // Mar
    		put(3, "D"); // Apr
    		put(4, "E"); // May
    		put(5, "F"); // Jun
    		put(6, "G"); // Jul
    		put(7, "H"); // Aug
    		put(8, "I"); // Sep
    		put(9, "J"); // Oct
    		put(10, "K"); // Nov
    		put(11, "L"); // Dec
    	}};
    Map<String, Quote> mapHot;

	@Override
	public void init(Calendar cal, Map<String, Quote> map) {
		if (cal != null) {
			this.cal = cal;
		}

		if (map != null && map.size() > 0) {
			mapHot = map;
		}
	}

	@Override
	public List<RefData> updateRefData(RefData refData) {
		DefaultContractPolicy policy;
		String category = refData.getCategory();
		String contractPolicy = refData.getContractPolicy();

		try {
			Class<DefaultContractPolicy> tempClz = (Class<DefaultContractPolicy>) Class
					.forName(CONTRACT_POLICY_PACKAGE + "." + contractPolicy + "ContractPolicy");
			if (contractPolicy.equals("Default")) {
				policy = getContractPolicy(category);
			} else {
				Constructor<DefaultContractPolicy> ctor = tempClz.getConstructor();
				policy = ctor.newInstance();
			}
		} catch (Exception e) {
			log.warn("Contract policy is not set for category: {}", category);
			policy = new DefaultContractPolicy() {
				@Override
				public List<String> getContractMonths(RefData refData) {
					return new ArrayList<>();
				}
			};
		}

		List<RefData> lstRefData = new ArrayList<>();
		List<String> lstContractMonth = policy.getContractMonths(refData);
		int num = 0;
		if (lstContractMonth == null || (num = lstContractMonth.size()) == 0) {
			lstRefData.add(refData);
			return lstRefData;
		}

		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < num; i++) {
			String month = lstContractMonth.get(i);
			String y = month.substring(1, 2);
			int m = Integer.parseInt(month.substring(2)) - 1;
			String a = mapMonthAlphabet.get(m);
			String seq = formatter.format(i);
			RefData data = (RefData)refData.clone();
			data.setENDisplayName(refData.getENDisplayName().replace(MONTH_PATTERN2, month));
			data.setCNDisplayName(refData.getCNDisplayName().replace(MONTH_PATTERN2, month));
			data.setTWDisplayName(refData.getTWDisplayName().replace(MONTH_PATTERN2, month));
			String symbol = refData.getSymbol();
			symbol = symbol.replace(MONTH_PATTERN2, month); // except LTFT
			symbol = symbol.replace(MONTH_PATTERN3, a + y); // only for LTFT, ex: C6 means 2016.03
			data.setSymbol(symbol);
			data.setRefSymbol(refData.getRefSymbol().replace(SEQ_PATTERN, seq));
			lstRefData.add(data);
		}

		return lstRefData;
	}

	@Override
	public void setRequireData(Object... objects) {
        this.marketSessionUtil = (MarketSessionUtil) objects[0];
	}

	protected String getIndexSessionType(RefData refData) {
		String calDate = getSettlementDateFormat().format(cal.getTime());
		log.info("calDate:{},{},{}",new Object[]{calDate,refData.getSettlementDate(),calDate.equals(refData.getSettlementDate())});
		if (calDate.equals(refData.getSettlementDate())) {
			return IndexSessionType.SETTLEMENT.name();
		} else {
			return IndexSessionType.SPOT.name();
		}
	}

	protected SimpleDateFormat getSettlementDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd") ;
	}

	private DefaultContractPolicy getContractPolicy(String category) {
		DefaultContractPolicy contractPolicy = new DefaultContractPolicy();
		List<Integer> lstMonth = new ArrayList<>();

		switch (category) {
		case "JD":
			// 鸡蛋 1、2、3、4、5、6、9、10、11、12月
			lstMonth.add(0);
			lstMonth.add(1);
			lstMonth.add(2);
			lstMonth.add(3);
			lstMonth.add(4);
			lstMonth.add(5);
			lstMonth.add(8);
			lstMonth.add(9);
			lstMonth.add(10);
			lstMonth.add(11);
			break;
		case "M":
		case "Y":
			// 豆粕 豆油 1，3，5，7，8，9，11，12月
			lstMonth.add(0);
			lstMonth.add(2);
			lstMonth.add(4);
			lstMonth.add(6);
			lstMonth.add(7);
			lstMonth.add(8);
			lstMonth.add(10);
			lstMonth.add(11);
			break;
		case "RM":
			// 菜籽粕 1、3、5、7、8、9、11月
			lstMonth.add(0);
			lstMonth.add(2);
			lstMonth.add(4);
			lstMonth.add(6);
			lstMonth.add(7);
			lstMonth.add(8);
			lstMonth.add(10);
			break;
		case "RS":
			// 油菜籽 7、8、9、11月
			lstMonth.add(6);
			lstMonth.add(7);
			lstMonth.add(8);
			lstMonth.add(10);
			break;
		case "RU":
			// 橡胶 1、3、4、5、6、7、8、9、10、11月
			lstMonth.add(0);
			lstMonth.add(2);
			lstMonth.add(3);
			lstMonth.add(4);
			lstMonth.add(5);
			lstMonth.add(6);
			lstMonth.add(7);
			lstMonth.add(8);
			lstMonth.add(9);
			lstMonth.add(10);
			break;
		case "FU":
			// 燃油 1 ~ 12 月（春节月份除外）
			lstMonth.add(0);
			lstMonth.add(2);
			lstMonth.add(3);
			lstMonth.add(4);
			lstMonth.add(5);
			lstMonth.add(6);
			lstMonth.add(7);
			lstMonth.add(8);
			lstMonth.add(9);
			lstMonth.add(10);
			lstMonth.add(11);
			break;
		default:
			// 1 ~ 12 月
			lstMonth.add(0);
			lstMonth.add(1);
			lstMonth.add(2);
			lstMonth.add(3);
			lstMonth.add(4);
			lstMonth.add(5);
			lstMonth.add(6);
			lstMonth.add(7);
			lstMonth.add(8);
			lstMonth.add(9);
			lstMonth.add(10);
			lstMonth.add(11);
			break;
		}

		contractPolicy.setContractMonths(lstMonth);

		return contractPolicy;
	}


}
