package com.castrovala.fraser.orbwar.weapons;

import com.castrovala.fraser.orbwar.gameobject.Bullet;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.gameobject.Turret;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;

public class BulletGun implements RechargeWeapon {
	private int charge;
	private int max;
	
	public BulletGun() {
		max = 25; // 25
		charge = 25; // 25
	}

	@Override
	public void fire(GameObject user, Position bulletpos) {
		if (user instanceof Turret) {
			//max = 5; // 100
		}
		
		if (charge >= max) {
			//Position bulletpos = new Position(user.getPosition().getX() + (user.getWidth() / 2), user.getPosition().getY() +  + (user.getHeight() / 2) );
			//Position bulletpos = getPosition().copy();
			float speed = 5; // 5
			if (user instanceof PlayerShip) {
				PlayerShip ps = (PlayerShip) user;
				speed += ps.getSpeed();
			}
			
			Position frontvelocity = Util.angleToVel(user.getRotation(), speed); //3
			float vx = (float) frontvelocity.getX();
			float vy = (float) frontvelocity.getY();
			//bulletpos.add(Util.angleToVel(user.getRotation(), 5));
			
			Bullet b = new Bullet(bulletpos, user.getController(), user);
			//b.getPosition().subtract(new Position(b.getWidth() / 2, b.getHeight() / 2));
			b.getVelocity().setX(vx);
			b.getVelocity().setY(vy);
			user.getController().addObject(b);
			charge = 0;
		}
		
		
	}

	@Override
	public void update(GameObject user) {
		if (charge != max) {
			charge++;
		}
		
	}

	@Override
	public int getCharge() {
		return charge;
	}

	@Override
	public int getMaxCharge() {
		return max;
	}

}
