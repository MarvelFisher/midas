package com.cyanspring.id.Library.Util;
import java.util.Date;

public class FixStringBuilder {
	
    StringBuilder m_sb = new StringBuilder();

    public static String getStringFX(double dValue, int nDP)
    {
        // FX �T�w�p���I�e��[�_��6��
        //String sformat = String.format("%%.%df", nDP);
    	//String strValue = String.format(sformat, dValue);
    	String strValue = StringUtil.formatDouble(nDP, dValue);
        //String strValue = String.format("%.*f", nDP, dValue);
        //String strValue = String.format("{0:0.00000}", dValue);
        int nLength = strValue.length();
        int i = nLength - 1;
        for (; i != 0; i--)
        {
            if (6 == i)
                break;

            char ch = strValue.charAt(i);
            if ('0' == ch)
            {
                continue;
            }
            else if ('.' == ch)
            {
                i--;
                break;
            }
            else
            {
                break;
            }
        }
        if (i != nLength - 1)
        {
            strValue = strValue.substring(0, i + 1);
        }
        return strValue;
    }

    public static String getString(double dValue, int nDP)
    {
        //String sformat = String.format("%%.%df", nDP);
        //String strValue = String.format(sformat, dValue);
        String strValue = StringUtil.formatDouble(nDP, dValue);
    	//String strValue = String.format("%.*f", nDP, dValue);
        //String strValue = String.format("{0:0.00000}", dValue);
        int nLength = strValue.length();
        int i = nLength - 1;
        for (; i != 0; i--)
        {
            char ch = strValue.charAt(i);
            if ('0' == ch)
            {
                continue;
            }
            else if ('.' == ch)
            {
                i--;
                break;
            }
            else
            {
                break;
            }
        }
        if (i != nLength - 1)
        {
            strValue = strValue.substring(0, i + 1);
        }
        return strValue;
    }

    public FixStringBuilder()
    {
        m_bIdentifier = false;
        m_bPaired = false;
        m_sSndIdentifier = m_sIFstdentifier = "";
        clear();
    }

    public FixStringBuilder(char cFstIdentifier)
    {
        m_bIdentifier = true;
        m_sIFstdentifier = m_sSndIdentifier = String.format("%c", cFstIdentifier);
        m_bPaired = false;
        clear();
    }

    public FixStringBuilder(char cFstIdentifier, char cSndIdentifier)
    {
        m_bIdentifier = true;
        m_sIFstdentifier = String.format("%c", cFstIdentifier);
        m_sSndIdentifier = String.format("%c", cSndIdentifier);
        m_bPaired = true;
        clear();
    }

    public FixStringBuilder(String sFstIdentifier)
    {
        m_bIdentifier = true;
        m_sIFstdentifier = m_sSndIdentifier = sFstIdentifier;
        m_bPaired = false;
        clear();
    }

    public FixStringBuilder(String sFstIdentifier, String sSndIdentifier)
    {
        m_bIdentifier = true;
        m_sIFstdentifier = sFstIdentifier;
        m_sSndIdentifier = sSndIdentifier;
        m_bPaired = true;
        clear();
    }

    protected void finalize() throws Throwable {
        try {
            //close();        // close open files
        } finally {
            super.finalize();
        }
    }

    public String toString()
    {
        return m_sb.toString();
    }

    public void clear()
    {
        m_sb.delete( 0, m_sb.length() );
    }

    public void addEmpty()
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        if (m_bBegin)
        {
        }
        else
        {
            m_sb.append(sIdentifier);
        }
        m_bBegin = false;

    }

    public void append(String str)
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        if (m_bBegin)
        {
            m_sb.append(str);
        }
        else
        {
        	m_sb.append(sIdentifier);
            m_sb.append(str);
        }
        m_bBegin = false;
    }

    public void appendFormat(String f, Object ... args)
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        if (m_bBegin)
        {
            m_sb.append(String.format(f, args));
        }
        else
        {
        	m_sb.append(sIdentifier);
            m_sb.append(String.format(f, args));
        }
        m_bBegin = false;
    }

    public void append(char c)
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        if (m_bBegin)
        {
            m_sb.append(c);
        }
        else
        {
        	m_sb.append(sIdentifier);
            m_sb.append(c);
        }
        m_bBegin = false;
    }

    public void append(int n)
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        if (m_bBegin)
        {
            m_sb.append(n);
        }
        else
        {
        	m_sb.append(sIdentifier);
            m_sb.append(n);
        }
        m_bBegin = false;
    }

	public void append(double dValue, int nDP)
    {
    	m_nCount ++;
	    String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ?  m_sIFstdentifier : m_sSndIdentifier;

	    if (m_bBegin)
	    {   
		    m_sb.append(getString(dValue, nDP));
	    }
	    else
	    {
	    	m_sb.append(sIdentifier);
	    	m_sb.append(getString(dValue, nDP));
	    }
    	m_bBegin = false;
    }

    public void append(long n)
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        if (m_bBegin)
        {
            m_sb.append(n);
        }
        else
        {
        	m_sb.append(sIdentifier);
            m_sb.append(n);
        }
        m_bBegin = false;
    }

    public void append(Date dt)
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        String strTime = DateUtil.formatTime(dt, false);
        if (m_bBegin)
        {
            m_sb.append(strTime);
        }
        else
        {
        	m_sb.append(sIdentifier);
            m_sb.append(strTime);      
        }
        m_bBegin = false;
    }
/*
    public void append2(DateTime dt)
    {
        m_nCount++;
        String sIdentifier = (m_bIdentifier == false) ? "" : (m_bPaired && m_nCount % 2 == 0) ? m_sIFstdentifier : m_sSndIdentifier;

        String strTime = Utility.formatTime2(dt);
        if (m_bBegin)
        {
            m_sb.append(strTime);
        }
        else
        {
            m_sb.appendFormat("{0}{1}", sIdentifier, strTime);
        }
        m_bBegin = false;
    }
*/
    public void appendTime(long lTime)
    {
    	Date dt = new Date(lTime);
    	append(dt);       
    }

    
    boolean m_bBegin = true;
    boolean m_bPaired;
    boolean m_bIdentifier;
    int m_nCount;
    String m_sIFstdentifier;
    String m_sSndIdentifier;
}	


