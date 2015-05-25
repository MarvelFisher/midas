package com.cyanspring.server.livetrading;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.livetrading.LiveTradingEndEvent;
import com.cyanspring.common.event.livetrading.LiveTradingStartEvent;
import com.cyanspring.common.util.TimeUtil;


public class LiveTradingSession implements IPlugin {
	
    private static final Logger log = LoggerFactory
            .getLogger(LiveTradingSession.class);
    
	@Autowired(required=false)
	private LiveTradingSetting liveTradingSetting;	
	
	@Autowired
    private IRemoteEventManager eventManager;	
	
	private LiveTradingState liveTradingState;	
	private AsyncTimerEvent stopUserTradingEvent = new AsyncTimerEvent();	
	private AsyncTimerEvent startUserTradingEvent = new AsyncTimerEvent();	
	private ScheduleManager scheduleManager = new ScheduleManager();
	
    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(AsyncTimerEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }

    };
	@Override
	public void init() throws Exception {
				
		if(!checkLiveTradingSessionSetting()){		
			log.error("Live Trade Setting not setting well");			
			return;		
		}	
		eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null){
            eventProcessor.getThread().setName("LiveTradingSession");
        }
        try{
            initState(); 
        }catch(Exception e){
        	log.error(e.getMessage(),e);
        	return;
        }
        scheduleLiveTradingEvent(stopUserTradingEvent);
        scheduleLiveTradingEvent(startUserTradingEvent);
        
	}
	private void scheduleLiveTradingEvent(AsyncTimerEvent event) {
		
		try {
			
			if( stopUserTradingEvent== event ){
				
				String stopStartTime = liveTradingSetting.getUserStopLiveTradingStartTime();
				Date stopStartDateTime = getDateFromString(stopStartTime);
				Date now = Clock.getInstance().now();
		        if (TimeUtil.getTimePass(now, stopStartDateTime) > 0){
		        	stopStartDateTime = TimeUtil.getNextDay(stopStartDateTime);
		        }
	            scheduleManager.scheduleTimerEvent(stopStartDateTime, eventProcessor, stopUserTradingEvent);
		        
			}else if( startUserTradingEvent == event ){
				
				String stopEndTime = liveTradingSetting.getUserStopLiveTradingEndTime();
				Date stopEndDateTime = getDateFromString(stopEndTime);
				Date now = Clock.getInstance().now();
		        if (TimeUtil.getTimePass(now, stopEndDateTime) > 0){
		        	stopEndDateTime = TimeUtil.getNextDay(stopEndDateTime);
		        }
	            scheduleManager.scheduleTimerEvent(stopEndDateTime, eventProcessor, startUserTradingEvent);

			}
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		
	}
	private boolean checkLiveTradingSessionSetting(){
		
		if(null == liveTradingSetting){
			return false;
		}
		
		if( null == liveTradingSetting.getUserStopLiveTradingStartTime()  
				||	null == liveTradingSetting.getUserStopLiveTradingEndTime()){
			return false;
		}
			
		if( liveTradingSetting.getUserStopLiveTradingStartTime().split(":").length != 3 ){
			return false;
		}
		
		if( liveTradingSetting.getUserStopLiveTradingEndTime().split(":").length != 3 ){
			return false;
		}
		
		return true;
	}
	
	
	private Date getDateFromString(String time)throws Exception{
		
		String[] times = time.split(":");
		
        if (times.length != 3){
            throw new Exception("time format is invalid ( HH:MM:SS )");
        }
        int timesHour = Integer.parseInt(times[0]);
        int timesMin = Integer.parseInt(times[1]);
        int timesSecond = Integer.parseInt(times[2]);
        
        Calendar cal = Default.getCalendar();
        Date tempNow = Clock.getInstance().now();
        Date tempDate = TimeUtil.getScheduledDate(cal, tempNow, timesHour, timesMin, timesSecond);
		
        return tempDate;		
	}
	
	
	public boolean isNowInTradingTime(String startTime,String endTime) throws Exception{
		
		long startToday = getDateFromString(startTime).getTime();
		long stopToday = getDateFromString(endTime).getTime();
        long now = Clock.getInstance().now().getTime();
		 
        if(startToday >= stopToday){
        	throw new Exception("start time over or equal than end time");
        }
       
        if(startToday <= now && now <= stopToday){
			return false;
		}else {
			return true;
		}
		
	}
	private void initState() throws Exception {
		
		if(null == liveTradingSetting){
			return;
		}
		
		String stopStartTime = liveTradingSetting.getUserStopLiveTradingStartTime();
		String stopEndTime = liveTradingSetting.getUserStopLiveTradingEndTime();
					
		if(isNowInTradingTime(stopStartTime, stopEndTime)){
				setLiveTradingState(LiveTradingState.USER_LIVE_TRADING_ON);
		}else{
				setLiveTradingState(LiveTradingState.USER_LIVE_TRADING_OFF);
		}
			
	}

	@Override
	public void uninit() {
		scheduleManager.uninit();
		eventProcessor.uninit();
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event){		
		
		if( startUserTradingEvent == event){			 

			setLiveTradingState(LiveTradingState.USER_LIVE_TRADING_ON);
			try {		
				log.info("User Live Trading Start:"+Clock.getInstance().now());
				eventManager.sendRemoteEvent(new LiveTradingStartEvent(null, null));
				scheduleLiveTradingEvent(startUserTradingEvent);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
			
		}else if( stopUserTradingEvent == event ){			

			setLiveTradingState(LiveTradingState.USER_LIVE_TRADING_OFF);
			try {	
				log.info("User Live Trading Stop:"+Clock.getInstance().now());
				eventManager.sendEvent(new LiveTradingEndEvent(null, null));
				scheduleLiveTradingEvent(stopUserTradingEvent);			
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
			
		}
		
	}
	public boolean isAllowLiveTrading(){
		
		if( LiveTradingState.USER_LIVE_TRADING_ON == getLiveTradingState() ){
			return true;
		}else{
			return false;
		}
		
	}
	
	
	public LiveTradingState getLiveTradingState() {
		return liveTradingState;
	}

	private void setLiveTradingState(LiveTradingState liveTradingState) {
		this.liveTradingState = liveTradingState;
	}
	

}
