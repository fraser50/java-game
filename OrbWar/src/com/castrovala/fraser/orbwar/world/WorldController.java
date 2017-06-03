package com.castrovala.fraser.orbwar.world;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.GameServer;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WormHoleData;

public class WorldController implements WorldProvider {
	private List<WorldZone> zones = new ArrayList<>();
	private List<Position> starpoints = new ArrayList<>();
	private HashMap<GameObject, Float> scanners = new HashMap<>();
	private HashMap<String, GameObject> objectuuid = new HashMap<>();
	private List<GameObject> newObjects = new ArrayList<>();
	private List<GameObject> deadObjects = new ArrayList<>();
	
	private List<WormHoleData> wormHoleDataList = new ArrayList<>();
	private HashMap<String, WormHoleData> wormHoleDataMap = new HashMap<>();
	
	private GameServer server;
	
	public WorldController(GameServer server) {
		this.server = server;
	}
	
	@Override
	public synchronized WorldZone getZone(Position pos) {
		for (WorldZone zone : zones) {
			//System.out.println("Checking: X=" + pos.x + " Y=" + pos.y + " = X=" + zone.getX() + " Y=" + zone.getY() + " --> " + (zone.getX() == ((long)pos.x) && zone.getY() == ((long)pos.y)));
			if (zone.getX() == ((long)pos.x) && zone.getY() == ((long)pos.y)) {
				return zone;
			}
		}
		
		WorldZone zone = new WorldZone((long)pos.x, (long)pos.y, this);
		zones.add(zone);
		zone.populate();
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
		return objectuuid.values().toArray(new GameObject[objectuuid.values().size()]);
	}

	@Override
	public boolean isServer() {
		return true;
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
				}*/
				
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
		return objectuuid.get(uuid);
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

	public synchronized GameServer getServer() {
		return server;
		
	}
	
	public synchronized void setServer(GameServer server) {
		this.server = server;
	}

	public List<WormHoleData> getWormHoleDataList() {
		return wormHoleDataList;
	}

	public HashMap<String, WormHoleData> getWormHoleDataMap() {
		return wormHoleDataMap;
	}
	
	public void saveZones(File f) throws IllegalArgumentException {
		if (!f.isDirectory()) {
			throw new IllegalArgumentException("You must input a folder");
		}
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		
		File zonedir = new File(f, "zones");
		zonedir.mkdirs();
		
		for (WorldZone zone : zones) {
			File zonefile = new File(zonedir, (int)zone.getX() + "_" + (int)zone.getY() + ".json");
			
			try {
				PrintWriter writer = new PrintWriter(zonefile);
				JSONObject zonedata = zone.saveAsJSON();
				
				engine.put("zonedata", zonedata.toJSONString());
				engine.eval("result = JSON.stringify(JSON.parse(zonedata), null, 2);");
				
				String jsonstr = (String) engine.get("result");
				//String jsonstr = zonedata.toJSONString();
				writer.print(jsonstr);
				writer.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JSONObject wormholedata = new JSONObject();
		JSONArray array = new JSONArray();
		for (WormHoleData data : wormHoleDataList) {
			
			JSONObject jobj = new JSONObject();
			jobj.put("id", data.getWormholeID());
			jobj.put("type", data.getType().toString());
			array.add(jobj);
			
		}
		
		wormholedata.put("holes", array);
		File wormfile = new File(f, "wormholes.json");
		PrintWriter writer;
		try {
			writer = new PrintWriter(wormfile);
			engine.put("wormholedata", wormholedata.toJSONString());
			engine.eval("result = JSON.stringify(JSON.parse(wormholedata), null, 2);");
		
			String jsonstr = (String) engine.get("result");
			//String jsonstr = zonedata.toJSONString();
			writer.print(jsonstr);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public static WorldController createFromFile(File f) throws FileNotFoundException, ParseException {
		System.out.println("Building zone...");
		WorldController c = new WorldController(null);
		
		File zonedirfile = new File(f, "zones");
		File zonefile;
		for (String s : zonedirfile.list()) {
			zonefile = new File(zonedirfile, s);
			
			if (!s.endsWith(".json")) {
				continue;
			}
			
			s = s.substring(0, s.length() - 5);
			
			String[] coords = s.split(Pattern.quote("_"));
			long x = Long.parseLong(coords[0]);
			long y = Long.parseLong(coords[1]);
			
			WorldZone zone = new WorldZone(x, y, c);
			
			Scanner scan = new Scanner(zonefile);
			scan.useDelimiter("\\Z"); 
			String zonejson = scan.next();
			scan.close();
			JSONParser parser = new JSONParser();
			JSONObject jsonobj = (JSONObject) parser.parse(zonejson);
			JSONArray array = (JSONArray) jsonobj.get("objects");
			
			for (Object gamejsonraw : array) {
				JSONObject gamejson = (JSONObject) gamejsonraw;
				GameObject gobj = GameObjectProcessor.fromJSON(gamejson);
				zone.getGameobjects().add(gobj);
			}
			
			
			c.getZones().add(zone);
		}
		
		return c;
		
	}

}