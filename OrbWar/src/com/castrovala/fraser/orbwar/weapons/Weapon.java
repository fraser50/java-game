package com.castrovala.fraser.orbwar.weapons;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public interface Weapon {
	
	public void fire(GameObject user);
	public void update(GameObject user);

}
