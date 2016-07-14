package com.castrovala.fraser.orbwar.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.nio.ByteBuffer;
import java.util.Random;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class Util {
	public static long randomRange(long min, long max) {
		Random rand = new Random();
		long number = min + (long) (rand.nextDouble() * (max - min));
		return number;
		//return rand.n(max - min) + 1) + min;
	}
	
	public static int randomRange(int min, int max) {
		return (int) randomRange((long)min, (long)max);
	}
	
	public static Position toZoneCoords(Position pos) {
		long x = (long) pos.x / WorldZone.len_x;
		long y = (long) pos.y / WorldZone.len_y;
		Position zonepos = new Position(x, y);
		return zonepos;
	}
	
	public static Position angleToVel(float angle, float speed) {
		double radian = Math.toRadians(angle);
		double vx = Math.cos(radian - Math.toRadians(45) - 0.80f) * speed;
		double vy = Math.sin(radian - Math.toRadians(45) - 0.80f) * speed;
		
		Position pos = new Position(vx, vy);
		return pos;
	}
	
	public static Position angleToVel(double angle, float speed) {
		return angleToVel((float) angle, speed);
	}
	
	public static Position coordToScreen(Position pos, Position screenpos) {
		long screenx = (long) screenpos.getX();
		long screeny = (long) screenpos.getY();
		long x = (long) pos.getX();
		long y = (long) pos.getY();
		long rel_x = (long)(x - screenx);
		long rel_y = (long)(y - screeny);
		return new Position(rel_x, rel_y);
	}
	
	public static double distance(Position a, Position b) {
		double x = b.getX() - a.getX();
		double y = b.getY() - a.getY();
		//System.out.println("Pythagoras x: " + x + " || y: " + y);
		double hyp = Math.sqrt((x * x) + (y * y));
		return hyp;
	}
	
	public static int toPercent(int amount, int max) {
		int percent = (int) (amount * 100) / max;
		return percent;
	}
	
	public static float targetRadius(Position a, Position b) {
		Position pos = a.copy();
		pos.subtract(b);
		double d = Math.toDegrees(Math.atan2((int)pos.getX(), (int)b.getY() - a.getY()) );
		return (float) d ;
	}
	
	public static float targetRadius(GameObject a, GameObject b) {
		return targetRadius(a.getPosition(), b.getPosition());
	}
	
	public static double fixAngle(double angle) {
		if (angle < 0) {
			return 360 + angle;
		}
		
		if (angle > 360) {
			return angle - 360;
		}
		
		return angle;
	}
	
	public static byte[] DoubleToBytes(double d) {
		ByteBuffer buff = ByteBuffer.allocate(8);
		buff.putDouble(d);
		return buff.array();
	}
	
	public static double BytesToDouble(ByteBuffer buff) {
		return buff.getDouble();
	}
	
	public static void renderLightning(Graphics2D g2d, Position start, Position finish) {
		long seed = System.currentTimeMillis() / 80;
		//long seed = Long.MAX_VALUE;
		Random r = new Random(seed);
		
		Stroke prevstroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(1));
		
		float lineangle = Util.targetRadius(finish, start) - 90;
		double distance = Util.distance(start, finish);
		
		Position prev = null;
		g2d.setColor(Color.WHITE);
		
		
		for (int i = 1; i < distance + 1; i+=4) {
			Position pointinline = Util.angleToVel(lineangle + 90, i);
			
			if (prev != null) {
				g2d.drawLine((int)prev.getX(), (int)prev.getY(), (int)pointinline.getX(), (int)pointinline.getY());
			}
			
			float randomangle = lineangle + r.nextInt(360) + 1;
			Position v = Util.angleToVel(randomangle, r.nextInt(7));
			Position newpointinline = new Position(pointinline.x, pointinline.y).add(v);
			prev = newpointinline;
			g2d.drawLine((int)pointinline.getX(), (int)pointinline.getY(), (int)newpointinline.getX(), (int)newpointinline.getY());
		}
		
		g2d.setStroke(prevstroke);
		
	}

}
