package com.cyanspring.common.marketdata;

import com.cyanspring.common.data.DataObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Description....
 * <ul>
 * <li> Description
 * </ul>
 * <p/>
 * Description....
 * <p/>
 * Description....
 * <p/>
 * Description....
 *
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public interface IQuoteSaver {
    HashMap<String, Quote> loadQuotes(String fileName);

    HashMap<String, DataObject> loadExtendQuotes(String fileName);

    void saveLastQuoteToFile(String fileName, Map<String, Quote> quotes);

    void saveLastTradeDateQuoteToFile(String lastTdqName,String lastQuoteFileName, Map<String, Quote> quotes);

    void saveLastQuoteExtendToFile(String fileName, Map<String, DataObject> quoteExtends);

    void saveLastTradeDateQuoteExtendToFile(String fileName, Map<String, DataObject> quoteExtends, Map<String, DataObject> lastTradeDateQuoteExtends);
}
