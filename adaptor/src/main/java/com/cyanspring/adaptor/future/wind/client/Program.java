package com.cyanspring.adaptor.future.wind.client;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Program {
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.client.Program.class);
	
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/windClientlog4j.xml");
		
		log.info("Wind MsgPackLite Client Start");
	}
}
