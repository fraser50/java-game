package com.castrovala.fraser.orbwar.net;

public class ShipRemovePacket implements AbstractPacket {
	private String uuid;
	
	public ShipRemovePacket(String uuid) {
		this.setUuid(uuid);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				ShipRemovePacket srp = (ShipRemovePacket) p;
				return srp.getUuid().getBytes();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				return new ShipRemovePacket(new String(data));
			}
		};
		
		PacketProcessor.addParser(parser, ShipRemovePacket.class);
	}

}
