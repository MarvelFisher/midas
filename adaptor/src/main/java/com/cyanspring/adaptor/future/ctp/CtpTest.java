package com.cyanspring.adaptor.future.ctp;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


public class CtpTest {
	
	public static void main(String args[]) throws Exception {
		DOMConfigurator.configure("conf/test/ctplog4j.xml");
		String configFile = "conf/test/ctptest.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		CtpAdaptor bean = (CtpAdaptor)context.getBean("ctpAdaptor");
		bean.init();
		
	}

}
