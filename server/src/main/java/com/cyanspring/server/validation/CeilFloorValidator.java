package com.cyanspring.server.validation;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.event.AsyncEventProcessor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
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
	
	private ConcurrentHashMap<String, DataObject> quoteExtendsMap = null;
	private String quoteExtendFile;
	@Autowired
	protected IRemoteEventManager eventManager;
	
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {			
			subscribeToEvent(QuoteExtEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);	
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	
	
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {		
		//when order incoming, check the quote ext file whether created. 
		if(quoteExtendsMap==null && !buildQuoteExtFile()){	
			log.warn("validation : No quoteExtends info");
			return;
		}
		
		try{
			
			String symbol;
			Double price;
			DataObject data=null;

			if(order == null){
				symbol = (String)map.get(OrderField.SYMBOL.value());
			}
			else{
				symbol = order.getSymbol();
			}
			
			if(null!=quoteExtendsMap){
				data = quoteExtendsMap.get(symbol);
			}
			 
			if(null==data){
				log.warn("validation : QuoteExtend symbol not exist - "+symbol);
			}else{
				
				price =(Double) map.get(OrderField.PRICE.value());
				Double ceil = data.get(Double.class, QuoteExtDataField.CEIL.value());
				Double floor = data.get(Double.class, QuoteExtDataField.FLOOR.value());				
				if(ceil<price ){
					throw new OrderValidationException("Order price over than ceil price:"+ceil,ErrorMessage.ORDER_OVER_CEIL_PRICE);
				}
				if(floor>price){
					throw new OrderValidationException("Order price lower than floor price:"+floor,ErrorMessage.ORDER_LOWER_FLOOR_PRICE);
				}
				
			}
		
		}catch(OrderValidationException e){	
			throw new OrderValidationException(e.getMessage(),e.getClientMessage());
		}catch(Exception e){
			throw new OrderValidationException(e.getMessage(),ErrorMessage.VALIDATION_ERROR);
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
		
		buildQuoteExtFile();
		
		
	}
	@Override
	public void uninit() {
		quoteExtendsMap = null;
		eventProcessor.uninit();
		eventManager.uninit();
	}
	public void processQuoteExtEvent(QuoteExtEvent event){
		//if quoteExt changed ,quoteExtMap needs to renew 
		try{
			
			DataObject updateObj = event.getQuoteExt();
			String symbol = updateObj.get(String.class, QuoteExtDataField.SYMBOL.value());
			if(null!=quoteExtendsMap){
				quoteExtendsMap.put(symbol, updateObj);
			}

		}catch(Exception e){
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public void processMarketSessionEvent(MarketSessionEvent event){
		//ceil and floor price need to be renew on preopen
		if (MarketSessionType.PREOPEN == event.getSession()) {
			quoteExtendsMap = null;			
		}else if(MarketSessionType.OPEN == event.getSession() && quoteExtendsMap==null){
			buildQuoteExtFile();
		}
	}
	public void requestMarketSession() {
		eventManager.sendEvent(new MarketSessionRequestEvent(null, null, true));
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean buildQuoteExtFile() {
		boolean isMapCreated = false;
		try{
			
			
			XStream xstream = new XStream(new DomDriver());
			File file = new File(getQuoteExtendFile());
			Map<String,DataObject> xmlMap;
			if (file.exists()) {
				xmlMap = (Map)xstream.fromXML(file);
				if(xmlMap.size()==0){
					log.warn("Quote Extends file(lastExtend.xml) has no data");					
					return false;
				}
				quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
				quoteExtendsMap.putAll(xmlMap);

				isMapCreated = true;
			} else {
				log.warn("Missing lastExtend(QuoteExt) file: " + getQuoteExtendFile());
			}
				
		}catch(Exception e){
			e.printStackTrace();
			log.warn("validation : read QuoteExtend file fail:"+e.getMessage());		
		}
		return isMapCreated;
	
	}
	
	
	public String getQuoteExtendFile() {
		return quoteExtendFile;
	}
	public void setQuoteExtendFile(String quoteExtendFile) {
		this.quoteExtendFile = quoteExtendFile;
	}
	
	

}
