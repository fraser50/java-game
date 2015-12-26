package com.castrovala.fraser.orbwar.server;

public class GameThread extends Thread {
	private GameServer server;
	private boolean active = true;
	
	public GameThread(GameServer server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
