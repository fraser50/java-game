package com.castrovala.fraser.orbwar.server;

import com.castrovala.fraser.orbwar.util.WorldController;

public class GameThread extends Thread {
	private GameServer server;
	private boolean active = true;
	private WorldController controller;
	
	public GameThread(GameServer server) {
		this.setServer(server);
	}
	
	@Override
	public void run() {
		controller = new WorldController();
		while (active) {
			synchronized (controller) {
				controller.updateGame();
			}
			
			try {
				Thread.sleep(1000 / 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}

}
