package com.cyanspring.adaptor.future.wind.refdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cyanspring.adaptor.future.wind.WindType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.staticdata.CodeTableData;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataCommodity;
import com.cyanspring.common.util.ChineseConvert;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataParser {

    private static final Logger log = LoggerFactory
            .getLogger(RefDataParser.class);

    public static RefData convertCodeTableToRefData(CodeTableData codeTableData, HashMap<RefDataField,Object> defaultHashMap){
        RefData refData = new RefData();
        refData.setSymbol(codeTableData.getWindCode());
        refData.setRefSymbol(codeTableData.getWindCode());
        refData.setCode(codeTableData.getShortName());
        refData.setExchange(codeTableData.getSecurityExchange());
        refData.setCNDisplayName(codeTableData.getCnName());
        refData.setTWDisplayName(ChineseConvert.StoT(codeTableData.getCnName()));
        refData.setENDisplayName(codeTableData.getEnglishName());
        refData.setIType(String.valueOf(codeTableData.getSecurityType()));
        String commodity = (String) defaultHashMap.get(RefDataField.COMMODITY);
        if(commodity.equals("IT")){
            commodity = RefDataCommodity.INDEX.getValue();
            try {
            String lastIndexStr = codeTableData.getWindCode().substring(
                    codeTableData.getWindCode().lastIndexOf(".") + 3,
                    codeTableData.getWindCode().length()
            );
            refData.setSymbol("TWSE" + lastIndexStr);
            refData.setRefSymbol(refData.getSymbol());
            refData.setCode(codeTableData.getWindCode());
            }catch (Exception e){
                log.error(e.getMessage(),e);
                return null;
            }
        }
        if(commodity.equals("FT")){
            commodity = RefDataCommodity.FUTUREINDEX.getValue();
            try {
                String extractYYMMStr = codeTableData.getWindCode().replaceAll("\\D+", "").substring(2);
                refData.setSymbol(codeTableData.getShowID());
                refData.setDesc(codeTableData.getGroup());
                refData.setCurrency(codeTableData.getCurrency());
                refData.setENDisplayName(codeTableData.getProduct() + extractYYMMStr);
                refData.setTWDisplayName(codeTableData.getProductName() + extractYYMMStr);
                refData.setCNDisplayName(codeTableData.getProductName() + extractYYMMStr);
                refData.setRefSymbol(refData.getSymbol() + "." + codeTableData.getSecurityExchange());
                refData.setCategory(codeTableData.getProduct());
                refData.setSpotTWName(codeTableData.getProductName());
                refData.setCode(codeTableData.getWindCode());
            }catch (Exception e){
                log.error(e.getMessage(),e);
                return null;
            }
        }
        if(commodity.equals("FC")){
            switch(codeTableData.getSecurityType()){
                case WindType.FC_INDEX:
                case WindType.FC_INDEX_CX:
                    commodity = RefDataCommodity.FUTUREINDEX.getValue();
                    break;
                case WindType.FC_COMMODITY:
                case WindType.FC_COMMODITY_CX:
                    commodity = RefDataCommodity.FUTURECOMMODITY.getValue();
                    break;
                default:
                    commodity = RefDataCommodity.FUTUREINDEX.getValue();
                    break;
            }
        }
        refData.setCommodity(commodity);
        if (commodity.equals(RefDataCommodity.STOCK.getValue())) {
        	refData.setCategory(refData.getExchange());
        }
        if (commodity.equals(RefDataCommodity.INDEX.getValue())) {
        	refData.setCategory((String) defaultHashMap.get(RefDataField.CATEGORY));
        }
        if (commodity.equals(RefDataCommodity.STOCK.getValue())
        		|| commodity.equals(RefDataCommodity.INDEX.getValue())) {
        	refData.setSpellName(codeTableData.getSpellName());
            refData.setDecimalPoint(Integer.parseInt((String)defaultHashMap.get(RefDataField.DECIMALPOINT)));
            refData.setLotSize(Integer.parseInt((String) defaultHashMap.get(RefDataField.LOT_SIZE)));
            refData.setLimitMaximumLot(Integer.parseInt((String) defaultHashMap.get(RefDataField.LIMIT_MAXIMUM_LOT)));
            refData.setMarketMaximumLot(Integer.parseInt((String) defaultHashMap.get(RefDataField.MARKET_MAXIMUM_LOT)));
            refData.setMaximumHold(Integer.parseInt((String) defaultHashMap.get(RefDataField.MAXIMUM_HOLD)));
            refData.setMarginRate(Double.parseDouble((String) defaultHashMap.get(RefDataField.MARGIN_RATE)));
            refData.setTradable((String) defaultHashMap.get(RefDataField.TRADABLE));
            refData.setStrategy((String) defaultHashMap.get(RefDataField.STRATEGY));
            refData.setTickTable((String) defaultHashMap.get(RefDataField.TICK_TABLE));
            refData.setPricePerUnit(Double.parseDouble((String) defaultHashMap.get(RefDataField.MARGIN_RATE)));
            refData.setIndexSessionType((String) defaultHashMap.get(RefDataField.INDEX_SESSION_TYPE));
		}
        
        return refData;
    }

    public static <T> List<T> getListFromFile(String listPath) {
        XStream xstream = new XStream(new DomDriver());
        File file = new File(listPath);
        List<T> list = new ArrayList<>();
        try {
            if (file.exists()) {
                list = (List<T>) xstream.fromXML(file);
            } else {
                log.error("Missing file: " + listPath);
            }
        }catch (Exception e) {
            log.error("load file X ", e);
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
        try {
            if (file.exists()) {
                hashMap = (HashMap<K, T>) xstream.fromXML(file);
            } else {
                log.error("Missing file: " + hashMapPath);
            }
        }catch (Exception e) {
            log.error("load file X ", e);
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
