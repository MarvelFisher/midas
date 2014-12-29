package com.cyanspring.id.Library.Frame;

public class InfoString {
	public static final int Info = 0;
	public static final int Warn = 1;
	public static final int ALert = 2;
	public static final int Error = 3;
	
	public String toString() {
		return _context;
	}
	public InfoString() {				
	}
	
	public InfoString( int nLevel, String context)
	{
		_nLevel = nLevel;
		_context = context;		
	}
	
	public static InfoString format(int nLevel, String fmt, Object... args) 
	{
		String value = String.format(fmt, args);
	
		return new InfoString(nLevel, value);
		
	}
	
	public static String getString(int nLevel) {
		String value = "INFO ";
		switch (nLevel) {
		case InfoString.Error:
			value = "ERROR";
			break;
		case InfoString.Warn:
			value = "WARN ";
			break;
		case InfoString.ALert:
			value = "ALERT";
			break;
		case InfoString.Info:
		default:
			break;
		}
		return value;
	}
	
	
	int _nLevel = Info;
	String _context = "";	
	public String getContext() { return _context;}
	public int getLevel() { return _nLevel; }
	
}
