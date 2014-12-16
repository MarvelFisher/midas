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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.ChangeUserPasswordEvent;
import com.cyanspring.common.event.account.ChangeUserPasswordReplyEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.CreateUserReplyEvent;
import com.cyanspring.common.event.account.PmChangeAccountSettingEvent;
import com.cyanspring.common.event.account.PmCreateAccountEvent;
import com.cyanspring.common.event.account.PmCreateUserEvent;
import com.cyanspring.common.event.account.PmEndOfDayRollEvent;
import com.cyanspring.common.event.account.PmRemoveDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.PmUpdateDetailOpenPositionEvent;
import com.cyanspring.common.event.account.PmUpdateUserEvent;
import com.cyanspring.common.event.account.PmUserLoginEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.event.signal.CancelSignalEvent;
import com.cyanspring.common.event.signal.SignalEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyUpdateEvent;
import com.cyanspring.common.type.PersistType;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.server.account.AccountKeeper;
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
	CentralDbConnector centralDbConnector;
	
	private boolean syncCentralDb = true;
	private boolean embeddedSQLServer;
	private int textSize = 4000;
	private boolean cleanStart;
	private boolean todayOnly;
	private boolean deleteTerminated = true;
	protected boolean persistSignal;
	NetworkServerControl server;
	private String embeddedHost = "localhost";
	private int embeddedPort = 1527;
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(UpdateParentOrderEvent.class, null);
			subscribeToEvent(UpdateChildOrderEvent.class, null);
			subscribeToEvent(SingleInstrumentStrategyUpdateEvent.class, null);
			subscribeToEvent(MultiInstrumentStrategyUpdateEvent.class, null);
			subscribeToEvent(PmCreateUserEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmUpdateUserEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmCreateAccountEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmUpdateAccountEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmRemoveDetailOpenPositionEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmUpdateDetailOpenPositionEvent.class, PersistenceManager.ID);
			subscribeToEvent(ClosedPositionUpdateEvent.class, null);
			subscribeToEvent(PmChangeAccountSettingEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmEndOfDayRollEvent.class, PersistenceManager.ID);
			subscribeToEvent(PmUserLoginEvent.class, PersistenceManager.ID);
			subscribeToEvent(ChangeUserPasswordEvent.class, null);

			if(persistSignal) {
				subscribeToEvent(SignalEvent.class, null);
				subscribeToEvent(CancelSignalEvent.class, null);
			}
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
		if(embeddedSQLServer)
			startEmbeddedSQLServer();
		
		if(cleanStart)
			truncateData(Clock.getInstance().now());
		else if (todayOnly)
			truncateData(TimeUtil.getOnlyDate(Clock.getInstance().now()));

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("PersistenceManager");
	}

	public void uninit() {
		log.info("uninitialising");
		eventProcessor.uninit();
		if(embeddedSQLServer)
			stopEmbeddedSQLServer();
	}
	
	private void startEmbeddedSQLServer() throws UnknownHostException, Exception {
		server = new NetworkServerControl
				(InetAddress.getByName(embeddedHost),embeddedPort);
		server.start(null);
		log.info("Embedded SQL server started");	
	}
	
	private void stopEmbeddedSQLServer() {
		try {
			server.shutdown();
			log.info("Embedded SQL server stopped");	
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void truncateData(Date date) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    String hql = "delete from User where created < :created";
		    Query query = session.createQuery(hql);
	        query.setParameter("created", date);
	        int rowCount = query.executeUpdate();
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
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        rowCount = query.executeUpdate();
	        log.debug("TextObject Records deleted: " + rowCount);
	        
	        hql = "delete from ChildOrder where created < :created and serverId = :serverId";
	        query = session.createQuery(hql);
	        query.setParameter("created", date);
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        rowCount = query.executeUpdate();
	        log.debug("ChildOrder Records deleted: " + rowCount);

	        hql = "delete from ChildOrderAudit where created < :created and serverId = :serverId";
	        query = session.createQuery(hql);
	        query.setParameter("created", date);
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        rowCount = query.executeUpdate();
	        log.debug("ChildOrderAudit Records deleted: " + rowCount);

	        hql = "delete from Execution where created < :created and serverId = :serverId";
	        query = session.createQuery(hql);
	        query.setParameter("created", date);
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        rowCount = query.executeUpdate();
	        log.debug("Execution Records deleted: " + rowCount);

	        tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}

	private void persistXml(String id, PersistType persistType, StrategyState state, String xml) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    @SuppressWarnings("unchecked")
			List<TextObject> list1 = (List<TextObject>)session.createCriteria(TextObject.class)
			    .add( Restrictions.eq("id", id ) )
			    .add(Restrictions.eq("persistType", persistType))
		    .list();
			
		    for(TextObject obj: list1) {
		    	session.delete(obj);
		    }
	        
	        List<TextObject> list2 = TextObject.createTextObjects(id, persistType, state, xml, textSize);
		    for(TextObject obj: list2) {
		    	session.save(obj);
		    }
	        
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	private void deleteXml(String id, PersistType persistType) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    @SuppressWarnings("unchecked")
			List<TextObject> list1 = (List<TextObject>)session.createCriteria(TextObject.class)
			    .add( Restrictions.eq("id", id ) )
			    .add( Restrictions.eq("persistType", persistType))
		    .list();
			
		    for(TextObject obj: list1) {
		    	session.delete(obj);
		    }
	        
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	public void processPmUserLoginEvent(PmUserLoginEvent event)
	{
		log.debug("Received PmUserLoginEvent: " + event.getOriginalEvent().getUserId());
		String userId = event.getOriginalEvent().getUserId().toLowerCase();
		UserKeeper userKeeper = (UserKeeper)event.getUserKeeper();
		AccountKeeper accountKeeper = (AccountKeeper)event.getAccountKeeper();
		boolean ok = false;
		String message = "";
		User user = null;
		Account defaultAccount = null;
		List<Account> list = null;
		if(null != userKeeper) {
			try 
			{
				if(!syncCentralDb || centralDbConnector.userLogin(userId, event.getOriginalEvent().getPassword()))
					ok = userKeeper.login(userId, event.getOriginalEvent().getPassword());

			} catch (UserException ue) {
				ue.printStackTrace();
				message = ue.getMessage();
			}
			
			if(ok) {
				user = userKeeper.getUser(userId);
				if(null != user.getDefaultAccount() && !user.getDefaultAccount().isEmpty()) {
					defaultAccount = accountKeeper.getAccount(user.getDefaultAccount());
				} 
				
				list = accountKeeper.getAccounts(userId);
				
				if(defaultAccount == null && (list == null || list.size() <= 0)) {
					ok = false;
					message = "No trading account available for this user";
				}
			}
		} else {
			ok = false;
			message = "Server is not set up for login";
		}
		
		try {
			eventManager.sendRemoteEvent(new UserLoginReplyEvent(event.getOriginalEvent().getKey(), 
					event.getOriginalEvent().getSender(), user, defaultAccount, list, ok, message, event.getOriginalEvent().getTxId()));
			
			if(ok) {
				user.setLastLogin(Clock.getInstance().now());
				eventManager.sendEvent(new PmUpdateUserEvent(PersistenceManager.ID, null, user));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void processSignalEvent(SignalEvent event) {
		persistXml(event.getKey(), PersistType.SIGNAL, StrategyState.Running, event.getSignal().toCompactXML());
	}
	
	public void processCancelSignalEvent(CancelSignalEvent event) {
		log.info("Deleting signal: " + event.getKey());
		deleteXml(event.getKey(), PersistType.SIGNAL);
	}
	
	public void processUpdateParentOrderEvent(UpdateParentOrderEvent event) {
		ParentOrder order = event.getParent();
		StrategyState state = order.getState();
		persistXml(order.getId(), PersistType.SINGLE_ORDER_STRATEGY, state, order.toCompactXML());
	}

	public void processMultiInstrumentStrategyUpdateEvent(MultiInstrumentStrategyUpdateEvent event) {
		MultiInstrumentStrategyData data = event.getStrategyData();
		StrategyState state = data.getState();
		persistXml(data.getId(), PersistType.MULTI_INSTRUMENT_STRATEGY, state, data.toCompactXML());
	}

	public void processSingleInstrumentStrategyUpdateEvent(SingleInstrumentStrategyUpdateEvent event) {
		Instrument data = event.getInstrument();
		StrategyState state = data.getState();
		persistXml(data.getId(), PersistType.SINGLE_INSTRUMENT_STRATEGY, state, data.toCompactXML());
	}

	public void processUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		Session session = sessionFactory.openSession();
		ChildOrder order = event.getOrder();
		ChildOrderAudit audit = new ChildOrderAudit(event.getExecType(), order);
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    if(order.getOrdStatus().isCompleted()) {
		    	session.delete(order);
		    } else {
		    	session.saveOrUpdate(order);
		    }
	    	session.save(audit);
	    	
	    	if(event.getExecution() != null) {
	    		session.save(event.getExecution());
	    	}
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<User> recoverUsers() {
		Session session = sessionFactory.openSession();
		List<User> result = new ArrayList<User>();
		try {
			result = (List<User>)session.createCriteria(User.class)
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
	public List<Account> recoverAccounts() {
		Session session = sessionFactory.openSession();
		List<Account> result = new ArrayList<Account>();
		try {
			result = (List<Account>)session.createCriteria(Account.class)
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
	public List<AccountSetting> recoverAccountSettings() {
		Session session = sessionFactory.openSession();
		List<AccountSetting> result = new ArrayList<AccountSetting>();
		try {
			result = (List<AccountSetting>)session.createCriteria(AccountSetting.class)
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
	public List<OpenPosition> recoverOpenPositions() {
		Session session = sessionFactory.openSession();
		List<OpenPosition> result = new ArrayList<OpenPosition>();
		try {
			result = (List<OpenPosition>)session.createCriteria(OpenPosition.class)
					.addOrder(Order.asc("created"))
				//.add(Restrictions.eq("class", OpenPosition.class)) not working!!!
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
	public List<ClosedPosition> recoverClosedPositions() {
		Session session = sessionFactory.openSession();
		List<ClosedPosition> result = new ArrayList<ClosedPosition>();
		try {
			result = (List<ClosedPosition>)session.createCriteria(ClosedPosition.class)
				.addOrder(Order.asc("created"))
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
	public List<Execution> recoverExecutions() {
		Session session = sessionFactory.openSession();
		List<Execution> result = new ArrayList<Execution>();
		try {
			result = (List<Execution>)session.createCriteria(Execution.class)
				.add( Restrictions.eq("serverId", IdGenerator.getInstance().getSystemId()))
				.add( Restrictions.gt("created", todayOnly?TimeUtil.getOnlyDate(Clock.getInstance().now()):new Date(0)))
				.addOrder(Order.asc("created"))
				.list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	private void processTextObject(List<DataObject> result, List<String> toBeRemoved, DataObject dataObject) {
		result.add(dataObject);
		if(deleteTerminated) {
			StrategyState state = dataObject.get(StrategyState.class, OrderField.STATE.value());
			if(null != state && state.equals(StrategyState.Terminated))
				toBeRemoved.add(dataObject.get(String.class, OrderField.ID.value()));
		}
	}
	
	public List<DataObject> recoverObject(PersistType persistType) {
		Session session = sessionFactory.openSession();
		
		List<DataObject> result = new ArrayList<DataObject>();
		List<String> toBeRemoved = new ArrayList<String>();
		try {
			@SuppressWarnings("unchecked")
			List<TextObject> list = (List<TextObject>)session.createCriteria(TextObject.class)
			.add(Restrictions.eq("serverId", IdGenerator.getInstance().getSystemId()))
			.add(Restrictions.eq("persistType", persistType))
			.add(Restrictions.gt("timeStamp", todayOnly?TimeUtil.getOnlyDate(Clock.getInstance().now()):new Date(0)))
			.addOrder( Order.asc("id") ) 
			.addOrder( Order.asc("line") ) 
			.list();

			String currentId = "";
			StringBuilder xml = new StringBuilder();
			for(TextObject obj: list) {
				if(!currentId.equals(obj.getId())) {
					if(xml.length() != 0) {
						DataObject dataObject = DataObject.fromString(DataObject.class, xml.toString());
						processTextObject(result, toBeRemoved, dataObject);
					}
					currentId = obj.getId();
					xml.setLength(0);
				}
				xml.append(obj.getXml());
			}
			if(xml.length() != 0) {
				DataObject dataObject = DataObject.fromString(DataObject.class, xml.toString());
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
			for(String id: toBeRemoved) {
				Transaction tx = session.beginTransaction();
			    @SuppressWarnings("unchecked")
				List<TextObject> list = (List<TextObject>)session.createCriteria(TextObject.class)
				    .add( Restrictions.eq("id", id ) )
			    .list();
				
			    for(TextObject obj: list) {
			    	session.delete(obj);
			    }
			    tx.commit();
			}
			log.info("Deleted " + persistType + " " + toBeRemoved.size() + " terminated items");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
		
		return result;
	}
	
	public void processPmCreateUserEvent(PmCreateUserEvent event) {
		Session session = sessionFactory.openSession();
		User user = event.getUser();
		Transaction tx = null;
		boolean ok = true;
		String message = "";
		
		try 
		{
			if(syncCentralDb)
			{
				if(centralDbConnector.isUserExist(user.getId()))
					throw new CentralDbException("This user already exists: " + user.getId());
				if(!centralDbConnector.registerUser(user.getId(), user.getName(), user.getPassword(), user.getEmail(), user.getPhone(), user.getUserType()))
					throw new CentralDbException("can't create this user: " + user.getId());
			}
			
			tx = session.beginTransaction();
			session.save(user);
			tx.commit();
			log.info("Created user: " + event.getUser());
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			ok = false;
			message = String.format("can't create user, err=[%s]", e.getMessage());
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
		
		for(Account account : event.getAccounts())
			createAccount(account);
		
		if(event.getOriginalEvent() != null)
		{
			try {
				eventManager.sendRemoteEvent(new CreateUserReplyEvent(event.getOriginalEvent().getKey(), 
						event.getOriginalEvent().getSender(), user, ok, message, event.getOriginalEvent().getTxId()));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void processPmUpdateUserEvent(PmUpdateUserEvent event) {
		Session session = sessionFactory.openSession();
		User user = event.getUser();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
	    	session.update(user);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	public void processPmCreateAccountEvent(PmCreateAccountEvent event) {
		Account account = event.getAccount();
		createAccount(account);
	}
	
	protected void createAccount(Account account)
	{
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
	    	session.save(account);
		    tx.commit();
		    log.debug("Persisted account=[" + account.getUserId() + ":" + account.getId() + "]");
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
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
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	public void processPmUpdateDetailOpenPositionEvent(PmUpdateDetailOpenPositionEvent event) {
		Session session = sessionFactory.openSession();
		OpenPosition position = event.getPosition();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
	    	session.saveOrUpdate(position);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	public void processPmRemoveDetailOpenPositionEvent(PmRemoveDetailOpenPositionEvent event) {
		Session session = sessionFactory.openSession();
		OpenPosition position = event.getPosition();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
	    	session.delete(position);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
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
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
		
	}

	public void processPmChangeAccountSettingEvent(PmChangeAccountSettingEvent event) {
		Session session = sessionFactory.openSession();
		AccountSetting accountSetting = event.getAccountSetting();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
	    	session.saveOrUpdate(accountSetting);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
		
	}
	
	public void processPmEndOfDayRollEvent(PmEndOfDayRollEvent event) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    Query query;
		    query = session.getNamedQuery("rollEndOfDay1");
	        query.executeUpdate();
		    query = session.getNamedQuery("rollEndOfDay2");
	        query.executeUpdate();
		    query = session.getNamedQuery("rollEndOfDay3");
	        query.executeUpdate();
	        tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	public void processChangeUserPasswordEvent(ChangeUserPasswordEvent event)
	{
		boolean ok = false;
		String message = "";
		
		try 
		{
			if(!syncCentralDb || centralDbConnector.changePassword(event.getUser(), event.getOriginalPassword(), event.getNewPassword()))
			{
				ok = true;
				log.info("Change password, user: " + event.getUser());
			}
			else
				message = "can't change user's password";
		}
		catch (Exception e) 
		{
			log.error(e.getMessage(), e);
			ok = false;
			message = String.format("can't change user's password, err=[%s]", e.getMessage());
		}
		
		try {
			eventManager.sendRemoteEvent(new ChangeUserPasswordReplyEvent(event.getKey(), 
					event.getSender(), event.getUser(), ok, message, event.getTxId()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
	
	public boolean isSyncCentralDb(){
		return this.syncCentralDb;
	}
	
	public void setSyncCentralDb(boolean syncCentralDb){
		this.syncCentralDb = syncCentralDb;
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
	
}
