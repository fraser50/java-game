package com.castrovala.fraser.orbwar.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import com.castrovala.fraser.orbwar.world.Position;

public class GuiLabel extends GuiElement {
	private Color colour;
	private String notice = "";

	public GuiLabel(Position start, Position end) {
		super(start, end);
	}

	@Override
	public void render(Graphics2D g, int mousex, int mousey) {
		if (!notice.equals("")) {
			g.setColor(colour);
			g.drawString(notice, (int)getStart().getX(), (int)getStart().getY());
		}
		
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

}
