package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class DeleteObjectPacket implements AbstractPacket {
	private String uuid;
	
	public DeleteObjectPacket(String uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public String getType() {
		return "obj_delete";
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
				DeleteObjectPacket dop = (DeleteObjectPacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", "obj_delete");
				obj.put("uuid", dop.getUuid());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				DeleteObjectPacket dop = new DeleteObjectPacket(obj.getAsString("uuid"));
				return dop;
			}
		};
		
		PacketProcessor.addParser("obj_delete", parser);
	}

}
