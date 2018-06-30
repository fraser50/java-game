package com.castrovala.fraser.orbwar.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldZone;

public class Util {
	public static long randomRange(long min, long max) {
		Random rand = ThreadLocalRandom.current();
		long number = min + (long) (rand.nextDouble() * (max - min));
		return number;
		//return rand.n(max - min) + 1) + min;
	}
	
	public static int randomRange(int min, int max) {
		return (int) randomRange((long)min, (long)max);
	}
	
	public static Position toZoneCoords(Position pos) {
		double x = (long) pos.getX() / WorldZone.len_x;
		double y = (long) pos.getY() / WorldZone.len_y;
		
		x = x < 0 ? x - 0.999 : x;
		y = y < 0 ? y - 0.999 : y;
		
		Position zonepos = new Position((int)x, (int)y);
		return zonepos;
	}
	
	public static Position angleToVel(float angle, float speed) {
		double radian = Math.toRadians(angle);
		double vx = Math.cos(radian - Math.toRadians(90)) * speed;
		double vy = Math.sin(radian - Math.toRadians(90)) * speed;
		
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
	
	public static double distanceSquared(Position a, Position b) {
		double x = b.getX() - a.getX();
		double y = b.getY() - a.getY();
		double hypsquare = Math.pow(x, 2) + Math.pow(y, 2);
		return hypsquare;
	}
	
	public static double distance(Position a, Position b) {
		return Math.sqrt(distanceSquared(a, b));
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
			Position newpointinline = new Position(pointinline.getX(), pointinline.getY()).add(v);
			prev = newpointinline;
			g2d.drawLine((int)pointinline.getX(), (int)pointinline.getY(), (int)newpointinline.getX(), (int)newpointinline.getY());
		}
		
		g2d.setStroke(prevstroke);
		
	}
	
	public static byte[] encodeString(String s) {
		ByteBuffer b = ByteBuffer.allocate(4 + s.getBytes().length);
		b.putInt(s.getBytes().length);
		b.put(s.getBytes());
		return b.array();
	}
	
	public static String decodeString(ByteBuffer buff) {
		int length = buff.getInt();
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			b[i] = buff.get();
		}
		
		return new String(b);
	}
	
	public static byte boolToByte(boolean b) {
		return b ? (byte)1 : (byte)0;
	}
	
	public static boolean byteToBool(byte b) {
		return b == (byte)1 ? true : false;
	}

}
