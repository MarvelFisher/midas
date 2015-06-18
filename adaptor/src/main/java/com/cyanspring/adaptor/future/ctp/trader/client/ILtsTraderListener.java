package com.cyanspring.adaptor.future.ctp.trader.client;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspInfoField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradeField;

public interface ILtsTraderListener {
	
	/**
	 * 1. 连接前置服务器
	 * 2. 登录成功
	 * 3. 查询投资者结算结果
	 * @param bIsLast
	 */
	public void onConnectReady(boolean isReady) ;
	
	public void onQryPositionReady(boolean isReady);
	
	/**
	 * 错误应答
	 * @param pRspInfo
	 * @param nRequestID
	 * @param bIsLast
	 */
	public void onError(String orderId, String msg);
	
	/**
	 * 报单通知
	 * @param order
	 */
	public void onOrder(CThostFtdcOrderField order) ;
	
	/**
	 * 成交通知
	 * @param trade
	 */
	public void onTrade(CThostFtdcTradeField trade) ;
	
	/**
	 * 撤单通知
	 * @param field
	 */
	public void onCancel(CThostFtdcInputOrderActionField field);
	
}
