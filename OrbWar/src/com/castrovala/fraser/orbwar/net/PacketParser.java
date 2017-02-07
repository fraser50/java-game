package com.castrovala.fraser.orbwar.net;

public abstract class PacketParser {
	private static byte currid = 0;
	private byte id;
	
	public PacketParser() {
		id = currid;
		currid++;
	}
	
	public abstract byte[] toBytes(AbstractPacket p);
	public abstract AbstractPacket fromBytes(byte[] data);
	
	public void setID(byte id) {
		this.id = id;
	}
	
	public byte getID() {
		return id;
	}

}
