package com.cyanspring.common.staticdata.fu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataException;
import com.cyanspring.common.staticdata.RefDataUtil;
import com.cyanspring.common.util.PriceUtils;

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

	@Override
	public void init(Calendar cal, RefData template) {

		this.template = template;
		spotCnName = template.getSpotCNName();
		spotTwName = template.getSpotTWName();

		if (cal != null) {
			this.cal = cal;
		}

	}

	@Override
	public void updateRefData(RefData refData) {

	}

	@Override
	public void setRequireData(Object... objects) {
        this.marketSessionUtil = (MarketSessionUtil) objects[0];
	}

	protected String getSymbol(RefData data) {
		if (IType.FUTURES.getValue().equals(data.getIType())) {
			return data.getRefSymbol();
		} else if (IType.FUTURES_CX.getValue().equals(data.getIType())) {
			return data.getCNDisplayName().replaceAll("\\W", "") + "." + data.getExchange();
		} else if (IType.FUTURES_IDX.getValue().equals(data.getIType())) {
			return data.getRefSymbol();
		} else if (IType.FUTURES_CX_IDX.getValue().equals(data.getIType())) {
			return data.getCategory() + data.getCNDisplayName().replaceAll("\\D", "") + "." + data.getExchange();
		} else if (IType.FUTURES_FT.getValue().equals(data.getIType())) {
			return data.getSymbol();
		} else {
			return "";
		}
	}

	protected void setTemplateData(RefData refData) throws Exception{
		if (null == template) {
			log.warn("template is null");
			return;
		}
		if (!StringUtils.hasText(refData.getRefSymbol())
				||!StringUtils.hasText(refData.getCNDisplayName())
				||!StringUtils.hasText(refData.getExchange())
				||!StringUtils.hasText(refData.getCode())
				||!StringUtils.hasText(refData.getIType())
				) {
			throw new Exception("refData required info missing:"+refData.getRefSymbol());
		}

		if (!checkAcceptableRefData(refData)) {
			throw new RefDataException("Not acceptable refData:"+refData.getRefSymbol());
		}

		if (StringUtils.hasText(refData.getSettlementDate())) {
			return;
		}

		double cf = template.getLotCommissionFee();
		if (!PriceUtils.isZero(cf)) {
			refData.setLotCommissionFee(cf);
		} else {
			refData.setCommissionFee(template.getCommissionFee());
		}

		refData.setMaximumHold(template.getMaximumHold());
		refData.setCommodity(template.getCommodity());
		refData.setType(template.getType());
		refData.setTWTradingUnit(template.getTWTradingUnit());
		refData.setPriceLimit(template.getPriceLimit());
		refData.setLotSize(template.getLotSize());
		refData.setMarketMaximumLot(template.getMarketMaximumLot());
		refData.setNumeratorDP(template.getNumeratorDP());
		refData.setTickTable(template.getTickTable());
		refData.setDecimalPoint(template.getDeciamlPoint());
		refData.setMinimalCommissionFee(template.getMinimalCommissionFee());
		refData.setCNTradingUnit(template.getCNTradingUnit());
		refData.setPricePerUnit(template.getPricePerUnit());
		refData.setLimitMaximumLot(template.getLimitMaximumLot());
		refData.setMarginRate(template.getMarginRate());
		refData.setMarketMaximumLot(template.getMarketMaximumLot());
		refData.setENDisplayName("");
		refData.setENTradingUnit(template.getENTradingUnit());
//		refData.setTradable(template.getTradable());
//		refData.setDenominator(template.getDenominator());

		String combineCnName = refData.getCNDisplayName();
		String combineTwName = refData.getTWDisplayName();
		if (!StringUtils.hasText(combineTwName)) {
			combineTwName = combineCnName;
		}

		String refSymbol = refData.getRefSymbol();

		refData.setStrategy(refData.getExchange());
		refData.setCategory(getCategory(refData));
		refData.setSymbol(getSymbol(refData));
		refData.setCNDisplayName(getCNName(combineCnName));
		refData.setTWDisplayName(getTWName(combineTwName));
		refData.setENDisplayName(getEnName(refData));
		if (template.getType().equals(Type.INDEX_FU.name())) {
			spotCnName += INDEX_FU_CN;
			spotTwName += INDEX_FU_TW;
		}
		refData.setSpotENName(getSpotName(combineCnName, Locale.CN));
		refData.setSpotTWName(getSpotName(combineTwName, Locale.TW));
		refData.setSpotCNName(getSpotName(combineCnName, Locale.CN));
		refData.setDetailCN(getCNDetailName(combineCnName));
		refData.setDetailTW(getTWDetailName(combineTwName));
		refData.setDetailEN(getCNDetailName(combineCnName));
		refData.setRefSymbol(getRefSymbol(refSymbol).toUpperCase());
	}

	private boolean checkAcceptableRefData(RefData refData) {
		String contractDate = refData.getCNDisplayName().replaceAll("\\D", "");
		if (!StringUtils.hasText(contractDate)) {
			log.info("this CNDisplayName name doesn't hava date:{}", refData.getCNDisplayName());
			return false;
		}

		return true;
	}

	protected String getEnName(RefData data) throws ParseException{
		String spotName = getCategory(data);
		String date = data.getCNDisplayName().replaceAll("\\W", "").replaceAll("\\D", "");
		if (date.length() == 3) {
			date = formatSymbolYearMonth(data.getCNDisplayName());
		}

		return spotName + date;
	}

	protected String getRefSymbol(String refSymbol) {
		return refSymbol.replaceAll("\\.[A-Z]+$", "");
	}

	protected String getCategory(RefData refData) {
		return RefDataUtil.getCategory(refData);
	}

	protected String getCNName(String combineCnName) throws ParseException{
		String spotName =  getSpotName(combineCnName, Locale.CN).replaceAll("\\d", "");
		String date = combineCnName.replaceAll("\\W", "").replaceAll("\\D", "");
		if (date.length() == 3) {
			date = formatSymbolYearMonth(combineCnName);
		}

		return spotName + date;
	}

	protected String getTWName(String combineTwName) throws ParseException{
		String spotName = getSpotName(combineTwName, Locale.TW).replaceAll("\\d", "");
		String date = combineTwName.replaceAll("\\W", "").replaceAll("\\D", "");
		if (date.length() == 3) {
			date = formatSymbolYearMonth(combineTwName);
		}

		return spotName + date;
	}

	private String formatSymbolYearMonth(String combineName) throws ParseException{
		if (StringUtils.hasText(combineName)) {
			SimpleDateFormat contractSdf = new SimpleDateFormat("yyMM");
			Calendar cal = getContractDate(combineName);
			return contractSdf.format(cal.getTime());
		}

		return combineName;
	}


	protected String getSpotName(String combineCnName, Locale locale) {
		if (locale.equals(Locale.CN) && StringUtils.hasText(spotCnName)) {
			return spotCnName;
		} else if (locale.equals(Locale.TW) && StringUtils.hasText(spotTwName)) {
			return spotTwName;
		} else {
			return combineCnName.replaceAll("\\w", "").replaceAll("\\-", "");
		}
	}

	protected String getCNDetailName(String combineCnName) {
		String cnName = getSpotName(combineCnName,Locale.CN);
		String contractDate = combineCnName.replaceAll("\\D", "");
		String year = getYearFromCNName(contractDate);
		String month = contractDate.substring(contractDate.length() - 2, contractDate.length());
		return String.format(detailDisplayPttern, cnName, year, month);
	}

	protected String getTWDetailName(String combineTwName) {
		String cnName = getSpotName(combineTwName,Locale.TW);
		String contractDate = combineTwName.replaceAll("\\D", "");
		String year = getYearFromCNName(contractDate);
		String month = contractDate.substring(contractDate.length() - 2, contractDate.length());
		return String.format(detailDisplayPttern, cnName, year, month);
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

	protected Calendar getContractDate(String combineCnName) throws ParseException {
		SimpleDateFormat contractSdf = new SimpleDateFormat("yyyyMM");
		String contractDate = combineCnName.replaceAll("\\D", "");
		String year = getYearFromCNName(contractDate);
		String month = contractDate.substring(contractDate.length() - 2, contractDate.length());
		Calendar contractCal = Calendar.getInstance();
		contractCal.setTime(contractSdf.parse(year + month));
		return contractCal;
	}

	private String getYearFromCNName(String contractDate) {
		String year = yearSdf.format(cal.getTime());
		int lastYearNum = 0 ;
		int contractYearNum = 0;
		int add = 0;
		if (contractDate.length() == 3) {

			lastYearNum = Integer.parseInt(year.substring(year.length() - 1, year.length()));
			contractYearNum = Integer.parseInt(contractDate.substring(0, 1));
			if (contractYearNum < lastYearNum) {
				add = (contractYearNum + 10) - lastYearNum;
			} else {
				add = contractYearNum - lastYearNum;
			}
		} else if (contractDate.length() == 4) {

			lastYearNum = Integer.parseInt(year.substring(year.length() - 2, year.length()));
			contractYearNum = Integer.parseInt(contractDate.substring(0, 2));
			add = contractYearNum - lastYearNum;
		}
		return year = Integer.toString(Integer.parseInt(year) + add);
	}

	protected Calendar getCalendar() {
		return (Calendar) this.cal.clone();
	}

	protected SimpleDateFormat getSettlementDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd") ;
	}

}
