package com.cyanspring.server.account;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.Default;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.business.CoinControl;
import com.cyanspring.common.business.CoinType;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.CoinSettingReplyEvent;
import com.cyanspring.common.event.account.CoinSettingRequestEvent;
import com.cyanspring.common.event.account.PmUpdateCoinControlEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.google.common.base.Strings;

public class CoinManager implements IPlugin{

	private static final Logger log = LoggerFactory.getLogger(CoinManager.class);
	private ConcurrentHashMap<String, CoinControl> accountCoinControlMap = new ConcurrentHashMap<String, CoinControl>();	
	private boolean activePositionCoinControl = true;
	private boolean activeDailyCoinControl = true;
	private boolean activeTrailingStopCoinControl = true;
	private boolean activeDayTradingModeCoinControl = true;
	
	@Autowired
	protected IRemoteEventManager eventManager;

	@Autowired
	@Qualifier("globalEventManager")
	private IRemoteEventManager globalEventManager;

	@Autowired
    AccountKeeper accountKeeper;
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {		
			subscribeToEvent(CoinSettingRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};


	private AsyncEventProcessor globalEventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(CoinSettingRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return globalEventManager;
		}
	};


	@Override
	public void init() throws Exception {
		log.info("init coinControl");
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null){
			eventProcessor.getThread().setName("CoinManager");
		}

		globalEventProcessor.setHandler(this);
		globalEventProcessor.init();
		if(globalEventProcessor.getThread() != null)
			globalEventProcessor.getThread().setName("CoinManager");

	}

	@Override
	public void uninit() {
		eventProcessor.uninit();
	}
	
	public Account getAccountFromUserId(String userId){
		List <Account> accountList = accountKeeper.getAccounts(userId);
		if(null == accountList || accountList.size() <= 0 ){
			return null;
		}
		
		return accountList.get(0);
	}
	
	public void processCoinSettingRequestEvent(CoinSettingRequestEvent event){

		// from global MQ, but not for this LTS
		if (!Strings.isNullOrEmpty(event.getMarket()) && !Default.getMarket().equals(event.getMarket())) {
			return;
		}
		
		String userId = event.getUserId();	
		log.info("Receive Coin Setting :{}",userId);
		CoinType coinType = event.getCoinType();
		boolean isOk = false;
		String message = "";
		Date now = new Date();	
		Date endDate = event.getEndDate();
		String accountId = "";
		CoinControl coin = null;
			
		check:{
			
			if(Strings.isNullOrEmpty(userId)){
				message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, "Account not exist!");
				break check;
			}
			
			Account account = getAccountFromUserId(userId);
			if(null == account){

				if (!Strings.isNullOrEmpty(event.getMarket())) {
					return; // from global MQ, but not for this LTS, don't send reply event
				}

				message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, "Account not exist!");
				break check;
			}
			
			accountId = account.getId();
			
			if(null == endDate){
				message = MessageLookup.buildEventMessage(ErrorMessage.COIN_END_DATE_NOT_SETTING, "Coin end date not setting!");
				break check;
			}			

			coin = getCoinControlOrDefault(accountId);	
			
			if(null == coin){
				message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, "get coin control error!");
				break check;
			}
			
			if(null == coinType){
				message = MessageLookup.buildEventMessage(ErrorMessage.COIN_TYPE_NOT_FOUND, "Coin type not found!");
				break check;
			}
			
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(endDate);
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)
					, calendar.getMaximum(Calendar.HOUR_OF_DAY), calendar.getMaximum(Calendar.MINUTE), calendar.getMaximum(Calendar.SECOND));		
			endDate = calendar.getTime();
			
			switch(coinType){
			
			case POSITION_STOP_LOSS:
				coin.setCheckPositionStopLossStart(now);
				coin.setCheckPositionStopLossEnd(endDate);
				break;
			case DAILY_STOP_LOSS:
				coin.setCheckDailyStopLossStart(now);
				coin.setCheckDailyStopLossEnd(endDate);
				break;
			case TRAILING_STOP:
				coin.setCheckTrailingStopStart(now);
				coin.setCheckTrailingStopEnd(endDate);
				break;
			case DAY_TRADING_MODE:
				coin.setCheckDayTradingModeStart(now);
				coin.setCheckDayTradingModeEnd(endDate);
				break;
			}	
		}
		
		if(Strings.isNullOrEmpty(message)){
			isOk = true;
			coin.setAccountId(accountId);
//			coin.setModifyTime(new Date());
			PmUpdateCoinControlEvent pmEvent = new PmUpdateCoinControlEvent(event.getKey(),event.getReceiver(),coin);
			eventManager.sendEvent(pmEvent);
			accountCoinControlMap.put(accountId, coin);
			log.info("Coin Setting success:{},{},{}",new Object[]{accountId,coinType,endDate});
		}

		CoinSettingReplyEvent reply = new CoinSettingReplyEvent(
				event.getKey(), event.getSender(), event.getTxId(), event.getUserId(), event.getClientId(),
				event.getCoinType(), event.getMarket(), isOk, message);
		try {
			if (!Strings.isNullOrEmpty(event.getMarket())) {
				globalEventManager.sendRemoteEvent(reply);
			} else {
				eventManager.sendRemoteEvent(reply);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
		}
	}
	
	public boolean canCheckPositionStopLoss(String accountId){	
		if(!activePositionCoinControl)
			return true;
		
		CoinControl coin = getCoinControlOrDefault(accountId);	
		if(null == coin)
			return false;
		
		return coin.canCheckPositionStopLoss();
	}

	public boolean canCheckDailyStopLoss(String accountId){	
		if(!activeDailyCoinControl)
			return true;
		
		CoinControl coin = getCoinControlOrDefault(accountId);	
		if(null == coin)
			return false;
		
		return coin.canCheckDailyStopLoss();
	}
	
	public boolean canCheckTrailingStop(String accountId){	
		if(!activeTrailingStopCoinControl)
			return true;
		
		CoinControl coin = getCoinControlOrDefault(accountId);			
		if(null == coin)
			return false;
		
		return coin.canCheckTrailingStop();
	}
	
	public boolean canCheckDayTradingMode(String accountId){	
		if(!activeDayTradingModeCoinControl)
			return true;
		
		CoinControl coin = getCoinControlOrDefault(accountId);		
		if(null == coin)
			return false;
		
		return coin.canCheckDayTradingMode();
	}
	
	private CoinControl getCoinControlOrDefault(String accountId){
	
		if(Strings.isNullOrEmpty(accountId))
			return null;
		
		CoinControl coin = accountCoinControlMap.get(accountId);		
		if(null == coin){
			coin = CoinControl.createDefaultCoinControl(accountId);
		}	
		return coin;
	}
	
	public CoinControl getCoinControl(String accountId){		
		CoinControl coin = getCoinControlOrDefault(accountId);			
		return coin;
	}
	
	public void injectCoinControls(List<CoinControl> coinControlList){		
		for(CoinControl coinControl :coinControlList){
			accountCoinControlMap.put(coinControl.getAccountId(), coinControl);
		}
	}

	public boolean isActivePositionCoinControl() {
		return activePositionCoinControl;
	}

	public void setActivePositionCoinControl(boolean activePositionCoinControl) {
		this.activePositionCoinControl = activePositionCoinControl;
	}

	public boolean isActiveDailyCoinControl() {
		return activeDailyCoinControl;
	}

	public void setActiveDailyCoinControl(boolean activeDailyCoinControl) {
		this.activeDailyCoinControl = activeDailyCoinControl;
	}

	public boolean isActiveTrailingStopCoinControl() {
		return activeTrailingStopCoinControl;
	}

	public void setActiveTrailingStopCoinControl(
			boolean activeTrailingStopCoinControl) {
		this.activeTrailingStopCoinControl = activeTrailingStopCoinControl;
	}

	public boolean isActiveDayTradingModeCoinControl() {
		return activeDayTradingModeCoinControl;
	}

	public void setActiveDayTradingModeCoinControl(
			boolean activeDayTradingModeCoinControl) {
		this.activeDayTradingModeCoinControl = activeDayTradingModeCoinControl;
	}
}
