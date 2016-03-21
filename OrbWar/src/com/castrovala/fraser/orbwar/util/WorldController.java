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
	private List<GameObject> newObjects = new ArrayList<>();
	private List<GameObject> deadObjects = new ArrayList<>();
	
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
	public synchronized void addObject(GameObject o) {
		getNewObjects().add(o);
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
	public synchronized GameObject[] allObjects() {
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
				
				WorldZone ozone = getZone(Util.toZoneCoords(obj.getPosition()));
				if (ozone != zone) {
					zone.getGameobjects().remove(obj);
					ozone.getGameobjects().add(obj);
					continue;
				}
				
				if (scanners.containsKey(obj)) {
					for (GameObject detected : obj.getNearby().toArray(new GameObject[obj.getNearby().size()])) {
						
						if (detected.isDeleted() || detected.isCleaned()) {
							obj.getNearby().remove(detected);
						}
						
						float scanrange = scanners.get(obj);
						float distance = (float) Util.distance(obj.getPosition(), detected.getPosition());
						if (distance > scanrange) {
							obj.getNearby().remove(detected);
						}
						
					} 
				}
				for (GameObject scan : getScanners().keySet()) {
					if (scan == obj) {
						continue;
					}
					
					if (scan.distance(obj) <= getScanners().get(scan)) {
						scan.getNearby().add(obj);
					}
				}
				
				if (obj.isDeleted()) {
					getDeadObjects().add(obj);
					zone.getGameobjects().remove(obj);
					objectuuid.remove(obj.getUuid());
					getScanners().remove(obj);
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
				
				/*if (obj.getPosition().getX() > 2048 - 70) {
					obj.getPosition().setX(2048 - 70);
				}
				
				if (obj.getPosition().getY() > 2048 - 70) {
					obj.getPosition().setY(2048 - 70);
				}
				
				if (obj.getPosition().getX() < -256) {
					obj.getPosition().setX(-256);
				}
				
				if (obj.getPosition().getY() < -256) {
					obj.getPosition().setY(-256);
				}
				
				if (obj instanceof CollisionHandler) {
					colliders.add( (CollisionHandler)obj);
				}*/
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

	public List<GameObject> getNewObjects() {
		synchronized (newObjects) {
			return newObjects;
		}
	}

	public List<GameObject> getDeadObjects() {
		synchronized (deadObjects) {
			return deadObjects;
		}
	}

}