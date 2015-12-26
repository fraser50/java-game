package com.castrovala.fraser.orbwar.server;

public class GameServer extends Thread {
	private boolean localserver;
	private boolean isTicking = true;
	private GameThread gamelogic;
	private boolean active = true;
	
	public GameServer(boolean localserver) {
		this.localserver = localserver;
	}

	public synchronized boolean isLocalserver() {
		return localserver;
	}

	public synchronized void setLocalserver(boolean localserver) {
		this.localserver = localserver;
	}

	public synchronized boolean isTicking() {
		return isTicking;
	}

	public synchronized void setTicking(boolean isTicking) {
		this.isTicking = isTicking;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}

}
