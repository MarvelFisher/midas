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
package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.ClientEvent;
import com.cyanspring.apievent.obj.Order;

public abstract class ParentOrderReplyEvent extends ClientEvent {
	private boolean ok;
	private String message;
	private String txId;
	private Order order;
	
	public ParentOrderReplyEvent(String key, String receiver, boolean ok,
								 String message, String txId, Order order) {
		super(key, receiver);
		this.ok = ok;
		this.message = message;
		this.txId = txId;
		this.order = order;
	}

	public boolean isOk() {
		return ok;
	}
	public String getMessage() {
		return message;
	}

	public String getTxId() {
		return txId;
	}

	public Order getOrder() {
		return order;
	}

}
