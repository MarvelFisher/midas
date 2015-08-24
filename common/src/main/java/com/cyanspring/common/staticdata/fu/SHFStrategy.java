package com.cyanspring.common.staticdata.fu;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;

/**
 * This strategy is used for Futures Master to change refData settings
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class SHFStrategy extends AbstractRefDataStrategy {
	
	protected static final Logger log = LoggerFactory.getLogger(SHFStrategy.class);

    @Override
    public void init(Calendar cal, RefData template) {
    	super.init(cal, template);
    }

	@Override
    public void updateRefData(RefData refData) {
		try {
			
			if( null == getMarketSessionUtil() || null == getTradeDateManager()){
				log.info("refData:{}- tradeDateManager or marketsessoinutil is null",refData.getCNDisplayName());
				return;
			}
			setTemplateData(refData);
			String combineCnName = refData.getCNDisplayName();			
			if(refData.getCategory().equals("FU")){
				refData.setSettlementDate(calFUSettlementDate(refData.getSymbol(),getContractDate(combineCnName)));
			}else{
				refData.setSettlementDate(calSettlementDate(refData.getSymbol(),getContractDate(combineCnName),15));
			}
			refData.setIndexSessionType(getIndexSessionType(refData));
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	private String calFUSettlementDate(String symbol, Calendar contractDate) {
		
		contractDate.set(Calendar.DATE, contractDate.getMinimum(Calendar.DATE));
		Date date = contractDate.getTime();

		if( null == getTradeDateManager()){
			log.warn("symbol:{} can't find TradeDateManager!",symbol);
			return "";
		}
		date = getTradeDateManager().preTradeDate(date);	
		return getSettlementDateFormat().format(date);
	}

	private String calSettlementDate(String symbol,Calendar cal,int dayInMonth) throws Exception{
		
		if( null == getMarketSessionUtil()){
			log.warn("symbol:{} can't find marketsessionutil!",symbol);
			return "";
		}
		
		cal.set(Calendar.DAY_OF_MONTH, dayInMonth);
		while(getMarketSessionUtil().isHoliday(symbol, cal.getTime())){
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return getSettlementDateFormat().format(cal.getTime());
	}
	
	@Override
	public void setRequireData(Object... objects) {
		super.setRequireData(objects);
	}
}
