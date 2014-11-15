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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.event.AsyncEventManager;

public class AsyncEventTest {
	AsyncEventManager eventManager = new AsyncEventManager();
	AsyncListner1 listener1 = new AsyncListner1();
	AsyncListner1 listener11 = new AsyncListner1();
	class AsyncEvent1 extends AsyncEvent {

		public AsyncEvent1(String string) {
			super(string);
		}

		public AsyncEvent1() {
			super();
		}
	}
	
	class AsyncEvent2 extends AsyncEvent {

	}
	
	class AsyncListner1 implements IAsyncEventListener{
		public AsyncEvent event;
		public void onEvent(AsyncEvent event) {
			this.event = event;
		}
	};
	
	@Before
	public void Before() {
		eventManager.clearAllSubscriptions();
	}
	
	@After
	public void After() {
	}
	
	
	@Test
	public void testBasic() {
		eventManager.subscribe(AsyncEvent1.class, listener1);
		eventManager.subscribe(AsyncEvent1.class, listener11);
		eventManager.sendEvent(new AsyncEventTest.AsyncEvent1());
		assertTrue(listener1.event instanceof AsyncEventTest.AsyncEvent1);
		assertTrue(listener11.event instanceof AsyncEventTest.AsyncEvent1);
		
		listener1.event = null;
		listener11.event = null;
		eventManager.sendEvent(new AsyncEventTest.AsyncEvent2());
		assertTrue(listener1.event == null);
		assertTrue(listener11.event == null);
		
		eventManager.unsubscribe(AsyncEvent1.class, listener1);
		eventManager.sendEvent(new AsyncEventTest.AsyncEvent1());
		assertTrue(listener1.event == null);
		assertTrue(listener11.event instanceof AsyncEventTest.AsyncEvent1);
	}
	
	@Test
	public void testKeySubscription() {
		eventManager.subscribe(AsyncEvent1.class, "ABC", listener1);
		eventManager.subscribe(AsyncEvent1.class, listener11);
		eventManager.sendEvent(new AsyncEventTest.AsyncEvent1("ABC"));
		assertTrue(listener1.event instanceof AsyncEventTest.AsyncEvent1);
		assertTrue(listener11.event instanceof AsyncEventTest.AsyncEvent1);
		
		listener1.event = null;
		listener11.event = null;
		eventManager.sendEvent(new AsyncEventTest.AsyncEvent1());
		assertTrue(listener1.event == null);
		assertTrue(listener11.event instanceof AsyncEventTest.AsyncEvent1);
		
		eventManager.unsubscribe(AsyncEvent1.class, "ABC", listener1);
		eventManager.sendEvent(new AsyncEventTest.AsyncEvent1("ABC"));
		assertTrue(listener1.event == null);
		assertTrue(listener11.event instanceof AsyncEventTest.AsyncEvent1);
	}
	
	
}
