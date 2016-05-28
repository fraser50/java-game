package com.castrovala.fraser.orbwar.net;

import com.castrovala.fraser.orbwar.util.Position;

import net.minidev.json.JSONObject;

public class PositionUpdatePacket implements AbstractPacket {
	private Position position;
	private String objectid;

	@Override
	public String getType() {
		return "pos_update";
	}
	
	public PositionUpdatePacket(Position position, String obj_id) {
		this.position = position;
		this.objectid = obj_id;
		
		//System.out.println("PUP has been constructed");
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				
				PositionUpdatePacket packet = (PositionUpdatePacket) p;
				
				JSONObject json = new JSONObject();
				json.put("type", p.getType());
				json.put("x", Double.toHexString(packet.getPosition().getX()) );
				json.put("y", Double.toHexString(packet.getPosition().getY()) );
				json.put("obj_id", packet.getObjectid());
				return json;
				
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				double x = Double.parseDouble(obj.getAsString("x"));
				double y = Double.parseDouble(obj.getAsString("y"));
				String obj_id = (String) obj.get("obj_id");
				
				PositionUpdatePacket packet = new PositionUpdatePacket(new Position(x, y), obj_id);
				return packet;
			}
		};
		
		PacketProcessor.addParser("pos_update", parser);
	}

	public String getObjectid() {
		return objectid;
	}

	public void setObjectid(String objectid) {
		this.objectid = objectid;
	}

}
