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
package com.cyanspring.event;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class BtRemoteEventManager extends RemoteEventManager {
	
	public BtRemoteEventManager() {
		
	}

	@Override
	public void init(String channel, String inbox)throws Exception  {
	}
	
	public void publishRemoteEvent(String channel, RemoteAsyncEvent event) throws Exception {
		super.sendEvent(event);
	}
	
	public void uninit() {
	}
	
	public void close() throws Exception {
	}
	
	public void sendLocalOrRemoteEvent(RemoteAsyncEvent event) throws Exception {
		sendEvent(event);
	}
	
	public void sendRemoteEvent(RemoteAsyncEvent event) throws Exception {
		super.sendEvent(event);
	}
	
	public void sendGlobalEvent(RemoteAsyncEvent event) throws Exception {
		super.sendEvent(event);
	}
}
