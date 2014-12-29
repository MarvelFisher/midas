package com.cyanspring.id.Library.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Summary description for LogFile.
 */
public class Log
{
	private static final Logger log = LoggerFactory.getLogger(Log.class);
	
	public static void writeWithTime(String sFileName, boolean fMilliSecond, String sFormat, Object... arg)
	{		
		String s = String.format(sFormat, arg);
		Date dNow = new Date();
		String sTime;
		SimpleDateFormat sdFormat;
		if (fMilliSecond)
		{
			sdFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		}
		else
		{
			sdFormat = new SimpleDateFormat("HH:mm:ss");
		}
		sTime = sdFormat.format(dNow);
		write(sFileName, "%s %s", sTime, s);
	}


	
	public static void write(String sFileName, String sFormat, Object... arg)
	{
		// StreamWriter writer = null;
		Writer writer = null;
		FileOutputStream fs = null;
		try
		{
			/*
			 * String strCodeBase = Assembly.GetExecutingAssembly().CodeBase; if
			 * (strCodeBase.startsWith("file:///")) { strCodeBase =
			 * strCodeBase.substring(8); }
			 * 
			 * String strPath = (new java.io.File(strCodeBase)).getParent();
			 * sFileName = Path.Combine(strPath, sFileName);
			 */
			fs = FileUtil.getLogFileStream(sFileName, true);

			if (fs != null)
			{
				// writer = new StreamWriter(fs, Encoding.GetEncoding(950));
				writer = new OutputStreamWriter(fs, "big5");
				writer.write(String.format(sFormat, arg));
				// writer.write(System.getProperty("line.separator"));
				writer.flush();
			}
		} catch (Exception e) {
			LogUtil.logException(log, e);
		} finally {
			try {
				if (writer != null)
					writer.close();

				if (fs != null)
					fs.close();

			} catch (IOException e) {

				writer = null;
				fs = null;
				LogUtil.logException(log, e);
			}
		}
	}

	public static void writeMulti(String sFileName, String[] arrLines)
	{
		Writer writer = null;
		FileOutputStream fs = null;
		try
		{
			/*
			 * String strCodeBase = Assembly.GetExecutingAssembly().CodeBase; if
			 * (strCodeBase.startsWith("file:///")) { strCodeBase =
			 * strCodeBase.substring(8); }
			 * 
			 * String strPath = (new java.io.File(strCodeBase)).getParent();
			 * sFileName = Path.Combine(strPath, sFileName);
			 */
			fs = FileUtil.getLogFileStream(sFileName, true);

			if (fs != null)
			{
				// writer = new StreamWriter(fs, Encoding.GetEncoding(950));
				writer = new OutputStreamWriter(fs, "big5");
				for (String strLine : arrLines)
				{
					// writer.WriteLine(strLine);
					writer.write(strLine);
					// writer.write(System.getProperty("line.separator"));
				}
				writer.flush();
			}
		} catch (Exception e) {
			LogUtil.logException(log, e);
		} finally {
			try {
				if (writer != null)
					writer.close();

				if (fs != null)
					fs.close();

			} catch (IOException e) {

				writer = null;
				fs = null;
				LogUtil.logException(log, e);
			}
		}
	}
}