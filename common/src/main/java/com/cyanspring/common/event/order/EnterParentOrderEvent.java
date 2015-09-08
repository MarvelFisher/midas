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

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class EnterParentOrderEvent extends RemoteAsyncEvent {
	private Map<String, Object> fields;
	private String txId;
	private boolean fix;
	private boolean isFromSystem;
	
	public EnterParentOrderEvent(String key, String receiver,
			Map<String, Object> fields, String txId, boolean fix) {
		this(key, receiver, fields, txId, fix,false);
	}
	
	public EnterParentOrderEvent(String key, String receiver,
			Map<String, Object> fields, String txId, boolean fix,boolean isFromSystem) {
		super(key, receiver);
		this.fields = fields;
		this.txId = txId;
		this.fix = fix;
		this.isFromSystem = isFromSystem;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}

	public boolean isFix() {
		return fix;
	}

	public boolean isFromSystem() {
		return isFromSystem;
	}
}
