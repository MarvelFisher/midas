package com.cyanspring.cstw.service.common;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/22
 * moved from Project S
 */
public enum RefreshEventType {
	
	
	//Trader
	
	// Common RiskCtrl
	InstrumentSummary4RC,	
	CapitalInfo4RC,
	AccountStatistics4RC,
	OrderRecordList4RC,
	
	// Front RiskCtrl
	CurrentPositionList4FrontRC,
	TradeRecordList4FrontRC,
	InstrumentStatistics4FrontRC,
	IndividualStatistics4FrontRC,
	
	//Admin	
	
	//常用事件
	LoginFinished,
	LoginFailed,
	Default;

}
