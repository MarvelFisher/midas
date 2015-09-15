package com.cyanspring.transport.tools;

import com.cyanspring.common.transport.ISerialization;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamSerialization implements ISerialization {

	private XStream xstream = new XStream(new DomDriver());

	@Override
	public Object serialize(Object obj) throws IllegalArgumentException {
		if (obj == null) {
			throw new IllegalArgumentException(
					"xstream serialize object is null !");
		}
		return xstream.toXML(obj);
	}

	@Override
	public Object deSerialize(Object obj) throws IllegalArgumentException {
		if (obj == null) {
			throw new IllegalArgumentException(
					"xstream deSerialize object is null !");
		}
		if (obj instanceof String) {
			String xml = (String) obj;
			if (!xml.equals("")) {
				return xstream.fromXML(xml);
			}
			return null;
		} else {
			throw new IllegalArgumentException(
					"xstream deSerialize object is not string !");
		}
	}

}
