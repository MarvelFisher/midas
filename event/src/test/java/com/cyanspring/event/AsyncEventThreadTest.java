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
import com.cyanspring.common.event.AsyncPriorityEventThread;

public class AsyncEventThreadTest {
	AsyncEventThread1 thread = new AsyncEventThread1();
	class AsyncEventThread1 extends AsyncPriorityEventThread {
		public AsyncEvent event;
		@Override
		public void onEvent(AsyncEvent t) {
			this.event = t;
			synchronized(AsyncEventThreadTest.this) {
				AsyncEventThreadTest.this.notify();
			}
		}
		
	};

	class AsyncEvent1 extends AsyncEvent {

		public AsyncEvent1(String key) {
			super(key);
		}
	}
	
	class AsyncEvent2 extends AsyncEvent {

		public AsyncEvent2(String key) {
			super(key);
		}
	}
	
	@Before
	public void Before() {
		thread.start();
	}
	
	@After
	public void After() {
		thread.exit();
	}
	
	@Test
	public void test() throws InterruptedException {
		thread.addEvent(new AsyncEvent1(null));
		synchronized(this) {
			this.wait(100);
		}
		assertTrue(thread.event instanceof AsyncEvent1);
		
		thread.addEvent(new AsyncEvent1(null));
		thread.addEvent(new AsyncEvent2(null));
		Thread.sleep(100);
		assertTrue(thread.event instanceof AsyncEvent2);
		
	}
	
	@Test
	public void testExit() throws InterruptedException {
		thread.exit();
		synchronized(this) {
			this.wait(100);
		}
		assertTrue(!thread.isAlive());
	}
}
