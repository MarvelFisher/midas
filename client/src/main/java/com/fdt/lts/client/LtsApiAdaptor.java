package com.fdt.lts.client;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class LtsApiAdaptor extends AbstractApi {
	
	
	
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/apilog4j.xml");
		String configFile = "conf/api.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		LtsApiAdaptor adaptor = (LtsApiAdaptor)context.getBean("apiAdaptor");
		adaptor.init();
	}
}
