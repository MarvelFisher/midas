package com.cyanspring.id.Library.Util;

import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;

/**
 * LogFile that writes at background
 */
public class BkLogFile implements AutoCloseable, TimerEventHandler
{
	private static final Logger log = LoggerFactory.getLogger(BkLogFile.class);
	protected String m_strFileName = "";
	protected int m_nFlushFreq = 60; // seconds
	protected int MaxTimeout = 6000;
	protected boolean m_fDaily = true; // store log daily
	protected TimerThread m_timer = null;

	private static class LogData
	{
		public Date Date = new Date(0);
		public ArrayList<String> Logs = new ArrayList<String>();
	}

	protected ArrayList<LogData> m_arrLD = new ArrayList<LogData>();
	protected Object m_lock = new Object();

	public BkLogFile(String strFileName)
	{
		m_strFileName = strFileName;
		start();
	}

	public BkLogFile(String strFileName, int nFlushFreq)
	{
		m_strFileName = strFileName;
		m_nFlushFreq = nFlushFreq;
		start();
	}

	public BkLogFile(String strFileName, int nFlushFreq, boolean fDaily)
	{
		m_strFileName = strFileName;
		m_nFlushFreq = nFlushFreq;
		m_fDaily = fDaily;
		start();
	}

	protected void finalize() throws Throwable
	{
		close();
	}

	@Override
	public final void close()
	{
		stop();
	}

	public final void write(String strFormat, Object... arg)
	{

		if (arg.length == 0)
		{
			addLog(strFormat);
		}
		else
		{
			String strText = String.format(strFormat, arg);
			addLog(String.format("%s%n", strText));
		}

	}

	public final void writeWithTime(String strFormat, Object... arg)
	{
		String strText = String.format(strFormat, arg);
		Date dNow = new Date();
		SimpleDateFormat sdFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		addLog(String.format("%s %s%n", sdFormat.format(dNow), strText));
	}

	private void start()
	{
		if (m_nFlushFreq <= 0)
		{
			m_nFlushFreq = 1;
		}

		m_timer = new TimerThread();
		m_timer.setInterval(m_nFlushFreq *1000);
		m_timer.TimerEvent = this;
		//new TimerThread.TimerEventHandler() {

		//	@Override
		//	public void onTimer(TimerThread objSender) {
		//		onTimer(objSender);
		//	}
		//};
		m_timer.start();
	}

	private void stop()
	{
		try
		{
			if (m_timer != null) {
				m_timer.close();
			}
		} catch (Exception e) {
			LogUtil.logException(log, e);
		} finally {
			m_timer = null;
		}

		flush();
	}

	private void flush()
	{
		// Flush any remaining text
		//

		ArrayList<LogData> arrLD = null;
		synchronized (m_lock)
		{
			if (m_arrLD.isEmpty())
			{
				return;
			}

			arrLD = m_arrLD;
			m_arrLD = new ArrayList<LogData>();
		}

		if (arrLD == null)
		{
			return;
		}

		for (LogData ld : arrLD)
		{
			if (ld.Logs.isEmpty())
			{
				continue;
			}

			Date dtFile = m_fDaily ? ld.Date : DateUtil.today();

			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
			String strFileName = m_strFileName.replace("${DATE}", sdFormat.format(dtFile));

			try
			{
				Log.writeMulti(strFileName, (String[]) ld.Logs.toArray(new String[0]));
			} catch (Exception ex)
			{
				LogUtil.logException(log, ex);
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void addLog(String strText)
	{
		synchronized (m_lock)
		{
			if (m_arrLD.isEmpty())
			{
				createNewLogData();
			}
			else
			{
				if (m_fDaily && DateUtil.today().getDate() != ((LogData) m_arrLD.get(m_arrLD.size() - 1)).Date.getDate())
				{
					createNewLogData();
				}
			}

			LogData ldLast = (LogData) m_arrLD.get(m_arrLD.size() - 1);
			ldLast.Logs.add(strText);
		}
	}

	private void createNewLogData()
	{
		LogData ld = new LogData();
		ld.Date = DateUtil.today();
		m_arrLD.add(ld);
	}

	@Override
	public void onTimer(TimerThread objSender) {
		// Suspend timer: to make sure only one flush run at a time
		//
		m_timer.setInterval(MaxTimeout * 1000);
		flush();

		// Resume timer
		//
		m_timer.setInterval(m_nFlushFreq * 1000);
	}
}
