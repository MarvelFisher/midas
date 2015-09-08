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

import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AmendParentOrderEvent extends RemoteAsyncEvent {
	private String id;
	private Map<String, Object> fields;
	private String txId;
	private boolean isFromSystem;
	
	public AmendParentOrderEvent(String key, String receiver, String id,
			Map<String, Object> fields, String txId) {
		this(key, receiver, id, fields, txId, false);
	}
	
	public AmendParentOrderEvent(String key, String receiver, String id,
			Map<String, Object> fields, String txId,boolean isFromSystem) {
		super(key, receiver);
		setPriority(EventPriority.HIGH);
		this.id = id;
		this.fields = fields;
		this.txId = txId;
		this.isFromSystem = isFromSystem;
	}

	public String getId() {
		return id;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}

	public boolean isFromSystem() {
		return isFromSystem;
	}

}
