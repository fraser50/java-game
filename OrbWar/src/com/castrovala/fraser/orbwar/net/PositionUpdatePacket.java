package com.castrovala.fraser.orbwar.net;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.util.Position;

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
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@SuppressWarnings("unchecked")
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				if (!(p instanceof PositionUpdatePacket) ) {
					return null;
				}
				
				PositionUpdatePacket packet = (PositionUpdatePacket) p;
				
				JSONObject json = new JSONObject();
				json.put("type", p.getType());
				json.put("x", packet.getPosition().getX());
				json.put("y", packet.getPosition().getY());
				json.put("obj_id", packet.getObjectid());
				return json;
				
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				double x = (double) obj.get("x");
				double y = (double) obj.get("y");
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
