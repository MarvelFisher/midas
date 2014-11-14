package com.cyanspring.event;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.event.AsyncEventManager;
import com.cyanspring.event.AsyncEventProcessor;

public class AsyncEventProcessorTest extends AsyncEventProcessor {
	IAsyncEventManager eventManager = new AsyncEventManager();
	String message;
	
	final class TestEvent extends AsyncEvent {
		String message;

		public String getMessage() {
			return message;
		}

		public TestEvent(String message) {
			super();
			this.message = message;
		}
	}
	
	final class TestEvent2 extends AsyncEvent {
		String message;

		public String getMessage() {
			return message;
		}

		public TestEvent2(String message) {
			super();
			this.message = message;
		}
	}
	
	public String getMessage() {
		return message;
	}
	
	public void processTestEvent(TestEvent event) {
		this.message = event.getMessage();
	}
	
	@Override
	public IAsyncEventManager getEventManager() {
		return eventManager;
	}
	
	@Override
	public void subscribeToEvents() {
		subscribeToEvent(TestEvent.class, null);
	}

	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
	}	
	
	@Before
	public void before() {
		message = "";
	}
	
	@Test
	public void testEventProcessingSync() throws Exception {
		AsyncEventProcessorTest eventProcessor = new AsyncEventProcessorTest();
		eventProcessor.setSync(true);
		eventProcessor.init();
		TestEvent event = new TestEvent("hello");
		eventProcessor.getEventManager().sendEvent(event);
		assertTrue(eventProcessor.getMessage().equals(event.getMessage()));
		eventProcessor.uninit();
		
	}

	@Test
	public void testEventProcessingAsync() throws Exception {
		AsyncEventProcessorTest eventProcessor = new AsyncEventProcessorTest();
		eventProcessor.init();
		TestEvent event = new TestEvent("hello");
		eventProcessor.getEventManager().sendEvent(event);
		Thread.sleep(100);
		assertTrue(eventProcessor.getMessage().equals(event.getMessage()));
		eventProcessor.uninit();
		
	}

	@Test
	public void testEventProcessingNotHandling() throws Exception {
		AsyncEventProcessorTest eventProcessor = new AsyncEventProcessorTest();
		eventProcessor.setSync(true);
		eventProcessor.init();
		TestEvent2 event = new TestEvent2("hello");
		eventProcessor.getEventManager().sendEvent(event);
		assertTrue(eventProcessor.getMessage() == null);
		eventProcessor.uninit();
		
	}

}
