package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class KeyPressPacket implements AbstractPacket {
	private String key;

	public KeyPressPacket(String key) {
		this.key = key;
	}
	@Override
	public String getType() {
		return "keypress";
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
			public JSONObject toJSON(AbstractPacket p) {
				JSONObject obj = new JSONObject();
				KeyPressPacket packet = (KeyPressPacket) p;
				obj.put("type", "keypress");
				obj.put("key", packet.getKey());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				KeyPressPacket p = new KeyPressPacket(obj.getAsString("key"));
				return p;
			}
		};
		
		PacketProcessor.addParser("keypress", parser);
	}

}
