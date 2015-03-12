package com.cyanspring.id.gateway;

import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Util.BitConverter;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.IdSymbolUtil;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.RingBuffer;
import com.cyanspring.id.Library.Util.StringUtil;
import com.cyanspring.id.Library.Util.Network.SpecialCharDef;
import com.cyanspring.id.gateway.netty.ServerHandler;

public class UserClient implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(IdGateway.class);
	static final int MAX_COUNT = 1024 * 1024;
	String key = createUniqKey();

	Date timeLast = new Date(0);

	public String getKey() {
		return key;
	}

	List<String> refList = new ArrayList<String>();

	ChannelHandlerContext ctx = null;

	Object refLock = new Object();

	boolean gateway = false;

	String ip;

	public String getIp() {
		return ip;
	}

	RingBuffer buffer = new RingBuffer();

	static String createUniqKey() {
		String sTime = DateUtil.formatDate("HHmmss.SSSS");
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.substring(uuid.length() - 4);
		return String.format("%s-%s", sTime, uuid).toUpperCase();
	}

	public boolean isGateway() {
		return gateway;
	}

	public void setGateway(boolean gateway) {
		this.gateway = gateway;
	}

	public UserClient(ChannelHandlerContext context) {
		ip = ServerHandler.getRemotIP(context.channel());
		buffer.init(MAX_COUNT, true);
		ctx = context;
		LogUtil.logInfo(log, "[%s] add new client", key);
	}

	protected void uninit() {
		if (refList != null) {
			refList.clear();
			refList = null;
		}
		if(buffer != null)
		{
			buffer.close();
			buffer = null;			
		}

		ctx = null;
	}

	public boolean isSameContext(ChannelHandlerContext context) {
		return context == ctx;
	}

	public void addRef(String symbol) {

		synchronized (refLock) {
			int pos = Collections.binarySearch(refList, symbol);
			if(pos < 0)
			{
				refList.add(~pos,symbol);
			}
			//if (refList.contains(symbol) == false) {
			//	refList.add(symbol);
			//}
		}
		LogUtil.logInfo(log, "[%s] add ref : %s", key, symbol);
	}

	public void removeRef(String symbol) {

		synchronized (refLock) {
			int pos = Collections.binarySearch(refList, symbol);
			if(pos >= 0)
			{
				refList.remove(pos);
			}			
			//if (refList.contains(symbol) == true) {
			//	refList.remove(symbol);
			//}
		}
		LogUtil.logInfo(log, "[%s] remove ref : %s", key, symbol);
	}

	public void sendData(String symbol, byte[] data) {

		//if (!gateway && refList.contains(symbol) == false) {
		if(!gateway && Collections.binarySearch(refList, symbol) < 0) {
			data = null;
			return;
		}

		if (ctx == null) {
			data = null;
			return;
		}

		timeLast = DateUtil.now();
		ServerHandler.sendData(ctx, symbol, data);
		IdGateway.instance().addSize(IDGateWayDialog.TXT_OutSize, data.length);
		data = null;
	}

	public void onReceive(byte[] srcData) {

		if(buffer == null)
		{
			return;
		}
		
		try {

			buffer.write(srcData, srcData.length);
			srcData = null;
			byte[] data = new byte[6];

			while (true) {				

				if (buffer.getQueuedSize() <= 0
						|| buffer.read(data, 6, false) != 6) {
					break;
				}

				if (data[0] != SpecialCharDef.EOT) {
					buffer.purge(1); // Skip One Byte
					continue;
				}
				if (data[1] != SpecialCharDef.SPC) {
					buffer.purge(1); // Skip One Byte
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
				int dwQueueLength = buffer.getQueuedSize();
				if (iPacketDataLength >= buffer.getBufSize()) {
					buffer.purge(1); // Skip One Byte
					continue;
				}

				if (dwQueueLength >= iPacketDataLength) {
					data = new byte[iPacketDataLength];
					int nSize = buffer.read(data, iPacketDataLength, false);
					if (nSize != iPacketDataLength) {
						break;
					}

					if (data[iPacketDataLength - 1] != SpecialCharDef.ETX) {
						buffer.purge(1); // Skip One Byte
						continue;
					}

					byte[] data2 = new byte[iDataLength];
					System.arraycopy(data, 6, data2, 0, iDataLength);
					buffer.purge(iPacketDataLength);

					String strFrame = new String(data2,
							Charset.defaultCharset());
					data2 = null;
					parse(strFrame);

				} else {
					break;
				}
			}
		} catch (Exception ex) {
			LogUtil.logException(log, ex);
		}
	}

	public void parse(String frame) {
		boolean isRemove = false;
		String[] vec = StringUtil.split(frame, '|');
		int exch = 687;
		for (int i = 0; i < vec.length; i++) {

			String[] vec2 = StringUtil.split(vec[i], '=');
			if (vec2.length != 2)
				continue;

			int nKey = Integer.parseInt(vec2[0]);
			switch (nKey) {
			case 5022:
				if (vec2[1].equals("Subscribe"))
					isRemove = false;
				else if (vec2[1].equals("Unsubscribe"))
					isRemove = true;
				break;
			case 4: {
				try {
					exch = Integer.parseInt(vec2[1]);
				} catch (Exception e) {
				}
			}
				break;
			case 5: {
				String symbol = IdSymbolUtil.toSymbol(vec2[1], exch);
				if (isRemove) {
					removeRef(symbol);
				} else {
					addRef(symbol);
				}
			}
				break;
			case 9999: {
				gateway = 1 == Integer.parseInt(vec2[1]);
			}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void close() throws Exception {

		LogUtil.logInfo(log, "[%s] remove client", key);
		uninit();
		FinalizeHelper.suppressFinalize(this);

	}

	public String toXml() {
		return String.format(
				"<Client ID=\"%s\" IP=\"%s\" Gateway=\"%s\" RefCount=\"%d\" Last=\"%s\"/>%n",
				key, ip, gateway ? "true" : "false",
				refList.size(),
				DateUtil.formatDate(timeLast, "yyyy-MM-dd HH:mm:ss"));
	}
}
