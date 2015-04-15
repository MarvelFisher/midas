/**
 * ****************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * <p/>
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * ****************************************************************************
 */
package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ParentOrderUpdateEvent extends RemoteAsyncEvent {
    private String txId;
    private Order order;
    private String info;

    public ParentOrderUpdateEvent(String key, String receiver,
                                  String txId, Order order, String info) {
        super(key, receiver);
        this.txId = txId;
        this.order = order;
        this.info = info;
    }

    public Order getOrder() {
        return order;
    }

    public String getTxId() {
        return txId;
    }

    public String getInfo() {
        return info;
    }

}
