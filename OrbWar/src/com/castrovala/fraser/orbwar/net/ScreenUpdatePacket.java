package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.world.Position;

public class ScreenUpdatePacket implements AbstractPacket {
	private Position pos;
	
	public ScreenUpdatePacket(Position pos) {
		this.pos = pos;
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				ScreenUpdatePacket sup = (ScreenUpdatePacket) p;
				ByteBuffer buff = ByteBuffer.allocate(8 * 2);
				buff.putDouble(sup.getPos().getX());
				buff.putDouble(sup.getPos().getY());
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				double x = buff.getDouble();
				double y = buff.getDouble();
				ScreenUpdatePacket sup = new ScreenUpdatePacket(new Position(x, y));
				return sup;
			}
		};
		
		PacketProcessor.addParser(parser, ScreenUpdatePacket.class);
	}

}
