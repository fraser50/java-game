package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class ChatEnterPacket implements AbstractPacket {
	private String message;
	
	public ChatEnterPacket(String message) {
		this.setMessage(message);
	}

	@Override
	public String getType() {
		return "cep";
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
			public JSONObject toJSON(AbstractPacket p) {
				ChatEnterPacket cep = (ChatEnterPacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", "cep");
				obj.put("message", cep.getMessage());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				ChatEnterPacket cep = new ChatEnterPacket(obj.getAsString("message"));
				return cep;
			}
		};
		
		PacketProcessor.addParser("cep", parser);
	}

}
