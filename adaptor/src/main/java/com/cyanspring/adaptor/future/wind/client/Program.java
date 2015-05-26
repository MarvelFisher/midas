package com.cyanspring.adaptor.future.wind.client;


import java.util.Scanner;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class Program {
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.client.Program.class);
	
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/windClientlog4j.xml");
		
		log.info("Wind MsgPackLite Client Start");
		MsgPackLiteDataClient client = new MsgPackLiteDataClient("127.0.0.1",10048);
		Thread clientThread = null;
		try {
			clientThread = new Thread(client,"DataClient");
			clientThread.start();
			
			consoleInput();
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		} finally {
			if(client != null) {
				client.stop();
			}
			if(clientThread != null) {
				clientThread.join();
			}
			log.info("Wind MsgPackLite Client Stop");
		}		
	}	
	
	private static void consoleInput() {
		Scanner in = new Scanner(System.in); 
		String msg = "";
		do {
			msg = in.nextLine();
			if(MsgPackLiteDataClientHandler.ctx != null) {
				MsgPackLiteDataClientHandler.ctx.writeAndFlush(msg.getBytes());
			}
		} while(msg.equalsIgnoreCase("exit") == false);
		in.close();
	}
}