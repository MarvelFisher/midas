package com.cyanspring.id.Library.Util.Network;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Util.BitConverter;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.RingBuffer;
import com.cyanspring.id.Library.Util.ZipUtil;

/**
 *  packet data format
 *  
 *  1) 0x02 + '0' + 4 bytes length + unzip data + 0x03
 *  
 *  2) 0x02 + '1' + 4 bytes length + zipped data + 0x03
 *  
 * @author Hudson Chen
 *
 */
public class SocketUtil {
	
	private static final Logger log = LoggerFactory.getLogger(SocketUtil.class);
	
	public static byte[] packData(byte[] data) {
		int nZip = (data.length > 1024) ? 1 : 0;
		try (ByteArrayOutputStream bs = new ByteArrayOutputStream()) {
			if (nZip == 1) {
				bs.write(new byte[] { SpecialCharDef.STX });
				bs.write(new byte[] { '1' });
				byte[] zipdata = ZipUtil.compress(ZipUtil.BZIP2, data);
				byte[] arLen = BitConverter.toBytes(zipdata.length, 4);
				bs.write(arLen);
				bs.write(zipdata);
				bs.write(new byte[] { 0x03 });
			} else {
				bs.write(new byte[] { 0x02 });
				bs.write(new byte[] { '0' });
				byte[] arLen = BitConverter.toBytes(data.length, 4);
				bs.write(arLen);
				bs.write(data);
				bs.write(new byte[] { 0x03 });
			}
			return bs.toByteArray();
		} catch (Exception ex) {
			LogUtil.logException(log, ex);
		}
		data = null;
		return null;

	}

	public static ArrayList<byte[]> unPackData(RingBuffer buffer) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		try {
			while (true) {

				byte[] data = new byte[6];

				if (buffer.getQueuedSize() <= 0
						|| buffer.read(data, 6, false) != 6) {
					break;
				}

				if (data[0] != SpecialCharDef.STX) {
					LogUtil.logTrace(log, "Parser.Parse szTempBuf[0] != EOT [0x%02x]",
							data[0]);
					buffer.purge(1); // Skip One Byte
					continue;
				}
				if (data[1] != '0' && data[1] != '1') {
					LogUtil.logTrace(log, "Parser.Parse szTempBuf[1] != SPC");
					LogUtil.logTrace(log, "Parser.Parse pop [0x%02x]", data[0]);
					buffer.purge(1); // Skip One Byte
					continue;
				}

				boolean bZip = data[1] == '1';

				int iDataLength = 0;
				try {
					iDataLength = (int) BitConverter.toLong(data, 2, data.length);
					// Logger.logDebug("%02X-%02X-%02X-%02X ==> %d Size", data[2],
					//		data[3], data[4], data[5], iDataLength);

				} catch (Exception e) {
					LogUtil.logException(log, e);
				}

				int iPacketDataLength = iDataLength + 7;
				int dwQueueLength = buffer.getQueuedSize();
				if (iPacketDataLength >= buffer.getBufSize()) {
					LogUtil.logError(log, 						
							"Parser.Parse iPacketDataLength[%d] >= sizeof(szTempBuf)[%d]",
							iPacketDataLength, data.length);
					LogUtil.logError(log, "Parser.Parse pop [0x%02x]", data[0]);
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
						LogUtil.logTrace(log, 
								"Parser.Parse szTempBuf[iPacketDataLength - 1][0x%02x] != ETX iPacketDataLength = %d",
								data[iPacketDataLength - 1], iPacketDataLength);
						LogUtil.logTrace(log, "Parser.Parse pop [0x%02x]", data[0]);
						buffer.purge(1); // Skip One Byte
						continue;
					}

					byte[] data2 = new byte[iDataLength];
					System.arraycopy(data, 6, data2, 0, iDataLength);
					buffer.purge(iPacketDataLength);

					if (bZip) {
						//Logger.logDebug("try unzip data Length = %d", data2.length);
						byte[] data3 = ZipUtil.decompress(ZipUtil.BZIP2, data2);
						list.add(data3);
					}
					else {
						list.add(data2);
					}

				} else {
					break;
				}
			}

		} catch (Exception ex) {
			LogUtil.logException(log, ex);
		}
		return list;
	}	
}

