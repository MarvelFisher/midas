package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.ChineseConvert;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RefDataParser {

    private static final Logger log = LoggerFactory
            .getLogger(RefDataParser.class);

    public static RefData convertCodeTableToRefData(CodeTableData codeTableData, HashMap<RefDataField,Object> defaultHashMap){
        RefData refData = new RefData();
        refData.setSymbol(codeTableData.getWindCode());
        refData.setExchange(codeTableData.getSecurityExchange());
        refData.setCNDisplayName(codeTableData.getCnName());
        refData.setTWDisplayName(ChineseConvert.StoT(codeTableData.getCnName()));
        refData.setENDisplayName(codeTableData.getEnglishName());
        refData.setSpellName(codeTableData.getSpellName());
        refData.setIType(String.valueOf(codeTableData.getSecurityType()));
        refData.setCommodity((String) defaultHashMap.get(RefDataField.COMMODITY));
        switch ((String)defaultHashMap.get(RefDataField.COMMODITY)) {
            case "S":
            case "I":
                refData.setDecimalPoint(Integer.parseInt((String)defaultHashMap.get(RefDataField.DECIMALPOINT)));
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

    public static <T> List<T> getListFromFile(String listPath) {
        XStream xstream = new XStream(new DomDriver());
        File file = new File(listPath);
        List<T> list = new ArrayList<>();
        if (file.exists()) {
            list = (List<T>) xstream.fromXML(file);
        } else {
            log.error("Missing file: " + listPath);
        }
        return list;
    }

    public static <T> void saveListToFile(String path, List<T> list) {
        File file = new File(path);
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            xstream.toXML(list, writer);
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static <K, T> HashMap<K, T> getHashMapFromFile(String hashMapPath){
        XStream xstream = new XStream(new DomDriver());
        File file = new File(hashMapPath);
        HashMap<K, T> hashMap = new HashMap<>();
        if (file.exists()) {
            hashMap = (HashMap<K, T>) xstream.fromXML(file);
        } else {
            log.error("Missing file: " + hashMapPath);
        }
        return hashMap;
    }

    public static <K, T> void saveHashMapToFile(String path, HashMap<K, T> hashMap) {
        File file = new File(path);
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            xstream.toXML(hashMap, writer);
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
