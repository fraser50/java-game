package com.castrovala.fraser.orbwar.net;

import org.json.simple.JSONObject;

public interface PacketParser {
	public JSONObject toJSON(AbstractPacket p);
	public AbstractPacket fromJSON(JSONObject obj);

}
