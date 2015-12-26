package com.castrovala.fraser.orbwar.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.castrovala.fraser.orbwar.OrbWarPanel;
import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class WorldController implements WorldProvider {
	private List<WorldZone> zones = new ArrayList<>();
	private List<Position> starpoints = new ArrayList<>();
	private HashMap<GameObject, Float> scanners = new HashMap<>();
	private OrbWarPanel panel;
	
	public WorldController(OrbWarPanel panel) {
		this.panel = panel;
	}
	
	@Override
	public WorldZone getZone(Position pos) {
		for (WorldZone zone : zones) {
			if (zone.x == pos.x && zone.y == pos.y) {
				return zone;
			}
		}
		WorldZone zone = new WorldZone((long)pos.x, (long)pos.y, this);
		zone.populate();
		zones.add(zone);
		return zone;
	}

	@Override
	public List<WorldZone> getZones() {
		return zones;
	}

	@Override
	public List<Position> getStarpoints() {
		return starpoints;
	}
	
	@Override
	public void addObject(GameObject o) {
		getZone(Util.toZoneCoords(o.getPosition())).getGameobjects().add(o);
	}
	
	public void handleZoneUnload() {
		
	}

	@Override
	public HashMap<GameObject, Float> getScanners() {
		return scanners;
	}
	
	@Override
	public GameObject[] allObjects() {
		List<GameObject> all = new ArrayList<>();
		for (WorldZone zone : zones) {
			for (GameObject obj : zone.getGameobjects()) {
				all.add(obj);
			}
		}
		
		return all.toArray(new GameObject[all.size()]);
	}

	@Override
	public void setShip(Controllable ship) {
		panel.myship = ship;
		
	}

}