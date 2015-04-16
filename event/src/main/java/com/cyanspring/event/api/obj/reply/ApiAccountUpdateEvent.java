package com.cyanspring.event.api.obj.reply;

import com.cyanspring.apievent.obj.Account;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.common.event.account.AccountUpdateEvent;
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
public class ApiAccountUpdateEvent implements  IApiReply {

    private ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        AccountUpdateEvent updateEvent = (AccountUpdateEvent) event;
        com.cyanspring.apievent.reply.AccountUpdateEvent sendEvent =
                new com.cyanspring.apievent.reply.AccountUpdateEvent(updateEvent.getKey(),
                        updateEvent.getReceiver(),
                        setAccountData(updateEvent.getAccount()));
        resourceManager.sendEventToUser(updateEvent.getAccount().getUserId(), sendEvent);
    }

    private Account setAccountData(com.cyanspring.common.account.Account account){
        Account eventAccount = new Account();
        eventAccount.setAllTimePnL(account.getAllTimePnL());
        eventAccount.setCash(account.getCash());
        eventAccount.setCurrency(account.getCurrency());
        eventAccount.setDailyPnL(account.getDailyPnL());
        eventAccount.setMargin(account.getMargin());
        eventAccount.setPnL(account.getPnL());
        eventAccount.setUrPnL(account.getUrPnL());
        eventAccount.setValue(account.getValue());
        return eventAccount;
    }

    @Override
    public void setResourceManager(ApiResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
