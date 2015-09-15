package com.cyanspring.event.api;

import com.cyanspring.event.api.obj.PendingRecord;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.UserConnectionType;
import com.cyanspring.common.transport.IServerSocketListener;
import com.cyanspring.common.transport.IServerUserSocketService;
import com.cyanspring.common.transport.IUserSocketContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class ApiResourceManager{

    @Autowired
    private IAsyncEventManager eventManager;
    private IServerUserSocketService socketService;

    private UserConnectionType userConnectionType = UserConnectionType.PREEMPTIVE;
    private String bridgeId = "ApiBridge-163168";
    private Map<String, PendingRecord> pendingRecords = new ConcurrentHashMap<String, PendingRecord>();
    private Map<String, Map<String, String>> quoteSubscription = new ConcurrentHashMap<String,  Map<String, String>>();
    private Map<String, String> accountUserMap = new ConcurrentHashMap<String, String>();
    private Map<String, ParentOrder> orders = new ConcurrentHashMap<String, ParentOrder>();

    public void init(IServerSocketListener listener) throws Exception {
        socketService.addListener(listener);
        socketService.init();
    }

	public void uninit(IServerSocketListener listener) {
		socketService.removeListener(listener);
		socketService.uninit();
	}

	public String getBridgeId() {
		return bridgeId;
	}

    public void setBridgeId(String bridgeId) {
        this.bridgeId = bridgeId;
    }

    public void putPendingRecord(String txId, String origTxId, IUserSocketContext ctx){
        PendingRecord record = new PendingRecord(txId, origTxId, ctx);
        pendingRecords.put(record.txId, record);
    }

    public PendingRecord getPendingRecord(String txId){
        return pendingRecords.remove(txId);
    }

    public void saveUser(String accountId, String eventUserId){
        accountUserMap.put(accountId, eventUserId);
    }

    public boolean hasUser(String account){
        return accountUserMap.containsKey(account);
    }

    public void sendEventToManager(RemoteAsyncEvent event) {
        event.setSender(this.getBridgeId());
        eventManager.sendEvent(event);
    }

    public void sendEventToUser(String user, Object event) {
        List<IUserSocketContext> list = socketService.getContextByUser(user);
        for(IUserSocketContext ctx: list) {
            if(ctx.isOpen())
                ctx.send(event);
        }
    }

    public boolean checkAccount(String account, String user) {
        return null != user && user.equals(accountUserMap.get(account));
    }

    public IServerUserSocketService getSocketService() {
        return socketService;
    }

    public void setSocketService(IServerUserSocketService socketService) {
        this.socketService = socketService;
    }

    public void putQuoteSubscription(String symbol, Map<String, String> map){
        quoteSubscription.put(symbol, map);
    }

    public Map<String, String> getSubscriptionMap(String symbol){
        return quoteSubscription.get(symbol);
    }

    public Collection<Map<String, String>> getAllSubscriptionbyList(){
        return quoteSubscription.values();
    }

    public void putOrder(String id, ParentOrder order){
        orders.put(id, order);
    }

    public ParentOrder getOrder(String id){
        return orders.get(id);
    }

    public void removeOrder(String id){
        orders.remove(id);
    }

    public UserConnectionType getUserConnectionType() {
        return userConnectionType;
    }
}
