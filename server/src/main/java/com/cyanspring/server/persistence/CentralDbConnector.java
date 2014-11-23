package com.cyanspring.server.persistence;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.UserType;

public class CentralDbConnector
{
	protected Connection conn = null;
	protected String host = "";
	protected int port = 0;
	protected String user = "";
	protected String pass = "";
	protected String database = "";
	
	private static String MARKET_FX = "FX";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static CentralDbConnector inst = null; 
	private static String insertUser = "INSERT INTO USERS(`USER_ID`, `USER_NAME`, `PASSWORD`, `EMAIL`, `PHONE`, `CREATED`, `LAST_LOGIN`, `USER_TYPE`, `DEFAULT_ACCOUNT`) VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";
	private static String insertAccount = "INSERT INTO ACCOUNTS(`ACCOUNT_ID`, `USER_ID`, `MARKET`, `CASH`, `MARGIN`, `PNL`, `ALL_TIME_PNL`, `UR_PNL`, `CASH_DEPOSITED`, `UNIT_PRICE`, `ACTIVE`, `CREATED`, `CURRENCY`) VALUES('%s', '%s', '%s', %f, %f, %f, %f, %f, %f, %f, %s, '%s', '%s')";
	private static String openSQL = "jdbc:mysql://%s:%d/%s?useUnicode=true";
	
	static Logger log = Logger.getLogger(CentralDbConnector.class);
	
	public CentralDbConnector()
	{
		
	}
	public boolean connect(String sIP, int nPort, String sUser, String sPassword, String sDatabase)
	{
		try 
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			String sReq = String.format(openSQL, sIP, nPort, sDatabase);
			conn = DriverManager.getConnection(sReq, sUser, sPassword);
			
			log.info("Connected to the database");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return (conn != null);
	}
	public boolean isClosed()
	{
		if(conn == null)
			return true;
		
		boolean bResult = true;
		try{
			bResult = conn.isClosed();
		}catch(SQLException e){
			e.printStackTrace();
			bResult = true;
		}
		return bResult;
	}
	public boolean connect()
	{
		return connect(host, port, user, pass, database);
	}
	public void close()
	{
		if(conn != null)
		{
			try
			{
				conn.close();
				conn = null;
				log.info("Disconnected from database");
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	protected String getInsertUserSQL(String userId, String userName, String password, String email, String phone, Date created, Date lastLogin, UserType userType, String defaultAccount)
	{
		return String.format(insertUser, userId, userName, password, email, phone, sdf.format(created), sdf.format(lastLogin), userType.name(), defaultAccount);
	}
	protected String getInsertAccountSQL(String accountId, String userId, String market, double cash, double margin, double pl, double allTimePl, double urPl, double cashDeposited, double unitPrice, boolean bActive, Date created, String currency)
	{
		return String.format(insertAccount, accountId, userId, margin, cash, margin, pl, allTimePl, urPl, cashDeposited, unitPrice, (bActive)?"0x01":"0x02", sdf.format(created), currency);
	}
	public boolean registerUser(String userId, String userName, String password, String email, String phone, UserType userType, String defaultAccount)
	{
		boolean bIsSuccess = false;
		Date now = Default.getCalendar().getTime();
		String sUserSQL = getInsertUserSQL(userId, userName, password, email, phone, now, now, userType, defaultAccount);
		String sAccountSQL = getInsertAccountSQL(defaultAccount, userId, MARKET_FX, Default.getAccountCash(), Default.getAccountCash()*Default.getMarginTimes(), 0, 0, 0, Default.getAccountCash(), 0, true, now, "USD");
		
		try
		{
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			
			stmt.executeUpdate(sUserSQL);
			stmt.executeUpdate(sAccountSQL);
			
			conn.commit();
			bIsSuccess = true;
		}
		catch(SQLException e)
		{
			bIsSuccess = false;
			e.printStackTrace();
			try{
				conn.rollback();
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		return bIsSuccess;
	}
}
