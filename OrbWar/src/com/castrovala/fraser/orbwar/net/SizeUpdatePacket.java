package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;

public class SizeUpdatePacket implements AbstractPacket {
	private String uuid;
	private int width;
	private int height;

	public SizeUpdatePacket(String uuid, int width, int height) {
		this.uuid = uuid;
		this.setWidth(width);
		this.setHeight(height);
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				SizeUpdatePacket sup = (SizeUpdatePacket) p;
				ByteBuffer buff = ByteBuffer.allocate(sup.getUuid().getBytes().length + 12);
				buff.put(Util.encodeString(sup.getUuid()));
				buff.putInt(sup.getWidth());
				buff.putInt(sup.getHeight());
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				String id = Util.decodeString(buff);
				int width = buff.getInt();
				int height = buff.getInt();
				return new SizeUpdatePacket(id, width, height);
			}
		};
		
		PacketProcessor.addParser(parser, SizeUpdatePacket.class);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
