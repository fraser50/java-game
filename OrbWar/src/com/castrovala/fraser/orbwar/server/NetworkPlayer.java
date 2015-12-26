package com.castrovala.fraser.orbwar.server;

import com.castrovala.fraser.orbwar.net.AbstractPacket;

public class NetworkPlayer {
	private GameServer server;
	
	public NetworkPlayer(GameServer server) {
		this.server = server;
	}
	
	public void sendPacket(AbstractPacket p) {
		
	}

}
