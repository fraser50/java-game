package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class HealthUpdatePacket implements AbstractPacket {
	private String uuid;
	private float rotation;
	private int health;

	public HealthUpdatePacket(String uuid, int health, float rotation) {
		this.uuid = uuid;
		this.health = health;
		this.rotation = rotation;
	}
	
	@Override
	public String getType() {
		return "health_update";
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				HealthUpdatePacket packet = (HealthUpdatePacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", "health_update");
				obj.put("uuid", packet.getUuid());
				obj.put("health", packet.getHealth());
				obj.put("rotation", packet.getRotation());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				String uuid = (String) obj.get("uuid");
				int health = obj.getAsNumber("health").intValue();
				double rotation = obj.getAsNumber("rotation").doubleValue();
				return new HealthUpdatePacket(uuid, health, (float)rotation);
			}
		};
		
		PacketProcessor.addParser("health_update", parser);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

}
