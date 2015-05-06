package com.cyanspring.common.marketsession;

import java.util.Date;
import java.util.List;

public interface ITradeDate {
    public void init(List<String> workDay, List<String> holiDay) throws Exception;
    public Date nextTradeDate(Date date);
    public Date preTradeDate(Date date);
    public boolean isHoliday(Date date);
    public int getWeekDay(Date date);
}
