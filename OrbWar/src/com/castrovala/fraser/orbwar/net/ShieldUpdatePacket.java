package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;

public class ShieldUpdatePacket implements AbstractPacket {
	private String shipid;
	private boolean shield;
	
	public ShieldUpdatePacket(String shipid, boolean shield) {
		this.shipid = shipid;
		this.shield = shield;
	}

	public String getShipid() {
		return shipid;
	}

	public void setShipid(String shipid) {
		this.shipid = shipid;
	}

	public boolean isShieldActive() {
		return shield;
	}

	public void setShield(boolean shield) {
		this.shield = shield;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				ShieldUpdatePacket sup = (ShieldUpdatePacket) p;
				ByteBuffer buff = ByteBuffer.allocate(sup.getShipid().getBytes().length + 5);
				buff.put(Util.encodeString(sup.getShipid()));
				buff.put(Util.boolToByte(sup.isShieldActive()));
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				String id = Util.decodeString(buff);
				boolean shield = Util.byteToBool(buff.get());
				return new ShieldUpdatePacket(id, shield);
			}
		};
		
		PacketProcessor.addParser(parser, ShieldUpdatePacket.class);
		
	}

}
