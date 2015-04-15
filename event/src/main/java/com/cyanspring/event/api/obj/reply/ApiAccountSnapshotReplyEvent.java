package com.cyanspring.event.api.obj.reply;

import com.cyanspring.apievent.obj.Account;
import com.cyanspring.apievent.obj.Execution;
import com.cyanspring.apievent.obj.OpenPosition;
import com.cyanspring.event.api.ApiResourceManager;
import com.cyanspring.event.api.obj.PendingRecord;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
public class ApiAccountSnapshotReplyEvent implements IApiReply {

    @Autowired
    ApiResourceManager resourceManager;

    @Override
    public void sendEventToClient(Object event) {
        AccountSnapshotReplyEvent replyEvent = (AccountSnapshotReplyEvent) event;

        PendingRecord record = resourceManager.getPendingRecord(replyEvent.getTxId());
        if(null == record)
            return;

        if(record.ctx.isOpen()){
            Account account = setAccountData(replyEvent.getAccount());
            List<OpenPosition> positions = setPositionsData(replyEvent.getOpenPositions());
            List<Execution> executions = setExecutionsData(replyEvent.getExecutions());
            record.ctx.send(new com.cyanspring.apievent.reply.AccountSnapshotReplyEvent(
                    replyEvent.getKey(),
                    replyEvent.getReceiver(),
                    account,
                    positions,
                    executions,
                    record.origTxId
            ));
        }
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

    private List<OpenPosition> setPositionsData(List<com.cyanspring.common.account.OpenPosition> positions){
        List<OpenPosition> eventPositions = new ArrayList<OpenPosition>();
        for (com.cyanspring.common.account.OpenPosition position : positions){
            OpenPosition newPosition = new OpenPosition();
            newPosition.setAccount(position.getAccount());
            newPosition.setAcPnL(position.getAcPnL());
            newPosition.setCreated(position.getCreated());
            newPosition.setId(position.getId());
            newPosition.setMargin(position.getMargin());
            newPosition.setPnL(position.getPnL());
            newPosition.setPrice(position.getPrice());
            newPosition.setQty(position.getQty());
            newPosition.setSymbol(position.getSymbol());
            newPosition.setUser(position.getUser());
            eventPositions.add(newPosition);
        }
        return eventPositions;
    }

    private List<Execution> setExecutionsData(List<com.cyanspring.common.business.Execution> executions){
        List<Execution> eventExecutions = new ArrayList<Execution>();
        for (com.cyanspring.common.business.Execution exe : executions){
            Execution newExe = new Execution();
            newExe.setAccount(exe.getAccount());
            newExe.setCreated(exe.getCreated());
            newExe.setExecID(exe.getExecId());
            newExe.setId(exe.getId());
            newExe.setModified(exe.getModified());
            newExe.setOrderID(exe.getOrderId());
            newExe.setParentOrderID(exe.getParentOrderId());
            newExe.setPrice(exe.getPrice());
            newExe.setQuantity(new Double(exe.getQuantity()).longValue());
            newExe.setServerID(exe.getServerId());
            newExe.setSide(exe.getSide().toString());
            newExe.setStrategyID(exe.getStrategyId());
            newExe.setSymbol(exe.getSymbol());
            newExe.setUser(exe.getUser());
            eventExecutions.add(newExe);
        }
        return eventExecutions;
    }
}
