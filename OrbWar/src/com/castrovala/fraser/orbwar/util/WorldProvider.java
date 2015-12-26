package com.castrovala.fraser.orbwar.util;

import java.util.HashMap;
import java.util.List;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public interface WorldProvider {
	public WorldZone getZone(Position pos);
	public List<WorldZone> getZones();
	public List<Position> getStarpoints();
	public void addObject(GameObject o);
	public HashMap<GameObject, Float> getScanners();
	public GameObject[] allObjects();
	public void setShip(Controllable ship);

}
