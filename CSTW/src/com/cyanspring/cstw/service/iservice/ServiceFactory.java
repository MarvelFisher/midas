package com.cyanspring.cstw.service.iservice;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.cstw.service.common.ExportCsvServiceImpl;
import com.cyanspring.cstw.service.impl.riskmgr.CurrentPositionServiceImpl;
import com.cyanspring.cstw.service.impl.riskmgr.InstrumentStatisticsServiceImpl;
import com.cyanspring.cstw.service.impl.riskmgr.InstrumentSummaryServiceImpl;
import com.cyanspring.cstw.service.impl.riskmgr.OrderRecordServiceImpl;
import com.cyanspring.cstw.service.impl.riskmgr.TradeRecordServiceImpl;
import com.cyanspring.cstw.service.impl.riskmgr.UserStatisticsServiceImpl;
import com.cyanspring.cstw.service.iservice.riskmgr.ICurrentPositionService;
import com.cyanspring.cstw.service.iservice.riskmgr.IInstrumentStatisticsService;
import com.cyanspring.cstw.service.iservice.riskmgr.IInstrumentSummaryService;
import com.cyanspring.cstw.service.iservice.riskmgr.IOrderRecordService;
import com.cyanspring.cstw.service.iservice.riskmgr.ITradeRecordService;
import com.cyanspring.cstw.service.iservice.riskmgr.IUserStatisticsService;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/19
 *
 */
public final class ServiceFactory {

	// RiskManagement Service
	
	public static ICurrentPositionService createRWCurrentPositionService() {
		CurrentPositionServiceImpl instance = new CurrentPositionServiceImpl();
		instance.setRoleType(UserRole.RiskManager);
		return instance;
	}
	
	public static ICurrentPositionService createBWCurrentPositionService() {
		CurrentPositionServiceImpl instance = new CurrentPositionServiceImpl();
		instance.setRoleType(UserRole.BackEndRiskManager);
		return instance;
	}
	
	public static ITradeRecordService createTradeRecordService() {
		TradeRecordServiceImpl instance = new TradeRecordServiceImpl();
		return instance;
	}
	
	public static IInstrumentStatisticsService createInstrumentStatisticsService() {
		InstrumentStatisticsServiceImpl instance = new InstrumentStatisticsServiceImpl();
		return instance;
	}
	
	public static IInstrumentSummaryService createInstrumentSummaryService() {
		InstrumentSummaryServiceImpl instance = new InstrumentSummaryServiceImpl();
		return instance;
	}
	
	public static IUserStatisticsService createUserStatisticsService() {
		UserStatisticsServiceImpl instance = new UserStatisticsServiceImpl();
		return instance;
	}
	
	public static IOrderRecordService createOrderRecordService() {
		OrderRecordServiceImpl instance = new OrderRecordServiceImpl();
		return instance;
	}
	
	public static IExportCsvService createExportCsvService() {
		ExportCsvServiceImpl instance = new ExportCsvServiceImpl();
		return instance;
	}

}
