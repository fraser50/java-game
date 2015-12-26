package com.castrovala.fraser.orbwar.gameobject;

import java.util.ArrayList;
import java.util.List;

import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.WorldProvider;

public class RespawnPoint extends GameObject {
	private int timealive;
	private float speed = 2;
	private List<RespawnLaser> lasers = new ArrayList<>();

	public RespawnPoint(Position pos, WorldProvider controller) {
		super(pos, controller);
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
				laser.firing = true;
			}
		}
		
		if (timealive == 550) {
			Position pos = getPosition().copy();
			pos.subtract(new Position(32, 32));
			PlayerShip ship = new PlayerShip(pos, getController());
			getController().addObject(ship);
			getController().setShip(ship);
		}
		
		if (timealive == 600) {
			for (RespawnLaser laser : lasers) {
				laser.firing = false;
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

}
