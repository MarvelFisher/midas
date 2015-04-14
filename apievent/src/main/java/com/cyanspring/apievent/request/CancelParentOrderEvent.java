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
package com.cyanspring.apievent.request;

import com.cyanspring.apievent.ClientEvent;

public final class CancelParentOrderEvent extends ClientEvent {
	private String orderId;
	private String txId;
	private boolean force;
	public CancelParentOrderEvent(String key, String receiver, String orderId, boolean force,
								  String txId) {
		super(key, receiver);
		this.orderId = orderId;
		this.txId = txId;
		this.force = force;
	}
	public String getOrderId() {
		return orderId;
	}
	public String getTxId() {
		return txId;
	}
	public boolean isForce() {
		return force;
	}
	
}
