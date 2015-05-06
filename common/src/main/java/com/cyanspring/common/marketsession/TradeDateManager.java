package com.cyanspring.common.marketsession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.util.TimeUtil;


public class TradeDateManager implements ITradeDate {

    private List<Date> workDays;
    private List<Date> holiDays;
    private String index;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public TradeDateManager(List<String> workDays, List<String> holiDays) throws Exception {
        this.workDays = new ArrayList<Date>();
        this.holiDays = new ArrayList<Date>();
        for (String day : workDays)
            this.workDays.add(sdf.parse(day));
        for (String day : holiDays)
            this.holiDays.add(sdf.parse(day));
    }

    @Override
    public void init(List<String> workDay, List<String> holiDay)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Date nextTradeDate(Date date) {
        Date nDate = TimeUtil.getNextDay(date);
        while (isHoliday(nDate)) {
            nDate = TimeUtil.getNextDay(nDate);
        }
        return nDate;
    }

    @Override
    public Date preTradeDate(Date date) {
        Date pDate = TimeUtil.getPreviousDay(date);
        while (isHoliday(pDate)) {
            pDate = TimeUtil.getPreviousDay(pDate);
        }
        return pDate;
    }

    @Override
    public boolean isHoliday(Date date) {
        for (Date wDate : workDays) {
            if (TimeUtil.sameDate(date, wDate))
                return false;
        }
        for (Date hDate : holiDays)
            if (TimeUtil.sameDate(date, hDate))
                return true;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7)
            return true;
        return false;
    }

    @Override
    public int getWeekDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }
}
