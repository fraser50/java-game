package com.castrovala.fraser.orbwar.client;

public class ClientPlayer {
	private String name;
	private boolean admin;
	
	public ClientPlayer(String name, boolean admin) {
		this.setName(name);
		this.setAdmin(admin);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

}
