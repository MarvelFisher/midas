package com.cyanspring.event.api;

import com.cyanspring.event.api.obj.reply.IApiReply;
import com.cyanspring.event.api.obj.request.IApiRequest;

import java.util.Map;

/**
 * The interface of event translator which is use for LTS API
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public interface IEventTranslatror {
    void init(Map<String, String> requestMap, Map<String, String> replyMap);
    IApiRequest translateRequest(Object object);
    IApiReply translateReply(Object object);
}
