package com.castrovala.fraser.orbwar.world;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.castrovala.fraser.orbwar.gameobject.Asteroid;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.Util;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class WorldZone {
	private List<GameObject> gameobjects = new ArrayList<>();
	public static final int len_x = 500;
	public static final int len_y = 500;
	private final long x;
	private final long y;
	private WorldProvider controller;
	
	public WorldZone(long x, long y, WorldProvider controller) {
		this.x = x;
		this.y = y;
		this.setController(controller);
	}

	public List<GameObject> getGameobjects() {
		return gameobjects;
	}
	
	public Position getStartPoint() {
		Position pos = new Position((double)x * len_x, (double)y * len_y);
		return pos;
	}
	
	public Position getEndPoint() {
		Position pos = new Position((double)(x * len_x) + len_x, (double)(y * len_y) + len_y);
		return pos;
	}
	
	public void populate() {
		Position startpos = getStartPoint();
		Position endpos = getEndPoint();
		List<Rectangle> prevAst = new ArrayList<>();
		
		for (int i = 1; i <= 5;i++) {
			Random rand = new Random();
			if (rand.nextInt(10) == rand.nextInt(10)) {
				long chosen_x = 0;
				long chosen_y = 0;
				
				boolean isTouched = true;
				Rectangle thisAst = new Rectangle((int)chosen_x, (int)chosen_y, 64, 64);
				
				while (isTouched) {
					
					chosen_x = Util.randomRange((long)startpos.x, (long)endpos.x);
					chosen_y = Util.randomRange((long)startpos.y, (long)endpos.y);
					
					
					isTouched = false;
					for (Rectangle rect : prevAst) {
						if (rect.intersects(thisAst)) {
							System.out.println("Intersects");
							isTouched = true;
							break;
						}
					}
					thisAst = new Rectangle((int)chosen_x, (int)chosen_y, 64, 64);
				}
				
				prevAst.add(thisAst);
				
				Position pos = new Position(chosen_x, chosen_y);
				Asteroid aster = new Asteroid(pos, controller);
				controller.addObject(aster);
			}
			
		}
	}

	public WorldProvider getController() {
		return controller;
	}

	public void setController(WorldProvider controller) {
		this.controller = controller;
	}
	
	public JSONObject saveAsJSON() {
		JSONObject jobj = new JSONObject();
		
		JSONArray array = new JSONArray();
		for (GameObject obj : gameobjects) {
			if (obj.shouldSave()) {
				array.add(GameObjectProcessor.toJSON(obj));
			}
			
		}
		
		jobj.put("objects", array);
		
		return jobj;
		
	}

	public long getX() {
		return x;
	}

	public long getY() {
		return y;
	}

}