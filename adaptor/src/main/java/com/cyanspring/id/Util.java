package com.cyanspring.id;

import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Test.Program;

public class Util {

	public static void addLog(String f, Object... args) {
		if (Program.mainFrame != null) {
			Program.mainFrame.addLog(InfoString.Info, f, args);
		}
	}

	public static void addLog(Integer nLevel, String f, Object... args) {
		if (Program.mainFrame != null) {
			Program.mainFrame.addLog(nLevel, f, args);
		}
	}

	public static void addMsg(String f, Object... args) {
		if (Program.mainFrame != null) {
			Program.mainFrame.addMsg(f, args);
		}
	}

	public static void setStatus(String value) {
		if (Program.mainFrame != null) {
			Program.mainFrame.setStatus(value);
		}
	}
}
