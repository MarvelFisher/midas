package com.cyanspring.info.alert;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.alert.ParseData;
import com.cyanspring.common.alert.SendNotificationRequestEvent;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class PremiumFollowManager extends Compute {
	private static final Logger log = LoggerFactory
			.getLogger(PremiumFollowManager.class);
	private String getPremiumFollowListURL;
	private int getPremiumTableInterval;
	private String SetInBoxURL;
	private String PremiumFollowHtmlFormat;
	private Map<String, ArrayList<PremiumUser>> PremiumUserTable = new HashMap<String, ArrayList<PremiumUser>>();
	private String lastUpdateTime;
	
	@Autowired
	ScheduleManager scheduleManager;
	
	@Override
	public void SubscirbetoEvents() {
	}

	@Override
	public void SubscribetoEventsMD() {
		SubscirbetoEvent(ChildOrderUpdateEvent.class);
		SubscirbetoEvent(AsyncTimerEvent.class);
		SubscirbetoEvent(MarketSessionEvent.class);
	}
	
	@Override
	public void init() {
		AsyncTimerEvent getPremiumTableUpdate = new AsyncTimerEvent();
		getPremiumTableUpdate.setKey("getPremiumTableUpdate");
		setPremiumFollowHtmlFormat(getPremiumFollowHtmlFormat().replace("##", "<"));		
		setPremiumFollowHtmlFormat(getPremiumFollowHtmlFormat().replace("$$", ">"));
		getPremiumUserTableAll();
		scheduleManager.scheduleRepeatTimerEvent(getGetPremiumTableInterval(), getEventProcessorMD(), getPremiumTableUpdate);
		
	}


	@Override
	public void processMarketSessionEvent(MarketSessionEvent event,
			List<Compute> computes) {
		MarketSessionType mst = event.getSession();
		if (MarketSessionType.PREOPEN == mst) {
			// Get All
			getPremiumUserTableAll();
		} else if (MarketSessionType.CLOSE == mst) {

		}
	}

	@Override
	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event,
			List<Compute> computes) {
		Execution execution = event.getExecution();
		if (null == execution)
			return;		
		try {
			ArrayList<PremiumUser> lstPU = PremiumUserTable.get(execution
					.getAccount());
			if (null != lstPU) {
				DecimalFormat qtyFormat = new DecimalFormat("#0");
				String strQty = qtyFormat.format(execution.getQuantity());
				DecimalFormat priceFormat = new DecimalFormat("#0.#####");
				String strPrice = priceFormat.format(execution.getPrice());
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String Datetime = dateFormat.format(execution.get(Date.class,
						"Created"));
//				String tradeMessage = "Trade " + execution.getSymbol() + " "
//						+ execution.getSide().toString() + " " + strQty + " @ "
//						+ strPrice;
				String keyValue = execution.getSymbol() + "," + strPrice + ","
						+ strQty + ","
						+ (execution.getSide().isBuy() ? "BOUGHT" : "SOLD");				
				String Msg = execution.getUser() + (execution.getSide().isBuy() ? " BOUGHT " : " SOLD ")
						+ execution.getSymbol() + " " + strQty + "@" + strPrice;
				
				//##b$$%UserName%##/b$$ %Side% ##b$$%Currency%##/b$$ %Quantity% @ %Price%
				String htmlFormat = getPremiumFollowHtmlFormat().replace("%UserName%", execution.getUser());
				htmlFormat = htmlFormat.replace("%Side%", (execution.getSide().isBuy() ? "BOUGHT" : "SOLD"));
				htmlFormat = htmlFormat.replace("%Currency%", execution.getSymbol());
				htmlFormat = htmlFormat.replace("%Quantity%", strQty);
				htmlFormat = htmlFormat.replace("%Price%", strPrice);
				
				String SendtoSocial = "action=31&comment=" + htmlFormat + "&userid=";
				for (PremiumUser user : lstPU) {
					// Send Notification to PremiumUser
					SendNotificationRequestEvent sendNotificationRequestEvent = new SendNotificationRequestEvent(
							null, null, "txId", new ParseData(user.getUserId(),
									Msg, "", AlertMsgType.MSG_TYPE_PREMIUMORDER.getType(),
									Datetime, keyValue));
					SendEvent(sendNotificationRequestEvent);
					// Send RemoteEvent
					SendtoSocial = SendtoSocial + user.getUserId() + ",";
				}
				// Send to Social
				SendtoSocial = SendtoSocial.substring(0,
						SendtoSocial.length() - 1);

				URL obj = new URL(getSetInBoxURL());
				HttpURLConnection httpCon = (HttpURLConnection) obj
						.openConnection();

				httpCon.setRequestMethod("POST");
				httpCon.setRequestProperty("user-Agent", "LTSInfo-SetInBox-31");
				httpCon.setRequestProperty("Content-Length",
						Integer.toString(SendtoSocial.length()));

				httpCon.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(
						httpCon.getOutputStream());
				wr.writeBytes(SendtoSocial);
				wr.flush();
				wr.close();
				int responseCode = httpCon.getResponseCode();
				if (responseCode != 200) {
					log.warn("[Social API]Send to Social Error. : "
							+ responseCode);
				} else {
					log.info("Send to Social : PostMsg=" + htmlFormat + " to "
							+ lstPU.size() + " users.");
				}
				httpCon.disconnect();
			}
		} catch (Exception e) {
			log.error("[processChildOrderUpdateEvent]" + e.getMessage());
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void processAsyncTimerEvent(AsyncTimerEvent event,
			List<Compute> computes) {
		if (event.getKey() == "getPremiumTableUpdate") {
			try {
				//Get PremiumFollow Update List	
				getPremiumUserTableUpdate();
			} catch (Exception e) {
				log.warn("[SendSQLHeartBeat] Exception : " + e.getMessage());
			}
		}
	}
	
	private void getPremiumUserTableUpdate() {

		try {
			URL obj = new URL(getPremiumFollowListURL);
			HttpURLConnection httpCon = (HttpURLConnection) obj
					.openConnection();
			if (null == lastUpdateTime || lastUpdateTime.equals(""))
			{
				log.warn("[getPremiumUserTableUpdate] : lastUpdateTime is emtpy.");
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
			
			URL obj = new URL(getPremiumFollowListURL);
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

	public String getSetInBoxURL() {
		return SetInBoxURL;
	}

	public void setSetInBoxURL(String setInBoxURL) {
		SetInBoxURL = setInBoxURL;
	}

	public String getGetPremiumFollowListURL() {
		return getPremiumFollowListURL;
	}

	public void setGetPremiumFollowListURL(String getPremiumFollowListURL) {
		this.getPremiumFollowListURL = getPremiumFollowListURL;
	}

	public int getGetPremiumTableInterval() {
		return getPremiumTableInterval;
	}

	public void setGetPremiumTableInterval(int getPremiumTableInterval) {
		this.getPremiumTableInterval = getPremiumTableInterval;
	}

	public String getPremiumFollowHtmlFormat() {
		return PremiumFollowHtmlFormat;
	}

	public void setPremiumFollowHtmlFormat(String premiumFollowHtmlFormat) {
		PremiumFollowHtmlFormat = premiumFollowHtmlFormat;
	}


}
