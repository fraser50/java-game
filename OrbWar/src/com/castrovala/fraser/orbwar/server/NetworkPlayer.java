package com.castrovala.fraser.orbwar.server;

import com.castrovala.fraser.orbwar.net.AbstractPacket;

public class NetworkPlayer {
	private GameServer server;
	
	public NetworkPlayer(GameServer server) {
		this.setServer(server);
	}
	
	public void sendPacket(AbstractPacket p) {
		
	}

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}

}
