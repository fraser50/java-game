package com.castrovala.fraser.orbwar.net;

public class ResetPacket implements AbstractPacket {
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				return new byte[0];
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				return new ResetPacket();
			}
		};
		
		PacketProcessor.addParser(parser, ResetPacket.class);
	}

}
