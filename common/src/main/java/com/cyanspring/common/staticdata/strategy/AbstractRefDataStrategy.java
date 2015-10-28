package com.cyanspring.common.staticdata.strategy;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.IndexSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.policy.ContractPolicy;

public abstract class AbstractRefDataStrategy implements IRefDataStrategy {

	enum Type {
		INDEX_FU,MERCHANDISE_FU
	}

	enum Locale{
		CN,TW
	}

	protected static final Logger log = LoggerFactory.getLogger(AbstractRefDataStrategy.class);
    private SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
    private RefData template;
    private String spotCnName;
    private String spotTwName;
    private MarketSessionUtil marketSessionUtil;
    private String detailDisplayPttern = "%s%s年%s月合约";
    private Calendar cal;
    private String INDEX_FU_CN = "指数";
    private String INDEX_FU_TW = "指數";
    private final String CONTRACT_POLICY_PACKAGE = "com.cyanspring.common.staticdata.policy";
    private final String MONTH_PATTERN = "${YYMM}";
    private final String SEQ_PATTERN = "${SEQ}";
    Map<String, Quote> mapHot;

    @Autowired
    @Qualifier("allContractPolicy")
    private ContractPolicy allContractPolicy;

    @Autowired
    @Qualifier("jdContractPolicy")
    private ContractPolicy jdContractPolicy;

    @Autowired
    @Qualifier("myContractPolicy")
    private ContractPolicy myContractPolicy;

    @Autowired
    @Qualifier("rmContractPolicy")
    private ContractPolicy rmContractPolicy;

    @Autowired
    @Qualifier("rsContractPolicy")
    private ContractPolicy rsContractPolicy;

    @Autowired
    @Qualifier("ruContractPolicy")
    private ContractPolicy ruContractPolicy;

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
		ContractPolicy policy = allContractPolicy;
		String contractPolicy = refData.getContractPolicy();
		if (contractPolicy != null) {
			try {
				Class<ContractPolicy> tempClz = (Class<ContractPolicy>) Class
						.forName(CONTRACT_POLICY_PACKAGE + "." + contractPolicy + "ContractPolicy");
				Constructor<ContractPolicy> ctor = tempClz.getConstructor();
				policy = ctor.newInstance();
			} catch (Exception e) {
				log.warn("Can't find contract policy: {}", contractPolicy);
				policy = allContractPolicy;
			}
		} else {
			String category = refData.getCategory();
			switch (category) {
			case "JD":
				policy = jdContractPolicy;
				break;
			case "M":
			case "Y":
				policy = myContractPolicy;
				break;
			case "RM":
				policy = rmContractPolicy;
				break;
			case "RS":
				policy = rsContractPolicy;
				break;
			case "RU":
				policy = ruContractPolicy;
				break;
			default:
				policy = allContractPolicy;
				break;
			}
		}

		List<RefData> lstRefData = new ArrayList<>();
		List<String> lstContractMonth = policy.getContractMonths(refData);
		int num = lstContractMonth.size();
		NumberFormat formatter = new DecimalFormat("##00");
		for (int i = 0; i < num; i++) {
			String month = lstContractMonth.get(i);
			String seq = formatter.format(i);
			RefData data = (RefData)refData.clone();
			data.setENDisplayName(refData.getENDisplayName().replace(MONTH_PATTERN, month));
			data.setCNDisplayName(refData.getCNDisplayName().replace(MONTH_PATTERN, month));
			data.setTWDisplayName(refData.getTWDisplayName().replace(MONTH_PATTERN, month));
			data.setSymbol(refData.getSymbol().replace(MONTH_PATTERN, month));
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

}
