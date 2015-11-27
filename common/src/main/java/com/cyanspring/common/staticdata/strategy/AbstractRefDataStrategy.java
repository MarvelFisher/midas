package com.cyanspring.common.staticdata.strategy;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
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
import com.cyanspring.common.staticdata.RefDataBitUtil;
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

    public Calendar getCal() {
		return cal;
	}

	public void setCal(Calendar cal) {
		this.cal = cal;
	}

	private final String CONTRACT_POLICY_PACKAGE = "com.cyanspring.common.staticdata.policy";
    private final String MONTH_PATTERN_YYYYMM = "${YYYYMM}";
    private final String MONTH_PATTERN_YYMM = "${YYMM}";
    private final String MONTH_PATTERN_YMM = "${YMM}";
    private final String MONTH_PATTERN_MY = "${MY}";
    private final String SEQ_PATTERN = "${SEQ}";
	private final Map<String, String> mapMonthAlphabet = new HashMap<String, String>() {
		{
			put("01", "A"); // Jan
			put("02", "B"); // Feb
			put("03", "C"); // Mar
			put("04", "D"); // Apr
			put("05", "E"); // May
			put("06", "F"); // Jun
			put("07", "G"); // Jul
			put("08", "H"); // Aug
			put("09", "I"); // Sep
			put("10", "J"); // Oct
			put("11", "K"); // Nov
			put("12", "L"); // Dec
		}
	};
    Map<String, Quote> qMap;

	@Override
	public void init(Calendar cal, Map<String, Quote> qMap) throws Exception {
		if (cal == null || qMap == null) {
			throw new Exception("Both cal and map cannot be null");
		}

		this.cal = cal;
		this.qMap = qMap;
	}

	@Override
	public List<RefData> updateRefData(RefData refData) throws ParseException {
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

		policy.init(this.cal);

		List<RefData> lstRefData = new ArrayList<>();
		List<String> lstContractMonth = policy.getContractMonths(refData);
		int num = 0;
		if (lstContractMonth == null || (num = lstContractMonth.size()) == 0) {
			lstRefData.add(refData);
			return lstRefData;
		}

		RefData hotOne = null;
		double highestVolume = 0;
		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < num; i++) {
			String yyyymm = lstContractMonth.get(i);
			String yymm = yyyymm.substring(2);
			String ymm = yyyymm.substring(3);
			String y = yymm.substring(1, 2);
			String m = yymm.substring(2);
			String a = mapMonthAlphabet.get(m);
			String seq = formatter.format(i);
			RefData data = (RefData)refData.clone();
			data.setENDisplayName(refData.getENDisplayName().replace(MONTH_PATTERN_YYMM, yymm));
			data.setCNDisplayName(refData.getCNDisplayName().replace(MONTH_PATTERN_YYMM, yymm));
			data.setTWDisplayName(refData.getTWDisplayName().replace(MONTH_PATTERN_YYMM, yymm));
			String symbol = refData.getSymbol();
			symbol = symbol.replace(MONTH_PATTERN_YYMM, yymm);
			symbol = symbol.replace(MONTH_PATTERN_YMM, ymm); // for exchange CZC
			symbol = symbol.replace(MONTH_PATTERN_MY, a + y); // for LTFT, ex: C6 means 2016.03
			data.setSymbol(symbol);
			data.setRefSymbol(refData.getRefSymbol().replace(SEQ_PATTERN, seq));
			String code = refData.getCode();
			if (code != null && code.length() > 0) {
				code = code.replace(MONTH_PATTERN_YYYYMM, yyyymm);
				data.setCode(code);
			} else {
				data.setCode(data.getRefSymbol());
			}
			String subscribeSymbol = refData.getSubscribeSymbol();
			if (subscribeSymbol != null && subscribeSymbol.length() > 0) {
				subscribeSymbol = subscribeSymbol.replace(MONTH_PATTERN_YYYYMM, yyyymm);
				data.setSubscribeSymbol(subscribeSymbol);
			}
			// Update hot RefData instrument type based on the volume last day
			if (qMap != null && qMap.size() > 0) {
				Quote q = qMap.get(symbol);
				if (q != null) {
					double vol = q.getTotalVolume();
					if (vol > highestVolume) {
						highestVolume = vol;
						hotOne = data;
					}
				}
			}

			// 動力煤 TC 合约文本运行至 TC604合约摘牌。
			// 動力煤 ZC 合约文本自 ZC605 合约起执行。
			// TODO 2016年5月起, 需將 TC 自 template_FC 移除, 改用 ZC 並拿掉此段 code
			if (category.equals("TC") && Integer.parseInt(ymm) > 604) {
				convertTC2ZC(data);
			}

			lstRefData.add(data);
		}

		if (hotOne != null) {
			long instrumentType = hotOne.getInstrumentType();
			instrumentType += RefDataBitUtil.HOT;
			hotOne.setInstrumentType(instrumentType);
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

	private void convertTC2ZC(RefData data) {
		data.setCategory("ZC");
		data.setSymbol(data.getSymbol().replace("TC", "ZC"));
		data.setRefSymbol(data.getRefSymbol().replace("TC", "ZC"));
		data.setENDisplayName(data.getENDisplayName().replace("TC", "ZC"));
		data.setCode(data.getCode().replace("TC", "ZC"));
		data.setPricePerUnit(100);
	}

}
