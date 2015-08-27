package com.cyanspring.common.staticdata.fu;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataException;

public class CZCStrategy extends AbstractRefDataStrategy {
	
	protected static final Logger log = LoggerFactory.getLogger(CZCStrategy.class);
	
	@Override
	public void init(Calendar cal, RefData template) {
		super.init(cal, template);
	}

    @Override
    public void updateRefData(RefData refData) {
		
		try {
		
			if( null == getTradeDateManager(refData.getCategory())){
				log.info("refData:{} - tradeDateManager is null",refData.getCNDisplayName());
				return;
			}
			
			setTemplateData(refData);
			String combineCnName = refData.getCNDisplayName();		
			
			if(refData.getCategory().equals("TC")){//動力煤
				refData.setSettlementDate(calSettlementDate(refData.getSymbol(),refData.getCategory(),getContractDate(combineCnName),5));
			}else{
				refData.setSettlementDate(calSettlementDate(refData.getSymbol(),refData.getCategory(),getContractDate(combineCnName),10));
			}
			refData.setIndexSessionType(getIndexSessionType(refData));
		} catch (RefDataException e){
			log.warn(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
    }

	private String calSettlementDate(String symbol,String category,Calendar cal,int dayInMonth){
		
		cal.set(Calendar.DATE, cal.getMinimum(Calendar.DATE));
		Date date = cal.getTime();
		if( null == getTradeDateManager(category)){
			log.warn("symbol:{} can't find tradeDateManager!",symbol);
			return "";
		}
		
		date = getTradeDateManager(category).preTradeDate(date);
		for(int i=0 ; i < dayInMonth ; i++){
			date = getTradeDateManager(category).nextTradeDate(date);
		}
		
		return getSettlementDateFormat().format(date);
	}
    
    @Override
    public void setRequireData(Object... objects) {
    	super.setRequireData(objects);
    }
}
