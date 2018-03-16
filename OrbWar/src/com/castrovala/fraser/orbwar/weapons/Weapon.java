package com.castrovala.fraser.orbwar.weapons;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.world.Position;

public interface Weapon {
	
	public void fire(GameObject user, Position gunpos);
	public void update(GameObject user);

}
