package com.cyanspring.id.Library.Threading;

public interface IReqThreadCallback {

	void onStartEvent(RequestThread sender);
	void onRequestEvent(RequestThread sender, Object reqObj);
	void onStopEvent(RequestThread sender);

}
