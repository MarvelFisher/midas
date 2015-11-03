package com.cyanspring.info.alert;
import java.io.DataOutputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.alert.ParseData;
import com.cyanspring.info.ne.NetEaseClient;

public class IMThread extends Thread{
	private String strThreadId = "";
	private String action = "";
	private String serverAccount = "";
	private NetEaseClient NEClient;
	ThreadStatus threadStatus ;
	private int timeoutSecond;
	private int retryTimes;
	private ConcurrentLinkedQueue<ParseData> ParseDataQueue ;
	
	private boolean startThread;
	private static final Logger log = LoggerFactory
			.getLogger(IMThread.class);	

	//********************************************************************************************
    //Dev-FX
    //App-Key = c146348b9dd8f246cef2f42750f160a8
    //App-Secret = b6eb8125c0ea
    //Dev-FC
    //App-Key = cad6d11d672277363d74635554e3f523
    //App-Secret = 184463f69a1b
    //Dev-SC
    //App-Key = e3138df7761d041bdaf39d73b4289b10
    //App-Secret = dd9712c1d9a5
    //********************************************************************************************	
	
	public IMThread(String strThreadId, ConcurrentLinkedQueue<ParseData> parseDataQueue, 
			int timeoutSecond, int retryTimes, String serverAccount, 
			String uri, String appKey, String appSecret, String tokenSalt, String iv, 
			String action)	
	{
		try
		{
			if (uri == "" || appKey == "" || appSecret == "" || serverAccount == "")
			{
				return ;
			}
			NEClient = new NetEaseClient(uri, appKey, appSecret, tokenSalt, iv);
			this.serverAccount = serverAccount;
	        this.action = action;
			this.startThread = true ;
	        this.ParseDataQueue = parseDataQueue ;
	        this.timeoutSecond = timeoutSecond;
	        this.strThreadId = strThreadId;
	        this.retryTimes = retryTimes ;
	        this.threadStatus = new ThreadStatus();
			setDaemon(true);
		}
		catch(Exception e)
		{
			log.warn("ParseThread Exception : " + e.getMessage()) ;
		}
	}
	
	@Override
	public void run()
	{	
		try
		{
			boolean bReSend = false ;
			log.info(this.strThreadId + " Start.");
			int iRetrytimes = 0;
			ParseData PD  = null;
			while (startThread)
			{
				if (!bReSend)
				{
					iRetrytimes = 0;
					PD = ParseDataQueue.poll();
				}
				if (PD == null)
				{
					threadStatus.setThreadState(ThreadState.IDLE) ;
					threadStatus.UpdateTime();
					Thread.sleep(300);		
					continue;
				}
				try
				{
					bReSend = false ;
//					log.debug("[ParseThread "+strThreadId+"] sending"+PD.strpushMessage);
					threadStatus.setThreadState(ThreadState.SENDDING) ;
					threadStatus.UpdateTime();
					sendPost(PD) ;					
				}
				catch (Exception ec)
				{
					log.warn(strThreadId + " SendPost Exception : " + ec.getMessage());
					iRetrytimes ++ ;
					if (iRetrytimes <= retryTimes)
					{
						log.warn("[ParseThread "+strThreadId+"] sending again "+PD.getStrpushMessage());
						threadStatus.setThreadState(ThreadState.SENDDING) ;
						threadStatus.UpdateTime();
						bReSend = true ;
					}
					else
					{
						log.warn(strThreadId + " Retrytimes out : " + PD.getStrpushMessage());
					}
				}
			}
		}
		catch (Exception e)
		{
			log.warn(strThreadId + " Exception : " + e.getMessage());
		}
	}
	
	protected void sendPost(ParseData PD) throws Exception 
	{ 
		String strPoststring = 
				"{\"type\": \"3\" \"data\": {\"alert\": \"" + PD.getStrpushMessage() + 
				"\",\"msgid\":\"" + PD.getStrMsgId() + 
				"\",\"msgType\":\"" + PD.getStrMsgType() +
                "\",\"action\":\"" + action + 
                ((PD.getStrLocalTime().length() > 0) ? ("\",\"datetime\":\"" + PD.getStrLocalTime()) : "") + 
                (((PD.getStrKeyValue()).length() > 0) ? ("\",\"KeyValue\":\"" + PD.getStrKeyValue()) : "") +
                ((PD.getStrDeepLink().length() > 0) ? ("\",\"deeplink\":\"" + PD.getStrDeepLink()) :"") +
                "\"" + ", \"badge\": \"Increment\"}}";
		
		JSONObject json = new JSONObject();
		json.put("msg", strPoststring);
		JSONObject responseJSON = 
				new JSONObject(NEClient.sendThirdPartyMsg(serverAccount, PD.getStrUserId(), json));
		String responseCode = responseJSON.get("code").toString();
		if (responseCode.equals("200") == false)
			responseCode += ", " + responseJSON.get("desc");
		
		log.info("Return code: " + responseCode);
	}
	
	public String getThreadId()
	{
		return strThreadId;
	}
	
	public void setstartThread(boolean startThread)
	{
		this.startThread = startThread  ;
	}
	
	public ThreadStatus getThreadStatus()
	{
		return threadStatus ;
	}

	public String getServerAccount()
	{
		return serverAccount;
	}

	public void setServerAccount(String serverAccount)
	{
		this.serverAccount = serverAccount;
	}
}
