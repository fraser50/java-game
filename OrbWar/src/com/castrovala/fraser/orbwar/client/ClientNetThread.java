package com.castrovala.fraser.orbwar.client;

import java.io.IOException;

import com.castrovala.fraser.orbwar.util.WorldNetController;

public class ClientNetThread extends Thread {
	private WorldNetController controller;
	private boolean active = true;
	
	public ClientNetThread(WorldNetController controller) {
		this.controller = controller;
	}
	
	@Override
	public void run() {
		while (active) {
			try {
				controller.processPackets();
				//Thread.sleep(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void finish() {
		active = false;
	}

}
