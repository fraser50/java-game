package com.castrovala.fraser.orbwar.gameobject.particle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class SmokeParticle extends GameObject {
	private static BufferedImage renderimage;
	
	public SmokeParticle(Position pos, WorldProvider controller, Position vel) {
		super(pos, controller, 30);
		setVelocity(vel);
	}
	
	@Override
	public void update() {
		super.update();
		
		getPosition().add(getVelocity());
		hurt(1);
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		//g2d.setColor(Color.WHITE);
		//g2d.drawOval(rel_x, rel_y, 5, 5);
		g2d.drawImage(renderimage, rel_x, rel_y, null);
		rd.onRender();
	}
	
	public static void loadResources() {
		renderimage = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = (Graphics2D) renderimage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setColor(Color.WHITE);
		g2d.drawOval(0, 0, 5, 5);
	}
	
	public static void setRenderimage(BufferedImage r) {
		renderimage = r;
	}
	
	public static BufferedImage getRenderimage() {
		return renderimage;
	}
	
	@Override
	public String getType() {
		return "smoke";
	}

}
