package com.cyanspring.common.staticdata.fu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;

public class IFStrategy implements IRefDataStrategy {
    private static final Logger log = LoggerFactory
            .getLogger(IFStrategy.class);

    private MarketSessionUtil marketSessionUtil;
    private StrategyData n0;
    private StrategyData n1;

    //	private int[] season = {Calendar.MARCH, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.DECEMBER};
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Calendar cal;
    private String symbol = "IF";
    private String detailDisplay = "%s指数%d年%d月合约";

    @Override
    public void init(Calendar cal) {
        if (this.cal == null) {
            n0 = new StrategyData();
            n1 = new StrategyData();
            setStrategyData((Calendar) cal.clone());
        }

        if (cal.compareTo(this.cal) < 0)
            return;
        cal.add(Calendar.DAY_OF_YEAR, 1);
        this.cal.add(Calendar.MONTH, 1);
        setStrategyData(this.cal);
    }

    @Override
    public boolean update(Calendar tradeDate) {
        if (tradeDate.compareTo(this.cal) < 0)
            return false;
        this.cal.add(Calendar.MONTH, 1);
        setStrategyData(this.cal);
        return true;
    }

    @Override
    public void updateRefData(RefData refData) {
        if (refData.getRefSymbol().equals("IFC1") || refData.getRefSymbol().equals("IHC1") || refData.getRefSymbol().equals("ICC1")) {
            refData.setSettlementDate(n0.settlementDay);
            refData.setCNDisplayName(refData.getCNDisplayName().substring(0, 2) + n0.ID);
            refData.setENDisplayName(refData.getENDisplayName().substring(0, 2) + n0.ID);
            refData.setTWDisplayName(refData.getTWDisplayName().substring(0, 2) + n0.ID);
            refData.setSymbol(refData.getSymbol().substring(0, 2) + n0.ID + "." + refData.getExchange());
            refData.setDetailCN(String.format(detailDisplay, refData.getCNDisplayName(), n0.year, n0.month + 1)); // The first month of the year in the Gregorian and Julian calendars is JANUARY which is 0
            refData.setDetailTW(String.format(detailDisplay, refData.getCNDisplayName(), n0.year, n0.month + 1));
            refData.setDetailEN(String.format(detailDisplay, refData.getCNDisplayName(), n0.year, n0.month + 1));
        } else if (refData.getRefSymbol().equals("IFC2") || refData.getRefSymbol().equals("IHC2") || refData.getRefSymbol().equals("ICC2")) {
            refData.setSettlementDate(n1.settlementDay);
            refData.setCNDisplayName(refData.getCNDisplayName().substring(0, 2) + n1.ID);
            refData.setENDisplayName(refData.getENDisplayName().substring(0, 2) + n1.ID);
            refData.setTWDisplayName(refData.getTWDisplayName().substring(0, 2) + n1.ID);
            refData.setSymbol(refData.getSymbol().substring(0, 2) + n1.ID + "." + refData.getExchange());
            refData.setDetailCN(String.format(detailDisplay, refData.getCNDisplayName(), n1.year, n1.month + 1));
            refData.setDetailTW(String.format(detailDisplay, refData.getCNDisplayName(), n1.year, n1.month + 1));
            refData.setDetailEN(String.format(detailDisplay, refData.getCNDisplayName(), n1.year, n1.month + 1));
        }
    }

    @Override
    public void setMarketSessionUtil(MarketSessionUtil marketSessionUtil) {
        this.marketSessionUtil = marketSessionUtil;
    }

    private void setStrategyData(Calendar cal) {
        n0.year = cal.get(Calendar.YEAR);
        n0.month = cal.get(Calendar.MONTH);
        String day = getSettlementDay(n0.year, n0.month);
        n0.settlementDay = day;
        n0.ID = day.substring(2, 7);
        n0.ID = n0.ID.replace("-", "");

        cal.add(Calendar.MONTH, 1);
        n1.year = cal.get(Calendar.YEAR);
        n1.month = cal.get(Calendar.MONTH);
        day = getSettlementDay(n1.year, n1.month);
        n1.settlementDay = day;
        n1.ID = day.substring(2, 7);
        n1.ID = n1.ID.replace("-", "");

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

    private String getSettlementDay(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 0);
        int dayCount = 0;
        while (dayCount != 3) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
                dayCount++;
        }

        while (marketSessionUtil.isHoliday(symbol, cal.getTime())) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return sdf.format(cal.getTime());
    }

    private class StrategyData{
        private String settlementDay;
        private String ID;
        private int year;
        private int month;
    }
}
