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

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncExecuteEvent;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.IAsyncEventListener;


public abstract class AsyncExecuteEventThread extends AsyncPriorityEventThread implements IAsyncEventInbox {
	
	public void addEvent(AsyncEvent event,
			IAsyncEventListener listener) {
		addEvent(new AsyncExecuteEvent(listener, event));
	}

	protected abstract void onNormalEvent(AsyncEvent event);
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof AsyncExecuteEvent) {
			AsyncExecuteEvent executeEvent = (AsyncExecuteEvent)event;
			executeEvent.getInnerListener().onEvent(executeEvent.getInnerEvent());
		} else {
			onNormalEvent(event);
		}
	}

}
