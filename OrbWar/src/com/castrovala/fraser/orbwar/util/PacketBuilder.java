package com.castrovala.fraser.orbwar.util;

import java.nio.ByteBuffer;

public final class PacketBuilder {
	
	private PacketBuilder() {} // There is no need to invoke the constructor as this is a utility class
	
	@Deprecated
	public static ByteBuffer ConvertData(byte[] data) {
		int len = data.length;
		ByteBuffer buff = ByteBuffer.allocate(4 + len);
		buff.putInt(len);
		buff.put(data);
		return buff;
	}

}
