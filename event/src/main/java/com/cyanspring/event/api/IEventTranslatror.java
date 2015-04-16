package com.cyanspring.event.api;

import com.cyanspring.event.api.obj.reply.IApiReply;
import com.cyanspring.event.api.obj.request.IApiRequest;

import java.util.Map;

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
public interface IEventTranslatror {
    void init(Map<String, String> requestMap, Map<String, String> replyMap);
    IApiRequest translateRequest(Object object);
    IApiReply translateReply(Object object);
}
