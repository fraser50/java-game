package com.castrovala.fraser.orbwar.server;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.net.HealthUpdatePacket;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PacketProcessor;
import com.castrovala.fraser.orbwar.net.PositionUpdatePacket;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldController;
import com.castrovala.fraser.orbwar.util.WorldZone;

public class GameThread extends Thread {
	private volatile GameServer server;
	private boolean active = true;
	private WorldController controller;
	
	public GameThread(GameServer server) {
		this.setServer(server);
	}
	
	@Override
	public void run() {
		controller = new WorldController();
		//PlayerShip ship = new PlayerShip(new Position(200, 200), controller);
		//controller.addObject(ship);
		while (active) {
			synchronized (controller) {
				controller.updateGame();
			}
			
			synchronized (controller) {
				for (GameObject obj : controller.allObjects()) {
					if (obj.getPosition().isEdited()) {
						for (NetworkPlayer player : getServer().getPlayers()) {
							//System.out.println("Sending PUP to player");
							PositionUpdatePacket pup = new PositionUpdatePacket(obj.getPosition(), obj.getUuid());
							//System.out.println("Serialised pup: " + PacketProcessor.toJSON(pup).toJSONString());
							
							synchronized (player) {
								player.sendPacket(pup);
							}
							
						}
						obj.getPosition().setEdited(false);
					}
					
					if (obj.isChanged()) {
						obj.setChanged(false);
						HealthUpdatePacket hup = new HealthUpdatePacket(obj.getUuid(), obj.getHealth(), obj.getRotation());
						for (NetworkPlayer player : getServer().getPlayers()) {
							synchronized (player) {
								player.sendPacket(hup);
							}
						}
					}
				}
			}
			
			synchronized (controller) {
				for (NetworkPlayer player : getServer().getPlayers()) {
					for (int x = WorldZone.len_x * -1;x<=1000; x+=WorldZone.len_x) {
						for (int y = WorldZone.len_y * -1;y<=1000; y+=WorldZone.len_y) {
							long px = (long) player.currentpos.getX() + x;
							long py = (long) player.currentpos.getY() + y;
							Position pos = new Position(px, py);
							WorldZone zone = controller.getZone(Util.toZoneCoords(pos));
							if (player.getSeenZones().contains(zone)) {
								continue;
							}
							player.getSeenZones().add(zone);
							for (GameObject obj : zone.getGameobjects()) {
								if (obj == null) {
									System.out.println("Gameobject is null!");
									continue;
								}
								
								ObjectTransmitPacket otp = new ObjectTransmitPacket(GameObjectProcessor.toJSON(obj));
								player.sendPacket(otp);
							}
						}
					}
				}
			}
			
			
			try {
				Thread.sleep(1000 / 60); // / 60
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
	
	public synchronized WorldController getController() {
		return controller;
	}
	
	public synchronized void stopLogic() {
		System.out.println("GameThread preparing death");
		active = false;
	}

}
