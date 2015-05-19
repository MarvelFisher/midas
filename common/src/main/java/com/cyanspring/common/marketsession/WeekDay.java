package com.cyanspring.common.marketsession;

/**
 * This enum present the day of week
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public enum WeekDay {
    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7);

    private int day;
    WeekDay(int day){
        this.day = day;
    }

    public int getDay() {
        return day;
    }
}
