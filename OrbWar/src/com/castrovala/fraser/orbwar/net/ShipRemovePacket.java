package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class ShipRemovePacket implements AbstractPacket {
	private String uuid;
	
	public ShipRemovePacket(String uuid) {
		this.setUuid(uuid);
	}
	
	@Override
	public String getType() {
		return "srp";
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
			public JSONObject toJSON(AbstractPacket p) {
				ShipRemovePacket srp = (ShipRemovePacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", "srp");
				obj.put("uuid", srp.getUuid());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				ShipRemovePacket srp = new ShipRemovePacket(obj.getAsString("uuid"));
				return srp;
			}
		};
		
		PacketProcessor.addParser("srp", parser);
	}

}
