package com.cyanspring.id.Library.Util;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//import FdtLib.Frame.GateWayDialog;
//import FdtLib.Frame.LogDialog;
import com.cyanspring.id.Library.Lock.WriteLock;

public class Logger {

	public final static int id_Error = 0x20; 
	public final static int id_Alert = 0x11; 
	public final static int id_Info =  0x02;
	public final static int id_Debug = 0x01;
	public final static int id_Trace = 0x00;
	
	public final static String constError = "ERROR"; 
	public final static String constAlert = "ALERT";
	public final static String constInfo = "INFO";
	public final static String constDebug = "DEBUG";
	public final static String constTrace = "TRACE";
	
	public final static int getLevel(String sLevel) {
		switch (sLevel.toUpperCase().trim()) {
		case Logger.constError:
			return Logger.id_Error;
			
		case Logger.constAlert:			
			return Logger.id_Alert;
			
		case Logger.constInfo:
			return Logger.id_Info;	
			
		case Logger.constDebug:
			return Logger.id_Debug;
			default:
				break;
		}
		return -1;
	}
	
	static boolean _fDisposing = false;
	static boolean _fInit = false;
	static Hashtable<Integer, InLog> _table = new Hashtable<Integer, InLog>();

	static int defaultLevel = id_Info;
	
	public static void setLevel(int nLevel) {
		defaultLevel = nLevel;
	}
	
	private static class Hook extends Thread {

		public void run() {
			_fDisposing = true;
			InLog.CloseLog();
		}
	}

	public static void Init() {
		Hook hook = new Hook();
		Runtime.getRuntime().addShutdownHook(hook);
		_fInit = true;
	}

	static int _NextID = 0;

	public static int Init(String strTitle, int nInterval) {
		Hook hook = new Hook();
		Runtime.getRuntime().addShutdownHook(hook);
		_NextID++;

		InLog log = null; // getInstance(_NextID);
		if (false == _table.contains(_NextID)) {
			log = new InLog();
			log.SetData(strTitle, nInterval);
			_table.put(_NextID, log);
		} else {
			log = _table.get(_NextID);
		}

		return _NextID;
	}

	public static InLog getInstance(int nID) {
		InLog log = _table.get(nID);
		if (log == null) {
			log = new InLog();
			_table.put(nID, log);
		}
		return log;
	}

	public interface ILog {
		void Log(String sLog);

		void Log(String f, Object... args);
	}

	public static void log(String sLog) {
		if (!_fInit)
			Init();

		if (_fDisposing)
			return;


		InLog log = InLog.getInstance();
		log.log(sLog);
	}

	public static void log(int nID, String sLog) {

		if (_fDisposing)
			return;

		InLog log = getInstance(nID);
		log.log2(sLog);
	}

	public static void log(int nID, String f, Object... args) {
		log(nID, String.format(f, args));
	}

	public static void log(String f, Object... args) {
		log(String.format(f, args));
	}

	//Trace
	public static void logTrace(String f, Object... args) {
		if (defaultLevel > id_Trace)
			return;

		log(String.format("%-7s - %s", constTrace, String.format(f, args)));
	}

	public static void LogTrace(int nID, String f, Object... args) {
		if (defaultLevel > id_Trace)
			return;
		
		log(nID, String.format("%-7s - %s", constTrace, String.format(f, args)));
	}	
	
	
	// debug
	public static void logDebug(int nID, String f, Object... args) {
		if (defaultLevel > id_Debug)
			return;
		
		log(nID, String.format("[%-7s] %s", constDebug, String.format(f, args)));
	}
	
	public static void logDebug(String f, Object... args) {

		if (defaultLevel > id_Debug)
			return;
		
		log(String.format("%-7s - %s", constDebug, String.format(f, args)));
	}

	// Info
	public static void logInfo(int nID, String f, Object... args) {
		if (defaultLevel > id_Info)
			return;
		
		log(nID, String.format("%-7s - %s", constInfo, String.format(f, args)));
	}

	public static void logInfo(String f, Object... args) {
		if (defaultLevel > id_Info)
			return;

		log(String.format("%-7s - %s", constInfo, String.format(f, args)));
	}

	// Error
	public static void logError(String f, Object... args) {
		if (defaultLevel > id_Error)
			return;

		log(String.format("%-7s - %s", constError, String.format(f, args)));
	}

	public static void LogError(int nID, String f, Object... args) {
		if (defaultLevel > id_Error)
			return;
		
		log(nID, String.format("%-7s - %s", constError, String.format(f, args)));
	}

	// Exception
	public static void logException(Exception ex) {
		log(getTrace(ex));
	}

	public static void logException(int nID, Exception ex) {
		log(nID, getTrace(ex));
	}

	public static String getTrace(Exception e) {
		String result = String.format("Exception : %s%n%n", e.getMessage());
		result = String.format("%s==========================================%n%n", result);
		StackTraceElement[] arr = e.getStackTrace();
		for (StackTraceElement elm : arr) {
			result = String.format("%s%s%n", result, elm);
		}
		return result;
	}
	
	/**
	 * Inner Log class
	 */
	public static class InLog implements AutoCloseable {
		BkLogFile fileLog = null;
		ReadWriteLock rwLock = new ReentrantReadWriteLock();

		static InLog ConsoleLog = new InLog();

		String _strTitle = "Log";
		int _nInterval = 60;

		public void SetData(String strTitle, int nInterval) {
			_strTitle = strTitle;
			_nInterval = nInterval;
			fileLog = new BkLogFile(String.format("Log/%s${DATE}.txt", strTitle), nInterval);
		}

		public static InLog getInstance() {
			return ConsoleLog;
		}

		public InLog() {
		}

		public void createFileLog(String strTitle, int nInterval) {
			fileLog = new BkLogFile(String.format("Log/%s${DATE}.txt", strTitle), nInterval);
			// fileLog = new BkLogFile("C:\\data001.txt", 10);
		}

		public static void CloseLog() {
			if (ConsoleLog != null) {
				ConsoleLog.close();
				ConsoleLog = null;
			}

			if (_table != null) {
				for (int nID : _table.keySet()) {
					InLog log = _table.get(nID);
					if (log != null) {
						log.close();
					}
				}
				_table.clear();
			}
		}

		public void log(String sLog) {
			if (fileLog == null)
				createFileLog(_strTitle, _nInterval);

			fileLog.writeWithTime(sLog);

			try (WriteLock wl = new WriteLock(rwLock)) {
				long threadId = Thread.currentThread().getId();
				SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
				System.out.printf("[%s][%03d] %s%n", sdFormat.format(new java.util.Date()), threadId, sLog);
			}
		}

		public void log2(String sLog) {
			// if (fileLog == null)
			// createFileLog(_strTitle, _nInterval);

			fileLog.writeWithTime(sLog);

			try (WriteLock wl = new WriteLock(rwLock)) {
				long threadId = Thread.currentThread().getId();
				SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
				System.out.printf("[%s][%03d] %s%n", sdFormat.format(new java.util.Date()), threadId, sLog);
			}
		}

		@Override
		public void close() {
			if (fileLog != null) {
				fileLog.close();
				fileLog = null;
			}
		}
	}
}
