package com.castrovala.fraser.orbwar.gameobject.particle;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class HydrogenParticle extends GameObject {
	private static BufferedImage renderimage;
	
	public HydrogenParticle(Position pos, WorldProvider controller, Position vel) {
		super(pos, controller, 50);
		setVelocity(vel);
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.setColor(Color.RED);
		Composite c = g2d.getComposite();
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f ));
		
		g2d.fillOval(rel_x, rel_y, 7, 7);
		g2d.setComposite(c);
		rd.onRender();
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/star.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setRenderimage(BufferedImage r) {
		renderimage = r;
	}
	
	public static BufferedImage getRenderimage() {
		return renderimage;
	}
	
	@Override
	public String getType() {
		return "hydrogen";
	}
	
	@Override
	public void clientUpdate() {
		getPosition().add(getVelocity());
		hurt(1);
		if (getHealth() < 1) {
			delete();
		}
	}

}
