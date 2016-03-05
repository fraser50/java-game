package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldProvider;
import com.castrovala.fraser.orbwar.weapons.BulletGun;
import com.castrovala.fraser.orbwar.weapons.Weapon;
import com.castrovala.fraser.orbwar.weapons.WeaponOwner;

public class Turret extends GameObject implements WeaponOwner, CollisionHandler {
	private static BufferedImage renderimage;
	private GameObject target;
	private Weapon primary;
	
	public Turret(Position pos, WorldProvider controller) {
		super(pos, controller, 100); // 100
		setWidth(64);
		setHeight(64);
		setHealth(100); // 100
		setMaxhealth(100);
		primary = new BulletGun();
	}

	@Override
	public Weapon getPrimaryweapon() {
		return primary;
	}

	@Override
	public void setPrimaryweapon(Weapon primaryweapon) {
		primary = primaryweapon;
		
	}

	@Override
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update() {
		super.update();
		if (primary != null) {
			primary.update(this);
		}
		
		
		if (target == null) {
			for (GameObject obj : getNearbyObjects(300)) {
				if (obj instanceof PlayerShip) {
					target = obj;
					break;
				}
			}
			
			if (target == null) {
				return;
			}
			
		}
		
		if (target.isDeleted() || target.isCleaned()) {
			target = null;
			return;
		}
		
		float requiredrota = Util.targetRadius(target, this);
		setRotation(requiredrota);
		if (primary != null) {
			primary.fire(this);
		}
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		g2d.rotate(Math.toRadians( ((double)this.getRotation() * -1)), centre_x, centre_y);
		rd.onRender();
		
		/*g2d.setColor(Color.GREEN);
		for (GameObject o : getNearbyObjects(300)) {
			if (o != this) {
				int rel_x_o = (int)(o.getPosition().getX() - rd.getRenderloc().getX());
				int rel_y_o = (int)(o.getPosition().getY() - rd.getRenderloc().getY());
				g2d.drawLine(rel_x, rel_y, rel_x_o, rel_y_o);
				rd.onRender();
			}
		}*/
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/turret.png")));
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
		return "turret";
	}

}
