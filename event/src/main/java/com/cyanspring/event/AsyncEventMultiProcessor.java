package com.cyanspring.event;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.util.RoundRobin;

public abstract class AsyncEventMultiProcessor extends AsyncEventProcessor  {
	private static final Logger log = LoggerFactory
			.getLogger(AsyncEventMultiProcessor.class);
	
	private boolean hash;
	private int threadCount = 10;
	private RoundRobin round;
	private List<AsyncPriorityEventThread> threads = new ArrayList<AsyncPriorityEventThread>();
	
	@Override
	public void init() throws Exception {
		if(!sync && threads.size() <= 0)
			createThread();
		
		subscribeToEvents();
		doSubscription();
	}
	@Override
	public void createThread() {
		round = new RoundRobin(threadCount);
		for(int i=0; i<threadCount; i++) {
			AsyncPriorityEventThread thread = new AsyncPriorityEventThread() {

				@Override
				public void onEvent(AsyncEvent event) {
					AsyncEventMultiProcessor.this.onAsyncEvent(event);
				}
				
			};
			thread.start();
			threads.add(thread);
		}
	}
	
	@Override
	public void uninit() {
		super.uninit();
		for(AsyncPriorityEventThread thread: threads) {
			thread.exit();
		}
		threads.clear();
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(sync) {
			onAsyncEvent(event);
		} else {
			if(hash && null != event.getKey()) {
				threads.get(Math.abs(event.getKey().hashCode() % threads.size())).addEvent(event);
			} else {
				threads.get(round.next()).addEvent(event);
			}
		}
	}
	
	public void setName(String name) {
		for(int i=0; i<threads.size(); i++) {
			threads.get(i).setName(name + "-" + i);
		}
		
	}

	@Override
	public void setSync(boolean sync) {
		if(this.sync && !sync) {// set to async
			createThread();
		}
		if(!this.sync && sync) {
			for(AsyncPriorityEventThread thread: threads) {
				thread.exit();
			}
			threads.clear();
		}
		this.sync = sync;
	}
	
	public int getThreadCount() {
		return threadCount;
	}
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
	public boolean isHash() {
		return hash;
	}
	public void setHash(boolean hash) {
		this.hash = hash;
	}
	
	
}
