package com.cyanspring.server.account;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
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
import com.cyanspring.common.util.IdGenerator;
import com.google.common.base.Strings;

public class CoinManager implements IPlugin{

	private static final Logger log = LoggerFactory
			.getLogger(CoinManager.class);
	private static final String ID = "CoinMgr-"+IdGenerator.getInstance().getNextID();
	private static final String SENDER = CoinManager.class.getSimpleName();
	private ConcurrentHashMap<String, CoinControl> accountCoinControlMap = new ConcurrentHashMap<String, CoinControl>();	

	@Autowired
	protected IRemoteEventManager eventManager;
	
	@Autowired
    AccountKeeper accountKeeper;
	
	private boolean activePositionCoinControl = true;
	private boolean activeDailyCoinControl = true;
	private boolean activeTrailingStopCoinControl = true;
	
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
	
	@Override
	public void init() throws Exception {
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null){
			eventProcessor.getThread().setName("CoinManager");
		}
	}

	@Override
	public void uninit() {
		eventProcessor.uninit();
	}
	
	public void processCoinSettingRequestEvent(CoinSettingRequestEvent event){
		String accountId = event.getAccountId();	
		log.info("Receive Coin Setting :{}",accountId);
		CoinType coinType = event.getCoinType();
		boolean isOk = false;
		String message = "";
		Date now = new Date();	
		Date endDate = event.getEndDate();

		CoinControl coin = getCoinControlOrDefault(accountId);			
	
		check:{
			
			if(!accountKeeper.accountExists(accountId)){
				message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, "Account not exist!");
				break check;
			}
			
			if(null == endDate){
				message = MessageLookup.buildEventMessage(ErrorMessage.COIN_END_DATE_NOT_SETTING, "Coin end date not setting!");
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
			}	
		}
		
		if(Strings.isNullOrEmpty(message)){
			isOk = true;
			coin.setAccountId(accountId);
			PmUpdateCoinControlEvent pmEvent = new PmUpdateCoinControlEvent(event.getKey(),event.getReceiver(),coin);
			eventManager.sendEvent(pmEvent);
			accountCoinControlMap.put(accountId, coin);
			log.info("Coin Setting success:{},{},{}",new Object[]{accountId,coinType,endDate});
		}

		CoinSettingReplyEvent reply = new CoinSettingReplyEvent(event.getKey(),event.getSender(),event.getTxId(),isOk,message);
		try {
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
		}
	}
	
	public boolean canCheckPositionStopLoss(String accountId){	
		if(!activePositionCoinControl)
			return false;
		
		CoinControl coin = getCoinControlOrDefault(accountId);			
		return coin.canCheckPositionStopLoss();
	}

	public boolean canCheckDailyStopLoss(String accountId){	
		if(!activeDailyCoinControl)
			return false;
		
		CoinControl coin = getCoinControlOrDefault(accountId);			
		return coin.canCheckDailyStopLoss();
	}
	
	public boolean canCheckTrailingStop(String accountId){	
		if(!activeTrailingStopCoinControl)
			return false;
		
		CoinControl coin = getCoinControlOrDefault(accountId);			
		return coin.canCheckTrailingStop();
	}
	
	private CoinControl getCoinControlOrDefault(String accountId){
	
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
}
