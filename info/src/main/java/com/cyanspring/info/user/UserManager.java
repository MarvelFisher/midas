package com.cyanspring.info.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.ResetAccountReplyEvent;
import com.cyanspring.common.event.account.ResetAccountReplyType;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.event.AsyncEventProcessor;

public class UserManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(UserManager.class);

	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	SessionFactory sessionFactoryCentral;

	@Autowired
	private IRemoteEventManager eventManager;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
		@Override
		public void subscribeToEvents() {
			subscribeToEvent(ResetAccountRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public void processResetAccountRequestEvent(ResetAccountRequestEvent event) {
		log.info("[processResetAccountRequestEvent] : AccountId :" + event.getAccount() + "Coinid : " + event.getCoinId());
		ResetUser(event);
	}

	private boolean ResetUser(ResetAccountRequestEvent event) {
		Session session = sessionFactory.openSession();
		Session sessionCentral = sessionFactoryCentral.openSession();
		SQLQuery query ;
		Iterator iterator ;
		String strCmd = "";
		int Return ;
		String UserId = event.getUserId();
		String AccountId = event.getAccount();
		ArrayList<String> ContestIdArray = new ArrayList<String>();
		try {
			// Local MYSQL	
			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + AccountId + "-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();			
			strCmd = "update CLOSED_POSITIONS set ACCOUNT_ID='" + AccountId + "-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + AccountId + "-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update CHILD_ORDER_AUDIT set ACCOUNT='" + AccountId + "-reset'" +
					 ",TRADER='" + UserId + "-reset' where ACCOUNT='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update EXECUTIONS set ACCOUNT='" + AccountId + "-reset'" +
					 ",TRADER='" + UserId + "-reset' where ACCOUNT='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();

			// Central MYSQL
			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + AccountId + "-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + AccountId + "'" ;
			query = sessionCentral.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + AccountId + "-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + AccountId + "'" ;
			query = sessionCentral.createSQLQuery(strCmd);
			Return = query.executeUpdate();			

			strCmd = "select * from CONTEST;";
			query = sessionCentral.createSQLQuery(strCmd);
			iterator = query.list().iterator();
			Date DateTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.0");
			String CurTime = dateFormat.format(DateTime);
			
			
			while (iterator.hasNext()) {	
				Object[] rows = (Object[]) iterator.next();				
				String StartDate = (String) rows[2].toString();
			    String EndDate = rows[3].toString();
			    
//				strStartDate = RS.getString("START_DATE");
				if(CurTime.compareTo(StartDate) < 0)
				{
					continue;
				}
//				strEndDate   = RS.getString("END_DATE");
				if(CurTime.compareTo(EndDate) > 0)
				{
					continue;
				}				
//				ContestIdArray.add(RS.getString("CONTEST_ID"));
				
				ContestIdArray.add((String) rows[0]);
			}				
			for (String ContestId : ContestIdArray)
			{
				//strCmd = "delete from "+ ContestId + "_fx where USER_ID='" + UserId + "'";
				
				strCmd = "delete from "+ ContestId + "_fx where USER_ID='" + UserId + "' and DATE<>'0'";
				query = sessionCentral.createSQLQuery(strCmd);
				Return = query.executeUpdate();
				strCmd = "update "+ ContestId + "_fx set UNIT_PRICE='1' where USER_ID='" + UserId + "' and DATE='0'";
				query = sessionCentral.createSQLQuery(strCmd);
				Return = query.executeUpdate();	
				
			}			
			ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(event.getKey(),event.getSender(), event.getAccount(), event.getTxId(), event.getUserId(), event.getMarket(), event.getCoinId(),ResetAccountReplyType.LTSINFO_USERMANAGER, true,"");
			eventManager.sendRemoteEvent(resetAccountReplyEvent);
			log.info("Reset User Success : " + UserId);
		} catch (Exception e) {
			log.error(e.getMessage());
			ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(event.getKey(),
					event.getSender(), 
					event.getAccount(), 
					event.getTxId(), 
					event.getUserId(), 
					event.getMarket(), 
					event.getCoinId(), 
					ResetAccountReplyType.LTSINFO_USERMANAGER, 
					false, 
					//"Reset User " + UserId + "fail.");
					MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_RESET_ERROR, "Reset User " + UserId + "fail."));
			try
			{
				eventManager.sendRemoteEvent(resetAccountReplyEvent);
			}
			catch(Exception ee)
			{
				log.error(ee.getMessage());
			}
		}
		finally
		{
			session.close();
			sessionCentral.close();
		}
		return true;
	}

	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		log.info("Initialising...");

		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("UserManager");
	}

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
		// TODO Auto-generated method stub
	}
}
