package com.cyanspring.apievent.version;

public class ApiVersion {
	private String ID ="1.0.0";

	public String getID() {
		return ID;
	} 
	
	public boolean isSameVersion(ApiVersion version){
		return version.getID().equals(getID())?true : false;
	}
}
