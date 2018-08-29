package com.castrovala.fraser.orbwar.util;

import java.awt.Color;

public class LightCircle {
	private int x;
	private int y;
	private int radius;
	private Color colour;
	
	public LightCircle(int x, int y, int radius, Color colour) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.colour = colour;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public void setRadius(int radius) {
		this.radius = radius;
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
	}

}
