package com.cyanspring.common.staticdata;

import java.io.File;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataTplLoader {

	private String tplPath;
	private List<RefData> lstRefData;
	private XStream xstream = new XStream(new DomDriver("UTF-8"));
	
    public String getTplPath() {
		return tplPath;
	}

	public void setTplPath(String tplPath) {
		this.tplPath = tplPath;
	}

	@SuppressWarnings("unchecked")
	public List<RefData> getRefDataList() throws Exception {
		File templateFile = new File(tplPath);
        if (templateFile.exists()) {
        	lstRefData = (List<RefData>) xstream.fromXML(templateFile);
        } else {
            throw new Exception("Missing refdata template: " + tplPath);
        }
        
		return lstRefData;
	}
}
