package com.cyanspring.common.api.obj.request;

import com.cyanspring.common.transport.IUserSocketContext;

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

}
