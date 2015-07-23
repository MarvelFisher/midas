package com.cyanspring.adaptor.future.wind.data;

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.data.DataObject;

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
}
