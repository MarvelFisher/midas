package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.common.staticdata.BaseDBData;
import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.data.JdbcSQLHandler;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Shuwei.Kuo.
 */
public class WindBaseDataDBHandler implements IBaseDataDBHandler{

    private static final Logger log = LoggerFactory
            .getLogger(WindBaseDataDBHandler.class);

    private BasicDataSource basicDataSource;
    private String lastQuoteExtendFile;
    private String executeTime = "08:15:00";

    public void saveDBDataToQuoteExtendFile(HashMap<String,BaseDBData> BaseDBDataHashMap){
        log.debug("write quoteExtend file begin");
        HashMap<String, DataObject> quoteExtends = FileUtil.getHashMapFromFile(lastQuoteExtendFile);
        if(BaseDBDataHashMap != null && BaseDBDataHashMap.size() > 0){
            for(String symbol: BaseDBDataHashMap.keySet()){
                BaseDBData baseDBData = BaseDBDataHashMap.get(symbol);
                DataObject quoteExtend;
                if(quoteExtends.containsKey(symbol)){
                    quoteExtend = quoteExtends.get(symbol);
                }else{
                    quoteExtend = new DataObject();
                    quoteExtend.put(QuoteExtDataField.SYMBOL.value(), symbol);
                    quoteExtends.put(symbol,quoteExtend);
                }
                if(!quoteExtend.fieldExists(QuoteExtDataField.TIMESTAMP.value())) {
                    quoteExtend.put(QuoteExtDataField.TIMESTAMP.value(), Clock.getInstance().now());
                }
                quoteExtend.put(QuoteExtDataField.FREESHARES.value(), baseDBData.getFreeShares());
                quoteExtend.put(QuoteExtDataField.TOTOALSHARES.value(), baseDBData.getTotalShares());
                quoteExtend.put(QuoteExtDataField.PERATIO.value(), baseDBData.getPERatio());
            }
            FileUtil.saveHashMapToFile(lastQuoteExtendFile, quoteExtends);
        }
        log.debug("write quoteExtend file end");
    }

    public HashMap<String, BaseDBData> getBaseDBData() {
        log.debug("wind baseDB process start");
        HashMap<String, BaseDBData> BaseDBDataHashMap = new HashMap<>();
        BaseDBData baseDBData = null;
        Date timeStamp;
        Connection conn = null;
        Statement stmt = null;
        try {
            JdbcSQLHandler jdbcSQLHandler = new JdbcSQLHandler(basicDataSource);
            conn = jdbcSQLHandler.getConnect();
            stmt = conn.createStatement();
            String sql =
                    "SELECT \n" +
                            "\tMAIN.*,IFNULL(SFREE.FREESHARES,0) FREESHARES,IFNULL(STOTAL.TOTALSHARES,0) TOTALSHARES\n" +
                            "    ,IFNULL(SPE.PERATIO,0) PERATIO,IFNULL(SEPS.EPSTTM,0) EPSTTM\n" +
                            "FROM\n" +
                            "(\n" +
                            "SELECT \n" +
                            "\tS_INFO_WINDCODE WINDCODE,S_INFO_NAME CNNAME,IFNULL(S_INFO_COMPNAMEENG,'') ENNAME,S_INFO_PINYIN PINYIN,'S' MARKETTYPE\n" +
                            "FROM ASHAREDESCRIPTION\n" +
                            "WHERE S_INFO_EXCHMARKET IN ('SSE','SZSE') AND S_INFO_DELISTDATE IS NULL AND S_INFO_NAME NOT LIKE '%ST%'\n" +
                            "UNION ALL\n" +
                            "SELECT \n" +
                            "\t(CASE S_INFO_WINDCODE WHEN '000016.SH' THEN '999987.SH' WHEN '000001.SH' THEN '999999.SH' ELSE S_INFO_WINDCODE END) WINDCODE\n" +
                            "\t,S_INFO_NAME,'' ENG,'' PINYIN,'I' AS MARKETTYPE\n" +
                            "FROM AINDEXDESCRIPTION\n" +
                            "WHERE S_INFO_EXCHMARKET IN ('SSE','SZSE') \n" +
                            "AND S_INFO_WINDCODE IN ('399001.SZ','399006.SZ','399905.SZ','000016.SH','399300.SZ','000001.SH')\n" +
                            ") AS MAIN\n" +
                            "LEFT JOIN\n" +
                            "(\n" +
                            "\tSELECT SF.S_INFO_WINDCODE WINDCODE, SF.S_SHARE_FREESHARES*10000 FREESHARES\n" +
                            "\tFROM ASHAREFREEFLOAT SF\n" +
                            "\tRIGHT JOIN\n" +
                            "\t(\n" +
                            "\tselect MAX(CHANGE_DT1) MAXCDT,S_INFO_WINDCODE WINDCODE\n" +
                            "\tfrom ASHAREFREEFLOAT\n" +
                            "\tgroup by S_INFO_WINDCODE\n" +
                            "\t) MAXSF ON SF.S_INFO_WINDCODE = MAXSF.WINDCODE AND SF.CHANGE_DT1 = MAXSF.MAXCDT\n" +
                            ") SFREE ON SFREE.WINDCODE = MAIN.WINDCODE\n" +
                            "LEFT JOIN\n" +
                            "(\n" +
                            "\tSELECT SA.S_INFO_WINDCODE WINDCODE, SA.TOT_SHR*10000 TOTALSHARES\n" +
                            "\tFROM ASHARECAPITALIZATION SA\n" +
                            "\tRIGHT JOIN\n" +
                            "\t(\n" +
                            "\tselect MAX(CHANGE_DT1) MAXCDT,S_INFO_WINDCODE WINDCODE\n" +
                            "\tfrom ASHARECAPITALIZATION\n" +
                            "\tgroup by S_INFO_WINDCODE\n" +
                            "\t) MAXSA ON SA.S_INFO_WINDCODE = MAXSA.WINDCODE AND SA.CHANGE_DT1 = MAXSA.MAXCDT\n" +
                            ") STOTAL ON STOTAL.WINDCODE = MAIN.WINDCODE\n" +
                            "LEFT JOIN\n" +
                            "(\n" +
                            "\tSELECT SEOD.S_INFO_WINDCODE WINDCODE, SEOD.S_VAL_PE PERATIO\n" +
                            "\tFROM ASHAREEODDERIVATIVEINDICATOR SEOD\n" +
                            "\tRIGHT JOIN\n" +
                            "\t(\n" +
                            "\tselect MAX(TRADE_DT) MAXDT,S_INFO_WINDCODE WINDCODE\n" +
                            "\tfrom ASHAREEODDERIVATIVEINDICATOR\n" +
                            "\tgroup by S_INFO_WINDCODE\n" +
                            "\t) MAXSEOD ON SEOD.S_INFO_WINDCODE = MAXSEOD.WINDCODE AND SEOD.TRADE_DT = MAXSEOD.MAXDT\n" +
                            ") SPE ON SPE.WINDCODE = MAIN.WINDCODE\n" +
                            "LEFT JOIN\n" +
                            "(\n" +
                            "\tSELECT S_INFO_WINDCODE WINDCODE,S_FA_EPS_TTM EPSTTM\n" +
                            "\tFROM ASHARETTMANDMRQ\n" +
                            "\tWHERE S_FA_EPS_TTM IS NOT NULL\n" +
                            ") SEPS ON SEPS.WINDCODE = MAIN.WINDCODE\n" +
                            "ORDER BY MAIN.MARKETTYPE,MAIN.WINDCODE;";
//            log.debug("BaseDataDB SQL = " + sql);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String windcode = rs.getString("WINDCODE");
                String cnName = rs.getString("CNNAME");
                String enName = rs.getString("ENNAME");
                String pinyin = rs.getString("PINYIN");
                Number freeShares = rs.getBigDecimal("FREESHARES");
                Number totalShares = rs.getBigDecimal("TOTALSHARES");
                Number peRatio = rs.getBigDecimal("PERATIO");
                Number epsttm = rs.getBigDecimal("EPSTTM");
                baseDBData = new BaseDBData();
                baseDBData.setSymbol(windcode);
                baseDBData.setSpellName(pinyin);
                baseDBData.setCNDisplayName(cnName);
                baseDBData.setENDisplayName(enName);
                baseDBData.setFreeShares(freeShares.longValue());
                baseDBData.setTotalShares(totalShares.longValue());
                baseDBData.setPERatio(peRatio.doubleValue());
                baseDBData.setEpsTTM(epsttm.doubleValue());
                timeStamp = Clock.getInstance().now();
                baseDBData.setTimeStamp(timeStamp);
                BaseDBDataHashMap.put(windcode, baseDBData);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            log.error(se.getMessage(), se);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                log.error(se.getMessage(), se);
            }
        }
        log.debug("wind baseDB process end");
        return BaseDBDataHashMap;
    }

    public void setLastQuoteExtendFile(String lastQuoteExtendFile) {
        this.lastQuoteExtendFile = lastQuoteExtendFile;
    }

    public void setBasicDataSource(BasicDataSource basicDataSource) {
        this.basicDataSource = basicDataSource;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }

    public String getExecuteTime() {
        return executeTime;
    }
}
