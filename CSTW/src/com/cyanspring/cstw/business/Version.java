package com.cyanspring.cstw.business;

public class Version {
	private String version= "1.0.1";
	
	public String getVersion() {
		return version;
	} 
	
	public String getVersionDetails(){
		String welcomeStr = 
				"----- LTW Version:"+getVersion()+" -------";
		return welcomeStr;
	}
		
	public boolean isSameVersion(Version version){
		return version.getVersion().equals(getVersion())?true : false;
	}
}
