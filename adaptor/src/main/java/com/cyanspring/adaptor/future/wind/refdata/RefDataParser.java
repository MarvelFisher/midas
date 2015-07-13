package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.ChineseConvert;
import java.util.HashMap;

public class RefDataParser {

    public static RefData convertCodeTableToRefData(CodeTableData codeTableData, HashMap<RefDataField,Object> defaultHashMap){
        RefData refData = new RefData();
        refData.setSymbol(codeTableData.getWindCode());
        refData.setExchange(codeTableData.getSecurityExchange());
        refData.setCNDisplayName(codeTableData.getCnName());
        refData.setTWDisplayName(ChineseConvert.StoT(codeTableData.getCnName()));
        refData.setENDisplayName(codeTableData.getEnglishName());
        refData.setSpellName(codeTableData.getSpellName());
        refData.setCommodity((String) defaultHashMap.get(RefDataField.COMMODITY));
        switch ((String)defaultHashMap.get(RefDataField.COMMODITY)) {
            case "S":
            case "I":
                refData.setCategory((String) defaultHashMap.get(RefDataField.CATEGORY));
                refData.setLotSize(Integer.parseInt((String) defaultHashMap.get(RefDataField.LOT_SIZE)));
                refData.setLimitMaximumLot(Integer.parseInt((String) defaultHashMap.get(RefDataField.LIMIT_MAXIMUM_LOT)));
                refData.setMarketMaximumLot(Integer.parseInt((String) defaultHashMap.get(RefDataField.MARKET_MAXIMUM_LOT)));
                refData.setMaximumHold(Integer.parseInt((String) defaultHashMap.get(RefDataField.MAXIMUM_HOLD)));
                refData.setMarginRate(Double.parseDouble((String) defaultHashMap.get(RefDataField.MARGIN_RATE)));
                refData.setTradable((String) defaultHashMap.get(RefDataField.TRADABLE));
                refData.setStrategy((String) defaultHashMap.get(RefDataField.STRATEGY));
                refData.setTickTable((String) defaultHashMap.get(RefDataField.TICK_TABLE));
                refData.setPricePerUnit(Double.parseDouble((String) defaultHashMap.get(RefDataField.MARGIN_RATE)));
                break;
            case "F":
                break;
            default:
                break;
        }
        return refData;
    }
}
