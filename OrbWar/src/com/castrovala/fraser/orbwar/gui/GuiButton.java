package com.castrovala.fraser.orbwar.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import com.castrovala.fraser.orbwar.util.Position;

public class GuiButton extends GuiElement implements GuiClickable {
	private String label;
	private Runnable run;

	public GuiButton(Position start, Position end, String label, Runnable run) {
		super(start, end);
		this.label = label;
		this.run = run;
		
		
	}
	
	@Override
	public void render(Graphics2D g) {
		int startx = (int) getStart().getX();
		int starty = (int) getStart().getY();
		
		int endx = (int) getEnd().getX();
		int endy = (int) getEnd().getY();
		
		g.setColor(Color.WHITE);
		g.drawRect(startx, starty, endx - startx, endy - starty);
		int offsetx = (int) ((endx - startx) * 0.5);
		int offsety = (int) ((endy - starty) * 0.5);
		
		g.drawString(label, startx + offsetx, starty + offsety);
		
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Runnable getRun() {
		return run;
	}

	public void setRun(Runnable run) {
		this.run = run;
	}
	
	@Override
	public void onClick() {
		if (run != null) {
			run.run();
		}
		
	}

}
