package com.castrovala.fraser.orbwar.util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class WorldController implements WorldProvider {
	private List<WorldZone> zones = new ArrayList<>();
	private List<Position> starpoints = new ArrayList<>();
	private HashMap<GameObject, Float> scanners = new HashMap<>();
	private HashMap<String, GameObject> objectuuid = new HashMap<>();
	
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
		objectuuid.put(o.getUuid(), o);
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
	public boolean isServer() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void updateGame() {
		List<CollisionHandler> colliders = new ArrayList<>();
		for (WorldZone zone : getZones().toArray(new WorldZone[getZones().size()])) {
			for (GameObject obj : (zone.getGameobjects().toArray(new GameObject[zone.getGameobjects().size()]))) {
				
				for (GameObject scan : getScanners().keySet()) {
					if (scan == obj) {
						continue;
					}
					
					if (scan.distance(obj) <= getScanners().get(scan)) {
						scan.getNearby().add(obj);
					}
				}
				
				if (obj.isDeleted()) {
					zone.getGameobjects().remove(obj);
					objectuuid.remove(obj.getUuid());
					continue;
				}
				
				WorldZone objzone = getZone(Util.toZoneCoords(obj.getPosition()));
				if (zone != objzone) {
					zone.getGameobjects().remove(obj);
					objzone.getGameobjects().add(obj);
				}
				
				if (!getScanners().containsKey(obj)) {
					obj.update();
				}
				
				if (obj instanceof CollisionHandler) {
					colliders.add( (CollisionHandler)obj);
				}
			}
		}
		
		for (GameObject obj : getScanners().keySet()) {
			obj.update();
		}
		
		for (CollisionHandler h : colliders) {
			ArrayList<CollisionHandler> collided = new ArrayList<>();
			GameObject obj = (GameObject) h;
			
			if (obj.isDeleted() || obj.isCleaned()) {
				continue;
			}
			
			for (CollisionHandler check : colliders) {
				
				if (check == h) {
					continue;
				}
				GameObject checkobj = (GameObject) check;
				Rectangle rect1 = obj.getBoundingBox();
				Rectangle rect2 = checkobj.getBoundingBox();
				if (rect1.intersects(rect2)) {
					collided.add(check);
				}
				
				
				
			}
			
			if (collided.size() >= 1) {
					h.onCollision(collided.toArray(new GameObject[collided.size()]));
				}
		}
		
	}

	@Override
	public GameObject getGameObject(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

}