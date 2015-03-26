package com.cyanspring.info.alert;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class PremiumFollowThread extends Thread {
	private static final Logger log = LoggerFactory
			.getLogger(PremiumFollowThread.class);

	private String PremiumFollowURL;
	private ConcurrentLinkedQueue<PremiumRequestType> QueryTypeQueue = new ConcurrentLinkedQueue<PremiumRequestType>();
	private Map<String, ArrayList<PremiumUser>> PremiumUserTable = new HashMap<String, ArrayList<PremiumUser>>();
	private String lastUpdateTime ;
	public PremiumFollowThread(String URL) {
		log.info("PrimiumFollow Thread... Create.");
		this.setPremiumFollowURL(URL);
		setDaemon(true);
		start();
	}

	protected void getPremiumUpdate() {
		QueryTypeQueue.add(PremiumRequestType.QUERY_UPDATE);
		notifyQueState();
	}

	protected void getPremiumAll() {
		QueryTypeQueue.add(PremiumRequestType.QUERY_All);
		notifyQueState();
	}

	protected ArrayList<PremiumUser> findUser(String followedUser) {
		return PremiumUserTable.get(followedUser);
	}

	@Override
	public void run() {
		while (true) {
			PremiumRequestType PRT = QueryTypeQueue.poll();
			if (PRT == null) {
				waitQueState();
				continue;
			}
			try {
				if (PRT == PremiumRequestType.QUERY_UPDATE) {
					getPremiumUserTableUpdate();
				} else if (PRT == PremiumRequestType.QUERY_All) {
					getPremiumUserTableAll();
				}
			} catch (Exception e) {
				log.error("[PremiumFollowThread] : " + e.getMessage());
			}
		}
	}

	protected synchronized void waitQueState() {
		try {
			wait();
		} catch (InterruptedException e) {
			log.error("[PremiumFollowThread] : " + e.getMessage());
		}
	}

	protected synchronized void notifyQueState() {
		notify();
	}

	private void getPremiumUserTableUpdate() {

		try {
			URL obj = new URL(PremiumFollowURL);
			HttpURLConnection httpCon = (HttpURLConnection) obj
					.openConnection();
			if (null == lastUpdateTime || lastUpdateTime.equals(""))
			{
				log.warn("[getPremiumUserTableUpdate] : lastUpdateTime is emtpy.");
				getPremiumUserTableAll();
				return;
			}
			String strPoststring = "Time=" + lastUpdateTime;
			httpCon.setRequestMethod("POST");
			httpCon.setRequestProperty("user-Agent",
					"LTSInfo-AlertManager-PremiumFollow");
			httpCon.setRequestProperty("Content-Length",
					"" + strPoststring.length());

			httpCon.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(
					httpCon.getOutputStream());
			wr.writeBytes(strPoststring);
			wr.flush();
			wr.close();
			int Count =0 ;
			int responseCode = httpCon.getResponseCode();
			if (responseCode != 200) {
				log.error("[PremiumFollow API]Send to Social Error. : "
						+ responseCode);
			} else {
				// Parse Social Msg
				log.info("[getPremiumUserTableUpdate] : GetSocialAPI Update...Start");
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new InputStreamReader(
						httpCon.getInputStream(), "UTF-8"));
				reader.beginObject();
				try {
					while (reader.hasNext()) {
						String name = reader.nextName();
						if (name.equals("meta")) {
							reader.skipValue();
						} else if (name.equals("data")) {
							reader.beginArray();
							while (reader.hasNext()) {
								String pfUserId = "";
								String userId = "";
								String endDate = "";
								
								JsonReader pm = reader;
								pm.beginObject();
								while(pm.hasNext())
								{
									name = pm.nextName();
									if (name.equals("pf_userid"))
									{
										pfUserId = pm.nextString();
									}
									else if (name.equals("userid"))
									{
										userId = pm.nextString();
									}
									else if (name.equals("end_date"))
									{
										endDate = pm.nextString();
									}
									else
									{
										pm.skipValue();
									}									
								}
								pm.endObject();
								// Add
								PremiumUser pu = new PremiumUser(userId, endDate) ;
								ArrayList<PremiumUser> lst = PremiumUserTable.get(pfUserId);
								int Search = 0;
								if (null == lst)
								{
									Count ++ ;
									//new + add
									lst = new ArrayList<PremiumUser>();
									lst.add(pu);
									PremiumUserTable.put(pfUserId, lst);
								}
								else
								{
									Search = Collections.binarySearch(lst,pu);
									if (Search < 0)
									{
										lst.add(~Search, pu);
										Count ++ ;
									}
									else
									{
										//update
										lst.get(Search).setEndDate(endDate);
									}
								}
							}
							reader.endArray();
						}
						else
						{
							reader.skipValue();
						}
					}
					log.info("[getPremiumUserTableUpdate] : GetSocialAPI Update...End");					
				} catch (Exception ee) {
					log.error("[PremiumFollowThread][Update] error : " + ee.getMessage());
				}
			}
			httpCon.disconnect();
		} catch (Exception e) {
			log.error("[PremiumFollowThread][Update] error : " + e.getMessage());
		}
	}

	private void getPremiumUserTableAll() {
		try {
			Calendar cd = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String curTime = format.format(cd.getTime());
			Map<String, ArrayList<PremiumUser>> tempPremiumUserTable = new HashMap<String, ArrayList<PremiumUser>>();
			
			URL obj = new URL(PremiumFollowURL);
			HttpURLConnection httpCon = (HttpURLConnection) obj
					.openConnection();
			String strPoststring = "Time=" + "";
			httpCon.setRequestMethod("POST");
			httpCon.setRequestProperty("user-Agent",
					"LTSInfo-AlertManager-PremiumFollow");
			httpCon.setRequestProperty("Content-Length",
					"" + strPoststring.length());

			httpCon.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(
					httpCon.getOutputStream());
			wr.writeBytes(strPoststring);
			wr.flush();
			wr.close();
			int Count =0 ;
			
			int responseCode = httpCon.getResponseCode();
			if (responseCode != 200) {
				log.error("[PremiumFollow API]Send to Social Error. : "
						+ responseCode);
			} else {
				// Parse Social Msg
				log.info("[getPremiumUserTableAll] : GetSocialAPI All...Start");
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new InputStreamReader(
						httpCon.getInputStream(), "UTF-8"));
				reader.beginObject();
				try {
					while (reader.hasNext()) {
						String name = reader.nextName();
						if (name.equals("meta")) {
							reader.skipValue();
						} else if (name.equals("data")) {
							reader.beginArray();
							while (reader.hasNext()) {
								String pfUserId = "";
								String userId = "";
								String endDate = "";
								
								JsonReader pm = reader;
								pm.beginObject();
								while(pm.hasNext())
								{
									name = pm.nextName();
									if (name.equals("pf_userid"))
									{
										pfUserId = pm.nextString();
									}
									else if (name.equals("userid"))
									{
										userId = pm.nextString();
									}
									else if (name.equals("end_date"))
									{
										endDate = pm.nextString();
									}
									else
									{
										pm.skipValue();
									}									
								}
								pm.endObject();
								// Add
								PremiumUser pu = new PremiumUser(userId, endDate) ;
								ArrayList<PremiumUser> lst = tempPremiumUserTable.get(pfUserId);
								int Search = 0;
								if (null == lst)
								{
									Count ++ ;
									//new + add
									lst = new ArrayList<PremiumUser>();
									lst.add(pu);
									tempPremiumUserTable.put(pfUserId, lst);
								}
								else
								{
									Search = Collections.binarySearch(lst,pu);
									if (Search < 0)
									{
										lst.add(~Search, pu);
										Count ++ ;
									}
									else
									{
										//update
										lst.get(Search).setEndDate(endDate);
									}
								}
							}
							reader.endArray();
						}
						else
						{
							reader.skipValue();
						}
					}
					log.info("[getPremiumUserTableAll] : GetSocialAPI All...End");	
					lastUpdateTime = curTime ;
					if (null != tempPremiumUserTable && tempPremiumUserTable.size() >0)
					{
						PremiumUserTable = tempPremiumUserTable ;						
					}
					else
					{
						log.error("[PremiumFollowThread][ALL] error : GetPremiumFollowTable Fail.");
					}
				} catch (Exception ee) {
					log.error("[PremiumFollowThread][ALL] error : " + ee.getMessage());
				}
			}
			httpCon.disconnect();
		} catch (Exception e) {
			log.error("[PremiumFollowThread][ALL] error : " + e.getMessage());
		}
	}

	public String getPremiumFollowURL() {
		return PremiumFollowURL;
	}

	public void setPremiumFollowURL(String premiumFollowURL) {
		PremiumFollowURL = premiumFollowURL;
	}
}
