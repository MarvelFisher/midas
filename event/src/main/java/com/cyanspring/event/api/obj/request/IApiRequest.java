package com.cyanspring.event.api.obj.request;

import com.cyanspring.common.transport.IUserSocketContext;
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
public interface IApiRequest {
    void sendEventToLts(Object event, IUserSocketContext ctx);
    void setResourceManager(ApiResourceManager resourceManager);
}
