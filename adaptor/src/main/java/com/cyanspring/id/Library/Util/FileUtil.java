package com.cyanspring.id.Library.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
	
	public static FileOutputStream getLogFileStream(String sFileName, boolean bAppend)
	{
		FileOutputStream fs = null;
		File file = null;
		try
		{
			int nPos = sFileName.lastIndexOf('/');
			if (nPos <= 0)
				nPos = sFileName.lastIndexOf('\\');

			if (nPos > 0) {
				String sDir = sFileName.substring(0, nPos + 1);
				File dir = new File(sDir);
				if (!dir.exists()) {
					dir.mkdirs();  
				}
				
				file = new File(sFileName);
				if (!file.exists()) {
					file.createNewFile();
				}
				fs = new FileOutputStream(file, bAppend);
			}
			else {
				file = new File(sFileName);
				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
				fs = new FileOutputStream(file, bAppend);
			}

			return fs;
			// get the content in bytes
			// byte[] contentInBytes = content.getBytes();

			// fs.write(contentInBytes);
			// fs.flush();
			// fs.close();
			// fs = new FileStream(sFileName, FileMode.Append, FileAccess.Write,
			// FileShare.Read); //.ReadWrite);

			// writer = new StreamWriter(sFileName, true,
			// Encoding.GetEncoding(950));
		} catch (java.io.IOException e)
		{
			LogUtil.logException(log, e);
			if (file != null && !file.exists()) {
				try {
					file.createNewFile();
					fs = new FileOutputStream(file, true);
				} catch (IOException e1) {
					LogUtil.logException(log, e1);
					return null;
				}
			}
			// FileHelper.EnsureFolderExists(sFileName);

			// try again

			// fs = new FileStream(sFileName, FileMode.Append, FileAccess.Write,
			// FileShare.Read); //.ReadWrite);
			// writer = new StreamWriter(sFileName, true,
			// Encoding.GetEncoding(950));
		} catch (RuntimeException ex)
		{
			System.out.println(ex.getMessage());
		}
		return fs;
	}

	public static void writeFile(String sFileName, String strData)
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
			fs = FileUtil.getLogFileStream(sFileName, false);

			if (fs != null)
			{
				// writer = new StreamWriter(fs, Encoding.GetEncoding(950));
				writer = new OutputStreamWriter(fs, "big5");
				writer.write(strData); //String.format(sFormat, arg));
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

	public static void writeFile(String sFileName, String[] arrLines)
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
			fs = FileUtil.getLogFileStream(sFileName, false);

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
