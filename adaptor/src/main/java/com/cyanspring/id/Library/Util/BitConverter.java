package com.cyanspring.id.Library.Util;

/**
 * object : to convert value and bytes for VC++
 *    
 * unsigned byte
 *  
 * @author fdt14004
 *
 */
public class BitConverter {

	/**********************************************************
	 * 
	 * int to Byte[]
	 * 
	 ***********************************************************/

	public static byte[] toBytes(int nValue, int nLen) {

		return toBytes(nValue, nLen, true);
	}

	public static byte[] toBytes(int nValue, boolean littleEndian) {

		return toBytes(nValue, 4, littleEndian);
	}

	/**
	 * convert int to UNSIGNED byte[]
	 * 
	 * @param nValue
	 * @param nLen
	 * @param littleEndian
	 *            if true little endian else big endian
	 * @return
	 */
	public static byte[] toBytes(int nValue, int nLen, boolean littleEndian) {
		// nLen = 4;
		byte[] ret = new byte[nLen];
		if (littleEndian) {
			ret[nLen - 1] = (byte) (nValue & 0xFF);
			ret[nLen - 2] = (byte) ((nValue >> 8) & 0xFF);
			ret[nLen - 3] = (byte) ((nValue >> 16) & 0xFF);
			ret[nLen - 4] = (byte) ((nValue >> 24) & 0xFF);
		} else {
			ret[nLen - 4] = (byte) (nValue & 0xFF);
			ret[nLen - 3] = (byte) ((nValue >> 8) & 0xFF);
			ret[nLen - 2] = (byte) ((nValue >> 16) & 0xFF);
			ret[nLen - 1] = (byte) ((nValue >> 24) & 0xFF);
		}
		return ret;
	}

	/************************************************************
	 * 
	 * byte[] to data
	 * 
	 ************************************************************/
	public static int ToUnsigned(int nValue) {
		return nValue >= 0 ? nValue : nValue + 256;
	}

	/**
	 * convert UNSIGNED byte[] to int (littleEndian is true)
	 * @param data
	 * @param idx
	 * @param nLen
	 * @return
	 * @throws Exception
	 */
	public static long toLong(byte[] data, int idx, long nLen) throws Exception {
		return toLong(data, idx, nLen, true);
	}

	/**
	 * convert UNSIGNED byte[] to int
	 * @param data
	 * @param idx
	 * @param nLen
	 * @param littleEndian
	 * @return
	 * @throws Exception
	 */
	public static long toLong(byte[] data, int idx, long nLen,
			boolean littleEndian) throws Exception {
		if (idx + 4 > nLen)
			throw new Exception("Out of Bound Exception");

		if (littleEndian) {
			return ToUnsigned(data[idx]) << 24
					| ToUnsigned(data[idx + 1]) << 16
					| ToUnsigned(data[idx + 2]) << 8
					| ToUnsigned(data[idx + 3]);
		} else {
			return ToUnsigned(data[idx + 3]) << 24
					| ToUnsigned(data[idx + 2]) << 16
					| ToUnsigned(data[idx + 1]) << 8
					| ToUnsigned(data[idx]);
		}
	}
	
	public static int toInt(byte[] data, int idx, long nLen) throws Exception {
		return toInt(data, idx, nLen, true);
	}

	public static int toInt(byte[] data, int idx, long nLen, boolean littleEndian) throws Exception {
		if (idx + 2 > nLen)
			throw new Exception("Out of Bound Exception");

		if (littleEndian) {
			return ToUnsigned(data[idx]) << 8
					| ToUnsigned(data[idx + 1]);
		} else {
			return ToUnsigned(data[idx + 1]) << 8
					| ToUnsigned(data[idx]);
		}
	}
	
	public static String toString(byte[] data, int idx, int nCount, long nLen)
			throws Exception {
		if (idx + nCount > nLen)
			throw new Exception("Out of Bound Exception");

		String sValue = "";

		for (int i = 0; i < nCount; i++)
			sValue += (char) data[idx + i];

		return sValue.trim();
	}
}
