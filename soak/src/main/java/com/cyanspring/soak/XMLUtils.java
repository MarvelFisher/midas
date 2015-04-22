package com.cyanspring.soak;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.info.HistoricalPriceRequestEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeRequestEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XMLUtils 
{
	
	public static void eventToXML(AsyncEvent event) throws FileNotFoundException, IOException
	{
		XStream xstream = new XStream(new DomDriver());
		File file = new File("events" + File.separator + event.getClass().getName() + ".xml");
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(event, os);
			os.close();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}
	
	public static AsyncEvent eventFromXML(String fileName) throws Exception
	{
		XStream xstream = new XStream(new DomDriver());
		File file = new File(fileName);
		AsyncEvent event = null;
		if (file.exists()) {
			try {
				ClassLoader save = xstream.getClassLoader();
				ClassLoader cl = AsyncEvent.class.getClassLoader();
				if (cl != null)
					xstream.setClassLoader(cl);
				event = (AsyncEvent) xstream.fromXML(file);
				if (!(event instanceof AsyncEvent))
					throw new Exception("Can't xstream load event: "
							+ fileName);
				xstream.setClassLoader(save);
			} catch (Exception e) {
				throw e;
			}
		}
		return event;
	}
	
	//Create XML
	public static void main(String args[]) throws Exception
	{
		HistoricalPriceRequestEvent event = new HistoricalPriceRequestEvent(null, "Test.Info.I1");
		Calendar cal = Calendar.getInstance();
		event.setEndDate(cal.getTime());
		cal.add(Calendar.DATE, -1);
		event.setStartDate(cal.getTime());
		event.setHistoryType("Q");
		event.setSymbol("IFC1.CF");
		eventToXML(event);
	}/**/
	
	//Read XML
	/*public static void main(String args[]) throws Exception
	{
		HistoricalPriceRequestEvent event = (HistoricalPriceRequestEvent) eventFromXML(HistoricalPriceRequestEvent.class.getName()+ ".xml");
		System.out.println(event);
	}*/

}
