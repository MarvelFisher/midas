package com.cyanspring.cstw.session;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.05
 *
 */
public class CSTWSession {

	private String configPath;

	private static CSTWSession instance;

	public static CSTWSession getInstance() {
		if (instance == null) {
			instance = new CSTWSession();
		}
		return instance;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

}
