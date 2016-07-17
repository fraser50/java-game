package com.castrovala.fraser.orbwar.gameobject;

import java.util.ArrayList;
import java.util.List;

import com.castrovala.fraser.orbwar.net.ShipDataPacket;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.ControlUser;
import com.castrovala.fraser.orbwar.server.NetworkPlayer;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldController;
import com.castrovala.fraser.orbwar.world.WorldProvider;

import net.minidev.json.JSONObject;

public class RespawnPoint extends GameObject {
	private int timealive;
	private float speed = 2;
	private List<RespawnLaser> lasers = new ArrayList<>();
	private ControlUser user;

	public RespawnPoint(Position pos, WorldProvider controller, ControlUser user) {
		super(pos, controller);
		this.user = user;
	}
	
	@Deprecated
	public RespawnPoint(Position pos, WorldProvider controller) {
		this(pos, controller, null);
	}
	
	@Override
	public void update() {
		super.update();
		
		if (timealive == 50) {
			System.out.println("Spawning lasers");
			for (int i = 1; i <= 360; i += 45) {
				OrbitControl control = new OrbitControl(this, speed, 0.25f); // 0.25f
				control.setRotation(i);
				RespawnLaser laser = new RespawnLaser(getPosition().copy(), getController());
				laser.control = control;
				lasers.add(laser);
				getController().addObject(laser);
			}
		}
		
		if (timealive > 50 && speed != 100) {
			speed += 0.25f;
			for (RespawnLaser laser : lasers) {
				laser.control.setSpeed(speed);
			}
		}
		
		if (timealive == 500 && speed == 100) {
			for (RespawnLaser laser : lasers) {
				laser.setFiring(true);
			}
		}
		
		if (timealive == 550) {
			Position pos = getPosition().copy();
			pos.subtract(new Position(32, 32));
			PlayerShip ship = new PlayerShip(pos, getController());
			
			if (user != null) {
				user.setControl(ship);
				ship.setControl(user);
				
				WorldController c = (WorldController) getController();
				
				for (NetworkPlayer p : c.getServer().getPlayers()) {
					p.sendPacket(new ShipDataPacket(((NetworkPlayer)user).getName(), ship.getUuid()));
				}
				
			} else {
				System.out.println("ControlUser is NULL!");
			}
			
			getController().addObject(ship);
			//getController().setShip(ship);
		}
		
		if (timealive == 600) {
			for (RespawnLaser laser : lasers) {
				laser.setFiring(false);
			}
		}
		
		if (timealive >= 650) {
			speed -= 1f;
			for (RespawnLaser laser : lasers) {
				laser.control.setSpeed(speed);
			}
		}
		
		if (timealive >= 650 && speed <= 2) {
			for (RespawnLaser laser : lasers) {
				laser.delete();
			}
			delete();
		}
		
		timealive++;
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				RespawnPoint point = (RespawnPoint) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "rpoint");
				jobj.put("x", point.getPosition().getX());
				
				jobj.put("y", point.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				RespawnPoint point = new RespawnPoint(null, null);
				return point;
			}
		};
		
		GameObjectProcessor.addParser("rpoint", parser);
	}
	
	@Override
	public String getType() {
		return "rpoint";
	}

	public ControlUser getUser() {
		return user;
	}

	public void setUser(ControlUser user) {
		this.user = user;
	}

}
