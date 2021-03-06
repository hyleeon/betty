package io.betty.codecs;

import java.nio.ByteBuffer;

import org.slf4j.Logger;

import io.betty.BettyProtocolCodecOrigin;
import io.betty.BettyProtocolCodec;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class StringProtocolCodec implements BettyProtocolCodec {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(StringProtocolCodec.class);

	@Override
	public byte[] encode(ChannelHandlerContext ctx, Object data) throws Exception {
		StringProtocolPacket packet = (StringProtocolPacket) data;
		byte[] bytes = packet.data.getBytes("utf-8");
		int byteslen = bytes.length;
		int size = 4 + 4 + 8 + 4 + byteslen;
		
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.putInt(packet.seq);
		buf.putLong(packet.uid);
		buf.putInt(packet.version);
		buf.putInt(byteslen);
		buf.put(bytes);

		return buf.array();
	}

	@Override
	public BettyProtocolCodecOrigin decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
		
		buf.markReaderIndex();
		
		if(logger.isDebugEnabled()) {
			logger.debug("buf.readableBytes(): {}", buf.readableBytes());
		}

		if(buf.readableBytes() < 20) {
			return null;
		}
		
		int seq = buf.readInt();
		long uid = buf.readLong();
		int version = buf.readInt();
		int byteslen = buf.readInt();
		if(buf.readableBytes() < byteslen) {
			buf.resetReaderIndex();
			return null;
		}
		//
		byte[] bytes = new byte[byteslen];
		buf.readBytes(bytes);
		String data = new String(bytes, "utf-8");
		
		if(logger.isDebugEnabled()) {
			logger.debug("Decode msg: {}, {}, {}, {}, {}", seq, uid, version, byteslen, data);
		}
		
		return new BettyProtocolCodecOrigin(seq, uid, "0", "231", new StringProtocolPacket(seq, uid, version, data));
	}

	@Override
	public String toString(Object data) {
		StringProtocolPacket packet = (StringProtocolPacket) data;
		StringBuilder sb = new StringBuilder();
		sb.append(packet.seq).append(',');
		sb.append(packet.uid).append(',');
		sb.append(packet.version).append(',');
		sb.append(packet.data);
		return sb.toString();
	}
}
