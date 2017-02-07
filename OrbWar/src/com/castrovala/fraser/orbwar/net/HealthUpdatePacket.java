package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;

public class HealthUpdatePacket implements AbstractPacket {
	private String uuid;
	private float rotation;
	private int health;

	public HealthUpdatePacket(String uuid, int health, float rotation) {
		this.uuid = uuid;
		this.health = health;
		this.rotation = rotation;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				HealthUpdatePacket hup = (HealthUpdatePacket) p;
				ByteBuffer buff = ByteBuffer.allocate(hup.getUuid().getBytes().length + 16);
				buff.put(Util.encodeString(hup.getUuid()));
				buff.putInt(hup.getHealth());
				buff.putFloat(hup.getRotation());
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				String id = Util.decodeString(buff);
				int health = buff.getInt();
				float rotation = buff.getFloat();
				return new HealthUpdatePacket(id, health, rotation);
			}
		};
		
		PacketProcessor.addParser(parser, HealthUpdatePacket.class);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

}
