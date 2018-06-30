package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;

public class DestructionPacket implements AbstractPacket {
	private String uuid;
	private int x;
	private int y;
	private int radius;
	
	public DestructionPacket(String uuid, int x, int y, int radius) {
		this.uuid = uuid;
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				DestructionPacket dep = (DestructionPacket) p;
				ByteBuffer buff = ByteBuffer.allocate(dep.getUuid().getBytes().length + 16);
				buff.put(Util.encodeString(dep.getUuid()));
				buff.putInt(dep.getX());
				buff.putInt(dep.getY());
				buff.putInt(dep.getRadius());
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				String id = Util.decodeString(buff);
				int x = buff.getInt();
				int y = buff.getInt();
				int r = buff.getInt();
				return new DestructionPacket(id, x, y, r);
			}
		};
		
		PacketProcessor.addParser(parser, DestructionPacket.class);
	}
	

}
