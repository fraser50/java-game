package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class ObjectTransmitPacket implements AbstractPacket {
	private JSONObject obj;
	
	public ObjectTransmitPacket(JSONObject obj) {
		this.setObj(obj);
		if (obj == null) {
			throw new NullPointerException("OTP obj was null!");
		}
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
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				ObjectTransmitPacket packet = (ObjectTransmitPacket) p;
				JSONObject json = new JSONObject();
				json.put("type", packet.getType());
				
				packet.getObj().put("x", (double)packet.getObj().get("x"));
				packet.getObj().put("y", (double)packet.getObj().get("y"));
				
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
