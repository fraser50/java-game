package com.castrovala.fraser.orbwar.item;

import java.nio.ByteBuffer;

public abstract class ItemParser {
	private static short currid = 0;
	private short id;
	
	public ItemParser() {
		id = currid;
		currid++;
	}
	
	public abstract byte[] toBytes(Item item);
	public abstract Item fromBytes(ByteBuffer buf);
	
	public void setID(short id) {
		this.id = id;
	}
	
	public short getID() {
		return id;
	}

}
