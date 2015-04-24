package com.cyanspring.server.account;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.account.State;
import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.UserTerminateReplyEvent;
import com.cyanspring.common.event.order.CancelStrategyOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.server.persistence.PersistenceManager;

public class LiveTradingChecker{
	
	public enum LiveTradingState{
		
		ON_CLOSING_POSITION,ON_GOING
		
	}
	
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingChecker.class);
	private Lock lock = new ReentrantLock();
	private boolean startLiveTrading ;
	@Autowired
	private LiveTradingSetting liveTradingSetting;
	@Autowired
	private PositionKeeper positionKeeper;
	@Autowired
	private AccountKeeper accountKeeper;
	@Autowired
	private IRemoteEventManager eventManager;
	
	private IQuoteChecker quoteChecker = new PriceQuoteChecker();
	
	private AsyncTimerEvent closeAllPositionEvent = new AsyncTimerEvent();
	private ScheduleManager scheduleManager = new ScheduleManager();
	private long timeInterval = 1000;
	private LiveTradingState status = LiveTradingState.ON_GOING;
	
	private AsyncEventProcessor timerProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}

	};
	
	@PostConstruct
	public void init() throws Exception{

		timerProcessor.setHandler(this);
		timerProcessor.init();
		if(timerProcessor.getThread() != null)
			timerProcessor.getThread().setName("LiveTradingChecker-Timer");
		
//		scheduleManager.scheduleRepeatTimerEvent(timeInterval, timerProcessor, closeAllPositionEvent);
		if(startLiveTrading)
			scheduleCloseAllPostionEvent();
	}
	@PreDestroy
	public void preDestroy(){
		scheduleManager.cancelTimerEvent(closeAllPositionEvent);
		timerProcessor.uninit();
	}
	
	private void scheduleCloseAllPostionEvent(){
		try{
			
			Date scheduleDate = getClosAllPositionTime();
			
			if(null == scheduleDate){
				log.warn("LiveTradingSetting doesn't set close time");
				return;
			}
			
			log.info("scheduleDate : {}",scheduleDate);
			
			scheduleManager.scheduleTimerEvent(scheduleDate, timerProcessor, closeAllPositionEvent);
		
		}catch(Exception e){
			
			log.error(e.getMessage(),e);
			
		}
		
	}
	private Date getClosAllPositionTime() throws Exception{
		String time = liveTradingSetting.getCloseAllPositionTime();
		if(!StringUtils.hasText(time)){
			return null;
		}

		String[] times = time.split(":");
		
		if(times.length != 3)
			throw new Exception("LiveTradingSetting close time is not valid");
		
		int nHour = Integer.parseInt(times[0]);
		int nMin = Integer.parseInt(times[1]);
		int nSecond = Integer.parseInt(times[2]);
		
		Calendar cal = Default.getCalendar();
		Date now = Clock.getInstance().now();
		
		Date scheduledToday = TimeUtil.getScheduledDate(cal, now, nHour, nMin, nSecond);
		
		if(TimeUtil.getTimePass(now, scheduledToday) > 0)
			scheduledToday = TimeUtil.getNextDay(scheduledToday);
		
		return scheduledToday;
	}
	public void processAsyncTimerEvent(AsyncTimerEvent event){
		try{
			if(!startLiveTrading){
				return;
			}
			log.info("LiveTradingState.ON_CLOSING_POSITION");
			status = LiveTradingState.ON_CLOSING_POSITION;
			//Thread.sleep(100*1000);
			List<Account> accountList= accountKeeper.getAllAccounts();
			
			for(Account account:accountList){
				log.info("close position account:{}",account.getId());
				closeAllPositoinAndOrder(account,OrderReason.LiveTradingRegularClose);
				
			}

		}catch(Exception e){
			
			log.error(e.getMessage(),e);
			
		}finally{
			
			status = LiveTradingState.ON_GOING;
			
			scheduleCloseAllPostionEvent();
			
		}

	}
	
	
	public Double getPositionStopLoss(Account account,AccountSetting accountSetting,Double positionStopLoss){
		
		if( !startLiveTrading || !liveTradingSetting.isNeedCheckPosition()){
			return positionStopLoss;
		}	
		
		
		
		Double stopLoss= account.getStartAccountValue() * accountSetting.getStopLossPercent();
		
		if(PriceUtils.isZero(stopLoss)){
			return positionStopLoss;
		}else{				
			stopLoss = Math.min(stopLoss, positionStopLoss);
		}
		
		
		return stopLoss;
		
	}
	private boolean isValidQuote(String symbol){
		
		Quote quote = positionKeeper.getQuote(symbol);
		
		if(null != quoteChecker && !quoteChecker.check(quote))
			return false;
		
		return !quote.isStale();
	}
	
	private void closeAllPositoinAndOrder(Account account,OrderReason reason){
		lock.lock();
		try{
				
			
				List <OpenPosition> positionList = positionKeeper.getOverallPosition(account);
				
				List <ParentOrder> orderList = positionKeeper.getParentOrders(account.getId());
				
				
				log.info("orderList:{}",orderList.size());
				//close all order
				if(null != orderList && orderList.size() >0){
					
					for(ParentOrder order : orderList){
						
						if( null != order 
								&& !order.getOrdStatus().isCompleted()){
		
								String source = order.get(String.class, OrderField.SOURCE.value());
								String txId = order.get(String.class, OrderField.CLORDERID.value());
								CancelStrategyOrderEvent cancel = 
										new CancelStrategyOrderEvent(order.getId(), order.getSender(), txId, source, OrderReason.StopLoss, false);
								eventManager.sendEvent(cancel);
		
						}//!order.getOrdStatus().isCompleted()
										
					}//ParentOrder order
								
				}//null != orderList	
				
				
				
				//close all position
				log.info("positionList:{}",positionList.size());
				if(null != positionList && positionList.size() >0){
					
					for(OpenPosition position : positionList){
						
						ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(), 
								null, position.getAccount(), position.getSymbol(), 0.0, OrderReason.StopLoss,
								IdGenerator.getInstance().getNextID());
						
						eventManager.sendEvent(event);
					}
					
				}
				
		}finally{
			
			lock.unlock();
			
		}
	
	}
	private void sendUpdateAccountEvent(Account account){
		try {
			
			//store in db
			eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));

			if(State.TERMINATED == account.getState()){
				String message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_TERMINATED, "Live Trading - Account:"+account.getId()+" is terminated caused by exceed loss limit");
				eventManager.sendRemoteEvent(new UserTerminateReplyEvent(null, null ,true, message, account.getId(), TerminationStatus.TERMINATED));
			}
			
			
		} catch (Exception e) {
			
			log.warn(e.getMessage(),e);
			
		}
	}
	private boolean checkQuotesAreAllValid(Account account){
		boolean allValid = false;
		List <OpenPosition> positionList = positionKeeper.getOverallPosition(account);
		if(null != positionList && positionList.size() >0){
			
			for(OpenPosition position : positionList){
				
				if(!isValidQuote(position.getSymbol())){
					log.info("quote not valid:"+position.getSymbol());
					return false;
				}
				
			}
			allValid = true;
		}else{
			allValid = true;
		}
		
		return allValid;
		
	}
	
	
	public boolean checkFreezeLoss(Account account,AccountSetting accountSetting){
		if( !startLiveTrading || !liveTradingSetting.isNeedCheckFreeze()){
			log.info("no need to check freeze");
			return false;
		}
		if(PriceUtils.isZero(accountSetting.getFreezePercent())){
			log.info("Freeze percent is 0");
			return false;
		}
		
		if(State.FROZEN == account.getState() ){
			
			if(hasOpenPositionsOrOrders(account)){
				
				closeAllPositoinAndOrder(account,OrderReason.StopLoss);
				
			}
			
			return true;
			
		}
		
		boolean isFrozen =false;
		
		Double fdtDailyStopLoss=account.getStartAccountValue() * accountSetting.getFreezePercent();
		//log.info("account:"+account.getId()+" state:"+account.getState());
		if(PriceUtils.isZero(fdtDailyStopLoss)){
			
			//log.info("fdtDailyStopLoss is 0 - StartAccountValue:"+account.getStartAccountValue()+" - FreezePercent:"+accountSetting.getFreezePercent());
			return false;
		}
		
		Double dailyLoss = account.getDailyPnL();

		
		if(PriceUtils.EqualLessThan(dailyLoss, -fdtDailyStopLoss)){
			
			log.info("account:"+account.getId());
			log.info("dailyLoss:"+dailyLoss);
			log.info("fdtDailyStopLoss:"+fdtDailyStopLoss);
			
			//close all position and orders
			
			boolean isQuoteValid = checkQuotesAreAllValid(account);
			
			if(!isQuoteValid){
				
				log.info("Account reach Freeze Stop loss, but quote is not valid ");
				
				return false;
				
			}
			
			closeAllPositoinAndOrder(account,OrderReason.StopLoss);

			
			//set account state to FROZEN
			
			account.setState(State.FROZEN);
			sendUpdateAccountEvent(account);
			
			isFrozen = true;
			
		}
		return isFrozen;
		
	}
	private boolean hasOpenPositionsOrOrders(Account account){
		boolean yetHave = false;
		
		List <OpenPosition> positionList = positionKeeper.getOverallPosition(account);
		
		List <ParentOrder> orderList = positionKeeper.getParentOrders(account.getId());
		
		if( (null != positionList && positionList.size()>0)
				|| (null != orderList && orderList.size()>0) ){
			yetHave = true;
		}
		
		
		return yetHave;
	}
	
	public boolean checkTerminateLoss(Account account,AccountSetting accountSetting){

		if(!startLiveTrading || !liveTradingSetting.isNeedCheckTerminate()){
			log.info("no need to check Terminate");
			return false;
		}
		
		if(PriceUtils.isZero(accountSetting.getTerminatePercent())){
			log.info("Terminate percent is 0");
			return false;
		}
		
		if(State.TERMINATED == account.getState() ){
			
			if(hasOpenPositionsOrOrders(account)){
				
				closeAllPositoinAndOrder(account,OrderReason.StopLoss);
				
			}
			
			return true;
			
		}
		
		
		
		boolean isTerminate =false;

		Double fdtTerminateLimit=Default.getAccountCash()-(Default.getAccountCash() * accountSetting.getTerminatePercent());
	
		Double nowCash = account.getValue();//ACCOUNT VALUE


		if(PriceUtils.EqualLessThan(nowCash, fdtTerminateLimit)){
			
			log.info("account:"+account.getId()+" state:"+account.getState());
			log.info("fdtTerminateLimit:"+fdtTerminateLimit);
			log.info("nowCash:"+nowCash);
			
			//close all position and orders
			
			boolean isQuoteValid = checkQuotesAreAllValid(account);
			
			if(!isQuoteValid){
				
				log.info("Account reached Terminate Stop loss value, but quote is not valid. ");
				
				return false;
				
			}
			
			closeAllPositoinAndOrder(account,OrderReason.StopLoss);

			log.info("before state:"+account.getState());
			//set account state to TERMINATE
			account.setState(State.TERMINATED);
			sendUpdateAccountEvent(account);
			log.info("after state:"+account.getState());
			
			isTerminate = true;
		}
			
		return isTerminate;
	}


	public LiveTradingState getStatus() {
		return status;
	}


	private void setStatus(LiveTradingState status) {
		this.status = status;
	}


	public boolean isStartLiveTrading() {
		return startLiveTrading;
	}


	public void setStartLiveTrading(boolean startLiveTrading) {
		this.startLiveTrading = startLiveTrading;
	}




}
