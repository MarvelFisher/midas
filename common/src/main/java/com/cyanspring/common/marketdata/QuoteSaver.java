package com.cyanspring.common.marketdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.util.TimeUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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
        synchronized (quotes){saveQuotesToFile(fileName,"", quotes);}
    }

    @Override
    public void saveLastTradeDateQuoteToFile(String fileName,String lastQuoteFileName, Map<String, Quote> quotes) {
        if (quotes.size() <= 0 && !StringUtils.hasText(lastQuoteFileName))
            return;
        
        saveQuotesToFile(fileName,lastQuoteFileName, quotes);
    }

    @Override
    public void saveLastQuoteExtendToFile(String fileName, Map<String, DataObject> quoteExtends) {
        if (TimeUtil.getTimePass(lastQuoteExtendSaveTime) < lastQuoteExtendSaveInterval)
            return;

        if (quoteExtends.size() <= 0)
            return;

        lastQuoteExtendSaveTime = Clock.getInstance().now();
        synchronized (quoteExtends){saveQuotesToFile(fileName,"", quoteExtends);}
    }

    @Override
    public void saveLastTradeDateQuoteExtendToFile(String fileName, Map<String, DataObject> quoteExtends, Map<String, DataObject> lastTradeDateQuoteExtends) {
        if (lastTradeDateQuoteExtends.size() <= 0 && quoteExtends.size() <= 0)
            return;
        lastTradeDateQuoteExtends = quoteExtends;
        saveQuotesToFile(fileName,"", lastTradeDateQuoteExtends);
    }
    
    private void saveQuotesToFile(String fileName,String copyFrom, Map quotes) {

		String prevFileName = fileName+".prev";
		String nowFileName = fileName;
		String nextFileName = fileName+".next";
		File prevFile = new File(prevFileName);
		File nowFile = new File(nowFileName);
		File nextFile = new File(nextFileName);
		File lastQuoteFile = null;
		boolean success = true;
		
    	try {
    		
    		if(prevFile != null && prevFile.exists()){
    			success = prevFile.delete();
    			if(!success)
    				log.warn("can't delete file:{}",prevFileName);
    			
    		}
    		
    		if(nowFile != null){  			
    		    success = nowFile.renameTo(prevFile);
    			if(!success)
    				log.warn("can't rename file:{} -> {}",nowFileName,prevFileName);
    		}
       
			if(StringUtils.hasText(copyFrom)){
				lastQuoteFile = new File(copyFrom);
			}
			
			if(lastQuoteFile!=null && lastQuoteFile.exists()){
				copyFile(new File(copyFrom), nextFile);
			}else{
				quoteToXML(nextFile,quotes);
			}
            nextFile.renameTo(nowFile);                 
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }finally{       	
        	prevFile = null;     
        	nowFile = null;
        	nextFile = null;     	
        }
    }
    
    private void quoteToXML(File toFile,Map quotes) throws IOException{
    	toFile.createNewFile();
        FileOutputStream os = new FileOutputStream(toFile);
            xstream.toXML(quotes, os);
        os.close();
    }
    
    private void copyFile(File source,File dest) throws IOException{
    	
    	InputStream inStream = null;
    	OutputStream outStream = null;
    	boolean success = false;
    	try{
 
    	    inStream = new FileInputStream(source);
    	    outStream = new FileOutputStream(dest);
 
    	    byte[] buffer = new byte[1024];
 
    	    int length;
    	    while ((length = inStream.read(buffer)) > 0){
    	    	outStream.write(buffer, 0, length);
    	    }
 
    	    if (inStream != null)
    	    	inStream.close();
    	    
    	    if (outStream != null)
    	    	outStream.close();
 
    	    success = true;
    	}finally{
    		
    		if(!success && null != dest ){
        	    if (inStream != null)
        	    	inStream.close();
        	    
        	    if (outStream != null)
        	    	outStream.close();
        	    
    			log.warn("write file fail, delete file:{}",dest.getName());
    			dest.delete();
    		}
    		
    	}
    }
}
