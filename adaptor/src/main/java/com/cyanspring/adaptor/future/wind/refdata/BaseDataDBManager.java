package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketdata.BaseDataDBInfoEvent;
import com.cyanspring.common.staticdata.BaseDBData;
import com.cyanspring.common.util.TimeUtil;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Shuwei on 2015/10/28.
 */
public class BaseDataDBManager implements IPlugin{

    private static final Logger log = LoggerFactory
            .getLogger(BaseDataDBManager.class);

    @Autowired
    protected IRemoteEventManager eventManager;
    protected ScheduleManager scheduleManager = new ScheduleManager();
    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected long timerInterval = 1000;
    private volatile int dbRetryCount = 0;
    public static final int BASEDATADB_RETRY_COUNT = 2;
    private volatile boolean inited = false;
    private String baseDataFile;
    private IBaseDataDBHandler baseDataDBHandler;
    public static HashMap<String, BaseDBData> baseDBDataHashMap = new HashMap<>();
    private long lastGetDBTime = System.currentTimeMillis();


    protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        if(inited){
            //Check Process DB
            Date tempDate = new Date(System.currentTimeMillis());
            String tempDateStr = TimeUtil.formatDate(tempDate, "HH:mm:ss");
            if(tempDate.getTime() > lastGetDBTime && baseDataDBHandler.getExecuteTime().equals(tempDateStr)){
                lastGetDBTime = tempDate.getTime();
                log.debug("Process DB method - " + tempDate);
                processWindDB();
                BaseDataDBInfoEvent windBaseInfoEvent = new BaseDataDBInfoEvent(null,null, baseDBDataHashMap);
                eventManager.sendEvent(windBaseInfoEvent);
            }
        }
    }

    public void processWindDB(){
        if (baseDataDBHandler != null) {
            dbRetryCount = 0;
            //connect WindSyn DB
            baseDBDataHashMap.clear();
            baseDBDataHashMap = baseDataDBHandler.getBaseDBData();
            while ((baseDBDataHashMap == null || baseDBDataHashMap.size() == 0)
                    && dbRetryCount <= BaseDataDBManager.BASEDATADB_RETRY_COUNT) {
                baseDBDataHashMap = baseDataDBHandler.getBaseDBData();
                dbRetryCount++;
            }
            if (baseDBDataHashMap == null || baseDBDataHashMap.size() == 0) {
                //getData from file
                baseDBDataHashMap = FileUtil.getHashMapFromFile(baseDataFile);
            } else {
                //write last ExtendFile
                baseDataDBHandler.saveDBDataToQuoteExtendFile(baseDBDataHashMap);
                FileUtil.saveHashMapToFile(baseDataFile, baseDBDataHashMap);
            }
        }
    }

    @Override
    public void init() throws Exception {
        log.info("BaseDataManager init begin");
        inited = false;
        processWindDB();
        if(baseDataDBHandler != null) {
            eventProcessor.setHandler(this);
            eventProcessor.init();
            if (eventProcessor.getThread() != null) {
                eventProcessor.getThread().setName("BaseDataDBEvent");
            }

            if (!eventProcessor.isSync()) {
                scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                        eventProcessor, timerEvent);
            }
        }
        inited = true;
        log.info("BaseDataManager init end");
    }
    @Override
    public void uninit() {
        log.info("BaseDataManager uninit");
        if(baseDataDBHandler != null) {
            if (!eventProcessor.isSync()) {
                scheduleManager.uninit();
            }
            eventProcessor.uninit();
        }
    }

    public void setBaseDataDBHandler(IBaseDataDBHandler baseDataDBHandler) {
        this.baseDataDBHandler = baseDataDBHandler;
    }

    public void setBaseDataFile(String baseDataFile) {
        this.baseDataFile = baseDataFile;
    }

    public static void main(String[] args) throws Exception {
        String logConfigFile = "conf/windlog4j.xml";
        String configFile = "conf/baseDataDB.xml";
        DOMConfigurator.configure(logConfigFile);
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
        log.debug("Process DB begin");
        BaseDataDBManager baseDataDBManager = (BaseDataDBManager) context.getBean("baseDataDBManager");
        baseDataDBManager.init();
        log.debug("Process DB end");
    }
}
