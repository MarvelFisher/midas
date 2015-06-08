package com.cyanspring.adaptor.future.ctp.trader;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.bridj.BridJ;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExecType;


public class CtpTradeTest implements IDownStreamListener {
	
//	static {
//		BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\win32\\thosttraderapi.dll"));		
//		BridJ.register();
//	}
	
	@Override
	public void onState(boolean on) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOrder(ExecType execType, ChildOrder order,
			Execution execution, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String orderId, String message) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String args[]) throws Exception {
		DOMConfigurator.configure("conf/test/ctplog4j.xml");
		String configFile = "conf/test/ctptest.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		CtpTradeAdaptor bean = (CtpTradeAdaptor)context.getBean("ctpAdaptor");
		bean.init();
		
		CtpTradeTest listener = new CtpTradeTest();
		
		IDownStreamConnection con = bean.getConnections().get(0);
		IDownStreamSender sender = con.setListener(listener);
		while(con.getState()) {
			
		}
		
	}


}
