package com.cyanspring.server.persistence;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserType;

public class CentralDbConnector {
	protected Connection conn = null;
//	protected String host = "";
//	protected int port = 0;
//	protected String user = "";
//	protected String pass = "";
//	protected String database = "";
//	private   String openSQL = "";

	private static String MARKET_FX = "FX";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static CentralDbConnector inst = null;
	private static String insertUser = "INSERT INTO AUTH(`USERID`, `USERNAME`, `PASSWORD`, `SALT`, `EMAIL`, `PHONE`, `CREATED`, `USERTYPE`, `COUNTRY`, `LANGUAGE`, `USERLEVEL`) VALUES('%s', '%s', md5('%s'), '%s', '%s', '%s', '%s', %d, '%s', '%s', %d)";
	private static String isUserExist = "SELECT COUNT(*) FROM AUTH WHERE `USERID` = '%s'";
	private static String isEmailExist = "SELECT COUNT(*) FROM AUTH WHERE `EMAIL` = '%s'";
	private static String getUserPasswordSalt = "SELECT `PASSWORD`, `SALT` FROM AUTH WHERE `USERID` = '%s'";
	private static String getUserAllInfo = "SELECT `USERID`, `USERNAME`, `PASSWORD`, `SALT`, `EMAIL`, `PHONE`, `CREATED`, `USERTYPE`, `COUNTRY`, `LANGUAGE`, `USERLEVEL` FROM AUTH WHERE `USERID` = '%s'";
	private static String setUserPassword = "UPDATE AUTH SET `PASSWORD` = '%s' WHERE `USERID` = '%s'";
	private static final Logger log = LoggerFactory.getLogger(CentralDbConnector.class);
	private ComboPooledDataSource cpds;	

	public void setCpds(ComboPooledDataSource cpds) {
		this.cpds = cpds;
	}

	public static CentralDbConnector getInstance() throws CentralDbException {
		if (inst == null)
			inst = new CentralDbConnector();

		if (inst.isClosed()) {
			if (!inst.connect())
				throw new CentralDbException(
						"can't connect to central database");
		}

		return inst;
	}

	private CentralDbConnector() {

	}

	public boolean connect() {
//	public boolean connect(String sIP, int nPort, String sUser,
//			String sPassword, String sDatabase) {
		try {
			conn = cpds.getConnection();

//			Class.forName("com.mysql.jdbc.Driver").newInstance();

//			String sReq = String.format(openSQL, sIP, nPort, sDatabase);
//			conn = DriverManager.getConnection(sReq, sUser, sPassword);

			log.info("Connected to the database");
		} catch (Exception e) {
			log.error("Cannot connection to Database", e);
			return false;
		}

		return true;
	}

	public boolean isClosed() {
		if (conn == null)
			return true;

		boolean bResult = true;
		try {
			bResult = conn.isClosed();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			bResult = true;
		}
		return bResult;
	}

//	public boolean connect() {
//		return connect(host, port, user, pass, database);
//	}

	public void close() {
		if (conn != null) {
			try {
				conn.close();
				conn = null;
				log.info("Disconnected from database");
			} catch (Exception e) {
				log.error("Cannot disconnect from database", e);
			}
		}
	}

	protected String getInsertUserSQL(String userId, String userName,
			String password, String salt, String email, String phone, Date created,
			UserType userType, String country, String language) {
		return String.format(insertUser, userId, userName, password+salt, salt, email,
				phone, sdf.format(created), userType.getCode(), country, language, 0);
	}

	/*
	 * protected String getInsertAccountSQL(String accountId, String userId,
	 * String market, double cash, double margin, double pl, double allTimePl,
	 * double urPl, double cashDeposited, double unitPrice, boolean bActive,
	 * Date created, String currency) { return String.format(insertAccount,
	 * accountId, userId, margin, cash, margin, pl, allTimePl, urPl,
	 * cashDeposited, unitPrice, (bActive)?"0x01":"0x02", sdf.format(created),
	 * currency); }
	 */
	protected boolean checkConnected() {
		if (!isClosed())
			return true;
		return connect();
	}

	public boolean registerUser(String userId, String userName,
			String password, String email, String phone, UserType userType, String country, String language) {
		if (!checkConnected())
			return false;

		boolean bIsSuccess = false;
		Date now = Default.getCalendar().getTime();
		String salt = getRandomSalt(10);
		
		String sUserSQL = getInsertUserSQL(userId, userName, password, salt, email, phone, now, userType, country, language);
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sUserSQL);
			bIsSuccess = true;
		} catch (SQLException e) {
			bIsSuccess = false;
			log.warn("Cannot register user."+userId, e);
			try {
				conn.rollback();
			} catch (SQLException se) {
				log.warn("Register User rollback fail."+userId, se);
			}
		} finally {
			if (stmt != null) {
				closeStmt(stmt);
			}
			try {
				conn.commit();
			} catch (SQLException se) {
				log.warn("Register User commit fail."+userId, se);
			}
		}
		return bIsSuccess;
	}

	public boolean isUserExist(String sUser) {
		if (!checkConnected())
			return false;

		String sQuery = String.format(isUserExist, sUser);
		Statement stmt = null;
		int nCount = 0;

		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sQuery);

			if (rs.next())
				nCount = rs.getInt("COUNT(*)");

		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
		} finally {
			if (stmt != null) {
				closeStmt(stmt);
			}
		}
		return (nCount > 0);
	}
	
	public boolean isEmailExist(String sEmail) {
		if (!checkConnected())
			return false;

		String sQuery = String.format(isEmailExist, sEmail);
		Statement stmt = null;
		int nCount = 0;

		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sQuery);

			if (rs.next())
				nCount = rs.getInt("COUNT(*)");

		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
		} finally {
			if (stmt != null) {
				closeStmt(stmt);
			}
		}
		return (nCount > 0);
	}

	public boolean userLogin(String sUser, String sPassword) {
		return userLoginEx(sUser, sPassword) != null;
	}
	public User userLoginEx(String sUser, String sPassword) {
		if (!checkConnected())
			return null;

		String sQuery = String.format(getUserAllInfo, sUser);
		Statement stmt = null;

		String md5Password = null;
		String salt = null;
		String username = null;
		String email = null;
		String phone = null;
		UserType userType = null;

		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sQuery);

			if (rs.next()){
				md5Password = rs.getString("PASSWORD");
				salt = rs.getString("SALT");
				username = rs.getString("USERNAME");
				email = rs.getString("EMAIL");
				phone = rs.getString("PHONE");
				userType = UserType.fromCode(rs.getInt("USERTYPE"));
			}
			
			if(md5Password == null){
				closeStmt(stmt);
				return null;
			}

			String fullPassword = (salt == null)? sPassword : sPassword + salt;
			
			if(md5Password.equals(md5(fullPassword))){
				closeStmt(stmt);
				return new User(sUser, username, sPassword, email, phone, userType);
			}

		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
		} finally {
			if (stmt != null) {
				closeStmt(stmt);
			}
		}
		return null;
	}	
	public boolean changePassword(String sUser, String originalPass, String newPass) {
		if (!checkConnected())
			return false;

		String sQuery = String.format(getUserPasswordSalt, sUser);
		Statement stmt = null;

		String md5Password = null;
		String salt = null;

		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sQuery);

			if (rs.next())
			{
				md5Password = rs.getString("PASSWORD");
				salt = rs.getString("SALT");
			}
			
			if(md5Password == null){
				closeStmt(stmt);				
				return false;
			}

			String fullPassword = (salt == null)? originalPass : originalPass + salt;
			
			if(!md5Password.equals(md5(fullPassword))){
				closeStmt(stmt);				
				return false;
			}
			
			String newMd5Password = md5(newPass + salt);
			String sQuerySet = String.format(setUserPassword, newMd5Password, sUser);
			
			int nResult = stmt.executeUpdate(sQuerySet);
			if(1 != nResult){
				closeStmt(stmt);
				return false;
			}
		} catch (SQLException e) {
			closeStmt(stmt);
			log.warn(e.getMessage(), e);
			return false;
		} finally {
			if (stmt != null) {
				closeStmt(stmt);
			}
		}
		return true;
	}

	public boolean updateConnection(){
		if (!checkConnected())
			return false;
		Statement stmt = null;
		boolean state = true;
		try{
			stmt = conn.createStatement();
			stmt.executeQuery("SELECT 1;");
		}catch(SQLException e){
			state = false;
			log.warn(e.getMessage(), e);
		}finally{
			if(stmt != null)
				closeStmt(stmt);			
		}
		return state;
	}
	
	private void closeStmt(Statement stmt) {
		try {
			stmt.close();
		} catch (SQLException e) {
			log.warn("Cannot close statement", e);
		}
	}

//	public String getHost() {
//		return this.host;
//	}
//
//	public void setHost(String host) {
//		this.host = host;
//	}
//
//	public int getPort() {
//		return this.port;
//	}
//
//	public void setPort(int port) {
//		this.port = port;
//	}
//
//	public String getUser() {
//		return this.user;
//	}
//
//	public void setUser(String user) {
//		this.user = user;
//	}
//
//	public String getPass() {
//		return this.pass;
//	}
//
//	public void setPass(String pass) {
//		this.pass = pass;
//	}
//
//	public String getDatabase() {
//		return this.database;
//	}
//
//	public void setDatabase(String database) {
//		this.database = database;
//	}
	
	 protected String md5(String str) 
	 {
	    String md5 = null;
	    
	    try 
	    {
	      MessageDigest md=MessageDigest.getInstance("MD5");
	      byte[] barr=md.digest(str.getBytes());
	      StringBuffer sb=new StringBuffer();
	      
	      for (int i=0; i < barr.length; i++) 
	    	  sb.append(byte2Hex(barr[i]));
	      
	      return sb.toString();
	    }
	    catch(Exception e) 
	    {
	    	log.warn("create md5 error", e);
	    }
	    return md5;
	 }
	 
	 protected String byte2Hex(byte b) 
	 {
	    String[] h={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
	    int i=b;
	    
	    if (i < 0) 
	    	{i += 256;}
	    
	    return h[i/16] + h[i%16];
	 }
		  
	 protected String getRandomSalt(int nLength)
	 {
		  int[] word = new int[nLength];
		  int mod;
		  for(int i = 0; i < nLength; i++)
		  {
			  mod = (int)((Math.random()*7)%3);
			  if(mod ==1)
				  word[i]=(int)((Math.random()*10) + 48);
			  else if(mod ==2)
				  word[i] = (char)((Math.random()*26) + 65);
			  else
				  word[i] = (char)((Math.random()*26) + 97);
		  }
		  
		  StringBuilder sKey = new StringBuilder();
		  for(int j=0 ; j<nLength ; j++)
			  sKey.append((char)word[j]);
		  
		  return sKey.toString();
	 }

	public static void main(String[] argv) {
//		CentralDbConnector conn = new CentralDbConnector();
//		boolean bConnect = conn.connect("125.227.191.247", 3306, "tqt001",
//				"tqt001", "LTS");
//		if (bConnect) {
//			conn.registerUser("aaaaaaa", "aaaaaaa", "aaaaaaa", "test1@test.com", "+886-12345678", UserType.NORMAL, "TW", "ZH");
//			//boolean bExist = conn.isUserExist("test1");
//			boolean bExist = conn.isEmailExist("phoenix.su@hkfdt.com");
//			boolean bLogin = conn.userLogin("seemo1", "1234");
//			//boolean bChangePassword = conn.changePassword("seemo1", "1111", "1234");
//			System.out.println("");
//		}
	}
}
