package com.cyanspring.event.api.obj.reply;

import com.cyanspring.event.api.ApiResourceManager;

/**
 * Description....
 * <ul>
 * <li> Description
 * </ul>
 * <p/>
 * Description....
 * <p/>
 * Description....
 * <p/>
 * Description....
 *
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public interface IApiReply {
    void sendEventToClient(Object event);
    void setResourceManager(ApiResourceManager resourceManager);
}
