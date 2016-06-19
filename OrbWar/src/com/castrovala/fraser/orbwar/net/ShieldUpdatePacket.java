package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class ShieldUpdatePacket implements AbstractPacket {
	private String shipid;
	private boolean shield;
	
	public ShieldUpdatePacket(String shipid, boolean shield) {
		this.shipid = shipid;
		this.shield = shield;
	}

	@Override
	public String getType() {
		return "fup";
	}

	public String getShipid() {
		return shipid;
	}

	public void setShipid(String shipid) {
		this.shipid = shipid;
	}

	public boolean isShieldActive() {
		return shield;
	}

	public void setShield(boolean shield) {
		this.shield = shield;
	}
	
	public static void registerPacket() {
		PacketParser proc = new PacketParser() {
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				ShieldUpdatePacket packet = (ShieldUpdatePacket) p;
				JSONObject json = new JSONObject();
				json.put("type", packet.getType());
				
				json.put("shipid", packet.getShipid());
				json.put("shield", packet.isShieldActive());
				return json;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				ShieldUpdatePacket p = new ShieldUpdatePacket(obj.getAsString("shipid"), (boolean)obj.get("shield"));
				return p;
			}
		};
		
		PacketProcessor.addParser("fup", proc);
		
	}

}
