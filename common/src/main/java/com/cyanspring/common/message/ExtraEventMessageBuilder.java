package com.cyanspring.common.message;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExtraEventMessageBuilder{
	private Map <ExtraEventMessage,String>paramMap = new LinkedHashMap<>();
	public ExtraEventMessageBuilder putMessage(ExtraEventMessage name,String value){
		paramMap.put(name, value);
		return this;
	}
	public String getMessage(ExtraEventMessage name){
		if(paramMap.containsKey(name)){
			return paramMap.get(name);
		}
		return null;
	}
}
