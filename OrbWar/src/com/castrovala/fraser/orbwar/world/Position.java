package com.castrovala.fraser.orbwar.world;

import com.castrovala.fraser.orbwar.util.Util;

public class Position {
	protected double x;
	protected double y;
	private boolean edited = false;
	
	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
		edited = true;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
		edited = true;
	}
	
	public Position copy() {
		return new Position(x, y);
	}
	
	public Position add(Position p) {
		double oldx = x;
		double oldy = y;
		
		x += p.getX();
		y += p.getY();
		
		edited = (long)oldx != (long) x || (long) oldy != (long) y;
		return this;
	}
	
	public Position subtract(Position p) {
		double oldx = x;
		double oldy = y;
		
		x -= p.getX();
		y -= p.getY();
		edited = (long)oldx != (long) x || (long) oldy != (long) y;
		return this;
	}
	
	public Position divide(Position p) {
		double oldx = x;
		double oldy = y;
		
		x /= p.getX();
		y /= p.getY();
		edited = (long)oldx != (long) x || (long) oldy != (long) y;
		return this;
	}
	
	public Position multiply(Position p) {
		double oldx = x;
		double oldy = y;
		
		x *= p.getX();
		y *= p.getY();
		edited = (long)oldx != (long) x || (long) oldy != (long) y;
		return this;
	}
	
	public double distance(Position pos) {
		return Util.distance(this, pos);
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Position)) {
			return false;
		}
		
		Position pos = (Position) obj;
		return pos.x == x && pos.y == y;
		
	}
}
