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
package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;

public class ServerStatusLocalEvent extends AsyncEvent {
	String server;
	boolean up;

	public String getServer() {
		return server;
	}

	public ServerStatusLocalEvent(String server, boolean up) {
		super();
		this.server = server;
		this.up = up;
	}

	public boolean isUp() {
		return up;
	}
	
}
