package com.castrovala.fraser.orbwar.server;

import java.io.FileNotFoundException;

import org.json.simple.parser.ParseException;

import com.castrovala.fraser.orbwar.world.UniverseManager;
import com.castrovala.fraser.orbwar.world.WorldController;

public class GameThread extends Thread {
	private volatile GameServer server;
	private boolean active = true;
	//private WorldController controller;
	private UniverseManager manager;
	
	public GameThread(GameServer server) {
		setServer(server);
		
		WorldController mainworld = null;
		
		if (server.getSavefile() == null) {
			mainworld = new WorldController(server);
			this.setName("Game Thread");
			return;
		}
		
		if (server.getSavefile().exists()) {
			try {
				mainworld = WorldController.createFromFile(server.getSavefile());
			} catch (FileNotFoundException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mainworld.setServer(server);
		} else {
			mainworld = new WorldController(server);
		}
		
		manager = new UniverseManager(getServer());
		manager.addUniverse(mainworld);
		this.setName("Game Thread");
	}
	
	@Override
	public void run() {
		while (active) {
			synchronized (manager) {
				manager.update();
			}
			
			try {
				Thread.sleep(1000 / 60); // 1000 / 60
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Thread died");
		
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public synchronized GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}
	
	public synchronized void stopLogic() {
		System.out.println("GameThread preparing death");
		active = false;
	}

	public synchronized UniverseManager getManager() {
		return manager;
	}

}
