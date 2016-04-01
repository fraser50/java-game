package com.castrovala.fraser.orbwar.gui;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.castrovala.fraser.orbwar.util.Position;

public abstract class GuiElement {
	private Position start;
	private Position end;
	
	public GuiElement(Position start, Position end) {
		this.start = start;
		this.end = end;
	}

	public Position getStart() {
		return start;
	}

	public void setStart(Position start) {
		this.start = start;
	}

	public Position getEnd() {
		return end;
	}

	public void setEnd(Position end) {
		this.end = end;
	}
	
	public abstract void render(Graphics2D g, int mousex, int mousey);
	
	public boolean collided(int x, int y) {
		Rectangle me = new Rectangle((int)start.getX(), (int)start.getY(), 
				(int)(end.getX() - start.getX()), (int)(end.getY() - start.getY()) );
		Rectangle other = new Rectangle(x, y, 1, 1);
		return me.intersects(other);
		
	}
	
	public void update(int mousex, int mousey) {}

}
