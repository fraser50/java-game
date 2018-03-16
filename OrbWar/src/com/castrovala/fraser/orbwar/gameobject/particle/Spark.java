package com.castrovala.fraser.orbwar.gameobject.particle;

import java.awt.Color;
import java.awt.Graphics2D;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class Spark extends GameObject {

	public Spark(Position pos, WorldProvider controller) {
		super(pos, controller);
		setMaxhealth(100);
		setHealth(getMaxhealth());
	}

	@Override
	public String getType() {
		return "spark";
	}
	
	public void clientUpdate() {
		
		if (getHealth() <= 0) {
			delete();
			return;
		}
		
		getPosition().add(getVelocity());
		hurt();
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.setColor(Color.WHITE);
		g2d.drawLine(rel_x, rel_y, rel_x, rel_y);
	}
	

}
