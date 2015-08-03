package com.cyanspring.common.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.util.TimeUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
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
public class QuoteSaver implements IQuoteSaver {

    private static final Logger log = LoggerFactory.getLogger(QuoteSaver.class);
    private XStream xstream = new XStream(new DomDriver());
    private long lastQuoteSaveInterval = 20000;
    private long lastQuoteExtendSaveInterval = 20000;
    private Date lastQuoteSaveTime = Clock.getInstance().now();
    private Date lastQuoteExtendSaveTime = Clock.getInstance().now();

    @Override
    public HashMap<String, Quote> loadQuotes(String fileName) {
        File file = new File(fileName);
        HashMap<String, Quote> quotes = new HashMap<>();
        if (file.exists() && quotes.size() <= 0) {
            try {
                ClassLoader save = xstream.getClassLoader();
                ClassLoader cl = HashMap.class.getClassLoader();
                if (cl != null)
                    xstream.setClassLoader(cl);
                quotes = (HashMap<String, Quote>) xstream.fromXML(file);
                if (!(quotes instanceof HashMap))
                    throw new Exception("Can't xstream load last quote: "
                            + fileName);
                xstream.setClassLoader(save);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            for (Quote quote : quotes.values()) {
                quote.setStale(true);
            }
            log.info("Quotes loaded: " + fileName);
        }
        return quotes;
    }

    @Override
    public HashMap<String, DataObject> loadExtendQuotes(String fileName) {
        File file = new File(fileName);
        HashMap<String, DataObject> quoteExtends = new HashMap<>();
        if (file.exists() && quoteExtends.size() <= 0) {
            try {
                ClassLoader save = xstream.getClassLoader();
                ClassLoader cl = HashMap.class.getClassLoader();
                if (cl != null)
                    xstream.setClassLoader(cl);
                quoteExtends = (HashMap<String, DataObject>) xstream.fromXML(file);
                if (!(quoteExtends instanceof HashMap))
                    throw new Exception("Can't xstream load last quote: "
                            + fileName);
                xstream.setClassLoader(save);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            log.info("QuoteExtends loaded: " + fileName);
        }
        return quoteExtends;
    }

    @Override
    public void saveLastQuoteToFile(String fileName, Map<String, Quote> quotes) {
        if (TimeUtil.getTimePass(lastQuoteSaveTime) < lastQuoteSaveInterval)
            return;

        if (quotes.size() <= 0)
            return;

        lastQuoteSaveTime = Clock.getInstance().now();
        synchronized (quotes){saveQuotesToFile(fileName, quotes);}
    }

    @Override
    public void saveLastTradeDateQuoteToFile(String fileName, Map<String, Quote> quotes, Map<String, Quote> lastTradeDateQuotes) {
        if (lastTradeDateQuotes.size() <= 0 && quotes.size() <= 0)
            return;
        lastTradeDateQuotes = quotes;
        saveQuotesToFile(fileName, lastTradeDateQuotes);
    }

    @Override
    public void saveLastQuoteExtendToFile(String fileName, Map<String, DataObject> quoteExtends) {
        if (TimeUtil.getTimePass(lastQuoteExtendSaveTime) < lastQuoteExtendSaveInterval)
            return;

        if (quoteExtends.size() <= 0)
            return;

        lastQuoteExtendSaveTime = Clock.getInstance().now();
        synchronized (quoteExtends){saveQuotesToFile(fileName, quoteExtends);}
    }

    @Override
    public void saveLastTradeDateQuoteExtendToFile(String fileName, Map<String, DataObject> quoteExtends, Map<String, DataObject> lastTradeDateQuoteExtends) {
        if (lastTradeDateQuoteExtends.size() <= 0 && quoteExtends.size() <= 0)
            return;
        lastTradeDateQuoteExtends = quoteExtends;
        saveQuotesToFile(fileName, lastTradeDateQuoteExtends);
    }

    private void saveQuotesToFile(String fileName, Map quotes) {
        File file = new File(fileName);
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
                xstream.toXML(quotes, os);
            os.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
