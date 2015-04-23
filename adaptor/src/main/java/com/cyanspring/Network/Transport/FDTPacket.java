package com.cyanspring.Network.Transport;

public class FDTPacket {
	public final static int  PKT_HEAD_SIZE = 4;			// LEAD_CODE(1) + ZIP_FALG(1) + LEN(2) 
	public final static int  PKT_TAIL_SIZE = 2;			// CRC_CODE(1) + TAIL_CODE(1) 
	public final static byte PKT_LEAD 	= 0x02;			// 每筆封包開頭
	public final static byte PKT_END 	= 0x03;			// 每筆封包結尾
	public final byte PKT_ZIP 	= 0x01;			// 資料壓縮
	public final static byte PKT_NOTZIP = 0x00;			// 資料未壓縮	
	public final int  PKT_COMPRESS_SIZE = 256;	// 發送封包的壓縮基準	
}
