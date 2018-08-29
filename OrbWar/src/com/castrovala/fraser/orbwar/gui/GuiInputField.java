package com.castrovala.fraser.orbwar.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import com.castrovala.fraser.orbwar.world.Position;

public class GuiInputField extends GuiElement implements GuiFocusable {
	private String text = "";
	private boolean focused = false;
	private int counter;
	private String shadowtext = "";

	public GuiInputField(Position start, Position end, String text) {
		super(start, end);
		this.text = text;
	}
	
	public GuiInputField(Position start, Position end) {
		this(start, end, "");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onKeyPress(KeyEvent key) {
		if (key.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (text.length() <= 1) {
				return;
			}
			
			text = text.substring(0, text.length() - 2);
		}
		
	}

	@Override
	public void onCharEnter(char c) {
		text = text + c;
		
	}

	@Override
	public void setFocus(boolean focus) {
		this.focused = focus;
		
	}

	@Override
	public void render(Graphics2D g, int mousex, int mousey) {
		
		if (focused) {
			g.setColor(Color.WHITE.darker().darker());
		} else {
			g.setColor(Color.WHITE);
		}
		
		g.fillRect((int)getStart().getX(), (int)getStart().getY(), 
				(int)(getEnd().getX() - getStart().getX()), 
				(int)(getEnd().getY() - getStart().getY()));
		
		if (focused) {
			g.setColor(Color.BLUE);
		} else {
			g.setColor(Color.GREEN);
		}
		
		g.drawRect((int)getStart().getX(), (int)getStart().getY(), 
				(int)(getEnd().getX() - getStart().getX()), 
				(int)(getEnd().getY() - getStart().getY()));
		
		g.setColor(Color.BLACK);
		String newtext = text;
		
		if (counter >= 15 && focused) {
			newtext = newtext.concat("|");
		}
		
		g.drawString(newtext, (int)(getStart().getX() + 20), (int)(getStart().getY() + getHeight() / 2));
		
		if ( (!focused) && !shadowtext.equals("")  && text.equals("")) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(shadowtext, (int)(getStart().getX() + 500), (int)(getStart().getY() + getHeight() / 2));
		}
		
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public double getWidth() {
		return getEnd().getX() - getStart().getX();
	}
	
	public double getHeight() {
		return getEnd().getY() - getStart().getY();
	}
	
	@Override
	public void update(int mousex, int mousey) {
		counter++;
		if (counter == 40) {
			counter = 0;
		}
	}

	public String getShadowtext() {
		return shadowtext;
	}

	public void setShadowtext(String shadowtext) {
		this.shadowtext = shadowtext;
	}

}
