package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;

public class SetLightingPacket implements AbstractPacket {
	private boolean lighting;
	
	public SetLightingPacket(boolean lighting) {
		this.setLighting(lighting);
	}

	public boolean isLighting() {
		return lighting;
	}

	public void setLighting(boolean lighting) {
		this.lighting = lighting;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				SetLightingPacket slp = (SetLightingPacket) p;
				ByteBuffer buff = ByteBuffer.allocate(1);
				buff.put(Util.boolToByte(slp.isLighting()));
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				boolean lighting = Util.byteToBool(buff.get());
				return new SetLightingPacket(lighting);
			}
		};
		
		PacketProcessor.addParser(parser, SetLightingPacket.class);
	}

}
