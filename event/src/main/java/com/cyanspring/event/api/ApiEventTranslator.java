package com.cyanspring.event.api;

import com.cyanspring.event.api.obj.reply.IApiReply;
import com.cyanspring.event.api.obj.request.IApiRequest;
import com.cyanspring.common.transport.IUserSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
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

public class ApiEventTranslator implements IEventTranslatror{
    private Logger log = LoggerFactory.getLogger(ApiEventTranslator.class);
    private Map<String, String> requestMap;
    private Map<String, String> replyMap;

    @Override
    public void init(Map<String, String> requestMap, Map<String, String> replyMap) {
        this.requestMap = requestMap;
        this.replyMap = replyMap;
    }

    @Override
    public IApiRequest translateRequest(Object object){
        IApiRequest tranObject;
        try {
            String clzName = object.getClass().getSimpleName();
            if (!requestMap.containsKey(clzName))
                return null;
            tranObject = (IApiRequest)Class.forName(requestMap.get(clzName)).newInstance();
        } catch (Exception e){
            log.info(e.getMessage());
            return null;
        }
        return tranObject;
    }

    @Override
    public IApiReply translateReply(Object object){
        IApiReply tranObject;
        try {
            String clzName = object.getClass().getSimpleName();
            if (!replyMap.containsKey(clzName))
                return null;
            tranObject = (IApiReply)Class.forName(replyMap.get(clzName)).newInstance();
        } catch (Exception e){
            return null;
        }
        return tranObject;
    }
}
