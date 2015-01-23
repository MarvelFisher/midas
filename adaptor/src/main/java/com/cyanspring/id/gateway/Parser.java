package com.cyanspring.id.gateway;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.IdSymbolUtil;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.RingBuffer;
import com.cyanspring.id.Library.Util.BitConverter;
import com.cyanspring.id.Library.Util.Network.SocketUtil;
import com.cyanspring.id.Library.Util.Network.SpecialCharDef;
import com.cyanspring.id.gateway.netty.ServerHandler;

public class Parser implements IReqThreadCallback {
	private static final Logger log = LoggerFactory.getLogger(IdGateway.class);
	static final int MAX_COUNT = 1024 * 1024;;
	static Parser _Instance = new Parser();
	RequestThread reqThread = new RequestThread(this, "Parser");

	public static Parser Instance() {
		return _Instance;
	}

	RingBuffer m_buffer = new RingBuffer();
	RingBuffer m_source = new RingBuffer();
	static Date tLast = new Date(0);

	public Parser() {
		m_buffer.init(MAX_COUNT, true);
		m_source.init(MAX_COUNT, true);
		reqThread.start();
	}

	public void addData(byte[] data) {
		reqThread.addRequest(data);
	}


	public void clearRingbuffer() {
		m_buffer.purge(-1);
	}

	public void processData(byte[] data) {
		if (IdGateway.instance().isGateway()) {
			m_source.write(data, data.length);
			ArrayList<byte[]> list = SocketUtil.unPackData(m_source);
			for (byte[] ba : list) {
				addData(ba);
			}
		} else {
			addData(data);
		}
	}

	public void Partse(byte[] srcData) {

		m_buffer.write(srcData, srcData.length);
		try {

			while (true) {

				byte[] data = new byte[6];

				if (m_buffer.getQueuedSize() <= 0
						|| m_buffer.read(data, 6, false) != 6) {
					break;
				}

				if (data[0] != SpecialCharDef.EOT) {
					LogUtil.logError(log,
							"Parser.Parse szTempBuf[0] != EOT [0x%02x]",
							data[0]);
					m_buffer.purge(1); // Skip One Byte
					continue;
				}

				if (data[1] != SpecialCharDef.SPC) {
					LogUtil.logError(log, "Parser.Parse szTempBuf[1] != SPC");
					LogUtil.logError(log, "Parser.Parse pop [0x%02x]", data[0]);
					m_buffer.purge(1); // Skip One Byte
					continue;
				}

				int iDataLength = 0;
				try {
					iDataLength = (int) BitConverter.toLong(data, 2,
							data.length);

				} catch (Exception e) {
					LogUtil.logError(log, e.getMessage());
					LogUtil.logException(log, e);
				}

				int iPacketDataLength = iDataLength + 7;
				int dwQueueLength = m_buffer.getQueuedSize();
				if (iPacketDataLength >= m_buffer.getBufSize()) {
					LogUtil.logError(
							log,
							"Parser.Parse iPacketDataLength[%d] >= sizeof(szTempBuf)[%d]",
							iPacketDataLength, data.length);
					LogUtil.logError(log, "Parser.Parse pop [0x%02x]", data[0]);
					m_buffer.purge(1); // Skip One Byte
					continue;
				}

				if (dwQueueLength >= iPacketDataLength) {
					data = new byte[iPacketDataLength];
					int nSize = m_buffer.read(data, iPacketDataLength, false);
					if (nSize != iPacketDataLength) {
						LogUtil.logError(
								log,
								"Parser.Parse m_RecvQueue.PeekData Fail! iPacketDataLength[%d]",
								iPacketDataLength);
						break;
					}

					if (data[iPacketDataLength - 1] != SpecialCharDef.ETX) {
						LogUtil.logError(
								log,
								"Parser.Parse szTempBuf[iPacketDataLength - 1][0x%02x] != ETX iPacketDataLength = %d",
								data[iPacketDataLength - 1], iPacketDataLength);
						LogUtil.logError(log, "Parser.Parse pop [0x%02x]",
								data[0]);
						m_buffer.purge(1); // Skip One Byte
						continue;
					}

					byte[] data2 = new byte[iDataLength];
					System.arraycopy(data, 6, data2, 0, iDataLength);
					m_buffer.purge(iPacketDataLength);

					String str = new String(data2, Charset.defaultCharset());
					int nCmd = str.indexOf("|5001=");
					if (nCmd >= 0) {
						LogUtil.logInfo(log, str);
					}
					
					int nStartIdx = str.indexOf("|4=");
					if (nStartIdx < 0)
						continue;
					
					int nEndIdx = str.indexOf("|", nStartIdx + 3);
					if (nEndIdx < 0)
						continue;
					
					String source = str.substring(nStartIdx + 3, nEndIdx);
					int sourceInt = Integer.parseInt(source);
					
					nStartIdx = str.indexOf("|5=");
					if (nStartIdx < 0)
						continue;

					nEndIdx = str.indexOf("|", nStartIdx + 3);
					if (nEndIdx < 0)
						continue;

					String symbol = str.substring(nStartIdx + 3, nEndIdx);
/*
 * following code is for testing 					
					if (symbol.equals("X:SUSDJPY")) {
						symbol = "C:PXAUUSDOZ\\SP";
						sourceInt = 691;
					}
*/					
					symbol = IdSymbolUtil.toSymbol(symbol, sourceInt);		

					QuoteMgr.Instance().updateAllSymbol(symbol);
					
					if (QuoteMgr.Instance().checkSymbol(symbol) == false) {
						continue;
					}

					//ServerHandler.asyncSendData(symbol, data);
					ServerHandler.sendData(symbol, data);

				} else {
					break;
				}
			}
		} catch (Exception ex) {

			LogUtil.logError(log, "%s", ex.getMessage());
			LogUtil.logException(log, ex);
		}
	}

	@Override
	public void onStartEvent(RequestThread sender) {
		
	}

	@Override
	public void onRequestEvent(RequestThread sender, Object reqObj) {
		byte[] data = (byte[]) reqObj;
		try {
			Partse(data);
		} catch (Exception ex) {
			LogUtil.logException(log, ex);
		}
		
	}

	@Override
	public void onStopEvent(RequestThread sender) {
		
	}
}
