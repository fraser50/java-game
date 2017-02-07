package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;

public class PositionUpdatePacket implements AbstractPacket {
	private Position position;
	private String objectid;
	
	public PositionUpdatePacket(Position position, String obj_id) {
		this.position = position;
		this.objectid = obj_id;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				PositionUpdatePacket pup = (PositionUpdatePacket) p;
				ByteBuffer buff = ByteBuffer.allocate(20 + pup.getObjectid().getBytes().length);
				buff.putDouble(pup.getPosition().getX());
				buff.putDouble(pup.getPosition().getY());
				buff.put(Util.encodeString(pup.getObjectid()));
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				double x = buff.getDouble();
				double y = buff.getDouble();
				String id = Util.decodeString(buff);
				return new PositionUpdatePacket(new Position(x, y), id);
			}
		};
		
		PacketProcessor.addParser(parser, PositionUpdatePacket.class);
	}

	public String getObjectid() {
		return objectid;
	}

	public void setObjectid(String objectid) {
		this.objectid = objectid;
	}

}
