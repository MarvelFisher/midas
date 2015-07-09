package com.cyanspring.common.staticdata.fu;


/**
 * This class is used to record strategy map to which symbol
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class MappingData {
    private String symbol;
    private String near1;
    private String near2;

    public void setNear1(String near1) {
        this.near1 = near1;
    }

    public void setNear2(String near2) {
        this.near2 = near2;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getNear1() {
        return near1;
    }

    public String getNear2() {
        return near2;
    }

    public String getSymbol() {
        return symbol;
    }
}
