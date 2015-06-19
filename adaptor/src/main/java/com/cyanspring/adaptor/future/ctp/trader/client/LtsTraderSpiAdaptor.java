package com.cyanspring.adaptor.future.ctp.trader.client;

import java.util.ArrayList;
import java.util.List;

import org.bridj.Pointer;
import org.bridj.ann.Virtual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcAccountregisterField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcBrokerTradingAlgosField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcBrokerTradingParamsField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcCFMMCTradingAccountKeyField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcCFMMCTradingAccountTokenField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcCancelAccountField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcChangeAccountField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcCombActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcCombInstrumentGuardField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcContractBankField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcDepthMarketDataField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcEWarrantOffsetField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcErrorConditionalOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcExchangeField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcExchangeMarginRateAdjustField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcExchangeMarginRateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcExchangeRateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcExecOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcExecOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcForQuoteField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcForQuoteRspField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputCombActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputExecOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputExecOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputForQuoteField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputQuoteActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputQuoteField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInstrumentCommissionRateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInstrumentField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInstrumentMarginRateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInstrumentStatusField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInvestorField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInvestorPositionCombineDetailField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInvestorPositionDetailField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInvestorPositionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInvestorProductGroupMarginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcNoticeField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcNotifyQueryAccountField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOpenAccountField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOptionInstrCommRateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOptionInstrTradeCostField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcParkedOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcParkedOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcProductExchRateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcProductField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcQueryCFMMCTradingAccountTokenField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcQueryMaxOrderVolumeField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcQuoteActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcQuoteField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRemoveParkedOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRemoveParkedOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcReqQueryAccountField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcReqRepealField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcReqTransferField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspAuthenticateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspInfoField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspRepealField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspTransferField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspUserLoginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcSecAgentACIDMapField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcSettlementInfoConfirmField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcSettlementInfoField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradeField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTraderSpi;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradingAccountField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradingAccountPasswordUpdateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradingCodeField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradingNoticeField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradingNoticeInfoField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTransferBankField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTransferSerialField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcUserLogoutField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcUserPasswordUpdateField;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary;

import static com.cyanspring.adaptor.future.ctp.trader.client.TraderHelper.*;

/**
 * @author Marvel
 *
 */
public class LtsTraderSpiAdaptor extends CThostFtdcTraderSpi {
	
	private final static Logger log = LoggerFactory.getLogger(LtsTraderSpiAdaptor.class);
	
	private CtpTraderProxy proxy;
	
	private List<ILtsTraderListener> tradelistens = new ArrayList<ILtsTraderListener>();
	private List<ILtsLoginListener> loginListens = new ArrayList<ILtsLoginListener>();
	private List<ILtsQryOrderListener> qryOrdListens = new ArrayList<ILtsQryOrderListener>();
	
	public void setCtpTraderProxy(CtpTraderProxy pro) {
		this.proxy = pro;
	}
	
	public void addTraderListener( ILtsTraderListener listener ) {
		if ( listener == null ) {
			return;
		}
		if ( !tradelistens.contains(listener) ) {
			tradelistens.add(listener);
		}
	}
	
	public void removeTraderListerner( ILtsTraderListener listener ) {
		if ( listener == null ) {
			return;
		}
		if ( tradelistens.contains(listener) ) {
			tradelistens.remove(listener);
		}
	}
	
	public void addLoginListener( ILtsLoginListener listener ) {
		if ( listener == null ) {
			return;
		}
		if ( !loginListens.contains(listener) ) {
			loginListens.add(listener);
		}
	}
	
	public void removeLoginListener( ILtsLoginListener listener ) {
		if ( listener == null ) {
			return;
		}
		if ( loginListens.contains(listener) ) {
			loginListens.remove(listener);
		}
	}
	
	public void addQryOrdListener( ILtsQryOrderListener listener ) {
		if ( listener == null ) {
			return;
		}
		if ( !qryOrdListens.contains(listener) ) {
			qryOrdListens.add(listener);
		}
	}
	
	public void removeQryOrdListener( ILtsQryOrderListener listener ) {
		if ( listener == null ) {
			return;
		}
		if ( qryOrdListens.contains(listener) ) {
			qryOrdListens.remove(listener);
		}
	}
	
	/**
	 * 当客户端与交易后台建立起通信连接时（还未登录前），该方法被调用。
	 */	
	@Virtual(0) 
	public void OnFrontConnected() {
		log.info("Network connected");
		proxy.doLogin();
	}
	
	/**
	 * 	当客户端与交易后台通信连接断开时，该方法被调用。当发生这个情况后，API会自动重新连接，客户端可不做处理。
	 *	//@param nReason 错误原因
	 *	        0x1001 网络读失败
	 *	        0x1002 网络写失败
	 *	        0x2001 接收心跳超时
	 *	        0x2002 发送心跳失败
	 *	        0x2003 收到错误报文
	 */
	@Virtual(1) 
	public  void OnFrontDisconnected(int nReason) {
		log.info("Network dissconnected" + nReason);
	}
	
	/**
	 * 心跳超时警告。当长时间未收到报文时，该方法被调用。
	 * @param nTimeLapse 距离上次接收报文的时间
	 */
	@Virtual(2) 
	public  void OnHeartBeatWarning(int nTimeLapse) {
		log.warn("Heart Beat:" + nTimeLapse);
	}
	
	/**
	 * 客户端认证响应
	 */
	@Virtual(3) 
	public  void OnRspAuthenticate(Pointer<CThostFtdcRspAuthenticateField > pRspAuthenticateField, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("Response Authenticate: " );
		
		
	}

	/**
	 * 登录请求响应
	 */
	@Virtual(4) 
	public  void OnRspUserLogin(Pointer<CThostFtdcRspUserLoginField > pRspUserLogin, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("Response UserLogin: ");
		CThostFtdcRspInfoField rsp = getStructObject(pRspInfo);
		if ( rsp.ErrorID() == 0 ) {
			for( ILtsLoginListener lis : loginListens ) {
				lis.onLogin(getStructObject(pRspUserLogin));
			}
			proxy.doReqSettlementInfoConfirm();
		} else {
			log.info("Login Fail: " + getStructObject(pRspInfo).ErrorMsg().getCString());
		}	
		
	}

	/**
	 * 登出请求响应
	 */
	@Virtual(5) 
	public  void OnRspUserLogout(Pointer<CThostFtdcUserLogoutField > pUserLogout, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response UserLogout Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {			
			//TODO
		}
		
		if ( pUserLogout == null ) {
			log.error("Response UserLogout Pointer<CThostFtdcUserLogoutField > pUserLogout = null");
		} else {
			log.info("Response UserLogout:");
			//TODO
		}		
		
	}

	/**
	 * 用户口令更新请求响应
	 */
	@Virtual(6) 
	public  void OnRspUserPasswordUpdate(Pointer<CThostFtdcUserPasswordUpdateField > pUserPasswordUpdate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response UserPasswordUpdate Pointer<CThostFtdcRspInfoField > pRspInfo == null");
		} else {
			
			// TODO
		}
		
		if ( pUserPasswordUpdate == null ) {
			log.error("Response UserPasswordUpdate Pointer<CThostFtdcUserPasswordUpdateField > p = null");
			
		} else {
			log.info("Response UserPasswordUpdate:");
			//TODO
		}	
	}
		
	 /**
	  *  资金账户口令更新请求响应
	  */
	@Virtual(7) 
	public  void OnRspTradingAccountPasswordUpdate(Pointer<CThostFtdcTradingAccountPasswordUpdateField > pTradingAccountPasswordUpdate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response TradingAccountPasswordUpdate Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
			// TODO
		}
		
		if ( pTradingAccountPasswordUpdate == null ) {
			log.error("Response TradingAccountPasswordUpdate Pointer<CThostFtdcTradingAccountPasswordUpdateField > p = null");
			return ;
		} else {
			log.info("Response TradingAccountPasswordUpdate:");
			// TODO
		}	
		
	}

	/**
	 * 报单录入请求响应
	 */
	@Virtual(8) 
	public  void OnRspOrderInsert(Pointer<CThostFtdcInputOrderField > pInputOrder, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("Response OrderInsert: " + pInputOrder);
		CThostFtdcInputOrderField rsp = getStructObject(pInputOrder);
		if ( rsp == null ) {
			return;
		}
		String orderId = rsp.OrderRef().getCString();
		CThostFtdcRspInfoField info = getStructObject(pRspInfo);
		if(null != info) {
			String msg = TraderHelper.toGBKString( getStructObject(pRspInfo).ErrorMsg().getBytes() );
			for ( ILtsTraderListener lis : tradelistens ) {
				lis.onError(orderId, "" + info.ErrorID() + " : " + msg);
			}
		}
	}

	/**
	 * 预埋单录入请求响应
	 */
	@Virtual(9) 
	public  void OnRspParkedOrderInsert(Pointer<CThostFtdcParkedOrderField > pParkedOrder, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response ParkedOrderInsert Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			//TODO
		}
		
		if ( pParkedOrder == null ) {
			log.error("Response ParkedOrderInsert Pointer<CThostFtdcParkedOrderField > pParkedOrder = null");
			
		} else {
			log.info("Response ParkedOrderInsert");
			//TODO
		}
	}
	
	/**
	 * 预埋撤单录入请求响应
	 */
	@Virtual(10) 
	public  void OnRspParkedOrderAction(Pointer<CThostFtdcParkedOrderActionField > pParkedOrderAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response ParkedOrderAction Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			// TODO
		}
		
		if ( pParkedOrderAction == null ) {
			log.error("Response ParkedOrderAction Pointer<CThostFtdcParkedOrderActionField > p = null");
			
		} else {
			log.info("Response ParkedOrderAction");
			//TODO
		}
			
	}
	
	/**
	 * 报单操作请求响应
	 */
	@Virtual(11) 
	public  void OnRspOrderAction(Pointer<CThostFtdcInputOrderActionField > pInputOrderAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("Response OrderAction:");
		CThostFtdcInputOrderActionField rsp = getStructObject(pInputOrderAction);
		CThostFtdcRspInfoField rspInfo = getStructObject(pRspInfo);
		if ( rsp.ActionFlag() == TraderLibrary.THOST_FTDC_AF_Delete ) {
			if ( rspInfo.ErrorID() == 0 ) {
				for ( ILtsTraderListener lis : tradelistens ) {
					lis.onCancel(rsp);
				}
			} else {
				String msg = TraderHelper.toGBKString( rspInfo.ErrorMsg().getBytes());
				for ( ILtsTraderListener lis : tradelistens ) {
					lis.onError(rsp.OrderRef().getCString(),"" + rspInfo.ErrorID() + " : " +  msg);
				}
			}
		}
		
	}
	
	/**
	 * 查询最大报单数量响应
	 */
	@Virtual(12) 
	public  void OnRspQueryMaxOrderVolume(Pointer<CThostFtdcQueryMaxOrderVolumeField > pQueryMaxOrderVolume, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pQueryMaxOrderVolume == null ) {
			log.error("Response Query MaxOrderVolume Pointer<CThostFtdcQueryMaxOrderVolumeField > p = null");
			
		} else {
			log.info("Response  Query MaxOrderVolume");
			// TODO
		}
		
		if ( pRspInfo == null ) {
			log.warn("Response Query MaxOrderVolume Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			// TODO
		}
		
	}
	
	/**
	 * 投资者结算结果确认响应
	 */
	@Virtual(13) 
	public  void OnRspSettlementInfoConfirm(Pointer<CThostFtdcSettlementInfoConfirmField > pSettlementInfoConfirm, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {	
		CThostFtdcRspInfoField rsp = getStructObject(pRspInfo);
		log.info("Response SettlementInfoConfirm: " + rsp.toString());
		proxy.doQryPosition();	
		proxy.cancelHistoryOrder();
	}
	
	/**
	 * 删除预埋单响应
	 */
	@Virtual(14) 
	public  void OnRspRemoveParkedOrder(Pointer<CThostFtdcRemoveParkedOrderField > pRemoveParkedOrder, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response RemoveParkedOrder Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pRemoveParkedOrder == null ) {
			log.error("Response RemoveParkedOrder Pointer<CThostFtdcRemoveParkedOrderField > p = null");
		} else {
			log.info("Response RemoveParkedOrder: ");
			// TODO
		}		
	}
	
	/**
	 * 删除预埋撤单响应
	 */
	@Virtual(15) 
	public  void OnRspRemoveParkedOrderAction(Pointer<CThostFtdcRemoveParkedOrderActionField > pRemoveParkedOrderAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response RemoveParkedOrderAction Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pRemoveParkedOrderAction == null ) {
			log.error("Response RemoveParkedOrderAction Pointer<CThostFtdcRemoveParkedOrderActionField > p");
		} else {
			log.info("Response RemoveParkedOrderAction: ");
		}		
	}
	
	/**
	 * 执行宣告录入请求响应
	 */
	@Virtual(16) 
	public  void OnRspExecOrderInsert(Pointer<CThostFtdcInputExecOrderField > pInputExecOrder, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response ExecOrderInsert Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pInputExecOrder == null ) {
			log.error("Response ExecOrderInsert Pointer<CThostFtdcInputExecOrderField > p = null");
		} else {
			log.info("Response ExecOrderInsert: ");
		}
	}
	
	/**
	 * 执行宣告操作请求响应
	 */
	@Virtual(17) 
	public  void OnRspExecOrderAction(Pointer<CThostFtdcInputExecOrderActionField > pInputExecOrderAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response ExecOrderAction Pointer<CThostFtdcRspInfoField > pRspInfo");
		} else {
			
		}
		
		if ( pInputExecOrderAction == null ) {
			log.error("Response ExecOrderAction Pointer<CThostFtdcInputExecOrderActionField > p = null");
		} else {			
			log.info("Response ExecOrderAction: ");
		}
	}
	
	/**
	 * 询价录入请求响应
	 */
	@Virtual(18) 
	public  void OnRspForQuoteInsert(Pointer<CThostFtdcInputForQuoteField > pInputForQuote, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pInputForQuote == null ) {
			log.error("Response ForQuoteInsert Pointer<CThostFtdcInputForQuoteField > p = null");
		} else {
			log.info("Response ForQuoteInsert: ");
		}
	}
	
	/**
	 * 报价录入请求响应
	 */
	@Virtual(19) 
	public  void OnRspQuoteInsert(Pointer<CThostFtdcInputQuoteField > pInputQuote, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pInputQuote == null ) {
			log.error("Response QuoteInsert Pointer<CThostFtdcInputQuoteField > p = null");
		} else {
			log.info("Response QuoteInsert:");
		}
	}
	
	/**
	 * 报价操作请求响应
	 */
	@Virtual(20) 
	public  void OnRspQuoteAction(Pointer<CThostFtdcInputQuoteActionField > pInputQuoteAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 申请组合录入请求响应
	 */
	@Virtual(21) 
	public  void OnRspCombActionInsert(Pointer<CThostFtdcInputCombActionField > pInputCombAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询报单响应
	 */
	@Virtual(22) 
	public  void OnRspQryOrder(Pointer<CThostFtdcOrderField > pOrder, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("Response QryOrder:" + pOrder);
		CThostFtdcOrderField order = getStructObject(pOrder);
		for ( ILtsQryOrderListener lis : qryOrdListens ) {
			lis.onQryOrder(order, bIsLast);
		}		
	}
	
	/**
	 * 请求查询成交响应
	 */
	@Virtual(23) 
	public  void OnRspQryTrade(Pointer<CThostFtdcTradeField > pTrade, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo == null ) {
			log.warn("Response QryTrade Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pTrade == null ) {
			log.error("Response QryTrade Pointer<CThostFtdcTradeField > pTrade = null");
		} else {
			log.info("Response QryTrade");
		}
		
	}
	
	/**
	 * 请求查询投资者持仓响应
	 */
	@Virtual(24) 
	public  void OnRspQryInvestorPosition(Pointer<CThostFtdcInvestorPositionField > pInvestorPosition, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		CThostFtdcInvestorPositionField rsp = getStructObject(pInvestorPosition);
		log.info("Response QryPosition: " + rsp);
		CThostFtdcRspInfoField rspInfo = getStructObject(pRspInfo);
		//notify tradeListeners
		for ( ILtsTraderListener lis : tradelistens ) {
			lis.onQryPosition(rsp, bIsLast);
			if ( bIsLast ) {
				lis.onConnectReady(true);
			}
		}
	}
	
	/**
	 * 请求查询资金账户响应
	 */
	@Virtual(25) 
	public  void OnRspQryTradingAccount(Pointer<CThostFtdcTradingAccountField > pTradingAccount, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response QryTradingAccount Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pTradingAccount == null ) {
			log.error("Response QryTradingAccount Pointer<CThostFtdcTradingAccountField > p = null");
		} else {
			log.info("Response QryTradingAccount: ");
		}	
	}
	
	/**
	 * 请求查询投资者响应
	 */
	@Virtual(26) 
	public  void OnRspQryInvestor(Pointer<CThostFtdcInvestorField > pInvestor, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {		
		if ( pRspInfo == null ) {
			log.warn("Response QryInvestor Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pInvestor == null ) {
			log.error("Response QryInvestor Pointer<CThostFtdcInvestorField > p = null");
		} else {
			log.info("Response QryInvestor: ");
		}		
	}
	
	/**
	 * 请求查询交易编码响应
	 */
	@Virtual(27) 
	public  void OnRspQryTradingCode(Pointer<CThostFtdcTradingCodeField > pTradingCode, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response QryTradingCode Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pTradingCode == null ) {
			log.error("Response QryTradingCode Pointer<CThostFtdcTradingCodeField > p = null");
		} else {
			log.info("Response QryTradingCode: ");
		}		
	}
	
	/**
	 * 请求查询合约保证金率响应
	 */
	@Virtual(28) 
	public  void OnRspQryInstrumentMarginRate(Pointer<CThostFtdcInstrumentMarginRateField > pInstrumentMarginRate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询合约手续费率响应
	 */
	@Virtual(29) 
	public  void OnRspQryInstrumentCommissionRate(Pointer<CThostFtdcInstrumentCommissionRateField > pInstrumentCommissionRate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询交易所响应
	 */
	@Virtual(30) 
	public  void OnRspQryExchange(Pointer<CThostFtdcExchangeField > pExchange, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.warn("Response QryExchange Pointer<CThostFtdcRspInfoField > pRspInfo = null ");
		} else {
			
		}
		
		if ( pExchange == null ) {
			log.error("Response QryExchange Pointer<CThostFtdcExchangeField > p = null");
		} else {
			log.info("Response QryExchange: ");
		}
			
	}
	
	/**
	 * 请求查询产品响应
	 */
	@Virtual(31) 
	public  void OnRspQryProduct(Pointer<CThostFtdcProductField > pProduct, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null) {
			log.warn("Response QryProduct Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pProduct == null ) {
			log.error("Response QryProduct Pointer<CThostFtdcProductField > p = null");
		} else {
			log.info("Response QryProduct: ");
		}
		
	}
	
	/**
	 * 请求查询合约响应
	 */
	@Virtual(32) 
	public  void OnRspQryInstrument(Pointer<CThostFtdcInstrumentField > pInstrument, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.error("Response QryInstrument Pointer<CThostFtdcInstrumentField > p = null");
		} else {
			log.info("Response QryInstrument: ");
		}

		if ( pInstrument == null ) {
			log.warn("Response QryInstrument Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}

	} 
	
	/**
	 * 请求查询行情响应
	 */
	@Virtual(33) 
	public  void OnRspQryDepthMarketData(Pointer<CThostFtdcDepthMarketDataField > pDepthMarketData, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询投资者结算结果响应
	 */
	@Virtual(34) 
	public  void OnRspQrySettlementInfo(Pointer<CThostFtdcSettlementInfoField > pSettlementInfo, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询转帐银行响应
	 */
	@Virtual(35) 
	public  void OnRspQryTransferBank(Pointer<CThostFtdcTransferBankField > pTransferBank, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询投资者持仓明细响应
	 */
	@Virtual(36) 
	public  void OnRspQryInvestorPositionDetail(Pointer<CThostFtdcInvestorPositionDetailField > pInvestorPositionDetail, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询客户通知响应
	 */
	@Virtual(37) 
	public  void OnRspQryNotice(Pointer<CThostFtdcNoticeField > pNotice, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询结算信息确认响应
	 */
	@Virtual(38) 
	public  void OnRspQrySettlementInfoConfirm(Pointer<CThostFtdcSettlementInfoConfirmField > pSettlementInfoConfirm, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pSettlementInfoConfirm == null ) {
			log.error("Response QrySettlementInfoConfirm Pointer<CThostFtdcSettlementInfoConfirmField > p = null");
			
		}
		if ( pRspInfo == null ) {
			log.info("Response QrySettlementInfoConfirm Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			//TODO
		}
		log.info("Response QrySettlementInfoConfirm:");
		//TODO
	}
	
	/**
	 * 请求查询投资者持仓明细响应
	 */
	@Virtual(39) 
	public  void OnRspQryInvestorPositionCombineDetail(Pointer<CThostFtdcInvestorPositionCombineDetailField > pInvestorPositionCombineDetail, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 查询保证金监管系统经纪公司资金账户密钥响应
	 */
	@Virtual(40) 
	public  void OnRspQryCFMMCTradingAccountKey(Pointer<CThostFtdcCFMMCTradingAccountKeyField > pCFMMCTradingAccountKey, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询仓单折抵信息响应
	 */
	@Virtual(41) 
	public  void OnRspQryEWarrantOffset(Pointer<CThostFtdcEWarrantOffsetField > pEWarrantOffset, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询投资者品种/跨品种保证金响应
	 */
	@Virtual(42) 
	public  void OnRspQryInvestorProductGroupMargin(Pointer<CThostFtdcInvestorProductGroupMarginField > pInvestorProductGroupMargin, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询交易所保证金率响应
	 */
	@Virtual(43) 
	public  void OnRspQryExchangeMarginRate(Pointer<CThostFtdcExchangeMarginRateField > pExchangeMarginRate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询交易所调整保证金率响应
	 */
	@Virtual(44) 
	public  void OnRspQryExchangeMarginRateAdjust(Pointer<CThostFtdcExchangeMarginRateAdjustField > pExchangeMarginRateAdjust, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询汇率响应
	 */
	@Virtual(45) 
	public  void OnRspQryExchangeRate(Pointer<CThostFtdcExchangeRateField > pExchangeRate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询二级代理操作员银期权限响应
	 */
	@Virtual(46) 
	public  void OnRspQrySecAgentACIDMap(Pointer<CThostFtdcSecAgentACIDMapField > pSecAgentACIDMap, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询产品报价汇率
	 */
	@Virtual(47) 
	public  void OnRspQryProductExchRate(Pointer<CThostFtdcProductExchRateField > pProductExchRate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询期权交易成本响应
	 */
	@Virtual(48) 
	public  void OnRspQryOptionInstrTradeCost(Pointer<CThostFtdcOptionInstrTradeCostField > pOptionInstrTradeCost, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询期权合约手续费响应
	 */
	@Virtual(49) 
	public  void OnRspQryOptionInstrCommRate(Pointer<CThostFtdcOptionInstrCommRateField > pOptionInstrCommRate, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询执行宣告响应
	 */
	@Virtual(50) 
	public  void OnRspQryExecOrder(Pointer<CThostFtdcExecOrderField > pExecOrder, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询询价响应
	 */
	@Virtual(51) 
	public  void OnRspQryForQuote(Pointer<CThostFtdcForQuoteField > pForQuote, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询报价响应
	 */
	@Virtual(52) 
	public  void OnRspQryQuote(Pointer<CThostFtdcQuoteField > pQuote, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询组合合约安全系数响应
	 */
	@Virtual(53) 
	public  void OnRspQryCombInstrumentGuard(Pointer<CThostFtdcCombInstrumentGuardField > pCombInstrumentGuard, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询申请组合响应
	 */
	@Virtual(54) 
	public  void OnRspQryCombAction(Pointer<CThostFtdcCombActionField > pCombAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询转帐流水响应
	 */
	@Virtual(55) 
	public  void OnRspQryTransferSerial(Pointer<CThostFtdcTransferSerialField > pTransferSerial, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询银期签约关系响应
	 */
	@Virtual(56) 
	public  void OnRspQryAccountregister(Pointer<CThostFtdcAccountregisterField > pAccountregister, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 错误应答
	 */
	@Virtual(57) 
	public  void OnRspError(Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
		if ( pRspInfo == null ) {
			log.error("Response Error Pointer<CThostFtdcRspInfoField > pRspInfo = null");
			
		} else {
			log.info("Response Error:" + pRspInfo.get());
		}
		
	}
	
	/**
	 * 报单通知
	 */
	@Virtual(58) 
	public  void OnRtnOrder(Pointer<CThostFtdcOrderField > pOrder) {
		log.info("Message On Order: ");
		for ( ILtsTraderListener lis : tradelistens ) {
			lis.onOrder(getStructObject(pOrder));
		}
	}
	
	/**
	 * 成交通知
	 */
	@Virtual(59) 
	public  void OnRtnTrade(Pointer<CThostFtdcTradeField > pTrade) {
		log.info("Message On Trade:");
		for ( ILtsTraderListener lis : tradelistens ) {
			lis.onTrade(getStructObject(pTrade));
		}
	}
	
	/**
	 * 报单录入错误回报
	 */
	@Virtual(60) 
	public  void OnErrRtnOrderInsert(Pointer<CThostFtdcInputOrderField > pInputOrder, Pointer<CThostFtdcRspInfoField > pRspInfo) {
		if ( pRspInfo == null ) {
			log.warn("Message On Error OrderInsert  Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pInputOrder == null ) {
			log.error("Message On Error OrderInsert Pointer<CThostFtdcInputOrderField > p = null");
		} else {
			log.info("Message On Error OrderInsert:");
		}
		
		
	}
	
	/**
	 * 报单操作错误回报
	 */
	@Virtual(61) 
	public  void OnErrRtnOrderAction(Pointer<CThostFtdcOrderActionField > pOrderAction, Pointer<CThostFtdcRspInfoField > pRspInfo) {
		if ( pRspInfo == null ) {
			log.warn("Message On Error OrderAction Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pOrderAction == null ) {
			log.error("Message On Error OrderAction Pointer<CThostFtdcOrderActionField > p = null");
		} else {
			log.info("Message On Error OrderAction:");
		}
		
		
	}
	
	/**
	 * 合约交易状态通知
	 */
	@Virtual(62) 
	public  void OnRtnInstrumentStatus(Pointer<CThostFtdcInstrumentStatusField > pInstrumentStatus) {
		if ( pInstrumentStatus == null ) {
			log.error("Message On InstrumentStatus Pointer<CThostFtdcInstrumentStatusField > p = null");
		} else {
			log.info("Message On InstrumentStatus");
		}
		
	}
	
	/**
	 * 交易通知
	 */
	@Virtual(63) 
	public  void OnRtnTradingNotice(Pointer<CThostFtdcTradingNoticeInfoField > pTradingNoticeInfo) {
		if ( pTradingNoticeInfo == null ) {
			log.error("Message On TradingNotice Pointer<CThostFtdcTradingNoticeInfoField > p = null");
		} else {
			log.info("Message On TradingNotice: ");
		}
		
	}
	
	/**
	 * 提示条件单校验错误
	 */
	@Virtual(64) 
	public  void OnRtnErrorConditionalOrder(Pointer<CThostFtdcErrorConditionalOrderField > pErrorConditionalOrder) {
		if ( pErrorConditionalOrder == null ) {
			log.error("Message On ErrorConditionalOrder Pointer<CThostFtdcErrorConditionalOrderField > p = null");
		} else {
			log.info("Message On ErrorConditionalOrder: ");
		}
		
	}
	
	/**
	 * 执行宣告通知
	 */
	@Virtual(65) 
	public  void OnRtnExecOrder(Pointer<CThostFtdcExecOrderField > pExecOrder) {
		if ( pExecOrder == null ) {
			log.error("Message On ExecOrder Pointer<CThostFtdcExecOrderField > p = null");
		} else {
			log.info("Message On ExecOrder: ");
		}
	}
	
	/**
	 * 执行宣告录入错误回报
	 */
	@Virtual(66) 
	public  void OnErrRtnExecOrderInsert(Pointer<CThostFtdcInputExecOrderField > pInputExecOrder, Pointer<CThostFtdcRspInfoField > pRspInfo) {
		if( pRspInfo == null ) {
			log.warn("Message On ExecOrderInsert Error Pointer<CThostFtdcRspInfoField > pRspInfo = null");
		} else {
			
		}
		
		if ( pInputExecOrder == null ) {
			log.error("Message On ExecOrderInsert Error Pointer<CThostFtdcInputExecOrderField > p = null");
		} else {
			log.info("Message On ExecOrderInsert Error： ");
		}
	}
	
	/**
	 * 执行宣告操作错误回报
	 */
	@Virtual(67) 
	public  void OnErrRtnExecOrderAction(Pointer<CThostFtdcExecOrderActionField > pExecOrderAction, Pointer<CThostFtdcRspInfoField > pRspInfo) {
		
	}
	
	/**
	 * 询价录入错误回报
	 */
	@Virtual(68) 
	public  void OnErrRtnForQuoteInsert(Pointer<CThostFtdcInputForQuoteField > pInputForQuote, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 报价通知
	 */
	@Virtual(69) 
	public  void OnRtnQuote(Pointer<CThostFtdcQuoteField > pQuote) {
	}
	
	/**
	 * 报价录入错误回报
	 */
	@Virtual(70) 
	public  void OnErrRtnQuoteInsert(Pointer<CThostFtdcInputQuoteField > pInputQuote, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 报价操作错误回报
	 */
	@Virtual(71) 
	public  void OnErrRtnQuoteAction(Pointer<CThostFtdcQuoteActionField > pQuoteAction, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 询价通知
	 */
	@Virtual(72) 
	public  void OnRtnForQuoteRsp(Pointer<CThostFtdcForQuoteRspField > pForQuoteRsp) {
	}
	
	/**
	 * 保证金监控中心用户令牌
	 */
	@Virtual(73) 
	public  void OnRtnCFMMCTradingAccountToken(Pointer<CThostFtdcCFMMCTradingAccountTokenField > pCFMMCTradingAccountToken) {
	}
	
	/**
	 * 申请组合通知
	 */
	@Virtual(74) 
	public  void OnRtnCombAction(Pointer<CThostFtdcCombActionField > pCombAction) {
	}
	
	/**
	 * 申请组合录入错误回报
	 */
	@Virtual(75) 
	public  void OnErrRtnCombActionInsert(Pointer<CThostFtdcInputCombActionField > pInputCombAction, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 请求查询签约银行响应
	 */
	@Virtual(76) 
	public  void OnRspQryContractBank(Pointer<CThostFtdcContractBankField > pContractBank, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询预埋单响应
	 */
	@Virtual(77) 
	public  void OnRspQryParkedOrder(Pointer<CThostFtdcParkedOrderField > pParkedOrder, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询预埋撤单响应
	 */
	@Virtual(78) 
	public  void OnRspQryParkedOrderAction(Pointer<CThostFtdcParkedOrderActionField > pParkedOrderAction, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询交易通知响应
	 */
	@Virtual(79) 
	public  void OnRspQryTradingNotice(Pointer<CThostFtdcTradingNoticeField > pTradingNotice, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询经纪公司交易参数响应
	 */
	@Virtual(80) 
	public  void OnRspQryBrokerTradingParams(Pointer<CThostFtdcBrokerTradingParamsField > pBrokerTradingParams, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询经纪公司交易算法响应
	 */
	@Virtual(81) 
	public  void OnRspQryBrokerTradingAlgos(Pointer<CThostFtdcBrokerTradingAlgosField > pBrokerTradingAlgos, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 请求查询监控中心用户令牌
	 */
	@Virtual(82) 
	public  void OnRspQueryCFMMCTradingAccountToken(Pointer<CThostFtdcQueryCFMMCTradingAccountTokenField > pQueryCFMMCTradingAccountToken, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 银行发起银行资金转期货通知
	 */
	@Virtual(83) 
	public  void OnRtnFromBankToFutureByBank(Pointer<CThostFtdcRspTransferField > pRspTransfer) {
	}
	
	/**
	 * 银行发起期货资金转银行通知
	 */
	@Virtual(84) 
	public  void OnRtnFromFutureToBankByBank(Pointer<CThostFtdcRspTransferField > pRspTransfer) {
	}
	
	/**
	 * 银行发起冲正银行转期货通知
	 */
	@Virtual(85) 
	public  void OnRtnRepealFromBankToFutureByBank(Pointer<CThostFtdcRspRepealField > pRspRepeal) {
	}
	
	/**
	 * 银行发起冲正期货转银行通知
	 */
	@Virtual(86) 
	public  void OnRtnRepealFromFutureToBankByBank(Pointer<CThostFtdcRspRepealField > pRspRepeal) {
	}
	
	/**
	 * 期货发起银行资金转期货通知
	 */
	@Virtual(87) 
	public  void OnRtnFromBankToFutureByFuture(Pointer<CThostFtdcRspTransferField > pRspTransfer) {
	}
	
	/**
	 * 期货发起期货资金转银行通知
	 */
	@Virtual(88) 
	public  void OnRtnFromFutureToBankByFuture(Pointer<CThostFtdcRspTransferField > pRspTransfer) {
	}
	
	/**
	 * 系统运行时期货端手工发起冲正银行转期货请求，银行处理完毕后报盘发回的通知
	 */
	@Virtual(89) 
	public  void OnRtnRepealFromBankToFutureByFutureManual(Pointer<CThostFtdcRspRepealField > pRspRepeal) {
	}
	
	/**
	 * 系统运行时期货端手工发起冲正期货转银行请求，银行处理完毕后报盘发回的通知
	 */
	@Virtual(90) 
	public  void OnRtnRepealFromFutureToBankByFutureManual(Pointer<CThostFtdcRspRepealField > pRspRepeal) {
	}
	
	/**
	 * 期货发起查询银行余额通知
	 */
	@Virtual(91) 
	public  void OnRtnQueryBankBalanceByFuture(Pointer<CThostFtdcNotifyQueryAccountField > pNotifyQueryAccount) {
	}
	
	/**
	 * 期货发起银行资金转期货错误回报
	 */
	@Virtual(92) 
	public  void OnErrRtnBankToFutureByFuture(Pointer<CThostFtdcReqTransferField > pReqTransfer, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 期货发起期货资金转银行错误回报
	 */
	@Virtual(93) 
	public  void OnErrRtnFutureToBankByFuture(Pointer<CThostFtdcReqTransferField > pReqTransfer, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 系统运行时期货端手工发起冲正银行转期货错误回报
	 */
	@Virtual(94) 
	public  void OnErrRtnRepealBankToFutureByFutureManual(Pointer<CThostFtdcReqRepealField > pReqRepeal, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 系统运行时期货端手工发起冲正期货转银行错误回报
	 */
	@Virtual(95) 
	public  void OnErrRtnRepealFutureToBankByFutureManual(Pointer<CThostFtdcReqRepealField > pReqRepeal, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 期货发起查询银行余额错误回报
	 */
	@Virtual(96) 
	public  void OnErrRtnQueryBankBalanceByFuture(Pointer<CThostFtdcReqQueryAccountField > pReqQueryAccount, Pointer<CThostFtdcRspInfoField > pRspInfo) {
	}
	
	/**
	 * 期货发起冲正银行转期货请求，银行处理完毕后报盘发回的通知
	 */
	@Virtual(97) 
	public  void OnRtnRepealFromBankToFutureByFuture(Pointer<CThostFtdcRspRepealField > pRspRepeal) {
	}
	
	/**
	 * 期货发起冲正期货转银行请求，银行处理完毕后报盘发回的通知
	 */
	@Virtual(98) 
	public  void OnRtnRepealFromFutureToBankByFuture(Pointer<CThostFtdcRspRepealField > pRspRepeal) {
	}
	
	/**
	 * 期货发起银行资金转期货应答
	 */
	@Virtual(99) 
	public  void OnRspFromBankToFutureByFuture(Pointer<CThostFtdcReqTransferField > pReqTransfer, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 期货发起期货资金转银行应答
	 */
	@Virtual(100) 
	public  void OnRspFromFutureToBankByFuture(Pointer<CThostFtdcReqTransferField > pReqTransfer, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 期货发起查询银行余额应答
	 */
	@Virtual(101) 
	public  void OnRspQueryBankAccountMoneyByFuture(Pointer<CThostFtdcReqQueryAccountField > pReqQueryAccount, Pointer<CThostFtdcRspInfoField > pRspInfo, int nRequestID, boolean bIsLast) {
	}
	
	/**
	 * 银行发起银期开户通知
	 */
	@Virtual(102) 
	public  void OnRtnOpenAccountByBank(Pointer<CThostFtdcOpenAccountField > pOpenAccount) {
	}
	
	/**
	 * 银行发起银期销户通知
	 */
	@Virtual(103) 
	public  void OnRtnCancelAccountByBank(Pointer<CThostFtdcCancelAccountField > pCancelAccount) {
	}
	
	/**
	 * 银行发起变更银行账号通知
	 */
	@Virtual(104) 
	public  void OnRtnChangeAccountByBank(Pointer<CThostFtdcChangeAccountField > pChangeAccount) {
	}
	
}
