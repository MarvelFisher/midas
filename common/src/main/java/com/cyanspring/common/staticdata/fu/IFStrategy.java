package com.cyanspring.common.staticdata.fu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;

public class IFStrategy implements IRefDataStrategy  {
	protected static final Logger log = LoggerFactory.getLogger(IFStrategy.class);
	
	private MarketSessionUtil marketSessionUtil;
    private List<String> near1List = new ArrayList<>();
    private List<String> near2List = new ArrayList<>();
    private List<String> season1List = new ArrayList<>();
    private List<String> season2List = new ArrayList<>();
    
    private String symbol;
    private String detailCNDisplay = "%s%d年%d月合约";
    private String detailTWDisplay = "%s%d年%d月合約";
    
    private int[] seasons = {Calendar.MARCH, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.DECEMBER};
	
    private StrategyData n0;
    private StrategyData n1;
    private StrategyData f0;
    private StrategyData f1;
    private StateChain stateChain;
    private Calendar cal;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
    @Override
    public void init(Calendar cal) {
        if (this.cal == null) {
        	for(int season : seasons){
        		if(stateChain == null){
        			stateChain = new StateChain(season);
        			continue;
        		}
        		StateChain state = new StateChain(season);
        		stateChain.addState(state);
        	}
        	stateChain.addState(stateChain);
            n0 = new StrategyData();
            n1 = new StrategyData();
            f0 = new StrategyData();
            f1 = new StrategyData();
            updateDynamicData((Calendar) cal.clone());
        }

        if (cal.compareTo(this.cal) < 0)
            return;
        this.cal.add(Calendar.MONTH, 1);
        updateDynamicData(this.cal);
    }

    @Override
    public void updateRefData(RefData refData) {
    	String refSymbol = refData.getRefSymbol();
        if (near1List.contains(refSymbol)) {
        	writeToRefData(refData, n0);
        } else if (near2List.contains(refSymbol)){
        	writeToRefData(refData, n1);
        } else if (season1List.contains(refSymbol)){
        	writeToRefData(refData, f0);
        } else if (season2List.contains(refSymbol)){
        	writeToRefData(refData, f1);
        }
    }

    @Override
    public void setRequireData(Object... objects) {
        this.marketSessionUtil = (MarketSessionUtil) objects[0];
        for (MappingData data : (List<MappingData>) objects[1]){
            if (symbol == null)
                symbol = data.getSymbol();
            near1List.add(data.getNear1());
            near2List.add(data.getNear2());
            season1List.add(data.getSeason1());
            season2List.add(data.getSeason2());
        }
    }
    
    private void writeToRefData(RefData refData, StrategyData data){
    	refData.setSettlementDate(data.settlementDay);
        refData.setCNDisplayName(refData.getCNDisplayName().substring(0, 2) + data.ID);
        refData.setENDisplayName(refData.getENDisplayName().substring(0, 2) + data.ID);
        refData.setTWDisplayName(refData.getTWDisplayName().substring(0, 2) + data.ID);
        refData.setSymbol(refData.getSymbol().substring(0, 2) + data.ID + "." + refData.getExchange());
        refData.setDetailCN(String.format(detailCNDisplay, refData.getSpotCNName(), data.year, data.month + 1)); // The first month of the year in the Gregorian and Julian calendars is JANUARY which is 0
        refData.setDetailTW(String.format(detailTWDisplay, refData.getSpotTWName(), data.year, data.month + 1));
        refData.setDetailEN(String.format(detailCNDisplay, refData.getSpotENName(), data.year, data.month + 1));
    }
    
    private String calSettlementDay(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 0);
        int dayCount = 0;
        while (dayCount != 3) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
                dayCount++;
        }

        try {
			while (marketSessionUtil.isHoliday(symbol, cal.getTime())) {
			    cal.add(Calendar.DAY_OF_YEAR, 1);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

        return sdf.format(cal.getTime());
    }
    
    private void saveStrategyData(Calendar cal, StrategyData data){
    	data.year = cal.get(Calendar.YEAR);
    	data.month = cal.get(Calendar.MONTH);
        String day = calSettlementDay(data.year, data.month);
        data.settlementDay = day;
        data.ID = day.substring(2, 7);
        data.ID = data.ID.replace("-", "");
    }
    
    private void updateDynamicData(Calendar cal) {
		saveStrategyData(cal, n0);
	    cal.add(Calendar.MONTH, 1);
	    saveStrategyData(cal, n1);
	    saveStrategyData(searchNearestSeason(stateChain, cal, seasons.length), f0);
        saveStrategyData(searchNearestSeason(stateChain, cal, seasons.length), f1);
	            
	    this.cal = Calendar.getInstance();
	    try {
	        this.cal.setTime(sdf.parse(n0.settlementDay));
	        this.cal.set(Calendar.HOUR_OF_DAY, 23);
	        this.cal.set(Calendar.MINUTE, 59);
	        this.cal.set(Calendar.SECOND, 59);
	    } catch (ParseException e) {
	        log.error(e.getMessage(), e);
	    }
	}

    public Calendar searchNearestSeason(StateChain chain, Calendar cal, int depth){
        int i = 0;
        int month = cal.get(Calendar.MONTH);
        int nowState = chain.nowState();
        if (nowState > month){
            cal.set(Calendar.MONTH, nowState);
            return cal;
        }

        do {
            chain = chain.getNextState();
            if (nowState > chain.nowState())
                cal.add(Calendar.YEAR, 1);
            nowState = chain.nowState();
            i++;
        }while (month >= nowState && i < depth);

        cal.set(Calendar.MONTH, chain.nowState());
        return cal;
    }
}
