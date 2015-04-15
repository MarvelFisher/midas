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

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Map;

public class AmendParentOrderEvent extends RemoteAsyncEvent {
	private String id;
	private Map<String, Object> fields;
	private String txId;
	
	public AmendParentOrderEvent(String key, String receiver, String id,
								 Map<String, Object> fields, String txId) {
		super(key, receiver);
		this.id = id;
		this.fields = fields;
		this.txId = txId;
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
	
}
