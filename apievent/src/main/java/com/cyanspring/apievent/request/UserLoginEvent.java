package com.cyanspring.apievent.request;

import com.cyanspring.apievent.version.ApiVersion;
import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
/**
 * @author jimmy.cheng
 * @version 1.0.1
 * @since 1.0.1
 * @modify : add version check 
 */
public class UserLoginEvent extends RemoteAsyncEvent {
    private String userId;
    private String password;
    private String txId;
    private ApiVersion version;
    public UserLoginEvent(String key, String receiver, String userId,
                          String password, String txId) {
        super(key, receiver);
        this.userId = userId;
        this.password = password;
        this.txId = txId;
        version = new ApiVersion();
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getTxId() {
        return txId;
    }

	public ApiVersion getVersion() {
		return version;
	}
}
