package com.castrovala.fraser.orbwar.weapons;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.ShieldDrone;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.Position;

public class Forcefield implements RechargeWeapon {
	private int maxcharge = 20;
	private int charge = 20;
	
	@Override
	public void fire(GameObject user) {
		if (charge == maxcharge) {
			Position pos = user.getPosition();
			for (int i = 1; i<360;i+=23.5) {
				OrbitControl c = new OrbitControl(user, 70, 1f);
				c.setRotation(i);
				ShieldDrone drone = new ShieldDrone(pos.copy(), user.getController(), c);
				user.getController().addObject(drone);
				drone.update();
			}
		}
		
	}
	
	@Override
	public void update(GameObject user) {
		if (charge < maxcharge) {
			charge++;
		}
		
	}

	@Override
	public int getCharge() {
		// TODO Auto-generated method stub
		return charge;
	}

	@Override
	public int getMaxCharge() {
		// TODO Auto-generated method stub
		return maxcharge;
	}

}
