package com.castrovala.fraser.orbwar.util;

import com.castrovala.fraser.orbwar.world.Position;

public class WormHoleData {
	private Position zonepos;
	private String wormholeID;
	private WormHoleType type;
	
	public WormHoleData(Position zonepos, String wormholeID, WormHoleType type) {
		this.zonepos = zonepos;
		this.wormholeID = wormholeID;
		this.type = type;
	}
	
	public Position getZonepos() {
		return zonepos;
	}
	
	public void setZonepos(Position zonepos) {
		this.zonepos = zonepos;
	}
	
	public String getWormholeID() {
		return wormholeID;
	}
	
	public void setWormholeID(String wormholeID) {
		this.wormholeID = wormholeID;
	}
	
	public WormHoleType getType() {
		return type;
	}
	
	public void setType(WormHoleType type) {
		this.type = type;
	}

}
