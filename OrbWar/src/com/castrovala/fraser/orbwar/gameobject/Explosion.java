package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldProvider;

import net.minidev.json.JSONObject;

public class Explosion extends GameObject {
	public Explosion(Position pos, WorldProvider controller) {
		super(pos, controller, 20);
	}

	@Override
	public String getType() {
		return "boom";
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		Random rand = new Random(UUID.fromString(getUuid()).getMostSignificantBits());
		List<Position> points = new ArrayList<>();
		for (int i = 0;i<360;i+=2) {
			if (rand.nextBoolean() != rand.nextBoolean()) {
				float far = (rand.nextInt(100)) + 0.5f;
				Position pos = Util.angleToVel(i, far - ( (getMaxhealth() - getHealth()) * 2) );
				points.add(pos);
			}
		}
		
		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];
		int count = 0;
		for (Position pos : points) {
			xPoints[count] = (int) ((int) rel_x + pos.getX());
			yPoints[count] = (int) ((int) rel_y + pos.getY());
			count++;
		}
		
		g2d.setColor(Color.YELLOW);
		//g2d.drawLine(xPoints[0], yPoints[0], xPoints[0], yPoints[0]);
		g2d.fillPolygon(xPoints, yPoints, xPoints.length);
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				Explosion ex = (Explosion) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "boom");
				jobj.put("x", ex.getPosition().getX());
				
				jobj.put("y", ex.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				double x = (double) obj.get("x");
				double y = (double) obj.get("y");
				Explosion ex = new Explosion(new Position(x, y), null);
				return ex;
			}
		};
			
		GameObjectProcessor.addParser("boom", parser);
	}
	
	@Override
	public void update() {
		super.update();
		hurt();
	}

}
