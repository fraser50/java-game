package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;

public class ShipDataPacket implements AbstractPacket {
	private String shipid;
	private String name;
	private boolean admin;
	
	public ShipDataPacket(String name, String shipid, boolean admin) {
		this.name = name;
		this.shipid = shipid;
		this.admin = admin;
	}

	public String getShipid() {
		return shipid;
	}

	public void setShipid(String shipid) {
		this.shipid = shipid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				ShipDataPacket sdp = (ShipDataPacket) p;
				ByteBuffer buff = ByteBuffer.allocate(sdp.getShipid().getBytes().length + sdp.getName().getBytes().length + 9);
				buff.put(Util.encodeString(sdp.getShipid()));
				buff.put(Util.encodeString(sdp.getName()));
				buff.put(Util.boolToByte(sdp.isAdmin()));
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				String id = Util.decodeString(buff);
				String name = Util.decodeString(buff);
				boolean admin = Util.byteToBool(buff.get());
				return new ShipDataPacket(name, id, admin);
			}
		};
		
		PacketProcessor.addParser(parser, ShipDataPacket.class);
	}

}
