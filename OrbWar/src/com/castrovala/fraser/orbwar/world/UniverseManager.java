package com.castrovala.fraser.orbwar.world;

import java.util.ArrayList;
import java.util.List;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.net.DeleteObjectPacket;
import com.castrovala.fraser.orbwar.net.HealthUpdatePacket;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PositionUpdatePacket;
import com.castrovala.fraser.orbwar.net.ScreenUpdatePacket;
import com.castrovala.fraser.orbwar.net.SizeUpdatePacket;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.GameServer;
import com.castrovala.fraser.orbwar.server.NetworkPlayer;
import com.castrovala.fraser.orbwar.util.Util;

public class UniverseManager {
	private List<WorldController> universes = new ArrayList<>();
	private List<WorldController> toadd = new ArrayList<>();
	private GameServer server;
	
	public UniverseManager(GameServer server) {
		this.setServer(server);
	}
	
	public void addUniverse(WorldController controller) {
		toadd.add(controller);
	}

	public List<WorldController> getUniverses() {
		return universes;
	}
	
	public void update() {
		
		for (WorldController add : toadd) {
			universes.add(add);
		}
		
		toadd.clear();
		
		for (WorldController universe : universes) {
			universe.updateGame();
			handlePlayers(universe);
			handleObjects(universe);
			handlePlayerLocation(universe);
			
		}
	}
	
	public void handlePlayers(WorldController controller) {
		for (GameObject obj : controller.allObjects()) {
			if (obj.getPosition().isEdited()) {
				for (NetworkPlayer player : getServer().getPlayers()) {
					if (player.getUniverse() != controller) continue;
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
					if (player.getUniverse() != controller) continue;
					synchronized (player) {
						player.sendPacket(hup);
					}
				}
			}
			
			if (obj.isSizechanged()) {
				obj.setSizechanged(false);
				SizeUpdatePacket siup = new SizeUpdatePacket(obj.getUuid(), obj.getWidth(), obj.getHeight());
				for (NetworkPlayer player : getServer().getPlayers()) {
					if (player.getUniverse() != controller) continue;
					synchronized (player) {
						player.sendPacket(siup);
					}
				}
			}
			
		}
		
		//
		
		for (NetworkPlayer player : getServer().getPlayers()) {
			if (player.getUniverse() != controller) continue;
			
			if (player.forward) player.getControl().fly();
			if (player.left) player.getControl().left();
			if (player.right) player.getControl().right();
			if (player.fire) player.getControl().fire();
			
			player.forward = false;
			player.left = false;
			player.right = false;
			player.fire = false;
			
			//TODO: Continue this!
			
			for (int x = WorldZone.len_x * -1;x<=1000; x+=WorldZone.len_x) {
				for (int y = WorldZone.len_y * -1;y<=1000; y+=WorldZone.len_y) {
					long px = (long) player.getCurrentpos().getX() + x;
					long py = (long) player.getCurrentpos().getY() + y;
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
	
	public void handleObjects(WorldController controller) {
		for (GameObject obj : controller.getNewObjects()) {
			if (obj.isDeleted() || obj.isCleaned()) {
				continue;
			}
			
			for (NetworkPlayer player : getServer().getPlayers()) {
				if (player.getUniverse() != controller) continue;
				if (GameObjectProcessor.toJSON(obj) == null) {
					System.out.println("couldn't parse " + obj.getClass().toString());
				}
				ObjectTransmitPacket otp = new ObjectTransmitPacket(GameObjectProcessor.toJSON(obj));
				player.sendPacket(otp);
			}
		}
		
		controller.getNewObjects().clear();
		
		for (GameObject obj : controller.getDeadObjects()) {
			for (NetworkPlayer player : getServer().getPlayers()) {
				if (player.getUniverse() != controller) continue;
				DeleteObjectPacket dop = new DeleteObjectPacket(obj.getUuid());
				player.sendPacket(dop);
			}
		}
		controller.getDeadObjects().clear();
	}
	
	public void handlePlayerLocation(WorldController controller) {
		for (NetworkPlayer p : getServer().getPlayers()) {
			if (p.getUniverse() != controller) continue;
			if (p.getControl() == null) {
				continue;
			}
			
			GameObject obj = (GameObject) p.getControl();
			Position mylocation = p.getCurrentpos();
			int PWIDTH = p.getScreenWidth();
			int PHEIGHT = p.getScreenHeight();
			
			if (mylocation.getX() - obj.getPosition().getX() >= 1000000 || mylocation.getY() - obj.getPosition().getY() >= 1000000 ||
				mylocation.getX() - obj.getPosition().getX() <= -1000000 || mylocation.getY() - obj.getPosition().getY() <= -1000000	) {
				mylocation.setX(obj.getPosition().getX() - (PWIDTH / 2) );
				mylocation.setY(obj.getPosition().getY() - (PHEIGHT / 2) );
			}
				
				
			Position toscreen = Util.coordToScreen(obj.getPosition(), mylocation);
			long screen_x = (long) toscreen.getX();
			long screen_y = (long) toscreen.getY();
			
			while (screen_x >= PWIDTH - 50 || screen_x <= 50 || screen_y >= PHEIGHT - 50 || screen_y <= 50) {
				if (screen_x >= PWIDTH - 50) {
					mylocation.setX(mylocation.getX() + 1);
				}
				
				if (screen_x <= 50) {
					mylocation.setX(mylocation.getX() - 1);
				}
					
				if (screen_y >= PHEIGHT - 50) {
					mylocation.setY(mylocation.getY() + 1);
				}
					
				if (screen_y <= 50) {
					mylocation.setY(mylocation.getY() - 1);
				}
				
				toscreen = Util.coordToScreen(obj.getPosition(), mylocation);
				screen_x = (long) toscreen.getX();
				screen_y = (long) toscreen.getY();
			}
			
			if (mylocation.isEdited()) {
				ScreenUpdatePacket sup = new ScreenUpdatePacket(mylocation);
				p.sendPacket(sup);
				mylocation.setEdited(false);
			}
		}
	}

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}

}
