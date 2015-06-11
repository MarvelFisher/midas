package com.cyanspring.adaptor.future.ctp.trader.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.bridj.StructObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspInfoField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspUserLoginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcSettlementInfoConfirmField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamListener;

public abstract class AbstractTraderProxy {
	private final static Logger log = LoggerFactory.getLogger(AbstractTraderProxy.class);
	protected AtomicInteger seqId = new AtomicInteger();
	
	protected IChainListener listener;
	
	
	
	protected abstract void responseLogin(CThostFtdcRspUserLoginField event) ;
	
	protected abstract void responseSettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField event) ;
	
	protected abstract void doLogin();
	
	protected abstract void doReqSettlementInfoConfirm();
	
	protected abstract void responseOnOrder(StructObject event) ;
	protected abstract void responseOnOrder(StructObject event, CThostFtdcRspInfoField rspInfo);
	
	protected void responseOnState() {
		
	}
	
	protected void responseOnError(CThostFtdcRspInfoField rspInfo) {
		
	}
	
	protected abstract void startThreadProcess();
	private Thread workThread1;
	protected void connect() {
		if ( workThread1 == null ) {
			workThread1 = new Thread() {
				@Override
				public void run() {
					startThreadProcess();
				}
			};
			workThread1.setDaemon(true);
			workThread1.start();
		} else {
			log.info("Already connecting, disconnect first");
		}
	}
	
	protected abstract void stopThreadProcess();
	private Thread workThread2;
	protected void disConnect() {
		if ( workThread2 == null ) {
			workThread2 = new Thread() {
				@Override
				public void run() {
					
				}
			};
			workThread2.setDaemon(true);
			workThread2.start();
		} else {
			log.info("Already disconnected, connect first");
		}
	}
	
	public void addListener(IChainListener listener) throws DownStreamException {
		if(listener != null && this.listener != null)
			throw new DownStreamException("Support only one listener");		
		this.listener = listener;
	}
	
	public synchronized int getNextSeq() {
		return seqId.incrementAndGet();
	}

	
	
}
