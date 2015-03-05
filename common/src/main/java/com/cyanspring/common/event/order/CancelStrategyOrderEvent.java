/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.event.order;

import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CancelStrategyOrderEvent extends RemoteAsyncEvent {
	private String txId;
	private String sourceId;
	private boolean force;
	private OrderReason reason;

	public CancelStrategyOrderEvent(String key, String receiver, String txId,
			String sourceId, OrderReason reason, boolean force) {
		super(key, receiver);
		this.txId = txId;
		this.sourceId = sourceId;
		this.reason = reason;
		this.force = force;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getTxId() {
		return txId;
	}

	public boolean isForce() {
		return force;
	}

	public OrderReason getReason() {
		return reason;
	}
	
}
