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
	RWOrderRecordList,
	
	// RW
	RWCurrentPositionList,
	RWTradeRecordList,
	RWInstrumentStatistics,
	RWIndividualStatistics,
	RWUserStatistics,
	
	// BW
	
	//Admin	
	
	//常用事件
	LoginFinished,
	LoginFailed,
	Default;

}
