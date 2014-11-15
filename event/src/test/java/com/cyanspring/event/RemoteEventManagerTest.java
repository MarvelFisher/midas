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

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.event.RemoteEventManager;
import com.cyanspring.transport.ActiveMQObjectService;

//@ContextConfiguration(locations = { "classpath:META-INFO/spring/RemoteEventManagerTest.xml" })
//@RunWith(SpringJUnit4ClassRunner.class)
public class RemoteEventManagerTest {
	static RemoteEventManager mgr1;
	static RemoteEventManager mgr2;
	static RemoteEventManager mgr3;
	class Event1 extends RemoteAsyncEvent {

		public Event1(String key, String receiver) {
			super(key, receiver);
		}
		
	}
	class Event2 extends RemoteAsyncEvent {

		public Event2(String key, String receiver) {
			super(key, receiver);
		}
		
	}
	
	class RemoteListener implements IAsyncEventListener {
		public AsyncEvent event;
		public void onEvent(AsyncEvent event) {
			System.out.println(event);
			this.event = event;
			synchronized(this) {
				this.notify();
			}
		}
	};
	RemoteListener listener1 = new RemoteListener();
	RemoteListener listener2 = new RemoteListener();
	RemoteListener listener3 = new RemoteListener();
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		mgr1 = new RemoteEventManager(new ActiveMQObjectService());	
		mgr1.setEmbedBroker(true);
		mgr1.init("broadcast", "inbox1");
		mgr1.addEventChannel("broadcast");
		mgr2 = new RemoteEventManager(new ActiveMQObjectService());	
		mgr2.init("broadcast", "inbox2");
		mgr2.addEventChannel("broadcast");
		mgr3 = new RemoteEventManager(new ActiveMQObjectService());		
		mgr3.init("broadcast", "inbox3");
		mgr3.addEventChannel("broadcast");
	}
	
	@AfterClass
	public static void AfterClass() throws Exception {
		mgr3.close();
		mgr2.close();
		mgr1.close();
	}

	@Before
	public void Before() throws Exception {
		listener1.event = null;
		listener2.event = null;
		listener3.event = null;
	}
	
	@After
	public void After() throws Exception {
		mgr1.clearAllSubscriptions();
		mgr2.clearAllSubscriptions();
		mgr3.clearAllSubscriptions();
	}

	
	@Test
	public void testRemoteEvent() throws Exception {
		mgr1.subscribe(Event1.class, listener1);
		mgr2.subscribe(Event1.class, listener2);
		mgr3.subscribe(Event1.class, listener3);
		mgr1.sendRemoteEvent(new Event1("", null));
		synchronized(listener1) {
			listener1.wait(100);
		}
		synchronized(listener2) {
			listener2.wait(100);
		}
		synchronized(listener3) {
			listener3.wait(100);
		}
		assertTrue(listener1.event == null);
		assertTrue(listener2.event != null);
		assertTrue(listener3.event != null);
	}
	
	@Test
	public void testGlobalEvent() throws Exception {
		mgr1.subscribe(Event1.class, listener1);
		mgr2.subscribe(Event1.class, listener2);
		mgr3.subscribe(Event1.class, listener3);
		mgr1.sendGlobalEvent(new Event1("", null));
		synchronized(listener1) {
			listener1.wait(100);
		}
		synchronized(listener2) {
			listener2.wait(100);
		}
		synchronized(listener3) {
			listener3.wait(100);
		}
		assertTrue(listener1.event != null);
		assertTrue(listener2.event != null);
		assertTrue(listener3.event != null);
	}

	@Test
	public void testPointEvent() throws Exception {
		mgr1.subscribe(Event1.class, listener1);
		mgr2.subscribe(Event1.class, listener2);
		mgr3.subscribe(Event1.class, listener3);
		mgr1.sendRemoteEvent(new Event1("", "inbox3"));
		synchronized(listener1) {
			listener1.wait(100);
		}
		synchronized(listener2) {
			listener2.wait(100);
		}
		synchronized(listener3) {
			listener3.wait(100);
		}
		assertTrue(listener1.event == null);
		assertTrue(listener2.event == null);
		assertTrue(listener3.event != null);
	}


}
