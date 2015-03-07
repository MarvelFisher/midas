package com.cyanspring.info.alert;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; 

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.event.AsyncEventProcessor;

public class NewsManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(NewsManager.class);

	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	ScheduleManager scheduleManager;
	
	private String endString;
	private String socialAPI;
	private String PostAccount;
	private int CheckThreadStatusInterval ;
	
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private ArrayList<News> newsLst;
	private boolean firstGetNews = true ;
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		HttpURLConnection con = null;
		
		try
		{
			if(event == timerEvent) 
			{
				URL obj = new URL("http://wallstreetcn.com/news?cid=6");
				Document doc = Jsoup.parse(obj, 3000);
				Elements links = doc.select("li[class$=news]");
				log.info("[NewsManager] Load News...");
				int Count =0;
				for (Element element : links)
				{
					News news = new News() ;
					String dwedf = element.toString() ;
					
					doc = Jsoup.parse(dwedf);
					Elements ems = doc.select("a[href]");
					String href = ems.get(0).attr("href"); //wallstreetcn child Site
					news.setChildSitePath(href);
					String PicturePath = ems.get(0).getElementsByAttribute("data-original").attr("data-original");//wallstreetcn Picture Path
					news.setPicturePath(PicturePath);
					String title = ems.get(1).text(); //wallstreetcn Title
					news.setTitle(title);
					
					if (newsLst.contains(news))
					{
						continue;
					}
					obj = new URL(href);
					doc = Jsoup.parse(obj,3000);
					Elements childSite = doc.select("div[class$=article-content]");
					doc = Jsoup.parse(childSite.toString()) ;
					childSite = doc.select("p");
					StringBuffer article = new StringBuffer();
					article.append(title + " -- ");
					for(Element em : childSite)
					{
						String ar = em.text();
						if (null == ar)
						{
							continue ;
						}
						if (ar.length() > 5)
						{
							if (ar.substring(0, 5).equals(getEndString()))
							{
								continue;
							}
						}
						article.append(ar + "\r\n");
//						log.info(em.text());
					}					
					news.setArticle(article.toString());
					newsLst.add(0,news);					
					Count ++;
					if (newsLst.size() > 35)
					{
						newsLst.remove(34);
					}
					if (firstGetNews)
					{	
//						continue;
					}
//					log.info(article.toString());
					//Send to Social
					obj = new URL(getSocialAPI());
					HttpURLConnection httpCon = (HttpURLConnection) obj.openConnection();
					
//					String strPoststring = "data={\"photoUrl\":\"" + URLEncoder.encode(PicturePath,"UTF-8") + "\",\"postMessage\":\"" +  URLEncoder.encode(article.toString(),"UTF-8") +
//							"\",\"userAccount\":\"" + getPostAccount() + "\"}";
					String strPoststring = "photoUrl=" + PicturePath + "&postMessage=" + URLEncoder.encode(article.toString(), "UTF-8") + "&userAccount=" + getPostAccount() ;
					log.info("Send to Social photoUrl=" + PicturePath);
					httpCon.setRequestMethod("POST");
					httpCon.setRequestProperty("user-Agent","LTSInfo-NewsManager");
//					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestProperty("Content-Length", Integer.toString(strPoststring.length()));
					
					httpCon.setDoOutput(true);
					DataOutputStream wr = new DataOutputStream(httpCon.getOutputStream());
					wr.writeBytes(strPoststring);
					wr.flush();
					wr.close();					
					
					int responseCode = httpCon.getResponseCode();
					if (responseCode != 200)
					{
						log.warn("[Social API]Send to Social Error.");
					}
//					URL childSite = new URL(href);
//					con = (HttpURLConnection) childSite.openConnection();
//					con.setRequestMethod("GET");
//					BufferedReader in = new BufferedReader(
//					        new InputStreamReader(con.getInputStream()));
//					
//					String inputLine;
//					StringBuffer response = new StringBuffer();
//					FileOutputStream out = new FileOutputStream("childSite.txt");
//					while ((inputLine = in.readLine()) != null) {
//						out.write(inputLine.getBytes());
////						log.info(inputLine);
//						response.append(inputLine);
//					}
							
//					URL picture = new URL("http://img.cdn.wallstreetcn.com/thumb/uploads/ae/75/52/image.jpg");
//					con = (HttpURLConnection) picture.openConnection();
//					con.setRequestMethod("GET");
//					BufferedReader in = new BufferedReader(
//					        new InputStreamReader(con.getInputStream()));
//					
//					String inputLine;
//					StringBuffer response = new StringBuffer();
////					FileOutputStream out = new FileOutputStream("picture.txt");
//					while ((inputLine = in.readLine()) != null) {
////						out.write(inputLine.getBytes());
//						response.append(inputLine);
//					}
				}
				if (firstGetNews)
				{
					firstGetNews = false ;
				}
				if (Count > 0)
				{
					log.info("Get " + String.valueOf(Count) + " new News");	
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Exception : " + e.getMessage());			
		}
		finally
		{
			if (null != con)
			{
				con.disconnect();
			}
		}
	}	
	
//	public String toUtf8(String str) {
//		String returnstr = "";
//		try {
//			returnstr =  new String(str.getBytes("UTF-8"),"UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return returnstr ;
//	}
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		log.info("Initialising...");
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("NewsManager");
		
		scheduleManager.scheduleRepeatTimerEvent(getCheckThreadStatusInterval() , eventProcessor, timerEvent);
		newsLst = new ArrayList<News>();
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub
		log.info("Uninitialising...");
		newsLst.clear();
		eventProcessor.uninit();
	}

	public String getEndString() {
		return endString;
	}

	public void setEndString(String endString) {
		this.endString = endString;
	}

	public int getCheckThreadStatusInterval() {
		return CheckThreadStatusInterval;
	}

	public void setCheckThreadStatusInterval(int checkThreadStatusInterval) {
		CheckThreadStatusInterval = checkThreadStatusInterval;
	}

	public String getSocialAPI() {
		return socialAPI;
	}

	public void setSocialAPI(String socialAPI) {
		this.socialAPI = socialAPI;
	}

	public String getPostAccount() {
		return PostAccount;
	}

	public void setPostAccount(String postAccount) {
		PostAccount = postAccount;
	}
}
