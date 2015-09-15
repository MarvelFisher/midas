package com.cyanspring.common.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ISerialization {

	Logger log = LoggerFactory.getLogger(ISerialization.class);

	Object serialize(Object obj) throws Exception;

	Object deSerialize(Object obj) throws Exception;

}
