package com.cyanspring.Network.Transport;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class FDTFrameDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,List<Object> out) throws Exception {
		while(in.readableBytes() > (FDTPacket.PKT_HEAD_SIZE + FDTPacket.PKT_TAIL_SIZE)) {
			//packet_head(4) + packet_tail(2)
		
		
			in.markReaderIndex();
			byte head_byte = in.readByte();
			if(head_byte != FDTPacket.PKT_LEAD) {			
				continue; //discard head byte that is not 0x02;
			}
			
			byte ptyp_byte = in.readByte();
			int packetlen_low = unsignedByteToInt(in.readByte());
			int packetlen_hi = unsignedByteToInt(in.readByte());
			int packetlen = packetlen_low + packetlen_hi*256;
			if(in.readableBytes() < (packetlen + FDTPacket.PKT_HEAD_SIZE + FDTPacket.PKT_TAIL_SIZE)) {			
				in.resetReaderIndex(); //more data needed;
				break;
			}
			
			byte[] body_buf = new byte[packetlen];
			in.readBytes(body_buf);
			
			
			byte crc_byte = in.readByte();  //skill checking crc
			byte tail_byte = in.readByte();
					
			if(tail_byte != FDTPacket.PKT_END) {
				//discard tail byte that is not 0x03, skill one byte, keep checking head byte(0x02)
				in.resetReaderIndex();
				in.readByte();
				continue; 
			}
			out.add(new String(body_buf, "UTF-8"));
		}
	}
	
	public static int unsignedByteToInt(byte b) {  
	    return (int) b & 0xFF;  
	}  
}
