package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldProvider;
import com.castrovala.fraser.orbwar.weapons.BulletGun;
import com.castrovala.fraser.orbwar.weapons.Weapon;
import com.castrovala.fraser.orbwar.weapons.WeaponOwner;

import net.minidev.json.JSONObject;

public class Turret extends GameObject implements WeaponOwner, CollisionHandler {
	private static BufferedImage renderimage;
	private GameObject target;
	private Weapon primary;
	
	public Turret(Position pos, WorldProvider controller) {
		super(pos, controller, 10); // 100
		setWidth(64);
		setHeight(64);
		//setHealth(100); // 100
		//setMaxhealth(100);
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
		if (isDeleted()) {
			return;
		}
		
		if (primary != null) {
			primary.update(this);
		}
		
		if (target != null) {
			if (target.isDeleted() || target.isCleaned()) {
				target = null;
			} 
		}
		
		if (target == null) {
			for (GameObject obj : getNearbyObjects(3000)) {
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
		
		if (target instanceof PlayerShip) {
			PlayerShip ship = (PlayerShip) target;
			double speed = ship.getSpeed();
			boolean finished = false;
			long iter = 1;
			/*while (!finished) {
				Position pos = ship.getPosition().copy().add(Util.angleToVel(ship.getRotation(), (float)speed * iter));
				requiredrota = Util.targetRadius(pos, getPosition());
				double timeship = Util.distance(ship.getPosition().copy(), pos) / speed;
				Position targetpos = Util.angleToVel(requiredrota, 5);
				double timebullet = Util.distance(getPosition().copy(), pos) / 5;
				System.out.println("iter: " + iter);
				System.out.println("timeship: " + timeship);
				System.out.println("timebullet: " + timebullet);
				System.out.println("");
				if (timebullet < timeship + 1 && timebullet >= timebullet) {
					finished = true;
				}
				iter++;
			}*/
			
			Position pos = ship.getPosition().copy();
			Position bulletpos = getPosition().copy();
			double distance = Util.distance(pos, bulletpos);
			double time = distance / 5;
			double newspeed = speed;
			//double distancemoved = time * ship.getSpeed();
			double distancemoved = 0;
			for (int i = 1; i < (int)time + 1; i++) {
				distancemoved += newspeed;
				newspeed -= 0.01d;
				if (newspeed <= 0) {
					break;
				}
			}
			
			Position targetpos = pos.copy().add(Util.angleToVel(ship.getRotation(), (float)distancemoved));
			requiredrota = Util.targetRadius(targetpos, bulletpos);
			
		}
		
		
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
		
		g2d.setColor(Color.GREEN);
		int green = (int) (getHealth() * getWidth()) / getMaxhealth();
		g2d.fillRect(rel_x, rel_y - 10, green, 5);
		
		g2d.setColor(Color.RED);
		g2d.fillRect(rel_x + green, rel_y - 10, getWidth() - green, 5);
		rd.onRender(4);
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
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				Turret turret = (Turret) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "turret");
				jobj.put("x", turret.getPosition().getX());
				
				jobj.put("y", turret.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				Turret turret = new Turret(null, null);
				return turret;
			}
		};
		
		GameObjectProcessor.addParser("turret", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Turret") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new Turret(new Position(0, 0), controller);
			}
		};
		
		EditorManager.addEditor(e);
	}
	
	@Override
	public boolean shouldRotate() {
		return true;
	}
	
	@Override
	public void death() {;
		Explosion ex = new Explosion(getPosition().copy(), getController());
		getController().addObject(ex);
	}

}
