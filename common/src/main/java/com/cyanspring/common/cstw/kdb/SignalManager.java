package com.cyanspring.common.cstw.kdb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.cstw.kdb.bean.SignalBean;
import com.cyanspring.common.cstw.kdb.bean.SignalScaleBean;
import com.cyanspring.common.util.PriceUtils;

public class SignalManager {
	
	private static final Logger log = LoggerFactory.getLogger(SignalManager.class);
	
	@Autowired
	List<SignalBean>symbolSignalList;
	
	@Autowired
	private SignalBean defaultSignal;
		
	public SignalType getSignal(String symbol,double scale){
		return getSignal(symbol,scale,true);
	}
	
	public SignalType getSignal(String symbol,double scale,boolean useDefault){
		
		if(!StringUtils.hasText(symbol) || null == symbolSignalList)
			return null;
		
		for(SignalBean bean:symbolSignalList){
			if(bean.getSymbol().equals(symbol)){
				return checkSignalType(bean,scale);
			}
		}
		
		if(useDefault && null != defaultSignal){
			return checkSignalType(defaultSignal,scale);
		}
		
		return null;
	}
	
	private SignalType checkSignalType(SignalBean signal,double scale){
		if(null == signal || null == signal.getScaleList() )
			return null;
		
		List<SignalScaleBean> list = signal.getScaleList();
		for(SignalScaleBean bean : list){
			Double from = bean.getFrom();
			Double to = bean.getTo();
			if(null != from && null != to ){	
				if( (PriceUtils.EqualGreaterThan(scale, from) && PriceUtils.LessThan(scale, to)))
					return bean.getSignal();	
			}else if(null != from){
				if(PriceUtils.EqualGreaterThan(scale, from))
					return bean.getSignal();
			}else if(null != to ){
				if(PriceUtils.LessThan(scale, to))
					return bean.getSignal();
			}
		}
				
		return null;
	}

}
