package com.cyanspring.id;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.IdSymbolUtil;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.RingBuffer;
import com.cyanspring.id.Library.Util.BitConverter;
import com.cyanspring.id.Library.Util.StringUtil;
import com.cyanspring.id.Library.Util.TimeSpan;
import com.cyanspring.id.Library.Util.Network.SocketUtil;
import com.cyanspring.id.Library.Util.Network.SpecialCharDef;

public class Parser implements IReqThreadCallback {

	private static final Logger log = LoggerFactory
			.getLogger(IdMarketDataAdaptor.class);

	static final int MAX_COUNT = 1024 * 1024;;
	static Parser instance = new Parser();

	public static Parser instance() {
		return instance;
	}

	RingBuffer buffer = new RingBuffer();
	RingBuffer source = new RingBuffer();
	protected RequestThread reqThread = new RequestThread(this, "Parser");

	static Date tLast = new Date(0);

	/**
	 * constructor
	 */
	public Parser() {
		buffer.init(MAX_COUNT, true);
		source.init(MAX_COUNT, true);
		reqThread.start();
	}

	/**
	 * clear Ringbuffer to empty status
	 */
	public void clearRingbuffer() {
		source.purge(-1);
	}

	/**
	 * processData - add data to queue if data packed, unpack first
	 * 
	 * @param time
	 *            receive time
	 * @param data
	 *            received data
	 */
	public void processData(Date time, byte[] data) {
		if (IdMarketDataAdaptor.instance.gateway) {
			source.write(data, data.length);
			ArrayList<byte[]> list = SocketUtil.unPackData(source);
			for (byte[] ba : list) {
				addData(time, ba);
			}
		} else {
			addData(time, data);
		}
		data = null;
	}

	/**
	 * parse :
	 * 
	 * @param srcData
	 */
	public void parse(Date time, byte[] srcData) {

		buffer.write(srcData, srcData.length);
		srcData = null;
		try {
			while (true) {

				byte[] data = new byte[6];

				if (buffer.getQueuedSize() <= 0
						|| buffer.read(data, 6, false) != 6) {
					break;
				}

				if (data[0] != SpecialCharDef.EOT) {
					LogUtil.logError(log, 
							"Parser.Parse szTempBuf[0] != EOT [0x%02x]",
							data[0]);
					buffer.purge(1); // Skip One Byte
					continue;
				}
				if (data[1] != SpecialCharDef.SPC) {
					LogUtil.logError(log, "Parser.Parse szTempBuf[1] != SPC");
					LogUtil.logError(log, "Parser.Parse pop [0x%02x]",
							data[0]);
					buffer.purge(1); // Skip One Byte
					continue;
				}

				int iDataLength = 0;
				try {
					iDataLength = (int) BitConverter.toLong(data, 2,
							data.length);
				} catch (Exception e) {
					LogUtil.logException(log, e);
				}

				int iPacketDataLength = iDataLength + 7;
				int dwQueueLength = buffer.getQueuedSize();
				if (iPacketDataLength >= buffer.getBufSize()) {
					LogUtil.logError(log, 
							"Parser.Parse iPacketDataLength[%d] >= sizeof(szTempBuf)[%d]",
							iPacketDataLength, data.length);
					LogUtil.logError(log, "Parser.Parse pop [0x%02x]",
							data[0]);
					buffer.purge(1); // Skip One Byte
					continue;
				}

				if (dwQueueLength >= iPacketDataLength) {
					data = new byte[iPacketDataLength];
					int nSize = buffer.read(data, iPacketDataLength, false);
					if (nSize != iPacketDataLength) {
						LogUtil.logError(log, 
								"Parser.Parse m_RecvQueue.PeekData Fail! iPacketDataLength[%d]",
								iPacketDataLength);
						break;
					}

					if (data[iPacketDataLength - 1] != SpecialCharDef.ETX) {
						LogUtil.logError(log, 
								"Parser.Parse szTempBuf[iPacketDataLength - 1][0x%02x] != ETX iPacketDataLength = %d",
								data[iPacketDataLength - 1], iPacketDataLength);
						LogUtil.logError(log, "Parser.Parse pop [0x%02x]",
								data[0]);
						buffer.purge(1); // Skip One Byte
						continue;
					}

					byte[] data2 = new byte[iDataLength];
					System.arraycopy(data, 6, data2, 0, iDataLength);
					buffer.purge(iPacketDataLength);

					String str = new String(data2, Charset.defaultCharset());
					//if (str.contains("C:P")) {
					//	LogUtil.logInfo(log, "%s", str);
					//}

					boolean bOk = parseLine(time, str);

					if (!bOk)
						continue;
					
					Date tNow = new Date();
					TimeSpan ts = TimeSpan.getTimeSpan(tNow, tLast);
					if (ts.getTotalSeconds() >= 30) {
						tLast = tNow;
						Util.addMsg("[%d] %s", str.length(), str);
						log.debug(str);
						//Util.setStatus(DateUtil.formatDate("[HH:mm:ss]"));
					}
				} else {
					break;
				}
			}
		} catch (Exception ex) {
			LogUtil.logException(log, ex);
		}
	}

	/**
	 * 
	 * @param strLine
	 * @return
	 */
	boolean parseLine(Date time, String strLine) {

		// final long ldiff = 6 * 24 * 60 * 60 * 1000;
		Hashtable<Integer, String> table = new Hashtable<Integer, String>();
		String strID = "";
		int nDP = 0;

		Date tTime = new Date(0);
		String[] vec = StringUtil.split(strLine, '|');
		int nSource = 0;
		for (int i = 0; i < vec.length; i++) {

			String[] vec2 = StringUtil.split(vec[i], '=');
			if (vec2.length != 2)
				continue;

			int nField;
			nField = Integer.parseInt(vec2[0]);

			switch (nField) {
			case FieldID.SourceID: {
				nSource = Integer.parseInt(vec2[1]);
				//if (nSource != 687) {
				//	return false;
				//}
			}
				break;
			case FieldID.Symbol: {
				
				String sID = new String(vec2[1]);
				sID = IdSymbolUtil.toSymbol(sID, nSource);
				
				if (sID.isEmpty()
						|| QuoteMgr.instance().checkSymbol(sID) == false)
					return false;

				strID = sID;
			}
				break;
			case FieldID.DisplayPrecision: {
				nDP = Integer.parseInt(vec2[1]);
			}
				break;
			case FieldID.LastTradeTime: {

				tTime = new Date((long) (Double.parseDouble(vec2[1]) * 1000));
			}
				break;
			case FieldID.LastActivityTime:
			case FieldID.QuoteTime: {
				if (0 == tTime.getTime()) {
					tTime = new Date(
							(long) (Double.parseDouble(vec2[1]) * 1000));
				}
			}
				break;
			default: {
				table.put(nField, vec2[1]);
			}
				break;
			}
		}

		// Date tGmtTime = DateUtil.toGmt(tTime);
		// System.out.printf("Time : %s\n", DateUtil.formatDate(tGmtTime,
		// "yyyy-MM-dd HH:mm:ss"));

		if (strID.isEmpty()) {
			return false;
		}

		if (0 == tTime.getTime()) {
			return false;
		}

		// check if is Refresh
		// refresh frame must skipped else the tick time may cause sunrise
		// incorrectly
		if (table.containsKey(FieldID.CycleMessageIndicator)) {
			//log.info(DateUtil
			//		.formatDate("--------------yyyy-MM-dd-HH-mm-ss-SSS----------------"));
			//SymbolItem item = QuoteMgr.instance().getItem(strID);
			//if (item != null) {
			//	item.parseRefresh(time, tTime, nDP, table);
			//	return true;
			//}
			return false;
		}

		IdMarketDataAdaptor adaptor = IdMarketDataAdaptor.instance;
		adaptor.setTime(tTime);
		int nStatus = adaptor.getStatus(tTime);
		
		if (MarketStatus.CLOSE == nStatus) {
			if (false == adaptor.getIsClose()) {
				if (adaptor.getStatus() == MarketStatus.CLOSE) {
					adaptor.setIsClose(true);
					QuoteMgr.instance().writeFile(true, true);
				}
			}
			return false;
		}

		if (true == adaptor.getIsClose()) {
			if (nStatus == MarketStatus.PREOPEN || nStatus == MarketStatus.OPEN ) {
				adaptor.setIsClose(false);
				QuoteMgr.instance().sunrise();
				QuoteMgr.instance().writeFile(false, true);
			}
		}
		
		// Get ForexData and process ForexData
		SymbolItem item = QuoteMgr.instance().getItem(strID);
		if (item != null) {
			item.parseTick(time, tTime, nDP, table);
			return true;
		}

		return false;

	}

	/**
	 * 
	 * @return
	 */
	public int getQueueSize() {
		return reqThread.getQueueSize();
	}

	/**
	 * 
	 * @param data
	 */
	public void addData(Date time, byte[] data) {
		reqThread.addRequest(new Object[] { time, data });
		data = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cyanspring.id.Library.Threading.IReqThreadCallback#onStartEvent(com
	 * .cyanspring.id.Library.Threading.RequestThread)
	 */
	@Override
	public void onStartEvent(RequestThread sender) {
		try {
			throw new Exception("NotImplementedException");
		} catch (Exception e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cyanspring.id.Library.Threading.IReqThreadCallback#onRequestEvent
	 * (com.cyanspring.id.Library.Threading.RequestThread, java.lang.Object)
	 */
	@Override
	public void onRequestEvent(RequestThread sender, Object reqObj) {
		Object[] arrObjects = (Object[]) reqObj;
		if (arrObjects.length != 2) {
			reqObj = null;
			arrObjects = null;
			return;
		}

		Date time = (Date) arrObjects[0];
		byte[] data = (byte[]) arrObjects[1];
		try {
			instance.parse(time, data);
		} catch (Exception ex) {
			LogUtil.logException(log, ex);
		}
		data = null;
		reqObj = null;
		arrObjects = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cyanspring.id.Library.Threading.IReqThreadCallback#onStopEvent(com
	 * .cyanspring.id.Library.Threading.RequestThread)
	 */
	@Override
	public void onStopEvent(RequestThread sender) {

		try {
			throw new Exception("NotImplementedException");
		} catch (Exception e) {
		}
	}
}
