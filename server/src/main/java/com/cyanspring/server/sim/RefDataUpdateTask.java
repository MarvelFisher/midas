package com.cyanspring.server.sim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataUtil;
import com.cyanspring.common.util.TimeUtil;

public class RefDataUpdateTask extends TimerTask {
	private static final Logger log = LoggerFactory.getLogger(RefDataUpdateTask.class);
	private static Map<String, String[]> map = new HashMap<>();
	private int[] seasons = { 3, 6, 9, 12 };
	private List<IRefDataListener> listeners;
	private List<RefData> list;
	private IRefDataAdaptor adaptor;

	public RefDataUpdateTask(List<IRefDataListener> listeners, List<RefData> list, IRefDataAdaptor adaptor) {
		this.listeners = listeners;
		this.list = list;
		this.adaptor = adaptor;
	}

	@Override
	public void run() {
		try {
			for (IRefDataListener listener : listeners) {
				try {
					listener.onRefData(list);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			adaptor.setStatus(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
