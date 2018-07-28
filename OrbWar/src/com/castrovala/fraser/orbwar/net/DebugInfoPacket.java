package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

public class DebugInfoPacket implements AbstractPacket {
	private float servertime;
	
	public DebugInfoPacket(float servertime) {
		this.setServertime(servertime);
	}

	public float getServertime() {
		return servertime;
	}

	public void setServertime(float servertime) {
		this.servertime = servertime;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				DebugInfoPacket dip = (DebugInfoPacket) p;
				ByteBuffer buff = ByteBuffer.allocate(4);
				buff.putFloat(dip.getServertime());
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				return new DebugInfoPacket(buff.getFloat());
			}
		};
		
		PacketProcessor.addParser(parser, DebugInfoPacket.class);
	}

}
