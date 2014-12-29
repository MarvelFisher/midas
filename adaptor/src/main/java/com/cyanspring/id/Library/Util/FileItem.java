package com.cyanspring.id.Library.Util;

import java.util.ArrayList;

import com.cyanspring.id.Library.Util.FinalizeHelper;

public class FileItem
{

	String m_strKey;

	ArrayList<String> m_listFrame = new ArrayList<String>();

	boolean m_bOverride;
	
	public FileItem(String strKey, String frame, boolean bOverride)
	{
		m_bOverride = bOverride;
		m_strKey = strKey;
		m_listFrame.add(frame);
	}

	public FileItem(String strKey, ArrayList<String> list, boolean bOverride)
	{
		m_bOverride = bOverride;
		m_strKey = strKey;
		m_listFrame.addAll(list);
	}
	
	protected void finalize() {

		m_listFrame.clear();
	}

	public void close()
	{
		finalize();
		FinalizeHelper.suppressFinalize(this);
	}
	
	String key()  
	{
		return m_strKey;
	}

	int compare(String strKey) 
	{			
		return m_strKey.compareTo(strKey);
	}

	boolean pushData(Object obj)
	{
		FileItem newItem2 = (FileItem) obj;
		
		if (newItem2 == null)
			return false;

		ArrayList<String> list = newItem2.popData();
		if (0 == list.size())
			return false;
		
		m_listFrame.addAll(list);
		
		return true;
	}

	public int size() {		
		return m_listFrame.size();
	}
	
	public void push(String frame)
	{
		m_listFrame.add(frame);
	}

	ArrayList<String> popData() 
	{
		ArrayList<String> list = new ArrayList<String>(m_listFrame);

		m_listFrame.clear();

		return list;
	}

	boolean isOverride() {return m_bOverride;}


}