package com.castrovala.fraser.orbwar.net;

public class KeyPressPacket implements AbstractPacket {
	private String key;

	public KeyPressPacket(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				KeyPressPacket kpp = (KeyPressPacket) p;
				return kpp.getKey().getBytes();
				
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				return new KeyPressPacket(new String(data));
			}
		};
		
		PacketProcessor.addParser(parser, KeyPressPacket.class);
	}

}
