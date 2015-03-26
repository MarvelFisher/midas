package com.cyanspring.info.alert;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

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
	
	private static String endString;
	private static String socialAPI;
	private static String PostAccount;
	private int CheckThreadStatusInterval ;
	
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	public static ArrayList<News> newsLst;

	private static boolean firstGetNews;
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
		try
		{
			if(event == timerEvent) 
			{
				NewsThread NT = new NewsThread() ;				
			}
		}
		catch (Exception e)
		{
			log.warn("Exception : " + e.getMessage());			
		}
	}
	
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		log.info("Initialising...");
		
		setFirstGetNews(true);
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

	public static String getEndString() {
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

	public static String getSocialAPI() {
		return socialAPI;
	}

	public void setSocialAPI(String socialAPI) {
		this.socialAPI = socialAPI;
	}

	public synchronized static String getPostAccount() {
		return PostAccount;
	}

	public void setPostAccount(String postAccount) {
		PostAccount = postAccount;
	}
	
	public synchronized static boolean isFirstGetNews() {
		return firstGetNews;
	}

	public synchronized static void setFirstGetNews(boolean firstGetNews) {
		NewsManager.firstGetNews = firstGetNews;
	}

	public synchronized static void GetNews()
	{		
		try
		{
			URL obj = new URL("http://wallstreetcn.com/news?cid=6");
			HttpURLConnection con = null;
			con = (HttpURLConnection)obj.openConnection();
			con.setRequestProperty("Content-Type", "");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
			con.setRequestMethod("GET");
			
			int responseCode = con.getResponseCode();
			if (responseCode != 200)
			{
				log.warn("Server returned HTTP response code: " + responseCode + " for URL: http://wallstreetcn.com/news?cid=6");
				return ; 
			}
			Document doc = Jsoup.parse(con.getInputStream(), null, "http://wallstreetcn.com/news?cid=6");
			
			Elements links = doc.select("li[class$=news]");
			log.info("[NewsManager] Load News... Start");
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
				
				ems = doc.select("span[class$=meta time visible-lg-inline-block]");
				String postTime = ems.get(0).text();
				news.setPostTime(postTime);
				
				int iSearch = Collections.binarySearch(newsLst, news);
				if (iSearch >= 0)
				{						
					continue;
				}					
				con.disconnect();
				obj = new URL(href);
				con = (HttpURLConnection)obj.openConnection();
				con.setRequestProperty("Content-Type", "");
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
				con.setRequestMethod("GET");
				
				responseCode = con.getResponseCode();
				
				doc = Jsoup.parse(con.getInputStream(), null, href) ;
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
//					log.info(em.text());
				}					
				news.setArticle(article.toString());
				newsLst.add(~iSearch, news);
				Count++;
				if (newsLst.size() > 100) {
					newsLst.remove(100);
				}
				
				if (isFirstGetNews())
				{	
					continue;
				}
				//Send to Social
				obj = new URL(getSocialAPI());
				HttpURLConnection httpCon = (HttpURLConnection) obj.openConnection();
				
//				String strPoststring = "data={\"photoUrl\":\"" + URLEncoder.encode(PicturePath,"UTF-8") + "\",\"postMessage\":\"" +  URLEncoder.encode(article.toString(),"UTF-8") +
//						"\",\"userAccount\":\"" + getPostAccount() + "\"}";
				String strPoststring = "photoUrl=" + PicturePath + "&postMessage=" + URLEncoder.encode(article.toString(), "UTF-8") + "&userAccount=" + getPostAccount() ;
				
				httpCon.setRequestMethod("POST");
				httpCon.setRequestProperty("user-Agent","LTSInfo-NewsManager");
				httpCon.setRequestProperty("Content-Length", Integer.toString(strPoststring.length()));
				
				httpCon.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(httpCon.getOutputStream());
				wr.writeBytes(strPoststring);
				wr.flush();
				wr.close();					
				con.disconnect();
				responseCode = httpCon.getResponseCode();
				if (responseCode != 200)
				{
					log.warn("[Social API]Send to Social Error.");
				}
				else
				{
					log.info("Send to Social : Title=" + title  + ", PostTime=" + postTime  + 
							", ChildSite=" + href + ", photoUrl=" + PicturePath);
				}
				httpCon.disconnect();
			}
			if (isFirstGetNews())
			{
				setFirstGetNews(false) ;
			}
			if (Count > 0)
			{
				log.info("Get " + String.valueOf(Count) + " new News");	
			}
			log.info("[NewsManager] Load News... End");
		}
		catch (Exception e)
		{
			log.warn("[NewsThread] : " + e.getMessage());
		}
	}
	
	
	public static class NewsThread extends Thread{
				
		public NewsThread()
		{
			setDaemon(true);
			this.start();
		}
		@Override
		public void run()
		{
			GetNews();
		}
	}
}
