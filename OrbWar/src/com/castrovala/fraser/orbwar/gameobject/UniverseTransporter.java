package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.GameServer;
import com.castrovala.fraser.orbwar.server.NetworkPlayer;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldController;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class UniverseTransporter extends GameObject implements CollisionHandler {
	private WorldController universe;

	public UniverseTransporter(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(64);
		setHeight(64);
	}
	
	@Override
	public void afterBirth() {
		GameServer server = ((WorldController)getController()).getServer();
		universe = new WorldController(server);
		server.getGameThread().getManager().addUniverse(universe);
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.setColor(Color.CYAN);
		g2d.fillRect(rel_x, rel_y, getWidth(), getHeight());
	}

	@Override
	public String getType() {
		return "universedebug";
	}

	@Override
	public void onCollision(GameObject[] objects) {
		for (GameObject obj : objects) {
			if (!(obj instanceof PlayerShip)) continue;
			PlayerShip ship = (PlayerShip) obj;
			ship.setSpeed(0);
			ship.setPosition(new Position(300, 300));
			ship.getPosition().setEdited(false);
			ship.setController(universe);
			universe.addObject(ship);
			NetworkPlayer p = (NetworkPlayer) ship.getControl();
			p.setUniverse(universe);
			p.getCurrentpos().setX(0);
			p.getCurrentpos().setY(0);
			p.getCurrentpos().setEdited(false);
		}
		
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				JSONObject jobj = new JSONObject();
				jobj.put("type", "universedebug");
				jobj.put("x", obj.getPosition().getX());
				jobj.put("y", obj.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				return new UniverseTransporter(null, null);
			}
		};
		
		GameObjectProcessor.addParser("universedebug", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Universe Debugger") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new UniverseTransporter(null, null);
			}
		};
		
		EditorManager.addEditor(e);
	}
	

}
