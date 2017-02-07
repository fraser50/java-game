package com.castrovala.fraser.orbwar.net;

public class ChatEnterPacket implements AbstractPacket {
	private String message;
	
	public ChatEnterPacket(String message) {
		this.setMessage(message);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public byte[] toBytes(AbstractPacket p) {
				ChatEnterPacket cep = (ChatEnterPacket) p;
				return cep.getMessage().getBytes();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				return new ChatEnterPacket(new String(data));
			}
		};
		
		PacketProcessor.addParser(parser, ChatEnterPacket.class);
	}

}
