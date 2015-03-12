package com.cyanspring.info.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.event.AsyncEventProcessor;

public class UserManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(UserManager.class);

	@Autowired
	SessionFactory sessionFactory;

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
		ResetUser(event.getAccount());
	}

	private boolean ResetUser(String UserId) {
		Session session = sessionFactory.openSession();
		SQLQuery query ;
		Iterator iterator ;
		String strCmd = "";
		int Return ;
		
		ArrayList<String> ContestIdArray = new ArrayList<String>();
		try {
			// Local MYSQL	
			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + UserId + "-FX-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + UserId + "-FX'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();			
			strCmd = "update CLOSED_POSITIONS set ACCOUNT_ID='" + UserId + "-FX-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + UserId + "-FX'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + UserId + "-FX-reset'" +
					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + UserId + "-FX'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update CHILD_ORDER_AUDIT set ACCOUNT='" + UserId + "-FX-reset'" +
					 ",TRADER='" + UserId + "-reset' where ACCOUNT='" + UserId + "-FX'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update EXECUTIONS set ACCOUNT='" + UserId + "-FX-reset'" +
					 ",TRADER='" + UserId + "-reset' where ACCOUNT='" + UserId + "-FX'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update ACCOUNT_SETTINGS set ACCOUNT_ID='" + UserId + "-FX-reset' where ACCOUNT_ID='" 
			        + UserId + "-FX'";
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();

			// Central MYSQL
//			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + UserId + "-FX-reset'" +
//					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + UserId + "-FX'" ;
//			query = session.createSQLQuery(strCmd);
//			Return = query.executeUpdate();
//			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + UserId + "-FX-reset'" +
//					 ",USER_ID='" + UserId + "-reset' where ACCOUNT_ID='" + UserId + "-FX'" ;
//			query = session.createSQLQuery(strCmd);
//			Return = query.executeUpdate();			
//			strCmd = "update ACCOUNT_SETTINGS set ACCOUNT_ID='" + UserId + "-FX-reset' where ACCOUNT_ID='" 
//			        + UserId + "-FX'"; 
//			query = session.createSQLQuery(strCmd);
//			Return = query.executeUpdate();
			
			strCmd = "select * from CONTEST;";
			query = session.createSQLQuery(strCmd);
			iterator = query.list().iterator();
			while (iterator.hasNext()) {		
				
//				strStartDate = RS.getString("START_DATE");
//				if(strToday.compareTo(strStartDate) < 0)
//				{
//					continue;
//				}
//				strEndDate   = RS.getString("END_DATE");
//				if(strToday.compareTo(strEndDate) > 0)
//				{
//					continue;
//				}				
//				ContestIdArray.add(RS.getString("CONTEST_ID"));
				
				ContestIdArray.add((String) iterator.next());
			}			
			for (String ContestId : ContestIdArray) {
				strCmd = "delete from " + ContestId + "_fx where USER_ID='"
						+ UserId + "'";
				query = session.createSQLQuery(strCmd);
				Return = query.executeUpdate();
			}
			
			for (String ContestId : ContestIdArray)
			{
				//strCmd = "delete from "+ ContestId + "_fx where USER_ID='" + UserId + "'";
				log.info("Reset User in Contest : " + ContestId);
				strCmd = "delete from "+ ContestId + "_fx where USER_ID='" + UserId + "' and DATE<>'0'";
				query = session.createSQLQuery(strCmd);
				Return = query.executeUpdate();
				strCmd = "update "+ ContestId + "_fx set UNIT_PRICE='1' where USER_ID='" + UserId + "' and DATE='0'";
				query = session.createSQLQuery(strCmd);
				Return = query.executeUpdate();				
			}
			
		} catch (Exception e) {
			log.warn(e.getMessage());
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
		// TODO Auto-generated method stub
	}
}
