/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 *
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.server.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.account.ThirdPartyUser;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.business.*;
import com.cyanspring.common.event.account.*;
import com.google.common.base.Strings;

import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.PositionPeakPrice;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.event.pool.PmAccountPoolsDeleteEvent;
import com.cyanspring.common.event.pool.PmAccountPoolsInsertEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountDeleteEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountInsertEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountUpdateEvent;
import com.cyanspring.common.event.pool.PmExchangeSubAccountDeleteEvent;
import com.cyanspring.common.event.pool.PmExchangeSubAccountInsertEvent;
import com.cyanspring.common.event.pool.PmExchangeSubAccountUpdateEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolDeleteEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolInsertEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolRecordUpdateEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolRecordsDeleteEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolRecordsInsertEvent;
import com.cyanspring.common.event.pool.PmUserExchangeSubAccountDeleteEvent;
import com.cyanspring.common.event.pool.PmUserExchangeSubAccountInsertEvent;
import com.cyanspring.common.event.signal.CancelSignalEvent;
import com.cyanspring.common.event.signal.SignalEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyUpdateEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.pool.AccountPool;
import com.cyanspring.common.pool.ExchangeAccount;
import com.cyanspring.common.pool.ExchangeSubAccount;
import com.cyanspring.common.pool.InstrumentPool;
import com.cyanspring.common.pool.InstrumentPoolRecord;
import com.cyanspring.common.pool.UserExchangeSubAccount;
import com.cyanspring.common.type.PersistType;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.server.account.UserKeeper;

import org.apache.derby.drda.NetworkServerControl;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PersistenceManager {
	private static final Logger log = LoggerFactory
			.getLogger(PersistenceManager.class);

	public static String ID = PersistenceManager.class.toString();

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	ScheduleManager scheduleManager;

	@Autowired
	CentralDbConnector centralDbConnector;

	private CheckEmailType checkEmailUnique = CheckEmailType.allCheck;
	private CheckPhoneType checkPhoneUnique = CheckPhoneType.allCheck;
	private boolean syncCentralDb = true;
	private boolean useLtsGateway = true;
	private boolean embeddedSQLServer;
	private int textSize = 4000;
	private boolean cleanStart;
	private boolean todayOnly;
	private long purgeOrderDays = 20;
	private boolean deleteTerminated = true;
	protected boolean persistSignal;
	NetworkServerControl server;
	private String embeddedHost = "localhost";
	private int embeddedPort = 1527;

	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long timeInterval = 10 * 60 * 1000;
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(UpdateParentOrderEvent.class, null);
			subscribeToEvent(UpdateChildOrderEvent.class, null);
			subscribeToEvent(SingleInstrumentStrategyUpdateEvent.class, null);
			subscribeToEvent(MultiInstrumentStrategyUpdateEvent.class, null);
			subscribeToEvent(PmUpdateUserEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmCreateAccountEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmUpdateAccountEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmRemoveDetailOpenPositionEvent.class,
					PersistenceManager.ID);
			subscribeToEvent(PmUpdateDetailOpenPositionEvent.class,
					PersistenceManager.ID);
			subscribeToEvent(ClosedPositionUpdateEvent.class, null);
			subscribeToEvent(PmChangeAccountSettingEvent.class,
					PersistenceManager.ID);
			subscribeToEvent(PmEndOfDayRollEvent.class, null);
			subscribeToEvent(InternalResetAccountRequestEvent.class, null);
			subscribeToEvent(PmPositionPeakPriceUpdateEvent.class, null);
			subscribeToEvent(PmPositionPeakPriceDeleteEvent.class, null);
			subscribeToEvent(PmCreateGroupManagementEvent.class, null);
			subscribeToEvent(PmDeleteGroupManagementEvent.class, null);
			subscribeToEvent(PmAddCashEvent.class, null);
			subscribeToEvent(PmUpdateCoinControlEvent.class, null);
			subscribeToEvent(PmExchangeAccountInsertEvent.class, null);
			subscribeToEvent(PmExchangeAccountUpdateEvent.class, null);
			subscribeToEvent(PmExchangeAccountDeleteEvent.class, null);
			subscribeToEvent(PmExchangeSubAccountInsertEvent.class, null);
			subscribeToEvent(PmExchangeSubAccountUpdateEvent.class, null);
			subscribeToEvent(PmExchangeSubAccountDeleteEvent.class, null);
			subscribeToEvent(PmInstrumentPoolInsertEvent.class, null);
			subscribeToEvent(PmInstrumentPoolDeleteEvent.class, null);
			subscribeToEvent(PmInstrumentPoolRecordsInsertEvent.class, null);
			subscribeToEvent(PmInstrumentPoolRecordsDeleteEvent.class, null);
			subscribeToEvent(PmInstrumentPoolRecordUpdateEvent.class, null);
			subscribeToEvent(PmAccountPoolsInsertEvent.class, null);
			subscribeToEvent(PmAccountPoolsDeleteEvent.class, null);
			subscribeToEvent(PmUserExchangeSubAccountInsertEvent.class, null);
			subscribeToEvent(PmUserExchangeSubAccountDeleteEvent.class, null);

			if (persistSignal) {
				subscribeToEvent(SignalEvent.class, null);
				subscribeToEvent(CancelSignalEvent.class, null);
			}
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	private AsyncEventProcessor userEventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(AsyncTimerEvent.class, null);
			subscribeToEvent(PmUserLoginEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmUserCreateAndLoginEvent.class,
					PersistenceManager.ID);
			subscribeToEvent(PmCreateUserEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmCreateCSTWUserEvent.class, PersistenceManager.ID);
			subscribeToEvent(ChangeUserPasswordEvent.class, null);
			subscribeToEvent(UserTerminateEvent.class, null);
			subscribeToEvent(UserMappingEvent.class, null);
			subscribeToEvent(UserMappingListEvent.class, null);
			subscribeToEvent(UserMappingDetachEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}

	};

	public PersistenceManager() {
	}

	public void init() throws Exception {
		log.info("initialising");
		if (embeddedSQLServer) {
			startEmbeddedSQLServer();
		}

		if (cleanStart) {
			truncateData(Clock.getInstance().now());
		} else if (todayOnly) {
			truncateData(TimeUtil.getOnlyDate(Clock.getInstance().now()));
		} else if (purgeOrderDays > 0) {
			truncateOrders();
		}

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null) {
			eventProcessor.getThread().setName("PersistenceManager");
		}

		userEventProcessor.setHandler(this);
		userEventProcessor.init();
		if (userEventProcessor.getThread() != null) {
			userEventProcessor.getThread().setName("PersistenceManager(Users)");
		}

		scheduleManager.scheduleRepeatTimerEvent(timeInterval,
				userEventProcessor, timerEvent);

	}

	public void uninit() {
		log.info("uninitialising");
		scheduleManager.uninit();
		eventProcessor.uninit();
		if (embeddedSQLServer) {
			stopEmbeddedSQLServer();
		}
	}

	private void startEmbeddedSQLServer() throws UnknownHostException,
			Exception {
		server = new NetworkServerControl(InetAddress.getByName(embeddedHost),
				embeddedPort);
		server.start(null);
		log.info("Embedded SQL server started");
	}

	private void stopEmbeddedSQLServer() {
		boolean interrupted = false;

		try {
			server.shutdown();
			log.info("Embedded SQL server stopped");
		} catch (InterruptedException ie) {
			log.error(ie.getMessage(), ie);
			interrupted = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (interrupted) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
			}
		}
	}

	private void truncateData(Date date) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "delete from GroupManagement";
			Query query = session.createQuery(hql);
			int rowCount = query.executeUpdate();
			log.debug("GROUP_MANAGEMENT Records deleted: " + rowCount);

			hql = "delete from User where created < :created";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			rowCount = query.executeUpdate();
			log.debug("USER Records deleted: " + rowCount);

			hql = "delete from Account where created < :created";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			rowCount = query.executeUpdate();
			log.debug("ACCOUNT Records deleted: " + rowCount);

			hql = "delete from ClosedPosition where created < :created";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			rowCount = query.executeUpdate();
			log.debug("CLOSED_POSITION Records deleted: " + rowCount);

			hql = "delete from OpenPosition where created < :created";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			rowCount = query.executeUpdate();
			log.debug("OPEN_POSITION Records deleted: " + rowCount);

			hql = "delete from TextObject where timeStamp < :timeStamp and serverId = :serverId";
			query = session.createQuery(hql);
			query.setParameter("timeStamp", date);
			query.setParameter("serverId", IdGenerator.getInstance()
					.getSystemId());
			rowCount = query.executeUpdate();
			log.debug("TextObject Records deleted: " + rowCount);

			hql = "delete from ChildOrder where created < :created and serverId = :serverId";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			query.setParameter("serverId", IdGenerator.getInstance()
					.getSystemId());
			rowCount = query.executeUpdate();
			log.debug("ChildOrder Records deleted: " + rowCount);

			hql = "delete from ChildOrderAudit where created < :created and serverId = :serverId";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			query.setParameter("serverId", IdGenerator.getInstance()
					.getSystemId());
			rowCount = query.executeUpdate();
			log.debug("ChildOrderAudit Records deleted: " + rowCount);

			hql = "delete from Execution where created < :created and serverId = :serverId";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			query.setParameter("serverId", IdGenerator.getInstance()
					.getSystemId());
			rowCount = query.executeUpdate();
			log.debug("Execution Records deleted: " + rowCount);

			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	private void truncateOrders() {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			Date date = new Date();
			date = new Date(date.getTime() - purgeOrderDays
					* TimeUtil.millisInDay);

			tx = session.beginTransaction();
			String hql;
			Query query;
			int rowCount;

			hql = "delete from TextObject where timeStamp < :timeStamp and serverId = :serverId";
			query = session.createQuery(hql);
			query.setParameter("timeStamp", date);
			query.setParameter("serverId", IdGenerator.getInstance()
					.getSystemId());
			rowCount = query.executeUpdate();
			log.debug("TextObject Records deleted: " + rowCount);

			hql = "delete from ChildOrder where created < :created and serverId = :serverId";
			query = session.createQuery(hql);
			query.setParameter("created", date);
			query.setParameter("serverId", IdGenerator.getInstance()
					.getSystemId());
			rowCount = query.executeUpdate();
			log.debug("ChildOrder Records deleted: " + rowCount);

			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processInternalResetAccountRequestEvent(
			InternalResetAccountRequestEvent event) {
		ResetAccountRequestEvent evt = event.getEvent();
		String account = evt.getAccount();
		log.info("Received InternalResetAccountRequestEvent: " + account);
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			Date date = new Date();
			date = new Date(date.getTime() - purgeOrderDays
					* TimeUtil.millisInDay);

			tx = session.beginTransaction();
			String hql;
			Query query;
			int rowCount;

			hql = "delete from TextObject where account = :account";
			query = session.createQuery(hql);
			query.setParameter("account", account);
			rowCount = query.executeUpdate();
			log.info("TextObject Records deleted: " + rowCount);

			hql = "delete from ChildOrder where account = :account";
			query = session.createQuery(hql);
			query.setParameter("account", account);
			rowCount = query.executeUpdate();
			log.info("ChildOrder Records deleted: " + rowCount);

			hql = "delete from ClosedPosition where account = :account";
			query = session.createQuery(hql);
			query.setParameter("account", account);
			rowCount = query.executeUpdate();
			log.info("CLOSED_POSITION Records deleted: " + rowCount);

			hql = "delete from OpenPosition where account = :account";
			query = session.createQuery(hql);
			query.setParameter("account", account);
			rowCount = query.executeUpdate();
			log.info("OPEN_POSITION Records deleted: " + rowCount);

			hql = "delete from Execution where account = :account";
			query = session.createQuery(hql);
			query.setParameter("account", account);
			rowCount = query.executeUpdate();
			log.info("Execution Records deleted: " + rowCount);

			query = session.getNamedQuery("cleanAccountsDailyByAccount");
			query.setParameter("account", account);
			rowCount = query.executeUpdate();
			log.info("AccountsDaily Records deleted: " + rowCount);

			tx.commit();

			CashAudit cashAudit = new CashAudit(IdGenerator.getInstance()
					.getNextID(), account, AuditType.RESET_ACCOUNT, Clock
					.getInstance().now(), Default.getAccountCash(), 0);
			tx = session.beginTransaction();
			session.save(cashAudit);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	private void persistXml(String id, PersistType persistType,
			StrategyState state, String user, String account, String route,
			String xml) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<TextObject> list1 = session.createCriteria(TextObject.class)
					.add(Restrictions.eq("id", id))
					.add(Restrictions.eq("persistType", persistType)).list();

			for (TextObject obj : list1) {
				session.delete(obj);
			}

			List<TextObject> list2 = TextObject.createTextObjects(id,
					persistType, state, user, account, route, xml, textSize);
			for (TextObject obj : list2) {
				session.save(obj);
			}

			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	private void deleteXml(String id, PersistType persistType) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<TextObject> list1 = session.createCriteria(TextObject.class)
					.add(Restrictions.eq("id", id))
					.add(Restrictions.eq("persistType", persistType)).list();

			for (TextObject obj : list1) {
				session.delete(obj);
			}

			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if (syncCentralDb) {
			log.debug("Received AsyncTimerEvent, connection:"
					+ centralDbConnector.updateConnection());
		}
	}

	public void processPmUserLoginEvent(PmUserLoginEvent event) {
		log.debug("Received PmUserLoginEvent: "
				+ event.getOriginalEvent().getUserId());
		String userId = event.getOriginalEvent().getUserId().toLowerCase()
				.trim();
		UserKeeper userKeeper = (UserKeeper) event.getUserKeeper();
		AccountKeeper accountKeeper = (AccountKeeper) event.getAccountKeeper();
		boolean ok = false;
		String message = "";
		User user = null;
		Account defaultAccount = null;
		List<Account> list = null;

		if (null != userKeeper) {
			Session session = sessionFactory.openSession();
			Transaction tx = null;
			ErrorMessage msg = null;

			try {
				/*
				 * if(!syncCentralDb || centralDbConnector.userLogin(userId,
				 * event.getOriginalEvent().getPassword())) ok =
				 * userKeeper.login(userId,
				 * event.getOriginalEvent().getPassword());
				 */

				if (!syncCentralDb && !useLtsGateway) {
					ok = userKeeper.login(userId, event.getOriginalEvent()
							.getPassword());
					if (ok) {
						user = userKeeper.getUser(userId);

						if (null != user.getDefaultAccount()
								&& !user.getDefaultAccount().isEmpty()) {
							defaultAccount = accountKeeper.getAccount(user
									.getDefaultAccount());
						}

						list = accountKeeper.getAccounts(userId);

						if (defaultAccount == null
								&& (list == null || list.size() <= 0)) {
							ok = false;
							// message = "No trading account available for this
							// user";
							message = MessageLookup
									.buildEventMessage(
											ErrorMessage.NO_TRADING_ACCOUNT,
											"No trading account available for this user");
						}
					} else {

						// message = "userid or password invalid";
						message = MessageLookup.buildEventMessage(
								ErrorMessage.INVALID_USER_ACCOUNT_PWD,
								"userid or password invalid");

					}
				} else if (!syncCentralDb && useLtsGateway) {
					user = event.getOriginalEvent().getUser();
					ok = userKeeper.userExists(userId);

					if (!ok) // user created by another LTS, must be created
								// here again
					{
						// generating default Account
						String defaultAccountId = user.getDefaultAccount();
						if (null == user.getDefaultAccount()
								|| user.getDefaultAccount().equals("")) {
							if (!accountKeeper.accountExists(user.getId() + "-"
									+ Default.getMarket())) {
								defaultAccountId = user.getId() + "-"
										+ Default.getMarket();
							} else {
								defaultAccountId = Default.getAccountPrefix()
										+ IdGenerator.getInstance()
												.getNextSimpleId();
								if (accountKeeper
										.accountExists(defaultAccountId)) {
									msg = ErrorMessage.CREATE_USER_FAILED;
									throw new UserException(
											"[PmUserLoginEvent]Cannot create default account for user: "
													+ user.getId()
													+ ", last try: "
													+ defaultAccountId);
								}
							}
						}

						// account creating process
						defaultAccount = new Account(defaultAccountId, userId);
						user.setDefaultAccount(defaultAccountId);
						accountKeeper.setupAccount(defaultAccount);
						createAccount(defaultAccount);
						list = new ArrayList<Account>();
						list.add(defaultAccount);
						eventManager.sendEvent(new OnUserCreatedEvent(user,
								list));
						eventManager.sendRemoteEvent(new AccountUpdateEvent(
								event.getOriginalEvent().getKey(), null,
								defaultAccount));

						tx = session.beginTransaction();
						session.save(user);
						tx.commit();
						log.info("[PmUserLoginEvent] Created user: " + userId);
						ok = true;

					} else // user exists in derby
					{
						user = userKeeper.getUser(userId);
						list = accountKeeper.getAccounts(userId);
					}
				} else {
					user = centralDbConnector.userLoginEx(userId, event
							.getOriginalEvent().getPassword(), event
							.getOriginalEvent().getLoginType());

					if (null != user) // login successful from mysql
					{
						userId = user.getId(); // It may be email or phone,
												// change back to user id.

						if (user.getTerminationStatus().isTerminated()) {
							ok = false;

							msg = user.getTerminationStatus() == TerminationStatus.TRANSFERRING ? ErrorMessage.FDT_ID_IS_UNDER_PROCESSING
									: ErrorMessage.USER_IS_TERMINATED;
							throw new UserException(
									user.getTerminationStatus() == TerminationStatus.TRANSFERRING ? "Your FDT ID is under processing. It will be created during weekend. Thank you!"
											: "User is terminated");
						}

						ok = userKeeper.userExists(userId);
						if (!ok) // user created by another LTS, must be created
									// here again
						{
							// generating default Account
							String defaultAccountId = user.getDefaultAccount();
							if (null == user.getDefaultAccount()
									|| user.getDefaultAccount().equals("")) {
								if (!accountKeeper.accountExists(user.getId()
										+ "-" + Default.getMarket())) {
									defaultAccountId = user.getId() + "-"
											+ Default.getMarket();
								} else {
									defaultAccountId = Default
											.getAccountPrefix()
											+ IdGenerator.getInstance()
													.getNextSimpleId();
									if (accountKeeper
											.accountExists(defaultAccountId)) {
										msg = ErrorMessage.CREATE_USER_FAILED;
										throw new UserException(
												"[PmUserLoginEvent]Cannot create default account for user: "
														+ user.getId()
														+ ", last try: "
														+ defaultAccountId);
									}
								}
							}

							// account creating process
							defaultAccount = new Account(defaultAccountId,
									userId);
							user.setDefaultAccount(defaultAccountId);
							accountKeeper.setupAccount(defaultAccount);
							createAccount(defaultAccount);
							list = new ArrayList<Account>();
							list.add(defaultAccount);
							eventManager.sendEvent(new OnUserCreatedEvent(user,
									list));
							eventManager
									.sendRemoteEvent(new AccountUpdateEvent(
											event.getOriginalEvent().getKey(),
											null, defaultAccount));

							tx = session.beginTransaction();
							session.save(user);
							tx.commit();
							log.info("[PmUserLoginEvent] Created user: "
									+ userId);
							ok = true;

						} else // user exists in derby
						{
							user = userKeeper.getUser(userId);
							list = accountKeeper.getAccounts(userId);
						}
					} else {
						ok = false;
						// message = "userid or password invalid";
						message = MessageLookup.buildEventMessage(
								ErrorMessage.INVALID_USER_ACCOUNT_PWD,
								"userid or password invalid");

					}
				}

				if (ok == true && null == user.getDefaultAccount()) {
					ok = false;
					msg = ErrorMessage.NO_TRADING_ACCOUNT;
					throw new UserException(
							"[PmUserLoginEvent]No trading account available for this user: "
									+ userId);
				}

			} catch (Exception ue) {

				if (ue instanceof UserException) {
					log.warn(ue.getMessage(), ue);
					message = MessageLookup.buildEventMessage(msg,
							ue.getMessage());
					if (msg == null) {
						message = MessageLookup.buildEventMessage(
								ErrorMessage.INVALID_USER_ACCOUNT_PWD,
								ue.getMessage());
					}
				} else {
					log.error(ue.getMessage(), ue);
					message = MessageLookup.buildEventMessage(
							ErrorMessage.CREATE_USER_FAILED, ue.getMessage());
				}

				if (tx != null) {
					tx.rollback();
				}

			} finally {
				session.close();
			}

		} else {
			ok = false;
			// message = "Server is not set up for login";
			message = MessageLookup.buildEventMessage(
					ErrorMessage.SYSTEM_NOT_READY,
					"Server is not set up for login");

		}

		try {
			eventManager.sendRemoteEvent(new UserLoginReplyEvent(event
					.getOriginalEvent().getKey(), event.getOriginalEvent()
					.getSender(), user, defaultAccount, list, ok, message,
					event.getOriginalEvent().getTxId()));

			if (ok) {
				user.setLastLogin(Clock.getInstance().now());
				eventManager.sendEvent(new PmUpdateUserEvent(
						PersistenceManager.ID, null, user));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("Login: " + event.getOriginalEvent().getUserId() + ", " + ok);
	}

	public void processPmUserCreateAndLoginEvent(PmUserCreateAndLoginEvent event) {
		log.debug("Received PmUserCreateAndLoginEvent: "
				+ event.getOriginalEvent().getUser().getId());

		UserKeeper userKeeper = (UserKeeper) event.getUserKeeper();
		AccountKeeper accountKeeper = (AccountKeeper) event.getAccountKeeper();
		boolean ok = true;

		if (null == event.getUser()) // user exist , getAccount
		{
			ok = loginAndGetAccount(event, userKeeper, accountKeeper);
			log.info("Login: " + event.getOriginalEvent().getUser().getId()
					+ ", " + ok);
		} else if (Strings.isNullOrEmpty(event.getUser().getId())) {
			ok = loginFromThirdPartyIdAndGetAccount(event, userKeeper,
					accountKeeper);
			log.info("Login 3rd: " + event.getOriginalEvent().getThirdPartyId()
					+ ", " + ok);
		} else // user not exist, create user and then getAccount
		{
			ok = createUserAndGetAccount(event);
			log.info("CreateAndLogin: "
					+ event.getOriginalEvent().getUser().getId() + ", " + ok);
		}
	}

	private boolean createUserAndGetAccount(PmUserCreateAndLoginEvent event) {
		boolean ok;
		Session session = sessionFactory.openSession();
		Account defaultAccount = event.getAccounts().size() > 0 ? event
				.getAccounts().get(0) : null;
		User user = event.getUser();
		Transaction tx = null;
		ok = true;
		ErrorMessage msg = null;
		String message = "";

		boolean isTransfer = false;

		try {
			if (syncCentralDb) {
				if (Strings.isNullOrEmpty(event.getOriginalEvent()
						.getThirdPartyId())
						&& centralDbConnector
								.isThirdPartyUserAnyMappingExist(user.getId())) {

					throw new CentralDbException(
							"This third party id is already used in the new version app",
							ErrorMessage.THIRD_PARTY_ID_USED_IN_NEW_APP);
				}

				isTransfer = !Strings.isNullOrEmpty(event.getOriginalEvent()
						.getThirdPartyId())
						&& centralDbConnector.isUserExist(event
								.getOriginalEvent().getThirdPartyId()
								.toLowerCase());

				createCentralDbUser(event, user, isTransfer);
			} else {
				isTransfer = event.getOriginalEvent().isTransfer();
			}

			// the 3rd user type is recorded in THIRD_PARTY_USER table.
			if (user.getUserType().isThirdParty()
					&& !Strings.isNullOrEmpty(event.getOriginalEvent()
							.getThirdPartyId())) {
				user.setUserType(UserType.NORMAL);
			}

			if (isTransfer) {
				// Set the old default account.

				UserKeeper userKeeper = (UserKeeper) event.getUserKeeper();
				AccountKeeper accountKeeper = (AccountKeeper) event
						.getAccountKeeper();

				// getAccount
				User oldUser = userKeeper.getUser(event.getOriginalEvent()
						.getThirdPartyId().toLowerCase());

				if (null != oldUser.getDefaultAccount()
						&& !oldUser.getDefaultAccount().isEmpty()) {
					defaultAccount = accountKeeper.getAccount(oldUser
							.getDefaultAccount());
				}

				user.setDefaultAccount(defaultAccount.getId());
			}

			tx = session.beginTransaction();
			session.save(user);
			tx.commit();
			log.info("Created user: " + event.getUser());
		} catch (Exception e) {
			if (e instanceof CentralDbException) {
				msg = ((CentralDbException) e).getClientMessage();
				log.warn(e.getMessage(), e);
			} else {
				msg = ErrorMessage.CREATE_USER_FAILED;
				log.error(e.getMessage(), e);
			}

			ok = false;
			message = MessageLookup.buildEventMessageWithCode(msg,
					e.getMessage());

			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}

		if (ok && !isTransfer) {
			for (Account account : event.getAccounts()) {
				createAccount(account);
			}

			eventManager.sendEvent(new OnUserCreatedEvent(user, event
					.getAccounts()));
		}

		if (event.getOriginalEvent() != null) {
			try {
				if (isTransfer) {

					UserKeeper userKeeper = (UserKeeper) event.getUserKeeper();
					AccountKeeper accountKeeper = (AccountKeeper) event
							.getAccountKeeper();

					String newEmail = user.getEmail();
					boolean updatedEmail;

					if (syncCentralDb) {
						updatedEmail = newEmail != centralDbConnector.getUser(
								event.getOriginalEvent().getThirdPartyId()
										.toLowerCase()).getEmail();
					} else {
						updatedEmail = event.getOriginalEvent()
								.isUpdatedEmail();
					}

					// getAccount
					user = userKeeper.getUser(event.getOriginalEvent()
							.getThirdPartyId().toLowerCase());

					if (null != user.getDefaultAccount()
							&& !user.getDefaultAccount().isEmpty()) {
						defaultAccount = accountKeeper.getAccount(user
								.getDefaultAccount());
					}

					List<Account> list = accountKeeper
							.getAccounts(user.getId());

					if (defaultAccount == null
							&& (list == null || list.size() <= 0)) {
						ok = false;
						message = MessageLookup.buildEventMessage(
								ErrorMessage.NO_TRADING_ACCOUNT,
								"No trading account available for this user");
					}

					event.getUser().setDefaultAccount(defaultAccount.getId());

					if (updatedEmail) {
						user.setEmail(newEmail);
					}

					eventManager
							.sendRemoteEvent(new UserCreateAndLoginReplyEvent(
									event.getOriginalEvent().getKey(), event
											.getOriginalEvent().getSender(),
									user, defaultAccount, list, ok,
									event.getOriginalEvent().getOriginalID(),
									message,
									event.getOriginalEvent().getTxId(), true,
									updatedEmail));
					if (ok) {
						user.setLastLogin(Clock.getInstance().now());
						eventManager.sendEvent(new PmUpdateUserEvent(
								PersistenceManager.ID, null, user));
					}

				} else {

					eventManager
							.sendRemoteEvent(new UserCreateAndLoginReplyEvent(
									event.getOriginalEvent().getKey(), event
											.getOriginalEvent().getSender(),
									user, defaultAccount, event.getAccounts(),
									ok, event.getOriginalEvent()
											.getOriginalID(), message, event
											.getOriginalEvent().getTxId(), true));
					if (ok) {
						for (Account account : event.getAccounts()) {
							eventManager
									.sendRemoteEvent(new AccountUpdateEvent(
											event.getOriginalEvent().getKey(),
											null, account));
						}
					}
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return ok;
	}

	private boolean loginAndGetAccount(PmUserCreateAndLoginEvent event,
			UserKeeper userKeeper, AccountKeeper accountKeeper) {
		boolean ok = true;
		Account defaultAccount = null;
		List<Account> list = null;
		String message = "";
		User user = null;
		ErrorMessage msg = null;

		try {
			if (syncCentralDb
					&& !Strings.isNullOrEmpty(event.getOriginalEvent()
							.getThirdPartyId())) {

				// Old 3rd party id can't be bound with exist FDT id.
				if (centralDbConnector.isUserExistAndNotTerminated(event
						.getOriginalEvent().getThirdPartyId().toLowerCase())) {
					ok = false;
					msg = ErrorMessage.USER_ALREADY_EXIST;
					throw new UserException("This user already exists: "
							+ event.getOriginalEvent().getUser().getId());
				}

				// login (backward compatibility, old version skip this.)
				user = centralDbConnector.userLoginEx(event.getOriginalEvent()
						.getUser().getId(), event.getOriginalEvent().getUser()
						.getPassword());

				if (null == user) {
					ok = false;
					msg = ErrorMessage.INVALID_USER_ACCOUNT_PWD;
					throw new UserException("userid or password invalid");
				}

				if (user.getTerminationStatus().isTerminated()) {
					ok = false;
					msg = ErrorMessage.USER_IS_TERMINATED;
					throw new UserException("User is terminated");
				}

				String userId = centralDbConnector.getUserIdFromThirdPartyId(
						event.getOriginalEvent().getThirdPartyId(), event
								.getOriginalEvent().getMarket(), event
								.getOriginalEvent().getLanguage());

				if (Strings.isNullOrEmpty(userId)) {

					if (!centralDbConnector.registerThirdPartyUser(event
							.getOriginalEvent().getUser().getId(), event
							.getOriginalEvent().getUser().getUserType(), event
							.getOriginalEvent().getThirdPartyId(), event
							.getOriginalEvent().getMarket(), event
							.getOriginalEvent().getLanguage())) {

						ok = false;
						msg = ErrorMessage.THIRD_PARTY_ID_REGISTER_FAILED;
						throw new UserException(
								"Register third party id failed");
					}

				} else {
					if (!userId.equals(event.getOriginalEvent().getUser()
							.getId())) {

						ok = false;
						msg = ErrorMessage.THIRD_PARTY_ID_NOT_MATCH_USER_ID;
						throw new UserException(
								"Third party id is not match with the user id");
					}
				}
			}

			// getAccount
			user = userKeeper.getUser(event.getOriginalEvent().getUser()
					.getId());

			if (null != user.getDefaultAccount()
					&& !user.getDefaultAccount().isEmpty()) {
				defaultAccount = accountKeeper.getAccount(user
						.getDefaultAccount());
			}

			list = accountKeeper.getAccounts(event.getOriginalEvent().getUser()
					.getId());

			if (defaultAccount == null && (list == null || list.size() <= 0)) {
				ok = false;
				message = MessageLookup.buildEventMessage(
						ErrorMessage.NO_TRADING_ACCOUNT,
						"No trading account available for this user");
			}

		} catch (Exception ue) {

			if (ue instanceof UserException) {
				log.warn(ue.getMessage(), ue);
				message = MessageLookup.buildEventMessage(msg, ue.getMessage());
			} else {
				log.error(ue.getMessage(), ue);
				message = MessageLookup.buildEventMessage(
						ErrorMessage.USER_LOGIN_FAILED, ue.getMessage());
			}
		}

		try {
			eventManager.sendRemoteEvent(new UserCreateAndLoginReplyEvent(event
					.getOriginalEvent().getKey(), event.getOriginalEvent()
					.getSender(), user, defaultAccount, list, ok, event
					.getOriginalEvent().getOriginalID(), message, event
					.getOriginalEvent().getTxId(), false));
			if (ok) {
				user.setLastLogin(Clock.getInstance().now());
				eventManager.sendEvent(new PmUpdateUserEvent(
						PersistenceManager.ID, null, user));
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return ok;
	}

	private boolean loginFromThirdPartyIdAndGetAccount(
			PmUserCreateAndLoginEvent event, UserKeeper userKeeper,
			AccountKeeper accountKeeper) {
		boolean ok = true;
		Account defaultAccount = null;
		List<Account> list = null;
		String message = "";
		User user = null;
		ErrorMessage msg = null;

		try {

			if (syncCentralDb) {
				String userId = centralDbConnector.getUserIdFromThirdPartyId(
						event.getOriginalEvent().getThirdPartyId(), event
								.getOriginalEvent().getMarket(), event
								.getOriginalEvent().getLanguage());

				if (Strings.isNullOrEmpty(userId)
						&& /* phase 1 */!centralDbConnector.isUserExist(event
								.getOriginalEvent().getThirdPartyId()
								.toLowerCase())) {

					ok = false;
					msg = ErrorMessage.INVALID_USER_ACCOUNT_PWD;
					throw new UserException("userid or password invalid");
				}

				/* phase 1 */
				if (Strings.isNullOrEmpty(userId)) {
					userId = event.getOriginalEvent().getThirdPartyId();
				}

				event.getOriginalEvent().getUser().setId(userId);

				// Get user from MySQL and check termination
				user = centralDbConnector.getUser(userId);

				if (null == user || user.getTerminationStatus().isTerminated()) {
					ok = false;
					msg = ErrorMessage.USER_IS_TERMINATED;
					throw new UserException("User is terminated");
				}
			}

			// Get account
			user = userKeeper.getUser(event.getOriginalEvent().getUser()
					.getId());

			if (null != user.getDefaultAccount()
					&& !user.getDefaultAccount().isEmpty()) {
				defaultAccount = accountKeeper.getAccount(user
						.getDefaultAccount());
			}

			list = accountKeeper.getAccounts(event.getOriginalEvent().getUser()
					.getId());

			if (defaultAccount == null && (list == null || list.size() <= 0)) {
				ok = false;
				message = MessageLookup.buildEventMessage(
						ErrorMessage.NO_TRADING_ACCOUNT,
						"No trading account available for this user");
			}

		} catch (Exception ue) {

			if (ue instanceof UserException) {
				log.warn(ue.getMessage(), ue);
				message = MessageLookup.buildEventMessage(msg, ue.getMessage());
			} else {
				log.error(ue.getMessage(), ue);
				message = MessageLookup.buildEventMessage(
						ErrorMessage.USER_LOGIN_FAILED, ue.getMessage());
			}
		}

		try {
			eventManager.sendRemoteEvent(new UserCreateAndLoginReplyEvent(event
					.getOriginalEvent().getKey(), event.getOriginalEvent()
					.getSender(), user, defaultAccount, list, ok, event
					.getOriginalEvent().getOriginalID(), message, event
					.getOriginalEvent().getTxId(), false));
			if (ok) {
				user.setLastLogin(Clock.getInstance().now());
				eventManager.sendEvent(new PmUpdateUserEvent(
						PersistenceManager.ID, null, user));
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return ok;
	}

	private void createCentralDbUser(PmUserCreateAndLoginEvent event,
			User user, boolean isTransfer) throws CentralDbException {

		if (!syncCentralDb) {
			return;
		}

		if (!centralDbConnector.isUserExist(user.getId())) { // user dose not
																// exist in
																// Mysql either

			if (checkEmailUnique.equals(CheckEmailType.allCheck)
					|| (checkEmailUnique.equals(CheckEmailType.onlyExist) && !Strings
							.isNullOrEmpty(user.getEmail()))) {

				if (centralDbConnector.isEmailExist(user.getEmail())) {
					if (!isTransfer) {
						throw new CentralDbException(
								"Your "
										+ user.getUserType().name()
										+ " account email has been used to register an FDT Account. Please login it with "
										+ user.getEmail() + " now.",
								ErrorMessage.CREATE_USER_FAILED);
					}
				}
			}

			if (checkPhoneUnique == CheckPhoneType.allCheck
					|| (checkPhoneUnique == CheckPhoneType.onlyExist && !Strings
							.isNullOrEmpty(user.getPhone()))) {

				if (centralDbConnector.isPhoneExist(user.getPhone())) {
					if (!isTransfer) {
						throw new CentralDbException(
								"Your phone has been used to register an FDT Account.",
								ErrorMessage.USER_PHONE_EXIST);
					}
				}
			}

			if (!centralDbConnector.registerUser(user.getId(), user.getName(),
					user.getPassword(), isTransfer ? "" : user.getEmail(), user
							.getPhone(), user.getUserType(), event
							.getOriginalEvent().getCountry(), event
							.getOriginalEvent().getLanguage(), event
							.getOriginalEvent().getThirdPartyId(), event
							.getOriginalEvent().getMarket())) {

				throw new CentralDbException("can't create this user: "
						+ user.getId(), ErrorMessage.CREATE_USER_FAILED);
			}
		}
	}

	public void processSignalEvent(SignalEvent event) {
		persistXml(event.getKey(), PersistType.SIGNAL, StrategyState.Running,
				null, null, null, event.getSignal().toCompactXML());
	}

	public void processCancelSignalEvent(CancelSignalEvent event) {
		log.info("Deleting signal: " + event.getKey());
		deleteXml(event.getKey(), PersistType.SIGNAL);
	}

	public void processUpdateParentOrderEvent(UpdateParentOrderEvent event) {
		ParentOrder order = event.getParent();
		StrategyState state = order.getState();
		persistXml(order.getId(), PersistType.SINGLE_ORDER_STRATEGY, state,
				order.getUser(), order.getAccount(), order.getRoute(),
				order.toCompactXML());
	}

	public void processMultiInstrumentStrategyUpdateEvent(
			MultiInstrumentStrategyUpdateEvent event) {
		MultiInstrumentStrategyData data = event.getStrategyData();
		StrategyState state = data.getState();
		persistXml(data.getId(), PersistType.MULTI_INSTRUMENT_STRATEGY, state,
				data.getUser(), data.getAccount(), data.getRoute(),
				data.toCompactXML());
	}

	public void processSingleInstrumentStrategyUpdateEvent(
			SingleInstrumentStrategyUpdateEvent event) {
		Instrument data = event.getInstrument();
		StrategyState state = data.getState();
		persistXml(data.getId(), PersistType.SINGLE_INSTRUMENT_STRATEGY, state,
				data.getUser(), data.getAccount(), data.getRoute(),
				data.toCompactXML());
	}

	public void processUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		Session session = sessionFactory.openSession();
		ChildOrder order = event.getOrder().clone();
		ChildOrderAudit audit = new ChildOrderAudit(event.getExecType(), order);
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			if (order.getOrdStatus().isCompleted()) {
				session.delete(order);
			} else {
				session.saveOrUpdate(order);
			}
			session.save(audit);

			if (event.getExecution() != null) {
				session.save(event.getExecution());
			}
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processPmDeleteGroupManagementEvent(
			PmDeleteGroupManagementEvent event) {

		List<GroupManagement> groups = event.getGroupManagementList();

		if (null == groups || groups.isEmpty()) {
			log.error("GroupManagement List is null");
			return;
		}

		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for (GroupManagement group : groups) {
				session.delete(group);
			}
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmCreateGroupManagementEvent(
			PmCreateGroupManagementEvent event) {

		List<GroupManagement> groups = event.getGroupManagementList();

		if (null == groups || groups.isEmpty()) {
			log.error("GroupManagement List is null");
			return;
		}

		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for (GroupManagement group : groups) {
				session.save(group);
			}
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmExchangeAccountInsertEvent(
			PmExchangeAccountInsertEvent event) {
		ExchangeAccount exchangeAccount = event.getExchangeAccount();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(exchangeAccount);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmExchangeAccountUpdateEvent(
			PmExchangeAccountUpdateEvent event) {
		ExchangeAccount exchangeAccount = event.getExchangeAccount();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(exchangeAccount);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmExchangeAccountDeleteEvent(
			PmExchangeAccountDeleteEvent event) {
		ExchangeAccount exchangeAccount = event.getExchangeAccount();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(exchangeAccount);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmExchangeSubAccountInsertEvent(
			PmExchangeSubAccountInsertEvent event) {
		ExchangeSubAccount exchangeSubAccount = event.getExchangeSubAccount();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(exchangeSubAccount);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmExchangeSubAccountUpdateEvent(
			PmExchangeSubAccountUpdateEvent event) {
		ExchangeSubAccount exchangeSubAccount = event.getExchangeSubAccount();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(exchangeSubAccount);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmExchangeSubAccountDeleteEvent(
			PmExchangeSubAccountDeleteEvent event) {
		ExchangeSubAccount exchangeSubAccount = event.getExchangeSubAccount();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(exchangeSubAccount);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmInstrumentPoolInsertEvent(
			PmInstrumentPoolInsertEvent event) {
		InstrumentPool instrumentPool = event.getInstrumentPool();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(instrumentPool);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmInstrumentPoolDeleteEvent(
			PmInstrumentPoolDeleteEvent event) {
		InstrumentPool instrumentPool = event.getInstrumentPool();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(instrumentPool);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmInstrumentPoolRecordsInsertEvent(
			PmInstrumentPoolRecordsInsertEvent event) {
		List<InstrumentPoolRecord> records = event.getInstrumentPoolRecords();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			if (!records.isEmpty()) {
				tx = session.beginTransaction();
				for (InstrumentPoolRecord record : records) {
					session.save(record);
				}
				tx.commit();
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmInstrumentPoolRecordsDeleteEvent(
			PmInstrumentPoolRecordsDeleteEvent event) {
		List<InstrumentPoolRecord> records = event.getInstrumentPoolRecords();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			if (!records.isEmpty()) {
				tx = session.beginTransaction();
				for (InstrumentPoolRecord record : records) {
					session.delete(record);
				}
				tx.commit();
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmInstrumentPoolRecordUpdateEvent(
			PmInstrumentPoolRecordUpdateEvent event) {
		InstrumentPoolRecord record = event.getInstrumentPoolRecord();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(record);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmAccountPoolsInsertEvent(PmAccountPoolsInsertEvent event) {
		List<AccountPool> accountPools = event.getAccountPools();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			if (!accountPools.isEmpty()) {
				tx = session.beginTransaction();
				for (AccountPool accountPool : accountPools) {
					session.save(accountPool);
				}
				tx.commit();
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmUserExchangeSubAccountInsertEvent(
			PmUserExchangeSubAccountInsertEvent event) {
		List<UserExchangeSubAccount> userExchangeSubAccounts = event
				.getUserExchangeSubAccounts();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			if (!userExchangeSubAccounts.isEmpty()) {
				tx = session.beginTransaction();
				for (UserExchangeSubAccount userExchangeSubAccount : userExchangeSubAccounts) {
					session.save(userExchangeSubAccount);
				}
				tx.commit();
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmUserExchangeSubAccountDeleteEvent(
			PmUserExchangeSubAccountDeleteEvent event) {
		List<UserExchangeSubAccount> userExchangeSubAccounts = event
				.getUserExchangeSubAccounts();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			if (!userExchangeSubAccounts.isEmpty()) {
				tx = session.beginTransaction();
				for (UserExchangeSubAccount userExchangeSubAccount : userExchangeSubAccounts) {
					session.delete(userExchangeSubAccount);
				}
				tx.commit();
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	public void processPmAccountPoolsDeleteEvent(PmAccountPoolsDeleteEvent event) {
		List<AccountPool> accountPools = event.getAccountPools();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			if (!accountPools.isEmpty()) {
				tx = session.beginTransaction();
				for (AccountPool accountPool : accountPools) {
					session.delete(accountPool);
				}
				tx.commit();
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<User> recoverUsers() {
		Session session = sessionFactory.openSession();
		List<User> result = new ArrayList<User>();
		try {
			result = session.createCriteria(User.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Account> recoverAccounts() {
		Session session = sessionFactory.openSession();
		List<Account> result = new ArrayList<Account>();
		try {
			result = session.createCriteria(Account.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<AccountSetting> recoverAccountSettings() {
		Session session = sessionFactory.openSession();
		List<AccountSetting> result = new ArrayList<AccountSetting>();
		try {
			result = session.createCriteria(AccountSetting.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<OpenPosition> recoverOpenPositions() {
		Session session = sessionFactory.openSession();
		List<OpenPosition> result = new ArrayList<OpenPosition>();
		try {
			result = session.createCriteria(OpenPosition.class)
					.addOrder(Order.asc("created"))
					// .add(Restrictions.eq("class", OpenPosition.class)) not
					// working!!!
					.list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<ClosedPosition> recoverClosedPositions(boolean todayOnly) {
		Session session = sessionFactory.openSession();
		List<ClosedPosition> result = new ArrayList<ClosedPosition>();
		try {
			result = session
					.createCriteria(ClosedPosition.class)
					.add(Restrictions.gt(
							"created",
							todayOnly ? TimeUtil.getOnlyDate(Clock
									.getInstance().now()) : new Date(0)))
					.addOrder(Order.asc("created")).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Execution> recoverExecutions(boolean todayOnly) {
		Session session = sessionFactory.openSession();
		List<Execution> result = new ArrayList<Execution>();
		try {
			result = session
					.createCriteria(Execution.class)
					.add(Restrictions.eq("serverId", IdGenerator.getInstance()
							.getSystemId()))
					.add(Restrictions.gt(
							"created",
							this.todayOnly || todayOnly ? TimeUtil
									.getOnlyDate(Clock.getInstance().now())
									: new Date(0)))
					.addOrder(Order.asc("created")).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<ExchangeAccount> recoverExchangeAccounts() {
		Session session = sessionFactory.openSession();
		List<ExchangeAccount> result = new ArrayList<ExchangeAccount>();
		try {
			result = session.createCriteria(ExchangeAccount.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<ExchangeSubAccount> recoverExchangeSubAccounts() {
		Session session = sessionFactory.openSession();
		List<ExchangeSubAccount> result = new ArrayList<ExchangeSubAccount>();
		try {
			result = session.createCriteria(ExchangeSubAccount.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<InstrumentPool> recoverInstrumentPools() {
		Session session = sessionFactory.openSession();
		List<InstrumentPool> result = new ArrayList<InstrumentPool>();
		try {
			result = session.createCriteria(InstrumentPool.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<AccountPool> recoverAccountPools() {
		Session session = sessionFactory.openSession();
		List<AccountPool> result = new ArrayList<AccountPool>();
		try {
			result = session.createCriteria(AccountPool.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<UserExchangeSubAccount> recoverUserExchangeSubAccounts() {
		Session session = sessionFactory.openSession();
		List<UserExchangeSubAccount> result = new ArrayList<UserExchangeSubAccount>();
		try {
			result = session.createCriteria(UserExchangeSubAccount.class)
					.list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<InstrumentPoolRecord> recoverAccountPoolRecords() {
		Session session = sessionFactory.openSession();
		List<InstrumentPoolRecord> result = new ArrayList<InstrumentPoolRecord>();
		try {
			result = session.createCriteria(InstrumentPoolRecord.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	private void processTextObject(List<DataObject> result,
			List<String> toBeRemoved, DataObject dataObject) {
		result.add(dataObject);
		if (deleteTerminated) {
			StrategyState state = dataObject.get(StrategyState.class,
					OrderField.STATE.value());
			if (null != state && state.equals(StrategyState.Terminated)) {
				toBeRemoved.add(dataObject.get(String.class,
						OrderField.ID.value()));
			}
		}
	}

	private void processChildOrderAudit(List<DataObject> result,
			List<ChildOrderAudit> list) {
		// If object in list has duplicate id, keep the latest created one
		if (list != null && list.size() > 0) {
			ChildOrderAudit toAdd = list.get(0);
			for (int i = 1; i < list.size(); i++) {
				ChildOrderAudit c = list.get(i);
				if (!toAdd.getId().equals(c.getId())) {
					result.add(toAdd.clone());
					toAdd = c;
				} else {
					if (toAdd.getCreated().before(c.getCreated())) {
						toAdd = c;
					}
				}
			}
			result.add(toAdd);
		}
	}

	public List<DataObject> recoverObject(PersistType persistType,
			boolean todayOnly) {
		Session session = sessionFactory.openSession();

		List<DataObject> result = new ArrayList<DataObject>();
		List<String> toBeRemoved = new ArrayList<String>();
		try {
			@SuppressWarnings("unchecked")
			List<TextObject> list = session
					.createCriteria(TextObject.class)
					.add(Restrictions.eq("serverId", IdGenerator.getInstance()
							.getSystemId()))
					.add(Restrictions.eq("persistType", persistType))
					.add(Restrictions.gt(
							"timeStamp",
							this.todayOnly || todayOnly ? TimeUtil
									.getOnlyDate(Clock.getInstance().now())
									: new Date(0))).addOrder(Order.asc("id"))
					.addOrder(Order.asc("line")).list();

			String currentId = "";
			StringBuilder xml = new StringBuilder();
			for (TextObject obj : list) {
				if (!currentId.equals(obj.getId())) {
					if (xml.length() != 0) {
						DataObject dataObject = DataObject.fromString(
								DataObject.class, xml.toString());
						processTextObject(result, toBeRemoved, dataObject);
					}
					currentId = obj.getId();
					xml.setLength(0);
				}
				xml.append(obj.getXml());
			}
			if (xml.length() != 0) {
				DataObject dataObject = DataObject.fromString(DataObject.class,
						xml.toString());
				processTextObject(result, toBeRemoved, dataObject);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
		log.info("loaded " + persistType + " " + result.size());
		session = sessionFactory.openSession();
		try {
			for (String id : toBeRemoved) {
				Transaction tx = session.beginTransaction();
				@SuppressWarnings("unchecked")
				List<TextObject> list = session
						.createCriteria(TextObject.class)
						.add(Restrictions.eq("id", id)).list();

				for (TextObject obj : list) {
					session.delete(obj);
				}
				tx.commit();
			}
			log.info("Deleted " + persistType + " " + toBeRemoved.size()
					+ " terminated items");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}

		return result;
	}

	public List<DataObject> recoverChildOrderAudit(boolean todayOnly) {
		Session session = sessionFactory.openSession();
		List<DataObject> result = new ArrayList<>();
		try {
			@SuppressWarnings("unchecked")
			List<ChildOrderAudit> list = session
					.createCriteria(ChildOrderAudit.class)
					.add(Restrictions.eq("serverId", IdGenerator.getInstance()
							.getSystemId()))
					.add(Restrictions.gt(
							"created",
							this.todayOnly || todayOnly ? TimeUtil
									.getOnlyDate(Clock.getInstance().now())
									: new Date(0))).addOrder(Order.asc("id"))
					.addOrder(Order.asc("created")).list();
			processChildOrderAudit(result, list);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}

		return result;
	}

	public void processPmPositionPeakPriceDeleteEvent(
			PmPositionPeakPriceDeleteEvent event) {
		Session session = null;
		try {

			session = sessionFactory.openSession();

			PositionPeakPrice ppp = event.getItem();

			session.delete(ppp);

			session.flush();

		} catch (Exception e) {

			log.error(e.getMessage(), e);

		} finally {

			if (null != session) {
				session.close();
			}
		}
	}

	public void processPmPositionPeakPriceUpdateEvent(
			PmPositionPeakPriceUpdateEvent event) {
		Session session = null;
		Transaction tx = null;
		try {

			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			tx.begin();
			for (PositionPeakPrice ppp : event.getUpdates()) {
				session.saveOrUpdate(ppp);
			}
			tx.commit();

		} catch (Exception e) {

			log.error(e.getMessage(), e);

			if (null != tx) {
				tx.rollback();
			}

		} finally {

			if (null != session) {
				session.close();
			}
		}

	}

	@SuppressWarnings("unchecked")
	public List<PositionPeakPrice> recoverPositionPeakPrices() {

		List<PositionPeakPrice> result = new ArrayList<PositionPeakPrice>();
		Session session = null;
		try {

			session = sessionFactory.openSession();
			result = session.createCriteria(PositionPeakPrice.class).list();

		} catch (HibernateException e) {

			log.error(e.getMessage(), e);

		} finally {

			if (session != null) {
				session.close();
			}

		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<GroupManagement> recoverGroupManagement() {
		List<GroupManagement> result = new ArrayList<GroupManagement>();
		Session session = null;
		try {
			session = sessionFactory.openSession();
			result = session.createCriteria(GroupManagement.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return result;
	}

	public void processPmCreateCSTWUserEvent(PmCreateCSTWUserEvent event) {
		Session session = sessionFactory.openSession();
		User user = event.getUser();
		Transaction tx = null;
		boolean ok = true;
		String message = "";

		try {
			tx = session.beginTransaction();
			session.save(user);
			tx.commit();
			log.info("Created CSTW user: " + event.getUser());
		} catch (Exception e) {

			message = MessageLookup.buildEventMessage(
					ErrorMessage.CREATE_USER_FAILED, String.format(
							"can't create user, err=[%s]", e.getMessage()));
			log.error(e.getMessage(), e);
			ok = false;
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}

		if (ok) {

			eventManager.sendEvent(new OnUserCreatedEvent(user, null));
			try {
				eventManager.sendRemoteEvent(new UserUpdateEvent(null, null,
						user));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		if (event.getOriginalEvent() != null) {
			try {
				eventManager.sendRemoteEvent(new CreateUserReplyEvent(event
						.getOriginalEvent().getKey(), event.getOriginalEvent()
						.getSender(), user, ok, message, event
						.getOriginalEvent().getTxId()));

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void processPmCreateUserEvent(PmCreateUserEvent event) {
		Session session = sessionFactory.openSession();
		User user = event.getUser();
		Transaction tx = null;
		boolean ok = true;
		String message = "";

		try {
			if (syncCentralDb) {
				if (centralDbConnector.isUserExist(user.getId())) {
					throw new CentralDbException("This user already exists: "
							+ user.getId(), ErrorMessage.USER_ALREADY_EXIST);
				}

				if (checkEmailUnique.equals(CheckEmailType.allCheck)
						|| (checkEmailUnique.equals(CheckEmailType.onlyExist)
								&& null != user.getEmail() && !user.getEmail()
								.isEmpty())) {
					if (centralDbConnector.isEmailExist(user.getEmail())) {
						throw new CentralDbException(
								"This email already exists: " + user.getEmail(),
								ErrorMessage.USER_EMAIL_EXIST);
					}
				}

				if (checkPhoneUnique == CheckPhoneType.allCheck
						|| (checkPhoneUnique == CheckPhoneType.onlyExist && !Strings
								.isNullOrEmpty(user.getPhone()))) {

					if (centralDbConnector.isPhoneExist(user.getPhone())) {
						throw new CentralDbException(
								"This phone already exists: " + user.getPhone(),
								ErrorMessage.USER_PHONE_EXIST);
					}
				}

				if (!centralDbConnector.registerUser(user.getId(), user
						.getName(), user.getPassword(), user.getEmail(), user
						.getPhone(), user.getUserType(), event
						.getOriginalEvent().getCountry(), event
						.getOriginalEvent().getLanguage())) {
					throw new CentralDbException("can't create this user: "
							+ user.getId(),
							ErrorMessage.CREATE_DEFAULT_ACCOUNT_ERROR);
				}
			}

			tx = session.beginTransaction();
			session.save(user);
			tx.commit();
			log.info("Created user: " + event.getUser());
		} catch (Exception e) {
			if (e instanceof CentralDbException) {
				log.warn(e.getMessage(), e);
				message = MessageLookup.buildEventMessage(
						((CentralDbException) e).getClientMessage(),
						String.format("can't create user, err=[%s]",
								e.getMessage()));
			} else {
				message = MessageLookup.buildEventMessage(
						ErrorMessage.CREATE_USER_FAILED,
						String.format("can't create user, err=[%s]",
								e.getMessage()));
				log.error(e.getMessage(), e);
			}
			ok = false;
			// message = String.format("can't create user, err=[%s]",
			// e.getMessage());
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}

		if (ok) {
			for (Account account : event.getAccounts()) {
				createAccount(account);
			}
			eventManager.sendEvent(new OnUserCreatedEvent(user, event
					.getAccounts()));

			try {
				eventManager.sendRemoteEvent(new UserUpdateEvent(null, null,
						user));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		if (event.getOriginalEvent() != null) {
			try {
				eventManager.sendRemoteEvent(new CreateUserReplyEvent(event
						.getOriginalEvent().getKey(), event.getOriginalEvent()
						.getSender(), user, ok, message, event
						.getOriginalEvent().getTxId()));
				if (ok) {
					for (Account account : event.getAccounts()) {
						eventManager.sendRemoteEvent(new AccountUpdateEvent(
								event.getOriginalEvent().getKey(), null,
								account));
					}
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void processPmUpdateUserEvent(PmUpdateUserEvent event) {
		Session session = sessionFactory.openSession();
		User user = event.getUser();
		Transaction tx = null;
		boolean isOk = true;

		try {
			tx = session.beginTransaction();
			session.update(user);
			tx.commit();
		} catch (Exception e) {
			isOk = false;
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}

		if (isOk) {
			try {
				eventManager.sendRemoteEvent(new UserUpdateEvent(null, null,
						user));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void processPmAddCashEvent(PmAddCashEvent event) {
		double cashDeposited = event.getAccount().getCashDeposited();
		double cash = event.getCash();
		log.info("process PmAddCashEvent, account: {}, cash: {}", event
				.getAccount().getId(), cash);
		Session session = sessionFactory.openSession();
		Transaction tx = null;

		CashAudit cashAudit = new CashAudit(IdGenerator.getInstance()
				.getNextID(), event.getAccount().getId(), event.getType(),
				Clock.getInstance().now(), cashDeposited, cash);
		try {
			tx = session.beginTransaction();
			session.save(cashAudit);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processPmCreateAccountEvent(PmCreateAccountEvent event) {
		Account account = event.getAccount();
		createAccount(account);
	}

	protected void createAccount(Account account) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(account);
			tx.commit();
			log.debug("Persisted account=[" + account.getUserId() + ":"
					+ account.getId() + "]");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processPmUpdateAccountEvent(PmUpdateAccountEvent event) {
		Session session = sessionFactory.openSession();
		Account account = event.getAccount();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(account);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processPmUpdateDetailOpenPositionEvent(
			PmUpdateDetailOpenPositionEvent event) {
		Session session = sessionFactory.openSession();
		OpenPosition position = event.getPosition();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(position);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processPmRemoveDetailOpenPositionEvent(
			PmRemoveDetailOpenPositionEvent event) {
		Session session = sessionFactory.openSession();
		OpenPosition position = event.getPosition();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(position);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
		Session session = sessionFactory.openSession();
		ClosedPosition position = event.getPosition();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(position);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}

	}

	public void processPmChangeAccountSettingEvent(
			PmChangeAccountSettingEvent event) {
		Session session = sessionFactory.openSession();
		AccountSetting accountSetting = event.getAccountSetting();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(accountSetting);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}

	}

	public void processPmEndOfDayRollEvent(PmEndOfDayRollEvent event) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query;
			query = session.getNamedQuery("rollEndOfDay2");
			query.setParameter("tradeDate", event.getTradeDateTime());
			query.executeUpdate();
			query = session.getNamedQuery("rollEndOfDay3");
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
		log.info("Account day end processing end");
	}

	public void processChangeUserPasswordEvent(ChangeUserPasswordEvent event) {
		boolean ok = false;
		String message = "";

		try {
			if (!syncCentralDb
					|| centralDbConnector
							.changePassword(event.getUser(),
									event.getOriginalPassword(),
									event.getNewPassword())) {
				ok = true;
				log.info("Change password, user: " + event.getUser());
			} else {
				// message = "can't change user's password";
				message = MessageLookup.buildEventMessage(
						ErrorMessage.CHANGE_USER_PWD_FAILED,
						"can't change user's password");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ok = false;
			// message = String.format("can't change user's password, err=[%s]",
			// e.getMessage());
			message = MessageLookup.buildEventMessage(
					ErrorMessage.CHANGE_USER_PWD_FAILED,
					String.format("can't change user's password, err=[%s]",
							e.getMessage()));

		}

		try {
			eventManager.sendRemoteEvent(new ChangeUserPasswordReplyEvent(event
					.getKey(), event.getSender(), event.getUser(), ok, message,
					event.getTxId()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processUserTerminateEvent(UserTerminateEvent event) {

		boolean ok = false;
		String message = "";

		try {

			if (!syncCentralDb
					|| centralDbConnector.changeTermination(event.getUserId(),
							event.getTerminationStatus())) {

				ok = true;
				log.info(
						"Change user termination status, user: {} terminate: {}",
						event.getUserId(), event.getTerminationStatus());

			} else {
				MessageLookup.buildEventMessage(
						ErrorMessage.TERMINATE_USER_FAILED,
						String.format("Can't change user termination status"));
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ok = false;
			message = MessageLookup.buildEventMessage(
					ErrorMessage.TERMINATE_USER_FAILED, String.format(
							"Can't change user termination status, err=[%s]",
							e.getMessage()));
		}

		try {
			eventManager.sendRemoteEvent(new UserTerminateReplyEvent(event
					.getKey(), event.getSender(), ok, message, event
					.getUserId(), event.getTerminationStatus()));
			eventManager.sendRemoteEvent(new UserTerminateUpdateEvent(event
					.getKey(), null, event.getUserId(), event
					.getTerminationStatus()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processUserMappingEvent(UserMappingEvent event) {

		if (!syncCentralDb) {
			return;
		}

		boolean userExist = false;
		boolean userThirdPartyExist = false;
		boolean isPendingTransfer = false;
		boolean isOldThirdPartyUser = false;
		String userId = event.getUser();
		String email = "";

		if (!Strings.isNullOrEmpty(event.getUser())) {
			userExist = centralDbConnector.isUserExist(event.getUser()
					.toLowerCase());
		}

		if (!Strings.isNullOrEmpty(event.getUserThirdParty())) {
			userThirdPartyExist = centralDbConnector.isThirdPartyUserExist(
					event.getUserThirdParty().toLowerCase(), event.getMarket(),
					event.getLanguage());

			if (userThirdPartyExist
					&& centralDbConnector.isThirdPartyUserPendingTransfer(event
							.getUserThirdParty().toLowerCase())) {
				isPendingTransfer = true;
			}

			if (userThirdPartyExist && Strings.isNullOrEmpty(event.getUser())) {

				userId = centralDbConnector.getUserIdFromThirdPartyId(
						event.getUserThirdParty(), event.getMarket(),
						event.getLanguage());
				userExist = true;
			}

			if (!userThirdPartyExist
					&& centralDbConnector.isUserExistAndNotTerminated(event
							.getUserThirdParty().toLowerCase())) {

				isOldThirdPartyUser = true;
				email = centralDbConnector.getUser(
						event.getUserThirdParty().toLowerCase()).getEmail();
			}
		}

		try {
			UserMappingReplyEvent reply = new UserMappingReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(), userId,
					event.getUserThirdParty(), userExist, userThirdPartyExist,
					event.getMarket(), event.getLanguage(),
					event.getClientId(), email);
			reply.setTransferring(isPendingTransfer);
			reply.setOldThirdPartyUser(isOldThirdPartyUser);

			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processUserMappingListEvent(UserMappingListEvent event) {

		if (!syncCentralDb) {
			return;
		}

		try {
			List<ThirdPartyUser> thirdPartyUsers = centralDbConnector
					.getThirdPartyUsers(event.getUser(), event.getMarket(),
							event.getLanguage());

			UserMappingListReplyEvent reply = new UserMappingListReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(),
					event.getUser(), event.getMarket(), event.getLanguage(),
					thirdPartyUsers);

			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processUserMappingDetachEvent(UserMappingDetachEvent event) {

		if (!syncCentralDb) {
			return;
		}

		boolean ok = false;
		String message = "";

		if (!event.isAttach()) {
			// detach
			try {
				if (centralDbConnector.detachThirdPartyUser(event.getUser(),
						event.getPassword(), event.getUserThirdParty(),
						event.getMarket(), event.getLanguage())) {

					ok = true;
					log.info("Detach third party id, {}", event.toString());
				} else {
					MessageLookup.buildEventMessage(
							ErrorMessage.DETACH_THIRD_PARTY_ID_FAILED,
							String.format("Can't detach third party id"));
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				ok = false;
				message = MessageLookup.buildEventMessage(
						ErrorMessage.DETACH_THIRD_PARTY_ID_FAILED,
						String.format("Can't detach third party id, err=[%s]",
								e.getMessage()));
			}
		} else {
			// attach
			try {
				if (centralDbConnector.registerThirdPartyUser(event.getUser(),
						event.getUserType(), event.getUserThirdParty(),
						event.getMarket(), event.getLanguage())) {

					ok = true;
					log.info("Attach third party id, {}", event.toString());
				} else {
					ok = false;
					message = MessageLookup.buildEventMessage(
							ErrorMessage.ATTACH_THIRD_PARTY_ID_FAILED,
							String.format("Can't attach third party id"));
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				ok = false;
				message = MessageLookup.buildEventMessage(
						ErrorMessage.ATTACH_THIRD_PARTY_ID_FAILED,
						String.format("Can't attach third party id, err=[%s]",
								e.getMessage()));
			}
		}

		try {
			eventManager.sendRemoteEvent(new UserMappingDetachReplyEvent(event
					.getKey(), event.getSender(), ok, message, event.getTxId(),
					event.getUser(), event.getUserThirdParty(), event
							.getMarket(), event.getLanguage(),
					event.isAttach(), event.getUserType()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<CoinControl> recoverCoinControl() {
		List<CoinControl> result = new ArrayList<CoinControl>();
		Session session = null;
		try {
			session = sessionFactory.openSession();
			result = session.createCriteria(CoinControl.class).list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return result;
	}

	public void processPmUpdateCoinControlEvent(PmUpdateCoinControlEvent event) {

		CoinControl coinControl = event.getCoinControl();
		if (null == coinControl) {
			log.warn("coin control is null");
			return;
		}

		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {

			tx = session.beginTransaction();
			session.saveOrUpdate(coinControl);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			session.close();
		}
	}

	// getters and setters
	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public boolean isCleanStart() {
		return cleanStart;
	}

	public void setCleanStart(boolean cleanStart) {
		this.cleanStart = cleanStart;
	}

	public boolean isTodayOnly() {
		return todayOnly;
	}

	public void setTodayOnly(boolean todayOnly) {
		this.todayOnly = todayOnly;
	}

	public boolean isDeleteTerminated() {
		return deleteTerminated;
	}

	public void setDeleteTerminated(boolean deleteTerminated) {
		this.deleteTerminated = deleteTerminated;
	}

	public boolean isPersistSignal() {
		return persistSignal;
	}

	public void setPersistSignal(boolean persistSignal) {
		this.persistSignal = persistSignal;
	}

	public boolean isEmbeddedSQLServer() {
		return embeddedSQLServer;
	}

	public void setEmbeddedSQLServer(boolean embeddedSQLServer) {
		this.embeddedSQLServer = embeddedSQLServer;
	}

	public boolean isSyncCentralDb() {
		return this.syncCentralDb;
	}

	public void setSyncCentralDb(boolean syncCentralDb) {
		this.syncCentralDb = syncCentralDb;
	}

	public boolean isUseLtsGateway() {
		return useLtsGateway;
	}

	public void setUseLtsGateway(boolean useLtsGateway) {
		this.useLtsGateway = useLtsGateway;
	}

	public String getEmbeddedHost() {
		return embeddedHost;
	}

	public void setEmbeddedHost(String embeddedHost) {
		this.embeddedHost = embeddedHost;
	}

	public int getEmbeddedPort() {
		return embeddedPort;
	}

	public void setEmbeddedPort(int embeddedPort) {
		this.embeddedPort = embeddedPort;
	}

	public CheckEmailType isCheckEmailUnique() {
		return checkEmailUnique;
	}

	public void setCheckEmailUnique(CheckEmailType checkEmailUnique) {
		this.checkEmailUnique = checkEmailUnique;
	}

	public CheckPhoneType getCheckPhoneUnique() {
		return checkPhoneUnique;
	}

	public void setCheckPhoneUnique(CheckPhoneType checkPhoneUnique) {
		this.checkPhoneUnique = checkPhoneUnique;
	}

	public long getPurgeOrderDays() {
		return purgeOrderDays;
	}

	public void setPurgeOrderDays(long purgeOrderDays) {
		this.purgeOrderDays = purgeOrderDays;
	}

}
