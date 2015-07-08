package com.cyanspring.common.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JdbcSQLHandler {
	private static final Logger log = LoggerFactory.getLogger(JdbcSQLHandler.class);

	/*-- for batch --*/
	private Connection connect = null ;
	private Statement  stat = null ;
	
	private ComboPooledDataSource cpds;
	public JdbcSQLHandler(ComboPooledDataSource cpds) throws Exception
	{
		setCpds(cpds);
	}

	public Connection getConnect()
	{
		try 
		{
			return cpds.getConnection();
		} 
		catch (SQLException e) 
		{
            log.error(e.getMessage(), e);
		}
		return null;
	}
	public void closeConnect(Connection conn) 
	{
        try 
        {
            conn.close();
        } 
        catch (SQLException e) 
        {
            log.warn("Cannot close connection", e);
        }
    }
	public boolean isConnected()
    {
        try
        {        	
            if (connect != null)
            {
            	if(connect.isValid(0)) 
            	{
            		return true;
            	}	
            	else 
            	{
            		log.warn("SQL connection is invalid, try to reconnect later");
            		return false;
            	}
            }
            else
            {
            	return false;
            }
        }
        catch (SQLException se)
        {
            log.error(se.getMessage(), se);
        }
        return false;
    }
    public void reconnectSQL()
    {
        try
        {
        	if (connect != null)
        	{
        		connect.close();
        	}
        	log.info("Reconnect to SQL, discard old connection");
        	connect = cpds.getConnection();
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    public void disconnectSQL()
    {
        try
        {
        	if (connect != null)
        	{
        		connect.close();
        		connect = null;
        	}
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    public boolean updateSQL(String sqlcmd)
    {
    	boolean retVal = true;
        Connection connect = getConnect();
        if (connect == null)
        {
        	return false;
        }
        Statement stat = null ;
        try {
        	connect.setAutoCommit(false);
			stat = connect.createStatement();

			stat.executeUpdate(sqlcmd);

			connect.commit();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			log.warn("Exception while: " + sqlcmd);
            this.reconnectSQL();
            retVal = false;
        }
        finally 
		{
			if (stat != null)
	    	{
	    		try 
	    		{
					stat.close();
					stat = null;
				} 
	    		catch (SQLException e) 
				{
					log.error(e.getMessage(), e);
				}
	    	}
			closeConnect(connect);
		}
		return retVal;
    }
    public ResultSet querySQL(Connection connect, String sqlcmd)
    {
        ResultSet rs = null;
        Statement stat = null ;
        try
        {
            stat = connect.createStatement();
        	connect.setAutoCommit(false);
            rs = stat.executeQuery(sqlcmd);
			connect.commit();
        }
        catch (SQLException se)
        {
            log.error(se.getMessage(), se);
			log.warn("Exception while: " + sqlcmd);
        }
        return rs;
    }
    public boolean checkSQLConnect()
    {
    	boolean retVal = true;
    	if (!isConnected())
    	{
    		if (this.stat != null)
    		{
    			closeStatement();
    		}
            reconnectSQL();
    	}
        Statement stat = null ;
        try
        {
            stat = connect.createStatement();
			stat.executeQuery("SELECT 1;");
		} 
    	catch (SQLException e) 
    	{
            log.error(e.getMessage(), e) ;
            this.reconnectSQL();
            retVal = false;
        }
    	finally
    	{
			if (stat != null)
	    	{
	    		try 
	    		{
					stat.close();
					stat = null;
				} 
	    		catch (SQLException e) 
				{
					log.error(e.getMessage(), e);
				}
	    	}
    	}
        return retVal;
    }
    public void createStatement()
    {
		if (stat != null)
		{
			return;
		}
    	try 
    	{
			stat = connect.createStatement() ;
		} 
    	catch (SQLException e) 
    	{
            log.error(e.getMessage(), e) ;
		}
    }
    public void closeStatement()
    {
    	if (stat != null)
    	{
    		try 
    		{
				stat.close();
				stat = null;
			} 
    		catch (SQLException e) 
			{
				log.error(e.getMessage(), e);
			}
    	}
    }
    public void addBatch(String sqlcmd)
    {
        if (connect == null)
        {
            connect = getConnect();
        }
    	createStatement();
        try
        {
            stat.addBatch(sqlcmd);
        }
        catch(SQLException se)
        {
        	log.error(se.getMessage(), se) ;
        }
    }
    public boolean executeBatch()
    {
    	boolean retVal = true;
        try 
        {
			stat.executeBatch();
		} 
        catch (SQLException e) 
        {
			log.error(e.getMessage(), e) ;
			retVal = false;
		}
        finally 
        {
        	closeStatement();
        	disconnectSQL();
        }
        return retVal;
    }

	public ComboPooledDataSource getCpds() {
		return cpds;
	}

	public void setCpds(ComboPooledDataSource cpds) {
		this.cpds = cpds;
	}	
}
