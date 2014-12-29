package com.cyanspring.id.gateway.netty;


import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.id.Library.Util.FinalizeHelper;



public class HttpUrlParam implements AutoCloseable {

	public static HttpUrlParam parse(String strUrl) {

		boolean bParams = true;
		int nPos = strUrl.lastIndexOf('/');
		int nPos2 = strUrl.indexOf('.');
		if (nPos2 < 0) {
			nPos2 = strUrl.indexOf('?');
			if (nPos2 < 0) {
				nPos2 = strUrl.length();
				bParams = false;
			}
		}

		String cmd = strUrl.substring(nPos + 1, nPos2).trim();

		if (bParams) {
			Hashtable<String, String> map = new Hashtable<String, String>();
			QueryStringDecoder decoderQuery = new QueryStringDecoder(strUrl);
			Map<String, List<String>> uriAttributes = decoderQuery.parameters();
	
			for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
				StringBuilder sb = new StringBuilder();
				for (String attrVal : attr.getValue()) {
					sb.append(attrVal);
				}
				map.put(attr.getKey(), sb.toString());
			}
			HttpUrlParam params = new HttpUrlParam();
			params.setUrl(strUrl);
			params.setCmd(cmd);
			params.setMap(map);		
			params.hasParams = true;
			return params;
		}
		else {
			HttpUrlParam params = new HttpUrlParam();
			params.setUrl(strUrl);
			params.setCmd(cmd);		
			params.hasParams = false;
			return params;
		}	
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public void setMap(Hashtable<String, String> map) {
		this.map = map;
	}

	boolean hasParams = true;
	String url;
	String cmd;
	Hashtable<String, String> map = new Hashtable<String, String>();

	public HttpUrlParam() {
	}

	public int getIntAt(String key) {
		return getIntAt(key, 0);
	}

	public double getDoubleAt(String key) {
		return getDoubleAt(key, 0);
	}

	public String getAt(String key) {
		return getAt(key, "");

	}

	public String getAt(String key, String strDefault) {
		String strRetVal = strDefault;
		if (map == null || map.containsKey(key) == false)
			return strDefault;
		
		try {
			strRetVal = map.get(key);
		} catch (Exception e) {
		}
		return strRetVal;
	}

	public int getIntAt(String key, int nDefault) {
		int nRetVal = nDefault;
		if (map == null || map.containsKey(key) == false)
			return nDefault;

		
		try {
			String value = map.get(key);
			nRetVal = Integer.parseInt(value);
		} catch (Exception e) {
		}
		return nRetVal;
	}

	public double getDoubleAt(String key, double dDefault) {
		double dRetVal = dDefault;
		if (map == null || map.containsKey(key) == false)
			return dDefault;

		
		try {
			String value = map.get(key);
			dRetVal = Integer.parseInt(value);
		} catch (Exception e) {
		}
		return dRetVal;
	}

	void fini() {
		url = null;;
		cmd = null;
		map.clear();
	}
	
	@Override
	public void close() throws Exception {
		fini();
		FinalizeHelper.suppressFinalize(this);

	}

}
