package com.cyanspring.event.api.obj.reply;

import com.cyanspring.common.account.Account;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.event.api.obj.PendingRecord;
import com.cyanspring.common.event.UserConnectionType;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.system.SystemErrorEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.transport.IUserSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
public class ApiUserLoginReplyEvent implements IApiReply {
    private static Logger log = LoggerFactory.getLogger(ApiUserLoginReplyEvent.class);

    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        UserLoginReplyEvent loginReplyEvent = (UserLoginReplyEvent) event;
        PendingRecord record = resourceManager.getPendingRecord(loginReplyEvent.getTxId());
        if(null == record)
            return;

        if(loginReplyEvent.isOk()) {
            String user = loginReplyEvent.getUser().getId();
            if(resourceManager.getUserConnectionType() == UserConnectionType.PREEMPTIVE) {
                List<IUserSocketContext> list = resourceManager.getSocketService().getContextByUser(user);
                for(IUserSocketContext ctx: list) {
                    log.info("Terminate existing connection: " + ctx.getUser() + ", " + ctx.getId());
                    ctx.close();
                }
            } else if (resourceManager.getUserConnectionType() == UserConnectionType.BLOCKING) {
                List<IUserSocketContext> list = resourceManager.getSocketService().getContextByUser(user);
                for(IUserSocketContext ctx: list) {
                    if(ctx.isOpen()) {
                        log.info("Terminate current connection since there is existing connection: "
                                + ctx.getUser() + ", " + ctx.getId());

                        record.ctx.send(new SystemErrorEvent(null, null, 301, MessageLookup.buildEventMessage(ErrorMessage.USER_NEED_LOGIN_BEFORE_EVENTS, "")));
                        record.ctx.close();
                    }
                }
            }

            resourceManager.getSocketService().setUserContext(loginReplyEvent.getUser().getId(), record.ctx);

            if(loginReplyEvent.getDefaultAccount() != null)
                resourceManager.saveUser(loginReplyEvent.getDefaultAccount().getId(), loginReplyEvent.getUser().getId());
            else
                resourceManager.saveUser(loginReplyEvent.getAccounts().get(0).getId(), loginReplyEvent.getUser().getId());
            for(Account account: loginReplyEvent.getAccounts()) {
                resourceManager.saveUser(account.getId(), loginReplyEvent.getUser().getId());
            }

        }

        com.cyanspring.apievent.reply.UserLoginReplyEvent reply = new com.cyanspring.apievent.reply.UserLoginReplyEvent(
                loginReplyEvent.getKey(),
                null,
                loginReplyEvent.isOk(), loginReplyEvent.getMessage(), record.origTxId);

        if(record.ctx.isOpen())
            record.ctx.send(reply);

        log.info("API user login: " + loginReplyEvent.getUser().getId() + ", " + loginReplyEvent.isOk());
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
