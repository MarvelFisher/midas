package com.cyanspring.event.api.obj.request;

import com.cyanspring.apievent.request.UserLoginEvent;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;

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
public class ApiUserLoginEvent implements IApiRequest {

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToLts(Object event, IUserSocketContext ctx) {
        UserLoginEvent loginEvent = (UserLoginEvent) event;
        String txId = IdGenerator.getInstance().getNextID();
        resourceManager.putPendingRecord(txId, loginEvent.getTxId(), ctx);
        com.cyanspring.common.event.account.UserLoginEvent request = new com.cyanspring.common.event.account.UserLoginEvent(loginEvent.getKey(), null, loginEvent.getUserId(), loginEvent.getPassword(), txId);
        request.setPriority(EventPriority.HIGH);
        request.setSender(resourceManager.getBridgeId());
        resourceManager.sendEventToManager(request);
    }
}
