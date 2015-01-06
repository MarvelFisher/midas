package com.cyanspring.common.downstream;

import com.cyanspring.common.data.DataObject;

public interface IOrderRouter {
	IDownStreamSender setRoute(DownStreamManager downStreamManager, DataObject data) throws Exception;
}
