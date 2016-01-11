package com.castrovala.fraser.orbwar.net;

import org.json.simple.JSONObject;

public class ObjectTransmitPacket implements AbstractPacket {
	private JSONObject obj;
	private String uuid;
	
	public ObjectTransmitPacket(JSONObject obj) {
		this.setObj(obj);
	}
	
	
	@Override
	public String getType() {
		return "obj_transmit";
	}


	public JSONObject getObj() {
		return obj;
	}


	public void setObj(JSONObject obj) {
		this.obj = obj;
	}
	
	public static void registerPacket() {
		PacketParser proc = new PacketParser() {
			
			@SuppressWarnings("unchecked")
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				ObjectTransmitPacket packet = (ObjectTransmitPacket) p;
				JSONObject json = new JSONObject();
				json.put("type", packet.getType());
				json.put("obj", packet.getObj());
				return json;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				JSONObject gobj = (JSONObject) obj.get("obj");
				ObjectTransmitPacket p = new ObjectTransmitPacket(gobj);
				return p;
			}
		};
		
		PacketProcessor.addParser("obj_transmit", proc);
		
	}

}
