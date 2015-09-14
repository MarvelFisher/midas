package com.cyanspring.info.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.ResetAccountReplyEvent;
import com.cyanspring.common.event.account.ResetAccountReplyType;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.event.AsyncEventProcessor;

public class UserManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(UserManager.class);

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	SessionFactory sessionFactoryCentral;

	@Autowired
	ScheduleManager scheduleManager;

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private IRemoteEventManager eventManagerMD;

	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private MarketSessionType marketSession;
	private String market;
	private String tradeDate;
    private Date LastHeartbeat;
    private int timerinterval;
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

	private AsyncEventProcessor eventProcessorMD = new AsyncEventProcessor() {
		@Override
		public void subscribeToEvents() {
			subscribeToEvent(MarketSessionEvent.class, null);
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}
	};

	public void processResetAccountRequestEvent(ResetAccountRequestEvent event) {
		log.info("[processResetAccountRequestEvent] : AccountId :"
				+ event.getAccount() + " Coinid : " + event.getCoinId());
		ResetUser(event);
	}

	public void processMarketSessionEvent(MarketSessionEvent event) {
		log.info("[MarketSessionEvent] : " + event);
		marketSession = event.getSession();
		market = event.getMarket();
		tradeDate = event.getTradeDate();
	}

	private void UpdateQuery(String Cmd, Session session) {
		SQLQuery query;
		int Return = 0;
		int RetryCount = 0;
		while (RetryCount < 20) {
			try {
				RetryCount++;
				query = session.createSQLQuery(Cmd);
				Return = query.executeUpdate();
				break;
			} catch (Exception e) {
				log.warn("Query Exception [" + RetryCount + "]: " + Cmd + " : "
						+ e.getMessage());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {

				}
			}
		}
	}

	private boolean ResetUser(ResetAccountRequestEvent event) {
		Session session = sessionFactory.openSession();
		Session sessionCentral = sessionFactoryCentral.openSession();
		SQLQuery query;
		Iterator iterator;
		String strCmd = "";
		int Return = 0;
		int RetryCount = 0;
		String UserId = event.getUserId();
		String AccountId = event.getAccount();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar cal = Calendar.getInstance();
		String ddateFormat = "-R" + dateFormat.format(cal.getTime());

		ArrayList<String> ContestIdArray = new ArrayList<String>();
		try {
			// Local MYSQL

			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + AccountId
					+ ddateFormat + "'" + ",USER_ID='" + UserId + ddateFormat
					+ "' where ACCOUNT_ID='" + AccountId + "'";
			UpdateQuery(strCmd, session);

			strCmd = "update CLOSED_POSITIONS set ACCOUNT_ID='" + AccountId
					+ ddateFormat + "'" + ",USER_ID='" + UserId + ddateFormat
					+ "' where ACCOUNT_ID='" + AccountId + "'";
			UpdateQuery(strCmd, session);
			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + AccountId
					+ ddateFormat + "'" + ",USER_ID='" + UserId + ddateFormat
					+ "' where ACCOUNT_ID='" + AccountId + "'";
			UpdateQuery(strCmd, session);
			strCmd = "update CHILD_ORDER_AUDIT set ACCOUNT='" + AccountId
					+ ddateFormat + "'" + ",TRADER='" + UserId + ddateFormat
					+ "' where ACCOUNT='" + AccountId + "'";
			UpdateQuery(strCmd, session);
			strCmd = "update EXECUTIONS set ACCOUNT='" + AccountId
					+ ddateFormat + "'" + ",TRADER='" + UserId + ddateFormat
					+ "' where ACCOUNT='" + AccountId + "'";
			UpdateQuery(strCmd, session);
			// Central MYSQL
			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + AccountId
					+ ddateFormat + "'" + ",USER_ID='" + UserId + ddateFormat
					+ "' where ACCOUNT_ID='" + AccountId + "'";
			UpdateQuery(strCmd, sessionCentral);
			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + AccountId
					+ ddateFormat + "'" + ",USER_ID='" + UserId + ddateFormat
					+ "' where ACCOUNT_ID='" + AccountId + "'";
			UpdateQuery(strCmd, sessionCentral);
			strCmd = "select * from CONTEST;";
			query = sessionCentral.createSQLQuery(strCmd);
			iterator = query.list().iterator();

			while (iterator.hasNext()) {
				Object[] rows = (Object[]) iterator.next();
				String StartDate = rows[2].toString();
				String EndDate = rows[3].toString();

				if (tradeDate.compareTo(EndDate) > 0) {
					continue;
				}
				ContestIdArray.add((String) rows[0]);
			}

			for (String ContestId : ContestIdArray) {
				strCmd = "delete from " + ContestId + "_"
						+ market.toLowerCase() + " where USER_ID='" + UserId
						+ "' and DATE<>'0'";
				UpdateQuery(strCmd, sessionCentral);
				strCmd = "update " + ContestId + "_" + market.toLowerCase()
						+ " set UNIT_PRICE='1' where USER_ID='" + UserId
						+ "' and DATE='0'";
				UpdateQuery(strCmd, sessionCentral);
			}
            strCmd = "insert into RESETUSER(USER_ID) values('" + UserId + "') on duplicate update set USER_ID='" + UserId + "';";
            try {
                query = session.createSQLQuery(strCmd);
                Return = query.executeUpdate();
            }
            catch (Exception e) {
            }

			ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(
					event.getKey(), event.getSender(), event.getAccount(),
					event.getTxId(), event.getUserId(), event.getMarket(),
					event.getCoinId(),
					ResetAccountReplyType.LTSINFO_USERMANAGER, true, "");
			eventManager.sendRemoteEvent(resetAccountReplyEvent);
			log.info("Reset User Success : " + UserId);
		} catch (Exception e) {
			log.error("[" + strCmd + "] " + e.getMessage(), e);
			ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(
					event.getKey(), event.getSender(), event.getAccount(),
					event.getTxId(), event.getUserId(), event.getMarket(),
					event.getCoinId(),
					ResetAccountReplyType.LTSINFO_USERMANAGER, false,
					MessageLookup.buildEventMessage(
							ErrorMessage.ACCOUNT_RESET_ERROR,
							"[UserManager]: Reset User " + UserId + " fail."));
			try {
				eventManager.sendRemoteEvent(resetAccountReplyEvent);
			} catch (Exception ee) {
				log.error("[" + strCmd + "] " + ee.getMessage(), ee);
			}
		} finally {
			session.close();
			sessionCentral.close();
		}
		return true;
	}

	private void SendSQLHeartBeat() {
        Calendar cal = Calendar.getInstance();
        if ((cal.getTimeInMillis() - LastHeartbeat.getTime()) < 240000)
        {
            return ;
        }
        else
        {
            LastHeartbeat = cal.getTime();
        }
		Session session = null;
		Session sessionCentral = null;
		try {
			session = sessionFactory.openSession();
			sessionCentral = sessionFactoryCentral.openSession();

			SQLQuery sq = session.createSQLQuery("select 1;");
			Iterator iterator = sq.list().iterator();
			sq = sessionCentral.createSQLQuery("select 1;");
			iterator = sq.list().iterator();
			log.info("Send SQLHeartBeat...");
		} catch (Exception e) {
			log.warn("[SendSQLHeartBeat] : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
			if (null != sessionCentral) {
				sessionCentral.close();
			}
		}
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if (event == timerEvent) {
			try {
				SendSQLHeartBeat();
			} catch (Exception e) {
				log.warn("[timerEvent] Exception : " + e.getMessage());
			}
		}
	}

	@Override
	public void init() throws Exception {
        // TODO Auto-generated method stub
        log.info("Initialising...");

        Calendar cal = Calendar.getInstance();
        LastHeartbeat = cal.getTime();

        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null) {
			eventProcessor.getThread().setName("UserManager");
		}

        eventProcessorMD.setHandler(this);
        eventProcessorMD.init();
        if (eventProcessorMD.getThread() != null) {
			eventProcessorMD.getThread().setName("UserManager-MD");
		}

        scheduleManager.scheduleRepeatTimerEvent(getTimerinterval(), eventProcessorMD,
                timerEvent);
    }

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
		eventProcessorMD.uninit();
		scheduleManager.uninit();
	}

    public int getTimerinterval() {
        return timerinterval;
    }

    public void setTimerinterval(int timerinterval) {
        this.timerinterval = timerinterval;
    }
}
