package com.castrovala.fraser.orbwar.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.castrovala.fraser.orbwar.gameobject.Asteroid;
import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class WorldZone {
	private List<GameObject> gameobjects = new ArrayList<>();
	public static final int len_x = 500;
	public static final int len_y = 500;
	protected long x;
	protected long y;
	private WorldProvider controller;
	
	public WorldZone(long x, long y, WorldProvider controller) {
		this.x = x;
		this.y = y;
		this.controller = controller;
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
		
		for (int i = 1; i <= 5;i++) {
			Random rand = new Random();
			if (rand.nextInt(5 + 1) == rand.nextInt(5 + 1)) {
				long chosen_x = Util.randomRange((long)startpos.x, (long)endpos.x);
				long chosen_y = Util.randomRange((long)startpos.y, (long)endpos.y);
				Position pos = new Position(chosen_x, chosen_y);
				//Asteroid aster = new Asteroid(pos, controller);
				//controller.addObject(aster);
			}
			
		}
	}

}