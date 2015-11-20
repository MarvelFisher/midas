package com.cyanspring.info.alert;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.type.ExecType;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.alert.*;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.ResetAccountReplyEvent;
import com.cyanspring.common.event.account.ResetAccountReplyType;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.alert.AlertType;
import com.cyanspring.common.event.alert.PriceAlertReplyEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertReplyEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertRequestEvent;
import com.cyanspring.common.event.alert.QueryPriceAlertRequestEvent;
import com.cyanspring.common.event.alert.SetPriceAlertRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.info.alert.Compute;

public class AlertManager extends Compute {
	private static final Logger log = LoggerFactory
			.getLogger(AlertManager.class);

	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	ScheduleManager scheduleManager;
	
	private int maxNoOfAlerts;
    private String deepLink;
    private String market ;
	private boolean checkAlertstart = true;
    private Date LastHeartbeat;
    private int timerinterval;

	private Map<String, ArrayList<BasePriceAlert>> symbolPriceAlerts = new ConcurrentHashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<BasePriceAlert>> userPriceAlerts = new ConcurrentHashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<BasePriceAlert>> userPastPriceAlerts = new ConcurrentHashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<TradeAlert>> userTradeAlerts = new ConcurrentHashMap<String, ArrayList<TradeAlert>>();

	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	private Map<String, Object> userLocks = new ConcurrentHashMap<String, Object>();
	
	private AlertProcessThread alertProcessor;

	@Override
	public void SubscirbetoEvents() {
		SubscirbetoEvent(SetPriceAlertRequestEvent.class);
		SubscirbetoEvent(QueryPriceAlertRequestEvent.class);
		SubscirbetoEvent(QueryOrderAlertRequestEvent.class);
		SubscirbetoEvent(ResetAccountRequestEvent.class);
	}

	@Override
	public void SubscribetoEventsMD() {
		SubscirbetoEvent(ChildOrderUpdateEvent.class);
//        SubscirbetoEvent(ParentOrderUpdateEvent.class);
		SubscirbetoEvent(AsyncTimerEvent.class);
		SubscirbetoEvent(MarketSessionEvent.class);
		SubscirbetoEvent(QuoteEvent.class);
	}	

	@Override
	public void init() {
		// TODO Auto-generated method stub
		loadSQLdata();
		alertProcessor = new AlertProcessThread(this);
		alertProcessor.start();
        Calendar cal = Calendar.getInstance();
        LastHeartbeat = cal.getTime();
        setThreadName("AlertManager");
		AsyncTimerEvent SendSQLHeartTimer = new AsyncTimerEvent();
		SendSQLHeartTimer.setKey("SendSQLHeartTimer");
		scheduleManager.scheduleRepeatTimerEvent(getTimerinterval(), getEventProcessorMD(), SendSQLHeartTimer);
	}

	@Override
	public void processMarketSessionEvent(MarketSessionEvent event,
			List<Compute> computes) {
		log.info("[MarketSessionEvent] : " + event);
		MarketSessionType mst = event.getSession();
		if (MarketSessionType.PREMARKET == mst) {			
			checkAlertstart = true;
		} else if (MarketSessionType.CLOSE == mst) {
			checkAlertstart = false;
		}
	}

	@Override
	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event,
			List<Compute> computes) {
		Execution execution = event.getExecution();
		if (null == execution)
			return;

		log.info("[processUpdateChildOrderEvent] " + execution.toString());
		receiveChildOrderUpdateEvent(execution);
	}

    @Override
    public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event,
                                             List<Compute> computes) {
        if (null == event)
            return;

        ExecType execType = event.getExecType();
        if (execType == ExecType.PARTIALLY_FILLED || execType == ExecType.FILLED)
        {
            ParentOrder parentOrder = event.getOrder();
            log.info("[processParentOrderUpdateEvent] " + parentOrder.toString());
            receiveParentOrderUpdateEvent(event);
        }
    }

	@Override
	public void processResetAccountRequestEvent(ResetAccountRequestEvent event, List<Compute> computes) {
		log.info("[processResetAccountRequestEvent] : AccountId :" + event.getAccount() + " Coinid : " + event.getCoinId());
		ResetUser(event);
	}
	
	@Override
	public void processQueryOrderAlertRequestEvent(
			QueryOrderAlertRequestEvent event, List<Compute> computes) {
		log.info("[receiveQueryOrderAlertRequestEvent] :" + event.toString());
		receiveQueryOrderAlertRequestEvent(event, computes) ;
	}

	synchronized private void ResetUser(ResetAccountRequestEvent event) {
		String UserId = event.getUserId();
		String strCmd = "";
		try {
			// Clear memory
			userTradeAlerts.remove(UserId);
			// Clear SQL

			Session session = sessionFactory.openSession();
			try {
				strCmd = "Delete from TRADEALERT_PAST where USER_ID='"
						+ UserId + "'";
				SQLQuery query = session.createSQLQuery(strCmd);
				int Return = query.executeUpdate();
			} catch (Exception ee) {
				ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(
						event.getKey(), event.getSender(), event.getAccount(),
						event.getTxId(), event.getUserId(), event.getMarket(),
						event.getCoinId(),
						ResetAccountReplyType.LTSINFO_ALERTMANAGER, false,
						MessageLookup.buildEventMessage(
								ErrorMessage.ACCOUNT_RESET_ERROR, " Reset User "
										+ UserId + "fail."));			
				SendRemoteEvent(resetAccountReplyEvent) ;
				log.warn("[ResetUser]:[" + strCmd + "] :"+ ee.getMessage());
			} finally {
				session.close();
			}
			ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(
					event.getKey(), event.getSender(), event.getAccount(),
					event.getTxId(), event.getUserId(), event.getMarket(),
					event.getCoinId(),
					ResetAccountReplyType.LTSINFO_ALERTMANAGER, true, "");
			SendRemoteEvent(resetAccountReplyEvent) ;
			log.info("Reset User Success : " + UserId);
		} catch (Exception e) {
			log.error("[ResetUser]:[" + strCmd + "] :" + e.getMessage());
		}
	}

    synchronized private void receiveParentOrderUpdateEvent(ParentOrderUpdateEvent parentOrderUpdateEvent) {
        Session session = null;
        try {
            ExecType execType = parentOrderUpdateEvent.getExecType();
            ParentOrder parentOrder = parentOrderUpdateEvent.getOrder();
            if (execType == null || parentOrder == null)
            {
                return ;
            }

    		RefData refdata = getGateway().getRefData(parentOrder.getSymbol());
    		if (refdata == null)
    		{
    			return;
    		}
            DecimalFormat qtyFormat = new DecimalFormat("#0");
            String strQty = "";
            DecimalFormat priceFormat = new DecimalFormat("#0.#####");
            String strAvgPx = priceFormat.format(parentOrder.getAvgPx());
            if (execType == ExecType.FILLED)
            {
                strQty = qtyFormat.format(parentOrder.getQuantity());
            }
            else if (execType == ExecType.PARTIALLY_FILLED)
            {
                strQty = qtyFormat.format(parentOrder.getCumQty());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            String Datetime = dateFormat.format(parentOrder.get(Date.class,
                    "Created"));
            String tradeMessage = "Trade " + parentOrder.getSymbol() + " "
                    + (parentOrder.getSide().isBuy() ? "BOUGHT" : "SOLD") + " " + strQty + "@"
//					+ "SOLD" + " " + strQty + "@"
                    + strAvgPx;
            TradeAlert TA;
            if (parentOrder.getSide().toString().toLowerCase().equals("sell")) {
                TA = new TradeAlert(parentOrder.getUser(), parentOrder.getSymbol(),
                        null, 0 - parentOrder.getQuantity(),
                        parentOrder.getPrice(), Datetime, tradeMessage);
            } else {
                TA = new TradeAlert(parentOrder.getUser(), parentOrder.getSymbol(),
                        null, parentOrder.getQuantity(), parentOrder.getPrice(),
                        Datetime, tradeMessage);
            }
			TA.setCommodity(refdata.getCommodity());
            String keyValue = parentOrder.getSymbol() + "," + strAvgPx + ","
                    + strQty + ","
                    + (parentOrder.getSide().isBuy() ? "BOUGHT" : "SOLD");
            // SendEvent
            SendNotificationRequestEvent sendNotificationRequestEvent = new SendNotificationRequestEvent(
                    null, null, "txId", new ParseData(parentOrder.getUser(),
                    tradeMessage, TA.getId(), AlertMsgType.MSG_TYPE_ORDER.getType(), Datetime,
                    keyValue, deepLink + parentOrder.getSymbol() + "." + market));
            // eventManagerMD.sendEvent(sendNotificationRequestEvent);
            SendEvent(sendNotificationRequestEvent) ;
            // save to Array
            ArrayList<TradeAlert> list;
            list = userTradeAlerts.get(parentOrder.getUser());
            synchronized(getUserLock(parentOrder.getUser())) {
	            if (null == list) {
	                session = sessionFactory.openSession();
	                Query query = session.getNamedQuery("LoadPastTradeAlert");
	                query.setString(0, parentOrder.getUser());
	//				query.setInteger("maxNoOfAlerts", maxNoOfAlerts);
	                Iterator<?> iterator = query.list().iterator();
	                list = new ArrayList<TradeAlert>();
	                ArrayList<TradeAlert> lstExpired = new ArrayList<TradeAlert>();
	                while (iterator.hasNext()) {
	                    TradeAlert pastTradeAlert = (TradeAlert) iterator.next();
	                    if (list.size() < 20)
	                    {
	                        list.add(pastTradeAlert);
	                    }
	                    else
	                    {
	                        lstExpired.add(pastTradeAlert);
	                    }
	                }
	                if (list.size() == 0) {
	                    list.add(TA);
	                } else if (list.size() >= 20) {
	                    list.remove(19);
	                    list.add(0, TA);
	                } else {
	                    list.add(0, TA);
	                }
	                userTradeAlerts.put(parentOrder.getUser(), list);
	                if (lstExpired.size() > 0)
	                {
	                    try {
	                        Transaction tx = session.beginTransaction();
	                        for (TradeAlert tradealert : lstExpired)
	                        {
	                            session.delete(tradealert);
	                        }
	                        tx.commit();
	                    } catch (Exception e) {
	                        log.warn("[SQLDelete] : " + e.getMessage());
	                    }
	                }
	            } else {
	                if (list.indexOf(TA) != -1) {
	                    log.warn("[UpdateChildOrderEvent][WARNING] : ChildOrderEvent already exists.");
//	                    if (null != session) {
//	                        session.close();
//	                    }
	                    return;
	                } else {
	                    if (list.size() >= 20) {
	                        list.remove(19);
	                        list.add(0, TA);
	                    } else {
	                        list.add(0, TA);
	                    }
	                }
	            }
            }
            // save to SQL
            SQLSave(TA);
        } catch (Exception e) {
            log.warn("[receiveChildOrderUpdateEvent] : " + e.getMessage());
        } finally {
            if (null != session) {
                session.close();
            }
        }
    }

	synchronized private void receiveChildOrderUpdateEvent(Execution execution) {
		Session session = null;
		RefData refdata = getGateway().getRefData(execution.getSymbol());
		if (refdata == null)
		{
			return;
		}
		try {
			DecimalFormat qtyFormat = new DecimalFormat("#0");
			String strQty = qtyFormat.format(execution.getQuantity());
			DecimalFormat priceFormat = new DecimalFormat("#0.#####");
			String strPrice = priceFormat.format(execution.getPrice());
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String Datetime = dateFormat.format(execution.get(Date.class,
					"Created"));
			String tradeMessage = "Trade " + execution.getSymbol() + " "
					+ (execution.getSide().isBuy() ? "BOUGHT" : "SOLD") + " " + strQty + "@"
//					+ "SOLD" + " " + strQty + "@"
					+ strPrice;
			TradeAlert TA;
			if (execution.getSide().toString().toLowerCase().equals("sell")) {
				TA = new TradeAlert(execution.getUser(), execution.getSymbol(),
						null, 0 - execution.getQuantity(),
						execution.getPrice(), Datetime, tradeMessage);
			} else {
				TA = new TradeAlert(execution.getUser(), execution.getSymbol(),
						null, execution.getQuantity(), execution.getPrice(),
						Datetime, tradeMessage);
			}
			TA.setCommodity(refdata.getCommodity());
			String keyValue = execution.getSymbol() + "," + strPrice + ","
					+ strQty + ","
					+ (execution.getSide().isBuy() ? "BOUGHT" : "SOLD");
			// SendEvent
			SendNotificationRequestEvent sendNotificationRequestEvent = new SendNotificationRequestEvent(
					null, null, "txId", new ParseData(execution.getUser(),
							tradeMessage, TA.getId(), AlertMsgType.MSG_TYPE_ORDER.getType(), Datetime,
							keyValue, deepLink + execution.getSymbol() + "." + market));
			// eventManagerMD.sendEvent(sendNotificationRequestEvent);
			SendEvent(sendNotificationRequestEvent) ;
			// save to Array
			ArrayList<TradeAlert> list;
			list = userTradeAlerts.get(execution.getUser());
            synchronized(getUserLock(execution.getUser())) {
				if (null == list) {
					session = sessionFactory.openSession();
					Query query = session.getNamedQuery("LoadPastTradeAlert");
					query.setString(0, execution.getUser());
	//				query.setInteger("maxNoOfAlerts", maxNoOfAlerts);
					Iterator<?> iterator = query.list().iterator();
					list = new ArrayList<TradeAlert>();
					ArrayList<TradeAlert> lstExpired = new ArrayList<TradeAlert>();
					while (iterator.hasNext()) {
						TradeAlert pastTradeAlert = (TradeAlert) iterator.next();
						if (list.size() < 20)
						{
							list.add(pastTradeAlert);
						}
						else
						{
							lstExpired.add(pastTradeAlert);
						}
					}
					if (list.size() == 0) {
						list.add(TA);
					} else if (list.size() >= 20) {
						list.remove(19);
						list.add(0, TA);
					} else {
						list.add(0, TA);
					}
					userTradeAlerts.put(execution.getUser(), list);
					if (lstExpired.size() > 0)
					{
						try {
							Transaction tx = session.beginTransaction();
							for (TradeAlert tradealert : lstExpired)
							{
								session.delete(tradealert);
							}
							tx.commit();
						} catch (Exception e) {
							log.warn("[SQLDelete] : " + e.getMessage());
						}
					}
				} else {
					if (list.indexOf(TA) != -1) {
						log.warn("[UpdateChildOrderEvent][WARNING] : ChildOrderEvent already exists.");
//						if (null != session) {
//							session.close();
//						}
						return;
					} else {
						if (list.size() >= 20) {
							list.remove(19);
							list.add(0, TA);
						} else {
							list.add(0, TA);
						}
					}
				}
            }
			// save to SQL
			SQLSave(TA);
		} catch (Exception e) {
			log.warn("[receiveChildOrderUpdateEvent] : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}
	
	private void receiveQueryOrderAlertRequestEvent(QueryOrderAlertRequestEvent event, List<Compute> computes) {
		String Msg = "";
		try {
			AlertType type = event.getType();
			
			QueryOrderAlertReplyEvent queryorderalertreplyevent = null;
			if (type == AlertType.TRADE_QUERY_PAST) {
				ArrayList<TradeAlert> list = userTradeAlerts.get(event
						.getuserId());
	            synchronized(getUserLock(event.getuserId())) {
					if (null == list) {
						Session session = null;
						try {
							session = sessionFactory.openSession();
	
							Query query = session
									.getNamedQuery("LoadPastTradeAlert");
							query.setString(0, event.getuserId());
	//						query.setInteger("maxNoOfAlerts", maxNoOfAlerts);
							Iterator<?> iterator = query.list().iterator();
							list = new ArrayList<TradeAlert>();
							ArrayList<TradeAlert> lstExpired = new ArrayList<TradeAlert>();
							while (iterator.hasNext()) {
								TradeAlert pastTradeAlert = (TradeAlert) iterator
										.next();
								if (list.size() < 20)
								{
									list.add(pastTradeAlert);
								}
								else
								{
									lstExpired.add(pastTradeAlert);
								}
							}
							if (list.size() == 0) {
								log.info("[processQueryOrderAlertRequestEvent] : user OrderAlert list isn't exists.");
								// Send orderalert event reply
								Msg = "userOrderAlert list isn't exists";
								queryorderalertreplyevent = new QueryOrderAlertReplyEvent(
										null, event.getSender(), list,
										event.getTxId(), event.getuserId(), true,
										Msg);							
							} else {
								queryorderalertreplyevent = new QueryOrderAlertReplyEvent(
										null, event.getSender(), list,
										event.getTxId(), event.getuserId(), true,
										null);
							}
							if (lstExpired.size() > 0)
							{
								try {
									Transaction tx = session.beginTransaction();
									for (TradeAlert TA : lstExpired)
									{
										session.delete(TA);
									}
									tx.commit();
								} catch (Exception e) {
									log.warn("[SQLDelete] : " + e.getMessage());
								}
							}						
						} catch (Throwable t) {
							log.warn("[processQueryOrderAlertRequestEvent] Exceptions : ",t);
						} finally {
							if (null != session) {
								session.close();
							}
						}
						userTradeAlerts.put(event.getuserId(), list);
					} else {
						if (list.size() == 0) {
							log.info("[processQueryOrderAlertRequestEvent] : user OrderAlert list isn't exists.");
							Msg = "userOrderAlert list isn't exists";
							queryorderalertreplyevent = new QueryOrderAlertReplyEvent(
									null, event.getSender(), list, event.getTxId(),
									event.getuserId(), true, Msg);
						} else {
							// Send orderalert event reply
							queryorderalertreplyevent = new QueryOrderAlertReplyEvent(
									null, event.getSender(), list, event.getTxId(),
									event.getuserId(), true, null);
						}
					}
	            }
			} else {
				Msg = "Event AlertTypeError.";
				queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null,
						event.getSender(), null, event.getTxId(),
						event.getuserId(), false, // Msg);
						MessageLookup.buildEventMessage(
								ErrorMessage.ALERT_TYPE_NOT_SUPPORT, Msg));
				log.error("[processQueryOrderAlertRequestEvent][AlertTypeError]: "
						+ event.toString());
			}
			try {
				SendRemoteEvent(queryorderalertreplyevent) ;
				log.info("[processQueryOrderAlertRequestEvent] Send Reply User : "
						+ queryorderalertreplyevent.getUserId());
			} catch (Exception e) {
				log.error("[processQueryOrderAlertRequestEvent] : Send RemoteEventError"
						+ e.getMessage());
			}
		} catch (Exception e) {
			log.warn("[processQueryOrderAlertRequestEvent] : " + Msg + " : "  + e.getMessage());
		}
	}
	
	synchronized private boolean checkSendFlag(BasePriceAlert alert)
	{
		if (alert.isSendFlag())
		{
			return false;
		}
		else
		{
			alert.setSendFlag(true);
			return true ;
		}
	}
	
	@Override
	public void processQuoteEvent(QuoteEvent event, List<Compute> computes) {
		Quote quote = event.getQuote();
		if (quotes.get(quote.getSymbol()) == null) {
			quotes.put(quote.getSymbol(), quote);
			return;
		}
		RefData refdata = getGateway().getRefData(quote.getSymbol());
		if (refdata == null)
		{
			return;
		}
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		ArrayList<BasePriceAlert> list = symbolPriceAlerts.get(quote
				.getSymbol());
		ArrayList<BasePriceAlert> UserPriceList;
		if (null != list && checkAlertstart) {
			BasePriceAlert alert;
			log.debug(String.format("Quote: %s - %s [%.1f@%.2f,%.1f@%.2f,%.1f@%.2f,%s,%s]", 
					quote.getId(), quote.getSymbol(), quote.getBidVol(), quote.getBid(),
					quote.getAskVol(), quote.getAsk(), quote.getLastVol(), quote.getLast(), 
					sdf.format(quote.getTimeStamp()), sdf.format(quote.getTimeSent())));
			synchronized (list) {
				for (int i = list.size(); i > 0; i--) {
					alert = list.get(i - 1);
					if (ComparePriceQuoto(alert, quotes.get(quote.getSymbol()),
							quote, refdata.getExchange().equals("FX"))) {
						if (!checkSendFlag(alert))
						{
							continue;
						}
						String setDateTime = alert.getDateTime();
						// SendEvent
						SendNotificationRequestEvent sendNotificationRequestEvent = new SendNotificationRequestEvent(
								null, null, "txId", PackPriceAlert(alert));
						SendEvent(sendNotificationRequestEvent);
						alertProcessor.putAlert(alert);
//						// Add Alert to PastSQL
//						PastPriceAlert pastPriceAlert = new PastPriceAlert(
//								alert.getUserId(), alert.getSymbol(),
//								alert.getPrice(), alert.getDateTime(),
//								alert.getContent());
//						pastPriceAlert.setId(alert.getId());
//						pastPriceAlert.setCommodity(refdata.getCommodity());
//						SQLSave(pastPriceAlert);
//						// Add Alert to pastUserPriceAlertList
//						UserPriceList = userPastPriceAlerts.get(alert.getUserId());
//						if (null == UserPriceList) {
//							loadPastPriceAlert(alert.getUserId());
//							UserPriceList = userPastPriceAlerts.get(alert
//									.getUserId());
//							synchronized (UserPriceList) {
//								if (UserPriceList.size() >= 20) {
//									UserPriceList.remove(19);
//									UserPriceList.add(0, pastPriceAlert);
//								} else {
//									UserPriceList.add(0, pastPriceAlert);
//								}
//							}
//						} else {
//							synchronized (UserPriceList) {
//								if (UserPriceList.size() >= 20) {
//									UserPriceList.remove(19);
//									UserPriceList.add(0, pastPriceAlert);
//								} else {
//									UserPriceList.add(0, pastPriceAlert);
//								}
//							}
//						}
//						// Delete Alert from CurSQL
//						CurPriceAlert curPriceAlert = new CurPriceAlert(
//								alert.getUserId(), alert.getSymbol(),
//								alert.getPrice(), setDateTime, alert.getContent());
//						curPriceAlert.setId(alert.getId());
//						curPriceAlert.setCommodity(refdata.getCommodity());
//						SQLDelete(curPriceAlert);
//						// Delete Alert from CurUserPriceAlertList
//						UserPriceList = userPriceAlerts.get(alert.getUserId());
//						if (null == UserPriceList) {
//							log.warn("[processQuoteEvent] : userPriceAlerts data didnt match with SQL");
//						} else {
//							synchronized (UserPriceList) {
//								UserPriceList.remove(alert);
//							}
//						}
						// Delete Alert from List
						list.remove(alert);
					}
				}
			}
		}
		quotes.put(quote.getSymbol(), quote);
	}
	
	public void processAlert(BasePriceAlert alert)
	{
		RefData refdata = getGateway().getRefData(alert.getSymbol());
		if (refdata == null)
		{
			return;
		}
		String setDateTime = alert.getDateTime();
		ArrayList<BasePriceAlert> UserPriceList;
		// Add Alert to PastSQL
		PastPriceAlert pastPriceAlert = new PastPriceAlert(
				alert.getUserId(), alert.getSymbol(),
				alert.getPrice(), alert.getDateTime(),
				alert.getContent());
		pastPriceAlert.setId(alert.getId());
		pastPriceAlert.setCommodity(refdata.getCommodity());
		SQLSave(pastPriceAlert);
		// Add Alert to pastUserPriceAlertList
		UserPriceList = userPastPriceAlerts.get(alert.getUserId());
		if (null == UserPriceList) {
			loadPastPriceAlert(alert.getUserId());
			UserPriceList = userPastPriceAlerts.get(alert
					.getUserId());
			synchronized (UserPriceList) {
				if (UserPriceList.size() >= 20) {
					UserPriceList.remove(19);
					UserPriceList.add(0, pastPriceAlert);
				} else {
					UserPriceList.add(0, pastPriceAlert);
				}
			}
		} else {
			synchronized (UserPriceList) {
				if (UserPriceList.size() >= 20) {
					UserPriceList.remove(19);
					UserPriceList.add(0, pastPriceAlert);
				} else {
					UserPriceList.add(0, pastPriceAlert);
				}
			}
		}
		// Delete Alert from CurSQL
		CurPriceAlert curPriceAlert = new CurPriceAlert(
				alert.getUserId(), alert.getSymbol(),
				alert.getPrice(), setDateTime, alert.getContent());
		curPriceAlert.setId(alert.getId());
		curPriceAlert.setCommodity(refdata.getCommodity());
		SQLDelete(curPriceAlert);
		// Delete Alert from CurUserPriceAlertList
		UserPriceList = userPriceAlerts.get(alert.getUserId());
		if (null == UserPriceList) {
			log.warn("[processQuoteEvent] : userPriceAlerts data didnt match with SQL");
		} else {
			synchronized (UserPriceList) {
				UserPriceList.remove(alert);
			}
		}
		log.debug("Alert " + alert.toString() + " process completed");
	}

	private boolean ComparePriceQuoto(BasePriceAlert alert,
			Quote Previousquoto, Quote quote, 
			boolean useMid) {
		double alertPrice = alert.getPrice();
		double PreviousPrice = getAlertPrice(Previousquoto, useMid);
		double currentPrice = getAlertPrice(quote, useMid);
		if (PriceUtils.isZero(PreviousPrice) || PriceUtils.isZero(currentPrice))
		{
			return false;
		}
		if (PriceUtils.GreaterThan(alertPrice, PreviousPrice)) {
			if (PriceUtils.GreaterThan(alertPrice, currentPrice)) {
				return false;
			} else {
				return true;
			}
		} else if (PriceUtils.EqualLessThan(alertPrice, PreviousPrice)) {
			if (PriceUtils.GreaterThan(alertPrice, currentPrice)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public void processSetPriceAlertRequestEvent(
			SetPriceAlertRequestEvent event, List<Compute> computes) {
		try {
			AlertType type = event.getType();
			log.info("[processSetPriceAlertRequestEvent] " + event.toString());
			if (type == AlertType.PRICE_SET_NEW) {
				receiveAddPriceAlert(event);
			} else if (type == AlertType.PRICE_SET_MODIFY) {
				receiveModifyPriceAlert(event);
			} else if (type == AlertType.PRICE_SET_CANCEL) {
				receiveCancelPriceAlert(event);
			} else {
				log.warn("[processSetPriceAlertRequestEvent][AlertTypeError]: "
						+ event.toString());
				PriceAlertReplyEvent pricealertreplyevent = null;
				// Send event reply
				String Msg = "Event AlertTypeError.";
				pricealertreplyevent = new PriceAlertReplyEvent(null,
						event.getSender(), null, event.getTxId(), event
								.getPriceAlert().getUserId(), event.getType(),
						null, false, // Msg);
						MessageLookup.buildEventMessage(
								ErrorMessage.ALERT_TYPE_NOT_SUPPORT, Msg));
				SendRemoteEvent(pricealertreplyevent);
			}
		} catch (Exception e) {
			log.warn("Exception : " + e.getMessage());
		}
	}

	@Override
	public void processQueryPriceAlertRequestEvent(
			QueryPriceAlertRequestEvent event, List<Compute> computes) {
		try {
			AlertType type = event.getType();
			log.info("[processQueryPriceAlertRequestEvent] " + event.toString());
			if (type == AlertType.PRICE_QUERY_CUR) {
				receiveQueryCurPriceAlert(event);
			} else if (type == AlertType.PRICE_QUERY_PAST) {
				receiveQueryPastPriceAlert(event);
			} else {
				log.warn("[processQueryPriceAlertRequestEvent][AlertTypeError]: "
						+ event.toString());
				PriceAlertReplyEvent pricealertreplyevent = null;
				// Send event reply
				String Msg = "Event AlertTypeError.";
				pricealertreplyevent = new PriceAlertReplyEvent(null,
						event.getSender(), null, event.getTxId(),
						event.getUserId(), event.getType(), null, false, // Msg);
						MessageLookup.buildEventMessage(
								ErrorMessage.ALERT_TYPE_NOT_SUPPORT, Msg));
				SendRemoteEvent(pricealertreplyevent);
			}
		} catch (Exception e) {
			log.warn("Exception : " + e.getMessage());
		}

	}

	private void loadPastPriceAlert(String userId) {
		Session session = null;
		try {
            synchronized(getUserLock(userId)) {
				session = sessionFactory.openSession();
				ArrayList<BasePriceAlert> BasePriceAlertlist = new ArrayList<BasePriceAlert>();
				ArrayList<PastPriceAlert> PastPriceAlertlist = new ArrayList<PastPriceAlert>();
				Query query = session.getNamedQuery("LoadPastPriceAlert");
				query.setString(0, userId);
	//			query.setInteger("maxNoOfAlerts", maxNoOfAlerts);
				Iterator<?> iterator = query.list().iterator();
				while (iterator.hasNext()) {
					PastPriceAlert pastPriceAlert = (PastPriceAlert) iterator
							.next();
					if (BasePriceAlertlist.size() < 20)
					{
						BasePriceAlertlist.add(pastPriceAlert);
					}
					else
					{
						PastPriceAlertlist.add(pastPriceAlert);
					}
				}
				userPastPriceAlerts.put(userId, BasePriceAlertlist);
				if (PastPriceAlertlist.size() > 0)
				{
					Transaction tx = session.getTransaction();
					for (PastPriceAlert PPT : PastPriceAlertlist)
					{
						session.delete(PPT);
					}
					tx.commit();
				}
            }
		} catch (Exception e) {
			log.warn("Exception : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}

	private void receiveAddPriceAlert(SetPriceAlertRequestEvent event) {
		ArrayList<BasePriceAlert> list;
		BasePriceAlert priceAlert = event.getPriceAlert();
		RefData refdata = getGateway().getRefData(priceAlert.getSymbol());
		if (refdata == null)
		{
			return;
		}
		int search;
		String Msg = "";
		PriceAlertReplyEvent pricealertreplyevent = null;
		// Add Alert to List
		list = userPriceAlerts.get(priceAlert.getUserId());
		if (null == list) {
			list = new ArrayList<BasePriceAlert>();
			// do new PriceAlert
			list.add(priceAlert);
			userPriceAlerts.put(priceAlert.getUserId(), list);
			// save to SQL
			CurPriceAlert curPriceAlert = new CurPriceAlert(
					priceAlert.getUserId(), priceAlert.getSymbol(),
					priceAlert.getPrice(), priceAlert.getDateTime(),
					priceAlert.getContent());
			curPriceAlert.setId(priceAlert.getId());
			curPriceAlert.setCommodity(refdata.getCommodity());
			SQLSave(curPriceAlert);
			// SendPriceAlertreplyEvent
			pricealertreplyevent = new PriceAlertReplyEvent(null,
					event.getSender(), null, event.getTxId(),
					priceAlert.getUserId(), event.getType(), list, true, null);
		} else {
			if (list.size() >= getMaxNoOfAlerts()) {
				// reject
				log.debug("[recevieAddPriceAlert] : UserAlert is Greater than maxNoOfAlerts -> reject");
				// Msg = "UserAlert is Greater than maxNoOfAlerts";
				Msg = MessageLookup.buildEventMessage(
						ErrorMessage.OVER_SET_MAX_PRICEALERTS,
						"You can only set 20 Price Alerts");
				pricealertreplyevent = new PriceAlertReplyEvent(null,
						event.getSender(), null, event.getTxId(),
						priceAlert.getUserId(), event.getType(), null, false,
						// Msg);
						MessageLookup.buildEventMessage(
								ErrorMessage.PRICE_ALERT_ERROR, Msg));
				try {
					SendRemoteEvent(pricealertreplyevent);
					log.info("[receiveAddPriceAlert] : send reject User : "
							+ pricealertreplyevent.getUserId() + " : " + Msg);
				} catch (Exception e) {
					log.debug("[recevieAddPriceAlert] : " + e.getMessage());
				}
				return;
			} else {
				if (list.indexOf(priceAlert) != -1) {
					log.debug("[recevieAddPriceAlert] : id already exists. -> reject");
					// SendPriceAlertreplyEvent
					Msg = "id already exists.";
					pricealertreplyevent = new PriceAlertReplyEvent(null,
							event.getSender(), null, event.getTxId(),
							priceAlert.getUserId(), event.getType(), null,
							false, // Msg);
							MessageLookup.buildEventMessage(
									ErrorMessage.PRICE_ALERT_ERROR, Msg));
					try {
						SendRemoteEvent(pricealertreplyevent);
						log.info("[receiveAddPriceAlert] : send reject User : "
								+ pricealertreplyevent.getUserId() + " : "
								+ Msg);
					} catch (Exception e) {
						log.debug("[recevieAddPriceAlert] : " + e.getMessage());
					}
					return;
				} else {
					synchronized (list) {
						list.add(priceAlert);
						// save to SQL
						CurPriceAlert curPriceAlert = new CurPriceAlert(
								priceAlert.getUserId(), priceAlert.getSymbol(),
								priceAlert.getPrice(), priceAlert.getDateTime(),
								priceAlert.getContent());
						curPriceAlert.setId(priceAlert.getId());
						SQLSave(curPriceAlert);
						// SendPriceAlertreplyEvent
						pricealertreplyevent = new PriceAlertReplyEvent(null,
								event.getSender(), null, event.getTxId(),
								priceAlert.getUserId(), event.getType(), list,
								true, null);
					}
				}
			}
		}

		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if (null == list) {
			list = new ArrayList<BasePriceAlert>();
			// do new PriceAlert
			list.add(priceAlert);
			symbolPriceAlerts.put(priceAlert.getSymbol(), list);
		} else {
			search = Collections.binarySearch(list, priceAlert);
			if (search < 0) {
				synchronized (list) {
					list.add(~search, priceAlert);
				}
			}
		}
		try {
			SendRemoteEvent(pricealertreplyevent);
			log.info("[receiveAddPriceAlert] : send reply User : "
					+ pricealertreplyevent.getUserId() + " : " + Msg);
		} catch (Exception e) {
			log.debug("[recevieAddPriceAlert] : " + e.getMessage());
		}
	}

	private void receiveModifyPriceAlert(SetPriceAlertRequestEvent event) {
		ArrayList<BasePriceAlert> list;
		int search;
		String Msg = "";
		BasePriceAlert priceAlert = event.getPriceAlert();
		RefData refdata = getGateway().getRefData(priceAlert.getSymbol());
		if (refdata == null)
		{
			return;
		}
		PriceAlertReplyEvent pricealertreplyevent = null;
		// Add Alert to List
		list = userPriceAlerts.get(priceAlert.getUserId());
		if (null == list) {
			log.debug("[receiveModifyPriceAlert] : id isn't exists. -> reject");
			// SendPriceAlertreplyEvent
			Msg = "id isn't exists.";
			pricealertreplyevent = new PriceAlertReplyEvent(null,
					event.getSender(), priceAlert.getId(), event.getTxId(),
					priceAlert.getUserId(), event.getType(), null, false,
					// Msg);
					MessageLookup.buildEventMessage(
							ErrorMessage.ACCOUNT_NOT_EXIST, Msg));
			try {
				SendRemoteEvent(pricealertreplyevent);
				log.info("[receiveModifyPriceAlert] : send reject User : "
						+ pricealertreplyevent.getUserId() + " : " + Msg);
			} catch (Exception e) {
				log.debug("[receiveModifyPriceAlert] : " + e.getMessage());
			}
			return;
		} else {
			if (list.indexOf(priceAlert) != -1) {
				synchronized (list) {
					for (BasePriceAlert basePriceAlert : list) {
						if (basePriceAlert.compareTo(priceAlert) == 0) {
							basePriceAlert.modifyPriceAlert(priceAlert);
							// update to SQL
							CurPriceAlert curPriceAlert = new CurPriceAlert(
									priceAlert.getUserId(), priceAlert.getSymbol(),
									priceAlert.getPrice(),
									priceAlert.getDateTime(),
									priceAlert.getContent());
							curPriceAlert.setId(priceAlert.getId());
							curPriceAlert.setCommodity(refdata.getCommodity());
							SQLUpdate(curPriceAlert);
							// SendPriceAlertreplyEvent
							pricealertreplyevent = new PriceAlertReplyEvent(null,
									event.getSender(), null, event.getTxId(),
									priceAlert.getUserId(), event.getType(), list,
									true, null);
						}
					}
				}
			} else {
				log.debug("[receiveModifyPriceAlert] : id isn't exists. -> reject");
				// SendPriceAlertreplyEvent
				Msg = "id isn't exists.";
				pricealertreplyevent = new PriceAlertReplyEvent(null,
						event.getSender(), priceAlert.getId(), event.getTxId(),
						priceAlert.getUserId(), event.getType(), null, false,
						// Msg);
						MessageLookup.buildEventMessage(
								ErrorMessage.ACCOUNT_NOT_EXIST, Msg));
				try {
					SendRemoteEvent(pricealertreplyevent);
					log.info("[receiveModifyPriceAlert] : send reject User : "
							+ pricealertreplyevent.getUserId() + " : " + Msg);
				} catch (Exception e) {
					log.debug("[receiveModifyPriceAlert] : " + e.getMessage());
				}
				return;
			}
		}

		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		search = Collections.binarySearch(list, priceAlert);
		if (search >= 0) {
			list.get(search).modifyPriceAlert(priceAlert);
		}
		try {
			SendRemoteEvent(pricealertreplyevent);
			log.info("[receiveModifyPriceAlert] : send reply User : "
					+ pricealertreplyevent.getUserId() + " : " + Msg);
		} catch (Exception e) {
			log.debug("[receiveModifyPriceAlert] : " + e.getMessage());
		}
	}

	private void receiveCancelPriceAlert(SetPriceAlertRequestEvent event) {
		ArrayList<BasePriceAlert> list;
		int search;
		BasePriceAlert priceAlert = event.getPriceAlert();
		RefData refdata = getGateway().getRefData(priceAlert.getSymbol());
		if (refdata == null)
		{
			return;
		}
		PriceAlertReplyEvent pricealertreplyevent = null;
		String Msg = "";
		// Add Alert to List
		list = userPriceAlerts.get(priceAlert.getUserId());
		if (null == list) {
			log.debug("[receiveCancelPriceAlert] : id isn't exists. ->reject");
			// SendPriceAlertreplyEvent
			Msg = "id isn't exists.";
			pricealertreplyevent = new PriceAlertReplyEvent(null,
					event.getSender(), priceAlert.getId(), event.getTxId(),
					priceAlert.getUserId(), event.getType(), null, false,
					// Msg);
					MessageLookup.buildEventMessage(
							ErrorMessage.ACCOUNT_NOT_EXIST, Msg));
			try {
				SendRemoteEvent(pricealertreplyevent);
				log.info("[receiveCancelPriceAlert] : send reject User : "
						+ pricealertreplyevent.getUserId());
			} catch (Exception e) {
				log.debug("[receiveCancelPriceAlert] : " + e.getMessage());
			}
			return;
		} else {
			if (list.indexOf(priceAlert) != -1) {
				synchronized (list) {
					list.remove(priceAlert);
					// update to SQL
					CurPriceAlert curPriceAlert = new CurPriceAlert(
							priceAlert.getUserId(), priceAlert.getSymbol(),
							priceAlert.getPrice(), priceAlert.getDateTime(),
							priceAlert.getContent());
					curPriceAlert.setId(priceAlert.getId());
					curPriceAlert.setCommodity(refdata.getCommodity());
					SQLDelete(curPriceAlert);
					// SendPriceAlertreplyEvent
					pricealertreplyevent = new PriceAlertReplyEvent(null,
							event.getSender(), null, event.getTxId(),
							priceAlert.getUserId(), event.getType(), list, true,
							null);
				}
			} else {
				log.debug("[receiveCancelPriceAlert] : id isn't exists. ->reject");
				// SendPriceAlertreplyEvent
				Msg = "id isn't exists.";
				pricealertreplyevent = new PriceAlertReplyEvent(null,
						event.getSender(), priceAlert.getId(), event.getTxId(),
						priceAlert.getUserId(), event.getType(), null, false,
						// Msg);
						MessageLookup.buildEventMessage(
								ErrorMessage.ACCOUNT_NOT_EXIST, Msg));
				try {
					SendRemoteEvent(pricealertreplyevent);
					log.info("[receiveCancelPriceAlert] : send reject User : "
							+ pricealertreplyevent.getUserId() + " : " + Msg);
				} catch (Exception e) {
					log.debug("[receiveCancelPriceAlert] : " + e.getMessage());
				}
				return;
			}
		}
		list = symbolPriceAlerts.get(priceAlert.getSymbol());

		search = Collections.binarySearch(list, priceAlert);
		if (search > 0) {
			synchronized (list) {
				list.remove(priceAlert);
			}
		}
		try {
			SendRemoteEvent(pricealertreplyevent);
			log.info("[receiveCancelPriceAlert] : send reply User : "
					+ pricealertreplyevent.getUserId() + " : " + Msg);
		} catch (Exception e) {
			log.debug("[receiveCancelPriceAlert] : " + e.getMessage());
		}
	}

	private void receiveQueryCurPriceAlert(QueryPriceAlertRequestEvent event) {
		try {
			String Msg = "";
			PriceAlertReplyEvent priceAlertReplyEvent;
			ArrayList<BasePriceAlert> list = userPriceAlerts.get(event
					.getUserId());
			if (null == list) {
				log.debug("[receiveQueryCurPriceAlert] : User CurPriceAlert list isn't exists.");
				// Send event reply
				list = new ArrayList<BasePriceAlert>();
				Msg = "User CurPriceAlert list isn't exists.";
				priceAlertReplyEvent = new PriceAlertReplyEvent(null,
						event.getSender(), null, event.getTxId(),
						event.getUserId(), event.getType(), list, true, Msg);
			} else {
				// Send event reply
				priceAlertReplyEvent = new PriceAlertReplyEvent(null,
						event.getSender(), null, event.getTxId(),
						event.getUserId(), event.getType(), list, true, null);
			}
			SendRemoteEvent(priceAlertReplyEvent);
			log.info("[receiveQueryCurPriceAlert] : send reply User : "
					+ priceAlertReplyEvent.getUserId() + " : " + Msg);
		} catch (Exception e) {
			log.debug("[receiveQueryCurPriceAlert] : " + e.getMessage());
		}
	}

	private void receiveQueryPastPriceAlert(QueryPriceAlertRequestEvent event) {
		try {
			String Msg = "";
			PriceAlertReplyEvent priceAlertReplyEvent;
			ArrayList<BasePriceAlert> list = userPastPriceAlerts.get(event
					.getUserId());
			if (null == list) {
				loadPastPriceAlert(event.getUserId());
				list = userPastPriceAlerts.get(event.getUserId());
				if (list.size() == 0) {
					log.debug("[receiveQueryPastPriceAlert] : User PastPriceAlert list isn't exists.");
					// Send orderalert event reply
					Msg = "User PastPriceAlert list isn't exists.";
					priceAlertReplyEvent = new PriceAlertReplyEvent(null,
							event.getSender(), null, event.getTxId(),
							event.getUserId(), event.getType(), list, true, Msg);
				} else {
					priceAlertReplyEvent = new PriceAlertReplyEvent(null,
							event.getSender(), null, event.getTxId(),
							event.getUserId(), event.getType(), list, true,
							null);
				}

			} else {
				if (list.size() == 0) {
					log.debug("[receiveQueryPastPriceAlert] : User PastPriceAlert list isn't exists.");
					// Send orderalert event reply
					Msg = "User PastPriceAlert list isn't exists.";
					priceAlertReplyEvent = new PriceAlertReplyEvent(null,
							event.getSender(), null, event.getTxId(),
							event.getUserId(), event.getType(), list, true, Msg);
				} else {
					priceAlertReplyEvent = new PriceAlertReplyEvent(null,
							event.getSender(), null, event.getTxId(),
							event.getUserId(), event.getType(), list, true,
							null);
				}
			}
			// eventManager.sendRemoteEvent(priceAlertReplyEvent);
			SendRemoteEvent(priceAlertReplyEvent);
			log.info("[receiveQueryPastPriceAlert] : send reply User : "
					+ priceAlertReplyEvent.getUserId() + " : " + Msg);
		} catch (Exception e) {
			log.debug("[receiveQueryPastPriceAlert] : " + e.getMessage());
		}
	}

	public <T> void SQLSave(T object) {
		Session session = null;
		try {
			log.debug("SQLSave " + object.toString());
			session = sessionFactory.openSession();			
			Transaction tx = session.beginTransaction();
			tx.setTimeout(3);
			if (tx.isActive())
			{
				session.save(object);
				tx.commit();
			}
		} catch (Exception e) {
			log.warn("[SQLSave] : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}

	private <T> void SQLUpdate(T object) {
		Session session = null;
		try {
			log.debug("SQLUpdate " + object.toString());
			session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.update(object);
			tx.commit();
		} catch (Exception e) {
			log.warn("[SQLUpdate] : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}

	private <T> void SQLDelete(T object) {
		Session session = null;
		try {
			log.debug("SQLDelete " + object.toString());
			session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.delete(object);
			tx.commit();
		} catch (Exception e) {
			log.warn("[SQLDelete] : " + e.getMessage()); 
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}

	synchronized private void SendSQLHeartBeat() {
        Calendar cal = Calendar.getInstance();
//        long aaa = cal.getTimeInMillis();
//        long bbb = LastHeartbeat.getTime() ;
        if ((cal.getTimeInMillis() - LastHeartbeat.getTime()) < 240000)
        {
            return ;
        }
        else
        {
            LastHeartbeat = cal.getTime();
        }
		Session session = null;
		try {
			session = sessionFactory.openSession();			
			SQLQuery sq = session.createSQLQuery("select 1;");
			Iterator<?> iterator = sq.list().iterator();
			log.info("Send SQLHeartBeat...");
		} catch (Exception e) {
			log.error("[SendSQLHeartBeat] : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}

	private double getAlertPrice(Quote quote, boolean useMid) {
		if (useMid)
			return (quote.getBid() + quote.getAsk()) / 2;
		else
			return quote.getLast();
	}

	public ParseData PackPriceAlert(BasePriceAlert priceAlert) {
		DecimalFormat priceFormat = new DecimalFormat("#0.#####");
		String strPrice = priceFormat.format(priceAlert.getPrice());
		String PriceAlertMessage = priceAlert.getSymbol() + " just reached $"
				+ strPrice;
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(Clock.getInstance().now());
		String keyValue = priceAlert.getSymbol() + "," + strPrice;
		priceAlert.setContent(PriceAlertMessage);
		priceAlert.setDateTime(strDate);
		return new ParseData(priceAlert.getUserId(), PriceAlertMessage,
				priceAlert.getId(), AlertMsgType.MSG_TYPE_PRICE.getType(), strDate, keyValue,
                deepLink + priceAlert.getSymbol() + "." + market);
	}

	@Override
	public void processAsyncTimerEvent(AsyncTimerEvent event,
			List<Compute> computes) {
		if (event.getKey() == "SendSQLHeartTimer") {
			try {
				SendSQLHeartBeat();
			} catch (Exception e) {
				log.warn("[SendSQLHeartBeat] Exception : " + e.getMessage());
			}
		}
	}

	private void loadSQLdata() {
		log.info("LoadSQLdata...");
		Session session = sessionFactory.openSession();
		try {
			ArrayList<BasePriceAlert> BasePriceAlertlist;
			Query query = session.getNamedQuery("LoadAllCurPriceAlert");
			Iterator<?> iterator = query.list().iterator();
			int search;
			while (iterator.hasNext()) {
				CurPriceAlert curPriceAlert = (CurPriceAlert) iterator.next();
				BasePriceAlertlist = userPriceAlerts.get(curPriceAlert
						.getUserId());
				if (null == BasePriceAlertlist) {
					BasePriceAlertlist = new ArrayList<BasePriceAlert>();
					BasePriceAlertlist.add(curPriceAlert);
					userPriceAlerts.put(curPriceAlert.getUserId(),
							BasePriceAlertlist);
				} else {
					synchronized (BasePriceAlertlist) {
						BasePriceAlertlist.add(curPriceAlert);
					}
				}

				BasePriceAlertlist = symbolPriceAlerts.get(curPriceAlert
						.getSymbol());
				if (null == BasePriceAlertlist) {
					BasePriceAlertlist = new ArrayList<BasePriceAlert>();
					BasePriceAlertlist.add(curPriceAlert);
					symbolPriceAlerts.put(curPriceAlert.getSymbol(),
							BasePriceAlertlist);
				} else {
					search = Collections.binarySearch(BasePriceAlertlist,
							curPriceAlert);
					if (search >= 0) {
						log.warn("[loadSQLdata] : PriceAlert id repeat warning.");
					} else {
						synchronized (BasePriceAlertlist) {
							BasePriceAlertlist.add(~search, curPriceAlert);
						}
					}
				}
			}
		} catch (Exception e) {
			log.warn("[loadSQLdata] : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
		}
		return;
	}

	// getters and setters

	public int getMaxNoOfAlerts() {
		return maxNoOfAlerts;
	}

	public void setMaxNoOfAlerts(int maxNoOfAlerts) {
		this.maxNoOfAlerts = maxNoOfAlerts;
	}

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public int getTimerinterval() {
        return timerinterval;
    }

    public void setTimerinterval(int timerinterval) {
        this.timerinterval = timerinterval;
    }
    
    private Object getUserLock(String userID) {
    	if (userLocks.get(userID) == null)
    		userLocks.put(userID, new Object());
    	return userLocks.get(userID);
    }

}

class AlertProcessThread extends Thread
{
	private static final Logger log = LoggerFactory
			.getLogger(AlertManager.class);
	
	private LinkedBlockingQueue<BasePriceAlert> m_q = new LinkedBlockingQueue<BasePriceAlert>();
	private AlertManager manager;
	private int m_queueMaxSize = 0;
	
	public AlertProcessThread(AlertManager manager)
	{
		this.setName("Alert-Processor-thread");
		this.manager = manager;
	}
	
	public void putAlert(BasePriceAlert alert)
	{
		try
		{
			log.info("Put " + alert.toString());
			m_q.put(alert);
		}
		catch (InterruptedException e)
		{
			log.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void run()
	{
		log.info("Alert-Processor-thread start");
		long lTimeOut = 50;
		BasePriceAlert alert;
		while (true)
		{
			try
			{
				alert = m_q.poll(lTimeOut, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				alert = null;
				log.error(e.getMessage(), e);
			}
			// log max queue size
			if (m_queueMaxSize < m_q.size())
			{
				m_queueMaxSize = m_q.size();
				log.info("Alert-Processor-thread QueueMaxSize=[" + m_queueMaxSize + "]");
			}
			if (alert != null)
			{
				manager.processAlert(alert);
			}
		}
	}
}
