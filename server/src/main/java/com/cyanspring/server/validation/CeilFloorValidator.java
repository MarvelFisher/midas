package com.cyanspring.server.validation;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.marketdata.MultiQuoteExtendEvent;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketdata.QuoteExtSubEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.common.event.AsyncEventProcessor;
/**
 * 
 * @author Jimmy
 * 
 * @Desc : prevent order price over than ceil or lower than floor price
 * 
 */
public class CeilFloorValidator implements IOrderValidator,IPlugin{
	private static final Logger log = LoggerFactory
			.getLogger(CeilFloorValidator.class);
	
	private static final String ID="CFValidator-"+IdGenerator.getInstance().getNextID();
	private static final String SENDER=CeilFloorValidator.class.getSimpleName();
	
	private ConcurrentHashMap<String, DataObject> quoteExtendsMap = new ConcurrentHashMap <String, DataObject>();
	private Date tradeDate = null;
	private String tradeDateFormat="yyyy-MM-dd";
	
	@Autowired
	protected IRemoteEventManager eventManager;
	
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {		
			/**
			 * @Purpose : Building QuoteExt Map from MarketDataManager
			 * 			  to validate order ceil and floor price.
			 */
			subscribeToEvent(QuoteExtEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);
			subscribeToEvent(TradeDateEvent.class,null);
			subscribeToEvent(MultiQuoteExtendEvent.class, null);

		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	
	
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {		
		
		if(quoteExtendsMap.size()==0){					
			log.warn("validation : No quoteExtends info");
			return;		
		}
		
		log.info("quoteExtendsMap size:"+quoteExtendsMap.size());

		try{
			
			String symbol;
			Double price;
			DataObject data = null;
			OrderType type = null;
			
			if(order == null){				
				symbol = (String)map.get(OrderField.SYMBOL.value());
				type =(OrderType) map.get(OrderField.TYPE.value());		
			}
			else{			
				symbol = order.getSymbol();
				type = order.getOrderType();	
			}
			
			if(OrderType.Market == type){			
				log.warn("this is market order");
				return;			
			}
				
			if(null!=quoteExtendsMap && quoteExtendsMap.size()!=0){
				data = quoteExtendsMap.get(symbol);
			}
			 
			if(null == data){
				log.warn("validation : QuoteExtend symbol not exist - "+symbol);
			}else{
				
				price = (Double) map.get(OrderField.PRICE.value());
				if(null == price){					
					return;						
				}
								
				Double ceil = data.get(Double.class, QuoteExtDataField.CEIL.value());
				Double floor = data.get(Double.class, QuoteExtDataField.FLOOR.value());				
	
				if(PriceUtils.GreaterThan(price, ceil)){
					throw new OrderValidationException("Order price over than ceil price:"+ceil,ErrorMessage.ORDER_OVER_CEIL_PRICE);
				}
				if(PriceUtils.LessThan(price,floor)){
					throw new OrderValidationException("Order price lower than floor price:"+floor,ErrorMessage.ORDER_LOWER_FLOOR_PRICE);
				}
				
			}
		
		}catch(OrderValidationException e){	
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
			
	}
	
	
	@Override
	public void init() throws Exception {

		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null){
			eventProcessor.getThread().setName("CeilFloorValidator");
		}
		
		requestMarketSession();

	}
	@Override
	public void uninit() {
		quoteExtendsMap = null;
		eventProcessor.uninit();
		eventManager.uninit();
	}
	public void processMultiQuoteExtendEvent(MultiQuoteExtendEvent event){

		Map <String,DataObject> receiveDataMap = event.getMutilQuoteExtend();
		int offset = event.getOffSet();
		int total = event.getTotalDataCount();
		Date eventTradeDate = event.getTradeDate();

		if(null == receiveDataMap || 0 == receiveDataMap.size()  ){			
			log.warn(" MultiQuoteExtendEvent reply doesn't contains any data ");
			return;	
		}else{
			log.info("receiveData size:"+receiveDataMap.size());
		}
		
		if(null == quoteExtendsMap ){
			quoteExtendsMap = new <String, DataObject>ConcurrentHashMap();
		}
		
		quoteExtendsMap.putAll(receiveDataMap);
		
	}
	public void sendQuoteExtSubEvent(){
		
		quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
		QuoteExtSubEvent event = new QuoteExtSubEvent(CeilFloorValidator.ID, CeilFloorValidator.SENDER);
		log.info("send QuoteExtSub event");
		eventManager.sendEvent(event);
		
	}
	

	public void processQuoteExtEvent(QuoteExtEvent event){
		//if quoteExt changed ,quoteExtMap needs to renew 
		try{
			
			DataObject updateObj = event.getQuoteExt();
			String symbol = updateObj.get(String.class, QuoteExtDataField.SYMBOL.value());
			if(null == quoteExtendsMap){				
				quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
			}
			if(null != symbol){
				quoteExtendsMap.put(symbol, updateObj);
			}

		}catch(Exception e){
			log.warn(e.getMessage(),e);
		}
		
	}

	public void processTradeDateEvent(TradeDateEvent event){
		log.info("into trade date event");
		try{
			
			String eventTradeDate = event.getTradeDate();
			if(null == eventTradeDate){
				return;
			}
			if(null == tradeDate
					|| !isSameTradeDate(eventTradeDate))
			{		
				setTradeDate(eventTradeDate);
				sendQuoteExtSubEvent();
			}

		}catch(Exception e){
			log.warn(e.getMessage(),e);
		}

	}
	
	public void processMarketSessionEvent(MarketSessionEvent event){
		//ceil and floor price need to be renew on preopen
		
		try{
			
			Date oldTradeDate = tradeDate;
			String td = event.getTradeDate();
			
			if( null == oldTradeDate
					|| MarketSessionType.PREOPEN == event.getSession())
			{// if oldTradeDate ==  null means reboot		
				setTradeDate(td);
				sendQuoteExtSubEvent();
			}
			
		} catch (ParseException e) {
			
			log.warn("Trade date parse error:"+event.getTradeDate(),e);
			
		}catch(Exception e){
			
			log.warn(e.getMessage(),e);
			
		}
	}
	public void requestMarketSession() {
		eventManager.sendEvent(new MarketSessionRequestEvent(CeilFloorValidator.ID, CeilFloorValidator.SENDER, true));
	}


	public ConcurrentHashMap<String, DataObject> getQuoteExtendsMap() {
		return quoteExtendsMap;
	}


	public void setQuoteExtendsMap(
			ConcurrentHashMap<String, DataObject> quoteExtendsMap) {
		this.quoteExtendsMap = quoteExtendsMap;
	}
	
	private boolean isSameTradeDate(String date)throws ParseException{
		
		Date dateC = TimeUtil.parseDate(date, tradeDateFormat);
		return TimeUtil.sameDate(tradeDate, dateC);
		
	}
	private boolean isSameTradeDate(Date date){
		return TimeUtil.sameDate(tradeDate, date);
	}

	
	private void setTradeDate(String td) throws ParseException{

		if(null != td && !"".equals(td)){
			tradeDate = TimeUtil.parseDate(td, tradeDateFormat);
		}
		
	}
	
}
