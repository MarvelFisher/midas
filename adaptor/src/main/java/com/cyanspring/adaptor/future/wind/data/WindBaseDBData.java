package com.cyanspring.adaptor.future.wind.data;

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.QuoteExtDataField;

import java.util.Date;

public class WindBaseDBData extends DataObject {

    public String getSymbol() {
        return this.get(String.class, RefDataField.SYMBOL.value());
    }
    public void setSymbol(String symbol) {
        this.set(symbol, RefDataField.SYMBOL.value());
    }
    public String getTWDisplayName() {
        return this.get(String.class, RefDataField.TW_DISPLAYNAME.value());
    }
    public void setTWDisplayName(String twName) {
        this.set(twName, RefDataField.TW_DISPLAYNAME.value());
    }
    public String getCNDisplayName() {
        return this.get(String.class, RefDataField.CN_DISPLAYNAME.value());
    }
    public void setCNDisplayName(String cnName) {
        this.set(cnName, RefDataField.CN_DISPLAYNAME.value());
    }
    public String getSpellName() {
        return this.get(String.class, RefDataField.SPELL_NAME.value());
    }
    public void setSpellName(String spellName) {
        this.set(spellName, RefDataField.SPELL_NAME.value());
    }
    public long getFreeShares() {
        return this.get(long.class, QuoteExtDataField.FREESHARES.value());
    }
    public void setFreeShares(long freeShares) {
        this.set(freeShares, QuoteExtDataField.FREESHARES.value());
    }
    public long getTotalShares() {
        return this.get(long.class, QuoteExtDataField.TOTOALSHARES.value());
    }
    public void setTotalShares(long totalShares) {
        this.set(totalShares, QuoteExtDataField.TOTOALSHARES.value());
    }
    public double getPERatio() {
        return this.get(double.class, QuoteExtDataField.PERATIO.value());
    }
    public void setPERatio(double peRatio) {
        this.set(peRatio, QuoteExtDataField.PERATIO.value());
    }
    public Date getTimeStamp(){
        return this.get(Date.class, QuoteExtDataField.TIMESTAMP.value());
    }
    public void setTimeStamp(Date timeStamp){
        this.set(timeStamp, QuoteExtDataField.TIMESTAMP.value());
    }
}
