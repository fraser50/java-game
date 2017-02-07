package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;

import com.castrovala.fraser.orbwar.util.Util;

public class NameCheckPacket implements AbstractPacket {
	private boolean login;
	private String name;
	private int width;
	private int height;
	
	public NameCheckPacket(boolean login, String name, int width, int height) {
		this.login = login;
		this.name = name;
		this.width = width;
		this.height = height;
	}

	public boolean isLogin() {
		//return true;
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				NameCheckPacket ncp = (NameCheckPacket) p;
				ByteBuffer buff = ByteBuffer.allocate(9 + ncp.getName().getBytes().length + 4);
				buff.put(Util.boolToByte(ncp.isLogin()));
				buff.putInt(ncp.getWidth());
				buff.putInt(ncp.getHeight());
				buff.put(Util.encodeString(ncp.getName()));
				
				return buff.array();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				ByteBuffer buff = ByteBuffer.allocate(data.length);
				buff.put(data);
				buff.position(0);
				boolean login = Util.byteToBool(buff.get());
				int width = buff.getInt();
				int height = buff.getInt();
				String name = Util.decodeString(buff);
				NameCheckPacket ncp = new NameCheckPacket(login, name, width, height);
				return ncp;
			}
		};
		
		PacketProcessor.addParser(parser, NameCheckPacket.class);
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
