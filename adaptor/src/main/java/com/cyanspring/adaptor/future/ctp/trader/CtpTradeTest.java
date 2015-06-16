package com.cyanspring.adaptor.future.ctp.trader;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.xml.DOMConfigurator;
import org.bridj.BridJ;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrderSide;


public class CtpTradeTest implements IDownStreamListener {
	
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
		final IDownStreamSender sender = con.setListener(listener);
		
		
		
		final LinkedBlockingQueue<ChildOrder> array = new java.util.concurrent.LinkedBlockingQueue<ChildOrder>(100);
		
		TimerTask task1 = new TimerTask() {

			@Override
			public void run() {
				try {
					
					ChildOrder order = new ChildOrder("rb1604", OrderSide.Buy, 10, 2360, ExchangeOrderType.LIMIT,
							 "", "", "", "", null);
					array.add(order);
					sender.newOrder(order);
				} catch (DownStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		TimerTask task2 = new TimerTask() {
			@Override
			public void run() {
				ChildOrder order = array.poll();
				try {
					sender.cancelOrder(order);
				} catch (DownStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		};
		//new Timer().schedule(task, 5000, 5000);
		Timer timer = new Timer();
		timer.schedule(task1, 5000);
		timer.schedule(task2, 10000);
//		timer.scheduleAtFixedRate(task1, 5000, 5000);
//		timer.scheduleAtFixedRate(task2, 7000, 6000);
		
		while(true) {
			
			Thread.sleep(100);
		}
		
	}


}
