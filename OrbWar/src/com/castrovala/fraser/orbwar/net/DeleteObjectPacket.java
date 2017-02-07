package com.castrovala.fraser.orbwar.net;

public class DeleteObjectPacket implements AbstractPacket {
	private String uuid;
	
	public DeleteObjectPacket(String uuid) {
		this.uuid = uuid;
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
				DeleteObjectPacket dop = (DeleteObjectPacket) p;
				return dop.getUuid().getBytes();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				return new DeleteObjectPacket(new String(data));
			}
		};
		
		PacketProcessor.addParser(parser, DeleteObjectPacket.class);
	}

}
