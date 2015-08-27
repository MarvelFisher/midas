package com.cyanspring.common.staticdata;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataTplLoader {

	private static final Logger log = LoggerFactory.getLogger(RefDataTplLoader.class);
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
        	log.error("RefData template not found at " + tplPath);
            throw new Exception("RefData template not found at " + tplPath);
        }
        
		return lstRefData;
	}
}
