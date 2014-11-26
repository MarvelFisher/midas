package com.cyanspring.server.persistence;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	private static String insertUser = "INSERT INTO AUTH(`USERID`, `USERNAME`, `PASSWORD`, `EMAIL`, `PHONE`, `CREATED`, `USERTYPE`) VALUES('%s', '%s', '%s', '%s', '%s', '%s', %d)";
	private static String isUserExist = "SELECT COUNT(*) FROM AUTH WHERE USERID='%s'";
	private static String getUserPassword = "SELECT PASSWORD FROM AUTH WHERE USERID='%s'";
	private static String openSQL = "jdbc:mysql://%s:%d/%s?useUnicode=true";
	
	static Logger log = Logger.getLogger(CentralDbConnector.class);
	
	public static CentralDbConnector getInstance() throws CentralDbException
	{
		if(inst == null)
			inst = new CentralDbConnector();
		
		if(inst.isClosed())
		{
			if(!inst.connect())
				throw new CentralDbException("can't connect to central database");
		}
		
		return inst;
	}
	private CentralDbConnector()
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
			return false;
		}
		
		return true;
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
	protected String getInsertUserSQL(String userId, String userName, String password, String email, String phone, Date created, UserType userType)
	{
		return String.format(insertUser, userId, userName, password, email, phone, sdf.format(created), userType.getCode());
	}
	/*
	protected String getInsertAccountSQL(String accountId, String userId, String market, double cash, double margin, double pl, double allTimePl, double urPl, double cashDeposited, double unitPrice, boolean bActive, Date created, String currency)
	{
		return String.format(insertAccount, accountId, userId, margin, cash, margin, pl, allTimePl, urPl, cashDeposited, unitPrice, (bActive)?"0x01":"0x02", sdf.format(created), currency);
	}
	*/
	protected boolean checkConnected()
	{
		if(!isClosed())
			return true;
		return connect();
	}
	public boolean registerUser(String userId, String userName, String password, String email, String phone, UserType userType)
	{	
		if(!checkConnected())
			return false;
		
		boolean bIsSuccess = false;
		Date now = Default.getCalendar().getTime();
		String sUserSQL = getInsertUserSQL(userId, userName, password, email, phone, now, userType);
		Statement stmt = null;
		
		try
		{
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			
			stmt.executeUpdate(sUserSQL);
			
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
		finally
		{
			if(stmt != null)
			{
				try{
					stmt.close();
				}catch(SQLException e){
					e.printStackTrace();
				}
			}
		}
		return bIsSuccess;
	}
	public boolean isUserExist(String sUser)
	{
		if(!checkConnected())
			return false;
		
		String sQuery = String.format(isUserExist, sUser);
		Statement stmt = null;
		int nCount = 0;
		
		try {
	        stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(sQuery);
	        
	        if(rs.next()) 
	        	nCount = rs.getInt("COUNT(*)");

	    } catch (SQLException e ) {
	        e.printStackTrace();
	    } finally {
	        if (stmt != null) 
	        { 
	        	try{
	        		stmt.close();
	        	}catch(SQLException e){
	        		e.printStackTrace();
	        	}
	        }
	    }
		return (nCount > 0);
	}
	public boolean userLogin(String sUser, String sPassword)
	{
		if(!checkConnected())
			return false;
		
		String sQuery = String.format(getUserPassword, sUser);
		Statement stmt = null;
		String curPassword = "";
		
		try {
	        stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(sQuery);
	        
	        if(rs.next()) 
	        	curPassword = rs.getString("PASSWORD");
	        
	        if(curPassword.length() == 0)
	        	return false;
	        
	        if(curPassword.equals(sPassword))
	        	return true;

	    } catch (SQLException e ) {
	        e.printStackTrace();
	    } finally {
	        if (stmt != null) 
	        { 
	        	try{
	        		stmt.close();
	        	}catch(SQLException e){
	        		e.printStackTrace();
	        	}
	        }
	    }
		return false;
	}
	public String getHost()
	{
		return this.host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	public int getPort()
	{
		return this.port;
	}
	public void setPort(int port)
	{
		this.port = port;
	}
	public String getUser()
	{
		return this.user;
	}
	public void setUser(String user)
	{
		this.user = user;
	}
	public String getPass()
	{
		return this.pass;
	}
	public void setPass(String pass)
	{
		this.pass = pass;
	}
	public String getDatabase()
	{
		return this.database;
	}
	public void setDatabase(String database)
	{
		this.database = database;
	}
	
	public static void main(String[] argv)
	{
		CentralDbConnector conn = new CentralDbConnector();
		boolean bConnect = conn.connect("125.227.191.247", 3306, "tqt001", "tqt001", "LTS");
		if(bConnect)
		{
			//conn.registerUser("test1", "TestUser1", "test1", "test1@test.com", "+886-12345678", UserType.NORMAL);
			//boolean bExist = conn.isUserExist("test1");
			boolean bLogin = conn.userLogin("test1", "xxx");
			System.out.println(bLogin);
		}
	}
}