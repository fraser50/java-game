package com.castrovala.fraser.orbwar.gameobject.interior;

import java.awt.Color;
import java.awt.Graphics2D;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.editor.Resizable;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class StationFloor extends GameObject implements Resizable {

	public StationFloor(Position pos, WorldProvider controller, int width, int height) {
		super(pos, controller);
		setWidth(width);
		setHeight(height);
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.setColor(Color.LIGHT_GRAY);
		int startx, starty;
		
		if (getWidth() >= 0) {
			startx = rel_x;
		} else {
			startx = rel_x + getWidth();
		}
		
		if (getHeight() >= 0) {
			starty = rel_y;
		} else {
			starty = rel_y + getHeight();
		}
		
		int width = getWidth() >= 0 ? getWidth() : -getWidth();
		int height = getHeight() >= 0 ? getHeight() : -getHeight();
		
		g2d.fillRect(startx, starty, width, height);
	}

	@Override
	public String getType() {
		return "statfloor";
	}

	@Override
	public int getPreferredWidth() {
		return 32;
	}

	@Override
	public int getPreferredHeight() {
		return 32;
	}
	
	@Override
	public RenderStage getRenderStage() {
		return RenderStage.BACKGROUND;
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				StationFloor floor = (StationFloor) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "statfloor");
				jobj.put("x", floor.getPosition().getX());
				jobj.put("y", floor.getPosition().getY());
				
				jobj.put("w", floor.getWidth());
				jobj.put("h", floor.getHeight());
				
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				int width = ((Number)obj.get("w")).intValue();
				int height = ((Number)obj.get("h")).intValue();
				
				return new StationFloor(null, null, width, height);
			}
		};
		
		GameObjectProcessor.addParser("statfloor", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Station Floor") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new StationFloor(null, null, 32, 32);
			}
		};
		
		EditorManager.addEditor(e);
	}

}
