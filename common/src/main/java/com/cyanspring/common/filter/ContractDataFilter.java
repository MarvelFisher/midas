package com.cyanspring.common.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;

public class ContractDataFilter implements IRefDataFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractDataFilter.class);
	private boolean checkValidContractDate;
	
	public boolean isCheckValidContractDate() {
		return checkValidContractDate;
	}

	public void setCheckValidContractDate(boolean checkValidContractDate) {
		this.checkValidContractDate = checkValidContractDate;
	}

	@Override
	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		List<RefData> newList = new ArrayList<RefData>();
		if(null == lstRefData || lstRefData.isEmpty())
			return newList;
		
		for(RefData data : lstRefData){
			if(isValidContractDate(data)){
				newList.add(data);
			}
		}
		return newList;
	}
	
	private boolean isValidContractDate(RefData refData){		
		if( null == refData)
			return false;
		
		String settlementDate	= null;	
		try {
			settlementDate = refData.getSettlementDate();
			if(!StringUtils.hasText(settlementDate))
				return false;
			
			SimpleDateFormat contractFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar now = Calendar.getInstance();
			int thisYear = now.get(Calendar.YEAR);			
			Calendar contractCal = Calendar.getInstance();
			contractCal.setTime(contractFormat.parse(settlementDate));
			int contractYear = contractCal.get(Calendar.YEAR);

			if((contractYear-thisYear)>=5 || (contractYear-thisYear)<0){
				return false;
			}else{
				return true;
			}
					
		} catch (ParseException e) {
			LOGGER.warn("not valid contract date:{},{}",settlementDate,e.getMessage());
		} catch (Exception e){
			LOGGER.warn("can't find settlementDate :{},{}",refData.getSymbol(),e.getMessage());
		}

		return false;
	}

}
