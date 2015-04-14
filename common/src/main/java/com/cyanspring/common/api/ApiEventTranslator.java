package com.cyanspring.common.api;

import com.cyanspring.common.api.obj.reply.IApiReply;
import com.cyanspring.common.api.obj.request.IApiRequest;
import com.cyanspring.common.transport.IUserSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

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

public class ApiEventTranslator {
    private Logger log = LoggerFactory.getLogger(ApiEventTranslator.class);
    public IApiRequest translateRequest(Object object){
        IApiRequest tranObject;
        try {
            String clzName = object.getClass().getName();
            Class<IApiRequest> clz = (Class<IApiRequest>)Class.forName("Api" + clzName);
            Constructor<IApiRequest> constructor = clz.getConstructor();
            tranObject = constructor.newInstance();
        } catch (Exception e){
            tranObject = new IApiRequest() {
                @Override
                public void sendEventToLts(Object event, IUserSocketContext ctx) {
                    log.warn("Unknow class from client");
                }
            };
        }
        return tranObject;
    }

    public IApiReply translateReply(Object object){
        IApiReply tranObject;
        try {
            String clzName = object.getClass().getName();
            Class<IApiReply> clz = (Class<IApiReply>)Class.forName("Api" + clzName);
            Constructor<IApiReply> constructor = clz.getConstructor();
            tranObject = constructor.newInstance();
        } catch (Exception e){
            return null;
        }
        return tranObject;
    }
}
