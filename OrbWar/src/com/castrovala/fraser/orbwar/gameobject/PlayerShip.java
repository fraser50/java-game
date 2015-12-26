package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.gameobject.particle.SmokeParticle;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.server.ControlUser;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Controllable;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldProvider;
import com.castrovala.fraser.orbwar.weapons.BulletGun;
import com.castrovala.fraser.orbwar.weapons.Forcefield;
import com.castrovala.fraser.orbwar.weapons.Weapon;
import com.castrovala.fraser.orbwar.weapons.WeaponOwner;

public class PlayerShip extends GameObject implements Controllable, WeaponOwner, CollisionHandler {
	private boolean tofly = false;
	private boolean toshoot = false;
	private boolean toprotect = false;
	private boolean toleft = false;
	private boolean toright = false;
	private static BufferedImage renderimage;
	private Weapon primaryweapon;
	private Weapon shield;
	private double speed;
	private double maxspeed = 6d; // 6d
	private boolean handbrake;
	
	public PlayerShip(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(64);
		setHeight(64);
		primaryweapon = new BulletGun();
		shield = new Forcefield();
	}
	
	public void shoot() {
		if (primaryweapon != null) {
			primaryweapon.fire(this);
		}
		
		/*Position bulletpos = new Position(getPosition().getX() + (getWidth() / 2), getPosition().getY() +  + (getHeight() / 2) );
		//Position bulletpos = getPosition().copy();
		Position frontvelocity = Util.angleToVel(rotation, 5); //3
		float vx = frontvelocity.getX();
		float vy = frontvelocity.getY();
		bulletpos.add(Util.angleToVel(rotation, 5));
		
		Bullet b = new Bullet(bulletpos, getController());
		b.getVelocity().setX(vx);
		b.getVelocity().setY(vy);
		getController().addObject(b);
		*/
	}
	
	@Override
	public void update() {
		//System.out.println("Health: " + getHealth() + "/" + getMaxhealth());
		super.update();
		
		if (primaryweapon != null) {
			primaryweapon.update(this);
		}
		
		
		if (rotation >= 360) {
			setRotation(0);
		}
		
		if (tofly) {
			tofly = false;
			forward();
		}
		
		if (toshoot) {
			toshoot = false;
			shoot();
		}
		
		if (toprotect) {
			toprotect = false;
			if (shield != null) {
				shield.fire(this);
			}
		}
		
		if (toleft) {
			toleft = false;
			setRotation(getRotation() - 5);
		}
		
		if (toright) {
			toright = false;
			setRotation(getRotation() + 5);
		}
		
		if (speed > maxspeed) {
			speed = maxspeed;
		}
		
		Position vel = Util.angleToVel(getRotation(), (float)speed);
		getPosition().add(vel);
		if (handbrake) {
			speed -= 0.1d; // 0.1
			double behind = Util.fixAngle(getRotation() + 360);
			Position pos = Util.angleToVel(behind, -20);
			Position smokepos1 = getPosition().copy().add(new Position(32, 32)).add(pos);
			Position smokepos2 = smokepos1.copy();
			
			for (int i = 1; i < 30; i++) {
				double smokeangle1 = Util.fixAngle(getRotation() + 90);
				double smokeangle2 = Util.fixAngle(getRotation() - 90);
				
				Position smokemv1 = Util.angleToVel(Util.randomRange((int)smokeangle1 - 20, (int)smokeangle1 + 20), Util.randomRange(10, 40) / 10);
				Position smokemv2 = Util.angleToVel(Util.randomRange((int)smokeangle2 - 20, (int)smokeangle2 + 20), Util.randomRange(10, 40) / 10);
				
				SmokeParticle smoke1 = new SmokeParticle(smokepos1.copy(), getController(), smokemv1);
				SmokeParticle smoke2 = new SmokeParticle(smokepos2.copy(), getController(), smokemv2);
				getController().addObject(smoke1);
				getController().addObject(smoke2);
			}
			
		} else {
			speed -= 0.01d;
		}
		
		if (speed < 0) {
			speed = 0;
		}
		
	}

	@Override
	public void left() {
		toleft = true;
		
	}

	@Override
	public void right() {
		toright = true;
		
	}

	@Override
	public void fly() {
		tofly = true;
	}
	
	public void forward() {
		//double radian = Math.toRadians(getRotation());
		//float vx = (float) Math.cos(radian - Math.toRadians(45) - 0.80f) * 16; //4
		//float vy = (float) Math.sin(radian - Math.toRadians(45) - 0.80f) * 16; //4
		//getPosition().setX(getPosition().getX() + vx);
		//getPosition().setY(getPosition().getY() + vy);
		speed += 2; // 2
		
	}

	@Override
	public void fire() {
		toshoot = true;
		
	}
	
	@Override
	public void shield() {
		toprotect = true;
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		
		g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		g2d.rotate(Math.toRadians( ((double)this.getRotation() * -1)), centre_x, centre_y);
		
		g2d.setColor(Color.RED);
		int rel_x_middle = rel_x + (this.getWidth() / 2);
		int rel_y_middle = rel_y + (this.getHeight() / 2);
		g2d.drawLine(rel_x_middle, rel_y_middle, rel_x_middle, rel_y_middle);
		rd.onRender(2);
		
		/*g2d.setColor(Color.GREEN);
		for (GameObject o : getNearbyObjects(1000f)) {
			if (o != this && o instanceof SmokeParticle) {
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
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/playership.png")));
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
	public Weapon getPrimaryweapon() {
		return primaryweapon;
	}

	@Override
	public void setPrimaryweapon(Weapon primaryweapon) {
		this.primaryweapon = primaryweapon;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void death() {
		Position pos = getPosition().copy();
		pos.add(new Position(getWidth() / 2, getHeight() / 2));
		RespawnPoint point = new RespawnPoint(pos, getController());
		getController().addObject(point);
	}

	public double getMaxspeed() {
		return maxspeed;
	}

	public void setMaxspeed(float maxspeed) {
		this.maxspeed = maxspeed;
	}

	public boolean isHandbrake() {
		return handbrake;
	}

	public void setHandbrake(boolean handbrake) {
		this.handbrake = handbrake;
	}
	
	@Override
	public void toggleBrake() {
		handbrake = !handbrake;
		
	}
	
	@Override
	public void suicide() {
		setHealth(0);
		
	}
	
	@Override
	public RenderStage getRenderStage() {
		return RenderStage.SHIPS;
	}

	public Weapon getShield() {
		return shield;
	}

	public void setShield(Weapon shield) {
		this.shield = shield;
	}
	
	@Override
	public ControlUser getControl() {
		return null;
	};

	@Override
	public void setControl(ControlUser user) {
		// TODO Auto-generated method stub
		
	}

}
