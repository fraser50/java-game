package com.castrovala.fraser.orbwar.util;

public class Position {
	protected double x;
	protected double y;
	
	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public Position copy() {
		return new Position(x, y);
	}
	
	public Position add(Position p) {
		x += p.getX();
		y += p.getY();
		return this;
	}
	
	public Position subtract(Position p) {
		x -= p.getX();
		y -= p.getY();
		return this;
	}
	
	public double distance(Position pos) {
		return Util.distance(this, pos);
	}

}
