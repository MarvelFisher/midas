package com.cyanspring.id.Library.Util;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.FileUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.Log;


public class FileMgr implements AutoCloseable, IReqThreadCallback {

	private static final Logger log = LoggerFactory.getLogger(FileMgr.class);
	
	static FileMgr instance = new FileMgr();

	public static FileMgr instance() {
		return instance;
	}

	String m_strPath = "";
	RequestThread thread = null;

	public FileMgr() {
	}

	public boolean init() {
		try {

			if (null == thread) {
				thread = new RequestThread(this, "FileManager");

				thread.start();
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	void uninit() {
		if (thread != null) {
			thread.close();
			thread = null;
		}
	}

	public void close() {

		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	public void writeDataToFile(String strKey, String frame, boolean bOverride) {
		try {
			FileItem obj = new FileItem(strKey, frame, bOverride);
			thread.addRequest(obj);
		} catch (Exception e) {
		}
	}

	public void writeDataToFile(String strKey, ArrayList<String> list,
			boolean bOverride) {
		try {
			FileItem obj = new FileItem(strKey, list, bOverride);
			thread.addRequest(obj);
		} catch (Exception e) {
		}
	}

	public static void writeFile(String strKey, String frame, boolean bOverride) {
		if (bOverride) {
			FileUtil.writeFile(strKey, frame);
		} else {
			Log.write(strKey, frame);
		}

	}

	public static void writeFile(String strKey, ArrayList<String> listData,
			boolean bOverride) {
		String[] arrData = Arrays.copyOf(listData.toArray(), listData.size(), String[].class); //. listData.toArray(new String[listData.size()]);
		if (bOverride) {
			FileUtil.writeFile(strKey, arrData);
		} else {

			Log.writeMulti(strKey, arrData);
		}
	}

	@Override
	public void onStartEvent(RequestThread sender) {
	
	}

	@Override
	public void onRequestEvent(RequestThread sender, Object reqObj) {
		
		FileItem item = (FileItem) reqObj;
		LogUtil.logInfo(log, String.format("[%s]onRequestEvent %d", sender.getName(), item.size()));		

		ArrayList<String> listFile = new ArrayList<String>(item.popData());
		writeFile(item.key(), listFile, item.isOverride());
		item.close();
		
	}

	@Override
	public void onStopEvent(RequestThread sender) {
		LogUtil.logInfo(log, "onStopEvent");
		Object[] arr = sender.getAllRequests();
		FileItem[] arrItem  = Arrays.copyOf(arr, arr.length, FileItem[].class);
		
		for (FileItem item : arrItem) {
			if (item.size() > 0) {
				onRequestEvent(sender, item);
			}
			item.close();
		}
		
	}
}
