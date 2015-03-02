package com.cyanspring.common.staticdata.fu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cyanspring.common.staticdata.RefData;

public class IFStrategy implements RefDataStrategy {
	private static final Logger log = LoggerFactory
			.getLogger(IFStrategy.class);
	public RefData n0;
	public RefData n1;
//	private RefData s0;
//	private RefData s1;
	
	private String n0ID;
	private String n1ID;	
//	private String s0ID;
//	private String s1ID;
	
//	private int[] season = {Calendar.MARCH, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.DECEMBER};
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private Calendar cal;
	
	@Override
	public void init(Calendar cal) {
		if(this.cal == null){
			n0 = new RefData();
			n1 = new RefData();
			setStragetyData(cal);		
		}

		if(cal.compareTo(this.cal) > 0){
			this.cal.add(Calendar.MONTH, 1);
			setStragetyData(this.cal);								
		}
	}
	
	@Override
	public void setExchangeRefData(RefData refData) {
		if(refData.getRefSymbol().equals("IFL0")){
			refData.setSettlementDate(n0.getSettlementDate());
			refData.setCNDisplayName(refData.getCNDisplayName().substring(0, 2) + n0ID + "." + refData.getExchange());
			refData.setENDisplayName(refData.getENDisplayName().substring(0, 2) + n0ID + "." + refData.getExchange());
			refData.setTWDisplayName(refData.getTWDisplayName().substring(0, 2) + n0ID + "." + refData.getExchange());
			refData.setSymbol(refData.getSymbol().substring(0, 2) + n0ID + "." + refData.getExchange());
		}else if(refData.getRefSymbol().equals("IFL1")){
			refData.setSettlementDate(n1.getSettlementDate());
			refData.setCNDisplayName(refData.getCNDisplayName().substring(0, 2) + n1ID + "." + refData.getExchange());
			refData.setENDisplayName(refData.getENDisplayName().substring(0, 2) + n1ID + "." + refData.getExchange());
			refData.setTWDisplayName(refData.getTWDisplayName().substring(0, 2) + n1ID + "." + refData.getExchange());
			refData.setSymbol(refData.getSymbol().substring(0, 2) + n1ID + "." + refData.getExchange());
		}
	}
	
	private void setStragetyData(Calendar cal) {
		String day = getSettlementDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
		n0.setSettlementDate(day);
		n0ID = day.substring(2, 7);
		n0ID = n0ID.replace("-", "");
		
		day = getSettlementDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
		n1.setSettlementDate(day);
		n1ID = day.substring(2, 7);
		n1ID = n1ID.replace("-", "");
		
		this.cal = Calendar.getInstance();
		try {
			this.cal.setTime(sdf.parse(n0.getSettlementDate()));
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private String getSettlementDay(int year, int month){
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 0);
		int dayCount = 0;
		while(dayCount != 3){
			cal.add(Calendar.DAY_OF_MONTH, 1);
			if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
				dayCount++;
		}
		return sdf.format(cal.getTime());
	}
}
