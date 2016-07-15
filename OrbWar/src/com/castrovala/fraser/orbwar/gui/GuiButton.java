package com.castrovala.fraser.orbwar.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import com.castrovala.fraser.orbwar.world.Position;

public class GuiButton extends GuiElement implements GuiClickable {
	private String label;
	private Runnable run;
	private Color fill;
	private Color text = Color.WHITE;

	public GuiButton(Position start, Position end, String label, Runnable run, Color fill) {
		super(start, end);
		this.label = label;
		this.run = run;
		this.fill = fill;
		
		
	}
	
	public GuiButton(Position start, Position end, String label, Runnable run) {
		this(start, end, label, run, null);
	}
	
	@Override
	public void render(Graphics2D g, int mousex, int mousey) {
		int startx = (int) getStart().getX();
		int starty = (int) getStart().getY();
		
		int endx = (int) getEnd().getX();
		int endy = (int) getEnd().getY();
		
		if (fill != null) {
			if (collided(mousex, mousey)) {
				g.setColor(fill.darker());
			} else {
				g.setColor(fill);
			}
			
			g.fillRect(startx, starty, endx - startx, endy - starty);
		}
		
		g.setColor(Color.WHITE);
		g.drawRect(startx, starty, endx - startx, endy - starty);
		int offsetx = (int) ((endx - startx) * 0.5);
		int offsety = (int) ((endy - starty) * 0.5 + 5);
		
		g.setColor(text);
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

	public Color getText() {
		return text;
	}

	public GuiButton setText(Color text) {
		this.text = text;
		return this;
	}

	public Color getFill() {
		return fill;
	}

	public void setFill(Color fill) {
		this.fill = fill;
	}

}
