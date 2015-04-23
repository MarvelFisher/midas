package com.cyanspring.Network.Transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class FDTFrameEncoder extends MessageToByteEncoder<Object> {

	private static Logger log = LoggerFactory.getLogger(com.cyanspring.Network.Transport.FDTFrameEncoder.class);
	@Override
	protected void encode(ChannelHandlerContext ctx, Object obj,ByteBuf buf) throws Exception { // (3)

		try
		{
			//MsgPackParser message = (MsgPackParser)object;
			//int datalen = message.toByteArray().length;
			//byte[] data = message.toByteArray();			
			String message = (String)obj;
			byte[] data = message.getBytes();
			int datalen = data.length;
		   	byte byChkSum = 0;
		   	byte byHead0, byHead1, byHead2, byHead3;
			byHead0 = (byte)(FDTPacket.PKT_LEAD);

			byHead1 = ((byte)(FDTPacket.PKT_NOTZIP));
			
			byHead2 = (byte)(datalen%256);
			byHead3 = (byte)(datalen/256);
			
			buf.writeByte(byHead0);
			buf.writeByte(byHead1);
			buf.writeByte(byHead2);
			buf.writeByte(byHead3);
			
			buf.writeBytes(data);
		
			byChkSum ^= byHead0;
			byChkSum ^= byHead1;
			byChkSum ^= byHead2;
			byChkSum ^= byHead3;
			for (int ii=0; ii<datalen; ii++)
			{
				byChkSum ^= data[ii];
			}	
			
			buf.writeByte(byChkSum);
			buf.writeByte(FDTPacket.PKT_END);
		}
		catch(Exception e)
		{
			log.error(e.getMessage(),e);
		}
	}
}
